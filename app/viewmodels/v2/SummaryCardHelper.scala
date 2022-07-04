/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.v2

import models.User
import models.v3.appealInfo.AppealStatusEnum
import models.v3.lpp.MainTransactionEnum._
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, MainTransactionEnum}
import models.v3.lsp._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.v2.PenaltyPeriodHelper
import utils.{CurrencyFormatter, ImplicitDateFormatter, ViewUtils}

import java.time.LocalDate
import javax.inject.Inject

class SummaryCardHelper @Inject()(link: views.html.components.link) extends ImplicitDateFormatter with ViewUtils {

  def populateLateSubmissionPenaltyCard(penalties: Seq[LSPDetails],
                                        threshold: Int, activePoints: Int)
                                       (implicit messages: Messages, user: User[_]): Seq[LateSubmissionPenaltySummaryCard] = {
    val thresholdMet: Boolean = pointsThresholdMet(threshold, activePoints)
    val filteredActivePenalties: Seq[LSPDetails] = penalties.filter(_.penaltyStatus != LSPPenaltyStatusEnum.Inactive).reverse
    val indexedActivePoints = filteredActivePenalties.zipWithIndex
    penalties.map { penalty =>
      val newPenalty = findAndReindexPointIfIsActive(indexedActivePoints, penalty)
      (newPenalty.penaltyCategory, newPenalty.appealInformation.map(_.head.appealStatus)) match {
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.FAPIndicator.contains("X") && newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Active => addedPointCard(newPenalty, thresholdMet) //Added FAP
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.FAPIndicator.contains("X") && newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive => removedPointCard(newPenalty) //Removed FAP
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive && newPenalty.expiryReason.isDefined => removedPointCard(newPenalty) //Removed point
        case (LSPPenaltyCategoryEnum.Point, Some(AppealStatusEnum.Upheld)) if newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive => removedPointCard(newPenalty) //Appealed point
        case (LSPPenaltyCategoryEnum.Point, _) => pointSummaryCard(newPenalty, thresholdMet) // normal points
        case (LSPPenaltyCategoryEnum.Threshold, _) => financialSummaryCard(newPenalty, threshold) //normal threshold
        case (LSPPenaltyCategoryEnum.Charge, _) => financialSummaryCard(newPenalty, threshold)
        case _ => throw new Exception("[SummaryCardHelper][populateLateSubmissionPenaltyCard] Incorrect values provided")
      }
    }
  }

  private def addedPointCard(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.addedOnKey"),
        Html(
          dateToString(penalty.penaltyCreationDate)
        )
      )
      ),
      if (!thresholdMet) {
        Some(summaryListRow(messages("summaryCard.key4"), Html(dateToMonthYearString(penalty.penaltyExpiryDate))))
      } else {
        None
      }
    ).collect {
      case Some(x) => x
    }

    buildLSPSummaryCard(rows, penalty, isAnAddedPoint = true, isAnAdjustedPoint = true)
  }

  private def buildLSPSummaryCard(rows: Seq[SummaryListRow], penalty: LSPDetails, isAnAddedPoint: Boolean = false,
                                  isAnAdjustedPoint: Boolean = false)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val isReturnSubmitted = penalty.lateSubmissions.map(penaltyPeriod =>
      PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head)
      .fold(false)(_.taxReturnStatus.equals(TaxReturnStatusEnum.Fulfilled))
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    LateSubmissionPenaltySummaryCard(
      rows,
      tagStatus(Some(penalty), None),
      if (!isAnAdjustedPoint || isAnAddedPoint) penalty.penaltyOrder.toInt.toString else "",
      penalty.penaltyNumber,
      isReturnSubmitted,
      isAddedPoint = isAnAddedPoint,
      isAppealedPoint = appealStatus.getOrElse(AppealStatusEnum.Unappealable) != AppealStatusEnum.Unappealable,
      appealStatus = appealStatus,
      appealLevel = appealLevel,
      isAdjustedPoint = isAnAdjustedPoint,
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty)
    )
  }

  private def getMultiplePenaltyPeriodMessage(penalty: LSPDetails)(implicit messages: Messages): Option[Html] = {
    if (penalty.lateSubmissions.getOrElse(Seq.empty).size > 1)
      Some(Html(messages("lsp.multiple.penaltyPeriod", dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).last.taxPeriodDueDate.get))))
    else None
  }

  def financialSummaryCard(penalty: LSPDetails, threshold: Int)(implicit messages: Messages, user: User[_]): LateSubmissionPenaltySummaryCard = {
    val base = Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head.taxPeriodStartDate.get),
            dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head.taxPeriodEndDate.get)
          )
        )
      ),
      summaryListRow(
        messages("summaryCard.key2"),
        Html(
          dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head.taxPeriodDueDate.get)
        )
      ),
      PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head.returnReceiptDate.fold(
        summaryListRow(
          messages("summaryCard.key3"),
          Html(
            messages("summaryCard.key3.defaultValue")
          )
        )
      )(dateSubmitted =>
        summaryListRow(
          messages("summaryCard.key3"),
          Html(
            dateToString(dateSubmitted)
          )
        )
      )
    )
    buildFinancialSummaryCard(penalty, threshold, base)
  }

  private def buildFinancialSummaryCard(penalty: LSPDetails, threshold: Int, baseRows: Seq[SummaryListRow])
                                       (implicit messages: Messages, user: User[_]): LateSubmissionPenaltySummaryCard = {
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    val appealInformationWithoutUnappealableStatus = penalty.appealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable))).getOrElse(Seq.empty)
    LateSubmissionPenaltySummaryCard(
      if (appealInformationWithoutUnappealableStatus.nonEmpty) {
        baseRows :+ summaryListRow(
          messages("summaryCard.appeal.status"),
          returnAppealStatusMessageBasedOnPenalty(Some(penalty), None)
        )
      } else baseRows,
      tagStatus(Some(penalty), None),
      getPenaltyNumberBasedOnThreshold(penalty.penaltyOrder, threshold),
      penalty.penaltyNumber,
      penalty.lateSubmissions.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head).fold(false)(_.returnReceiptDate.isDefined),
      isFinancialPoint = penalty.penaltyCategory == LSPPenaltyCategoryEnum.Charge,
      isThresholdPoint = penalty.penaltyCategory == LSPPenaltyCategoryEnum.Threshold,
      isAppealedPoint = appealInformationWithoutUnappealableStatus.nonEmpty,
      appealStatus = appealStatus,
      appealLevel = appealLevel,
      totalPenaltyAmount = penalty.chargeAmount.getOrElse(BigDecimal(0)),
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty)
    )
  }

  def getPenaltyNumberBasedOnThreshold(penaltyNumberAsString: String, threshold: Int): String = {
    if (penaltyNumberAsString.toInt > threshold) "" else penaltyNumberAsString.toInt.toString
  }

  def pointSummaryCard(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages, user: User[_]): LateSubmissionPenaltySummaryCard = {
    val cardBody = PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head.returnReceiptDate match {
      case Some(_: LocalDate) => returnSubmittedCardBody(penalty, thresholdMet)
      case None => returnNotSubmittedCardBody(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).head)
    }
    val appealInformationWithoutUnappealableStatus = penalty.appealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable))).getOrElse(Seq.empty)
    if (appealInformationWithoutUnappealableStatus.nonEmpty) {
      buildLSPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(Some(penalty), None)
      ), penalty)
    } else {
      buildLSPSummaryCard(cardBody, penalty)
    }
  }

  def returnSubmittedCardBody(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages): Seq[SummaryListRow] = {
    val period = penalty.lateSubmissions
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val base = Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.taxPeriodStartDate.get),
            dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.taxPeriodEndDate.get)
          )
        )
      ),
      summaryListRow(messages("summaryCard.key2"), Html(dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.taxPeriodDueDate.get))),
      summaryListRow(messages("summaryCard.key3"), Html(dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get)
        .head.returnReceiptDate.get)))
    )

    if (penalty.penaltyExpiryDate.toString.nonEmpty && !thresholdMet && !appealStatus.contains(AppealStatusEnum.Upheld)) {
      base :+ summaryListRow(messages("summaryCard.key4"), Html(dateToMonthYearString(penalty.penaltyExpiryDate)))
    } else {
      base
    }
  }

  def returnNotSubmittedCardBody(lateSubmissions: LateSubmission)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    summaryListRow(
      messages("summaryCard.key1"),
      Html(
        messages(
          "summaryCard.value1",
          dateToString(lateSubmissions.taxPeriodStartDate.get),
          dateToString(lateSubmissions.taxPeriodEndDate.get)
        )
      )
    ),
    summaryListRow(messages("summaryCard.key2"), Html(dateToString(lateSubmissions.taxPeriodDueDate.get))),
    summaryListRow(messages("summaryCard.key3"), Html(messages("summaryCard.key3.defaultValue")))
  )

  private def removedPointCard(penalty: LSPDetails)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateToString(penalty.lateSubmissions.flatMap(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.taxPeriodStartDate).get),
            dateToString(penalty.lateSubmissions.flatMap(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.taxPeriodEndDate).get)
          )
        )
      )),
      penalty.expiryReason.fold[Option[SummaryListRow]](None)(expiryReason => {
        Some(summaryListRow(messages("summaryCard.removedReason"), if(expiryReason.equalsIgnoreCase("FAP")) Html(messages(s"summaryCard.removalReason.FAP")) else Html(expiryReason)))
      })
    ).collect {
      case Some(x) => x
    }

    buildLSPSummaryCard(rows, penalty, isAnAdjustedPoint = true)
  }

  def findAndReindexPointIfIsActive(indexedActivePoints: Seq[(LSPDetails, Int)], penaltyPoint: LSPDetails): LSPDetails = {
    if (indexedActivePoints.map(_._1).contains(penaltyPoint)) {
      val numberOfPoint = indexedActivePoints.find(_._1 == penaltyPoint).get._2 + 1
      penaltyPoint.copy(penaltyOrder = s"$numberOfPoint")
    } else {
      penaltyPoint
    }
  }

  def pointsThresholdMet(threshold: Int, activePoints: Int): Boolean = activePoints >= threshold

  def populateLatePaymentPenaltyCard(lpp: Option[Seq[LPPDetails]])
                                    (implicit messages: Messages, user: User[_]): Option[Seq[LatePaymentPenaltySummaryCard]] = {
    lpp.map {
      _.map(penalty => lppSummaryCard(penalty))
    }
  }

  def lppSummaryCard(lpp: LPPDetails)(implicit messages: Messages, user: User[_]): LatePaymentPenaltySummaryCard = {
    val cardBody = if (lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP2) lppAdditionalCardBody(lpp) else lppCardBody(lpp)
    val isPaid = lpp.penaltyAmountOutstanding.contains(BigDecimal(0))
    val isVatPaid = lpp.penaltyStatus == LPPPenaltyStatusEnum.Posted
    val appealInformationWithoutUnappealableStatus = lpp.appealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable))).getOrElse(Seq.empty)
    if (appealInformationWithoutUnappealableStatus.nonEmpty) {
      buildLPPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(None, Some(lpp))
      ), lpp, isPaid, isVatPaid)
    } else {
      buildLPPSummaryCard(cardBody, lpp, isPaid, isVatPaid)
    }
  }

  private def returnAppealStatusMessageBasedOnPenalty(penaltyPoint: Option[LSPDetails], lpp: Option[LPPDetails])
                                                     (implicit messages: Messages, user: User[_]): Html = {
    val seqAppealInformation = if (penaltyPoint.isDefined) penaltyPoint.get.appealInformation else lpp.get.appealInformation
    val appealInformationWithoutUnappealableStatus = seqAppealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable)))
    val appealStatus = appealInformationWithoutUnappealableStatus.get.headOption.flatMap(_.appealStatus).get
    val appealLevel = appealInformationWithoutUnappealableStatus.get.headOption.flatMap(_.appealLevel).get
    (appealStatus, appealLevel) match {
      case (AppealStatusEnum.Upheld | AppealStatusEnum.Rejected, _) =>
        html(
          Html(messages(s"summaryCard.appeal.${appealStatus.toString}.${appealLevel.toString}")),
          Html("<br>"),
          if (!user.isAgent) link("#", "summaryCard.appeal.readMessage") else HtmlFormat.empty
        )
      //TODO: implementation for AppealStatus Reinstated
      /*case Some(AppealStatusEnum.Reinstated) =>
        html(
          Html(messages(s"summaryCard.appeal.${penalty.get.toString}")),
          Html("<br>"),
          if (!user.isAgent) link("#", "summaryCard.appeal.readMessageReinstated") else HtmlFormat.empty
        )*/
      case _ =>
        Html(messages(s"summaryCard.appeal.${appealStatus.toString}.${appealLevel.toString}"))
    }
  }

  def summaryListRow(label: String, value: Html): SummaryListRow = SummaryListRow(
    key = Key(
      content = Text(label),
      classes = "govuk-summary-list__key"
    ),
    value = Value(
      content = HtmlContent(value),
      classes = "govuk-summary-list__value"
    ),
    classes = "govuk-summary-list__row"
  )

  private def buildLPPSummaryCard(rows: Seq[SummaryListRow],
                                  lpp: LPPDetails, isPaid: Boolean, isVatPaid: Boolean)
                                 (implicit messages: Messages): LatePaymentPenaltySummaryCard = {
    val amountDue = lpp.penaltyAmountOutstanding.getOrElse(BigDecimal(0)) + lpp.penaltyAmountPaid.getOrElse(BigDecimal(0))
    val appealStatus = lpp.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = lpp.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    LatePaymentPenaltySummaryCard(
      cardRows = rows,
      status = tagStatus(None, Some(lpp)),
      penaltyChargeReference = lpp.penaltyChargeReference,
      principalChargeReference = lpp.principalChargeReference,
      isPenaltyPaid = isPaid,
      amountDue,
      appealStatus,
      appealLevel,
      isVatPaid = isVatPaid,
      penaltyCategory = lpp.penaltyCategory
    )
  }

  private def lppCardBody(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages("summaryCard.lpp.key2.value.lpp1"))),
      summaryListRow(messages("summaryCard.lpp.key3"), Html(messages(getLPPPenaltyReasonKey(lpp.LPPDetailsMetadata.mainTransaction.get),
        dateToString(lpp.principalChargeBillingFrom),
        dateToString(lpp.principalChargeBillingTo)))
      ),
      summaryListRow(messages("summaryCard.lpp.key4"), Html(dateToString(lpp.principalChargeDueDate))),
      summaryListRow(messages("summaryCard.lpp.key5"), Html(messages(getVATPaymentDate(lpp))))
    )
  }

  private def lppAdditionalCardBody(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages("summaryCard.lpp.key2.value.lpp2"))),
      summaryListRow(messages("summaryCard.lpp.key3"), Html(messages(getLPPPenaltyReasonKey(lpp.LPPDetailsMetadata.mainTransaction.get),
        dateToString(lpp.principalChargeBillingFrom),
        dateToString(lpp.principalChargeBillingTo)))
      ),
      summaryListRow(messages("summaryCard.lpp.key4"), Html(dateToString(lpp.principalChargeDueDate))),
      summaryListRow(messages("summaryCard.lpp.key5"), Html(messages(getVATPaymentDate(lpp))))
    )
  }

  private def getVATPaymentDate(lpp: LPPDetails)(implicit messages: Messages): String = {
    if (lpp.penaltyStatus.equals(LPPPenaltyStatusEnum.Posted) && lpp.principalChargeLatestClearing.isDefined) {
      dateToString(lpp.principalChargeLatestClearing.get)
    } else {
      "summaryCard.lpp.paymentNotReceived"
    }
  }

  private def getLPPPenaltyReasonKey(mainTransactionEnum: MainTransactionEnum.Value): String = {
    mainTransactionEnum match {
      case VATReturnFirstLPP | VATReturnSecondLPP => "summaryCard.lpp.key3.value.vat"
      case CentralAssessmentFirstLPP | CentralAssessmentSecondLPP => "summaryCard.lpp.key3.value.centralAssessment"
      case OfficersAssessmentFirstLPP | OfficersAssessmentSecondLPP => "summaryCard.lpp.key3.value.officersAssessment"
      case ErrorCorrectionFirstLPP | ErrorCorrectionSecondLPP => "summaryCard.lpp.key3.value.ecn"
      case AdditionalAssessmentFirstLPP | AdditionalAssessmentSecondLPP => "summaryCard.lpp.key3.value.vat" //TODO: content to be added later
      case ProtectiveAssessmentFirstLPP | ProtectiveAssessmentSecondLPP => "summaryCard.lpp.key3.value.vat" //TODO: content to be added later
      case POAReturnChargeFirstLPP | POAReturnChargeSecondLPP => "summaryCard.lpp.key3.value.vat" //TODO: content to be added later
      case AAReturnChargeFirstLPP | AAReturnChargeSecondLPP => "summaryCard.lpp.key3.value.vat" //TODO: content to be added later
      case _ => "summaryCard.lpp.key3.value.vat" //Should be unreachable
    }
  }

  def tagStatus(lsp: Option[LSPDetails], lpp: Option[LPPDetails])(implicit messages: Messages): Tag = {
    if (lsp.isDefined) {
      getTagStatus(lsp.get)
    } else {
      getTagStatus(lpp.get)
    }
  }

  private def getTagStatus(penalty: LSPDetails)(implicit messages: Messages): Tag = {
    val penaltyPointStatus = penalty.penaltyStatus
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val penaltyAmountPaid = penalty.chargeAmount.getOrElse(BigDecimal(0)) - penalty.chargeOutstandingAmount.getOrElse(BigDecimal(0))
    val penaltyAmount = penalty.chargeAmount.getOrElse(BigDecimal(0))
    penaltyPointStatus match {
      case LSPPenaltyStatusEnum.Inactive if appealStatus.contains(AppealStatusEnum.Upheld) => renderTag(messages("status.cancelled"))
      case LSPPenaltyStatusEnum.Inactive => renderTag(messages("status.removed"))
      case LSPPenaltyStatusEnum.Active if penaltyAmount > BigDecimal(0) => showDueOrPartiallyPaidDueTag(penalty.chargeOutstandingAmount, penaltyAmountPaid)
      case _ => renderTag(messages("status.active"))
    }
  }

  private def getTagStatus(penalty: LPPDetails)(implicit messages: Messages): Tag = {
    val latePaymentPenaltyStatus = penalty.penaltyStatus
    val latePaymentPenaltyAppealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    (latePaymentPenaltyAppealStatus, latePaymentPenaltyStatus) match {
      case (Some(AppealStatusEnum.Upheld), _) => renderTag(messages("status.cancelled"))
      case (_, LPPPenaltyStatusEnum.Accruing) => renderTag(messages("status.estimated"))
      case (_, LPPPenaltyStatusEnum.Posted) if penalty.penaltyAmountOutstanding.contains(BigDecimal(0)) => renderTag(messages("status.paid"))
      case (_, _) => showDueOrPartiallyPaidDueTag(penalty.penaltyAmountOutstanding, penalty.penaltyAmountPaid.getOrElse(BigDecimal(0)))
    }
  }

  def renderTag(status: String, cssClass: String = ""): Tag = Tag(
    content = Text(status),
    classes = s"$cssClass"
  )

  def showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding: Option[BigDecimal], penaltyAmountPaid: BigDecimal)(implicit messages: Messages): Tag = (penaltyAmountOutstanding, penaltyAmountPaid) match {
    case (Some(outstanding), _) if outstanding == 0 => renderTag(messages("status.paid"))
    case (Some(outstanding), paid) if paid > 0 =>
      renderTag(messages("status.partialPayment.due", CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(outstanding)), "penalty-due-tag")
    case _ => renderTag(messages("status.due"), "penalty-due-tag")
  }

}
