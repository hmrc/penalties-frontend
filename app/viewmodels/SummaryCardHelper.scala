/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import java.time.LocalDate

import javax.inject.Inject
import models.User
import models.appealInfo.AppealStatusEnum
import models.lpp.LPPPenaltyStatusEnum.Posted
import models.lpp.MainTransactionEnum._
import models.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, MainTransactionEnum}
import models.lsp._
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.{CurrencyFormatter, ImplicitDateFormatter, PenaltyPeriodHelper, ViewUtils}

class SummaryCardHelper @Inject()() extends ImplicitDateFormatter with ViewUtils {

  def populateLateSubmissionPenaltyCard(penalties: Seq[LSPDetails],
                                        threshold: Int, activePoints: Int)
                                       (implicit messages: Messages): Seq[LateSubmissionPenaltySummaryCard] = {
    val thresholdMet: Boolean = pointsThresholdMet(threshold, activePoints)
    val filteredActivePenalties: Seq[LSPDetails] = penalties.filter(_.penaltyStatus != LSPPenaltyStatusEnum.Inactive).reverse
    val indexedActivePoints = filteredActivePenalties.zipWithIndex
    penalties.map { penalty =>
      val newPenalty = findAndReindexPointIfIsActive(indexedActivePoints, penalty)
      newPenalty.lspTypeEnum match {
        case Some(LSPTypeEnum.AddedFAP) => addedPointCard(newPenalty, thresholdMet)
        case Some(LSPTypeEnum.RemovedFAP) => removedPointCard(newPenalty)
        case Some(LSPTypeEnum.AppealedPoint) => pointSummaryCard(newPenalty, thresholdMet)
        case Some(LSPTypeEnum.RemovedPoint) => removedPointCard(newPenalty)
        case Some(LSPTypeEnum.Point) => pointSummaryCard(newPenalty, thresholdMet)
        case Some(LSPTypeEnum.Financial) => financialSummaryCard(newPenalty, threshold)
        case _ => throw new Exception("[SummaryCardHelper][populateLateSubmissionPenaltyCard] No LSPTypeEnum provided")
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

    buildLSPSummaryCard(rows, penalty, isAnAddedPoint = true, isAnAddedOrRemovedPoint = true)
  }

  private def buildLSPSummaryCard(rows: Seq[SummaryListRow], penalty: LSPDetails, isAnAddedPoint: Boolean = false,
                                  isAnAddedOrRemovedPoint: Boolean = false, isManuallyRemovedPoint:Boolean = false)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val isReturnSubmitted = penalty.lateSubmissions.map(penaltyPeriod =>
      PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head)
      .fold(false)(_.taxReturnStatus.contains(TaxReturnStatusEnum.Fulfilled))
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    val dueDate = penalty.lateSubmissions.map(lateSubmissions => PenaltyPeriodHelper.sortedPenaltyPeriod(lateSubmissions)).map(_.head.taxPeriodDueDate.get)
    LateSubmissionPenaltySummaryCard(
      rows,
      tagStatus(Some(penalty), None),
      penalty.penaltyOrder.map(_.toInt.toString).getOrElse(""),
      penalty.penaltyNumber,
      isReturnSubmitted,
      isAddedPoint = isAnAddedPoint,
      isAppealedPoint = appealStatus.getOrElse(AppealStatusEnum.Unappealable) != AppealStatusEnum.Unappealable,
      appealStatus = appealStatus,
      appealLevel = appealLevel,
      isAddedOrRemovedPoint = isAnAddedOrRemovedPoint,
      isManuallyRemovedPoint = isManuallyRemovedPoint,
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty),
      dueDate = dueDate.map(dateToString(_)),
      penaltyCategory = penalty.penaltyCategory
    )
  }

  private def getMultiplePenaltyPeriodMessage(penalty: LSPDetails)(implicit messages: Messages): Option[Html] = {
    if (penalty.lateSubmissions.getOrElse(Seq.empty).size > 1)
      Some(Html(
        s"""
          |${messages("lsp.multiple.penaltyPeriod.1", dateToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.lateSubmissions.get).last.taxPeriodDueDate.get))}
          |<br>
          |${messages("lsp.multiple.penaltyPeriod.2")}
          |""".stripMargin
      ))
    else None
  }

  def financialSummaryCard(penalty: LSPDetails, threshold: Int)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
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
                                       (implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    val appealInformationWithoutUnappealableStatus = penalty.appealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable))).getOrElse(Seq.empty)
    val dueDate = penalty.lateSubmissions.map(lateSubmissions => PenaltyPeriodHelper.sortedPenaltyPeriod(lateSubmissions)).map(_.head.taxPeriodDueDate.get)
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
      penaltyCategory = penalty.penaltyCategory,
      isAppealedPoint = appealInformationWithoutUnappealableStatus.nonEmpty,
      appealStatus = appealStatus,
      appealLevel = appealLevel,
      totalPenaltyAmount = penalty.chargeAmount.getOrElse(BigDecimal(0)),
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty),
      dueDate = dueDate.map(dateToString(_))
    )
  }

  def getPenaltyNumberBasedOnThreshold(penaltyOrderNumberAsString: Option[String], threshold: Int): String = {
    penaltyOrderNumberAsString match {
      case None => ""
      case Some(penaltyNumber) if penaltyNumber.toInt > threshold => ""
      case Some(penaltyNumber) => penaltyNumber.toInt.toString
    }
  }

  def pointSummaryCard(penalty: LSPDetails, thresholdMet: Boolean)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
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
    val isFAP: Option[Boolean] = penalty.expiryReason.map(_.equals(ExpiryReasonEnum.Adjustment))
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
        Some(summaryListRow(messages("summaryCard.removedReason"), Html(messages(s"summaryCard.removalReason.${expiryReason.toString}"))))
      })
    ).collect {
      case Some(x) => x
    }

    buildLSPSummaryCard(rows, penalty, isAnAddedOrRemovedPoint = true, isManuallyRemovedPoint = !isFAP.getOrElse(false))
  }

  def findAndReindexPointIfIsActive(indexedActivePoints: Seq[(LSPDetails, Int)], penaltyPoint: LSPDetails): LSPDetails = {
    if (indexedActivePoints.map(_._1).contains(penaltyPoint)) {
      val numberOfPoint = indexedActivePoints.find(_._1 == penaltyPoint).get._2 + 1
      penaltyPoint.copy(penaltyOrder = Some(s"$numberOfPoint"))
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
    val cardBody = {
      lpp.penaltyCategory match {
        case LPPPenaltyCategoryEnum.MANUAL => lppManual(lpp)
        case LPPPenaltyCategoryEnum.LPP2 => lppAdditionalCardBody(lpp)
        case _ => lppCardBody(lpp)
      }
    }
    val isPaid = isPenaltyPaid(lpp)
    val isVatPaid = lpp.principalChargeLatestClearing.isDefined
    val appealInformationWithoutUnappealableStatus = lpp.appealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable))).getOrElse(Seq.empty)
    if (appealInformationWithoutUnappealableStatus.nonEmpty) {
      buildLPPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(None, Some(lpp))
      ), lpp, isPaid, isVatPaid)
    } else if(!isVatPaid && !lpp.penaltyCategory.equals(LPPPenaltyCategoryEnum.MANUAL)) {
      buildLPPSummaryCard(cardBody :+ SummaryListRow(),
        lpp, isPaid, isVatPaid)
    } else {
      buildLPPSummaryCard(cardBody,
        lpp, isPaid, isVatPaid)
    }
  }

  private def isPenaltyPaid(lpp: LPPDetails) = if(lpp.penaltyAmountPaid.isDefined) lpp.penaltyAmountPaid.get == lpp.penaltyAmountPosted else false

  private def returnAppealStatusMessageBasedOnPenalty(penaltyPoint: Option[LSPDetails], lpp: Option[LPPDetails])
                                                     (implicit messages: Messages): Html = {
    val seqAppealInformation = if (penaltyPoint.isDefined) penaltyPoint.get.appealInformation else lpp.get.appealInformation
    val appealInformationWithoutUnappealableStatus = seqAppealInformation.map(_.filterNot(_.appealStatus.contains(AppealStatusEnum.Unappealable)))
    val appealStatus = appealInformationWithoutUnappealableStatus.get.headOption.flatMap(_.appealStatus).get
    val appealLevel = appealInformationWithoutUnappealableStatus.get.headOption.flatMap(_.appealLevel).get
    Html(messages(s"summaryCard.appeal.${appealStatus.toString}.${appealLevel.toString}"))
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
                                 (implicit messages: Messages, user: User[_]): LatePaymentPenaltySummaryCard = {
    val amountDue = if(lpp.penaltyStatus == Posted) lpp.penaltyAmountPosted else lpp.penaltyAmountAccruing
    val appealStatus = lpp.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = lpp.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))
    val isCentralAssessment = lpp.LPPDetailsMetadata.mainTransaction.get.equals(CentralAssessmentFirstLPP) ||
      lpp.LPPDetailsMetadata.mainTransaction.get.equals(CentralAssessmentSecondLPP) || lpp.LPPDetailsMetadata.mainTransaction.get.equals(CentralAssessment)
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
      penaltyCategory = lpp.penaltyCategory,
      dueDate = dateToString(lpp.principalChargeDueDate),
      taxPeriodStartDate = lpp.principalChargeBillingFrom.toString,
      taxPeriodEndDate = lpp.principalChargeBillingTo.toString,
      isAgent = user.isAgent,
      isCentralAssessment = isCentralAssessment
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

  private def lppManual(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages("summaryCard.lpp.key2.value.manual"))),
      summaryListRow(messages("summaryCard.addedOnKey"), Html(dateToString(lpp.penaltyChargeCreationDate.get)))
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
      case VATReturnFirstLPP | VATReturnSecondLPP | VATReturnCharge => "summaryCard.lpp.key3.value.vat"
      case CentralAssessmentFirstLPP | CentralAssessmentSecondLPP | CentralAssessment => "summaryCard.lpp.key3.value.centralAssessment"
      case OfficersAssessmentFirstLPP | OfficersAssessmentSecondLPP | OfficersAssessment => "summaryCard.lpp.key3.value.officersAssessment"
      case ErrorCorrectionFirstLPP | ErrorCorrectionSecondLPP| ErrorCorrection => "summaryCard.lpp.key3.value.ecn"
      case AdditionalAssessmentFirstLPP | AdditionalAssessmentSecondLPP | AdditionalAssessment => "summaryCard.lpp.key3.value.additionalAssessment"
      case ProtectiveAssessmentFirstLPP | ProtectiveAssessmentSecondLPP | ProtectiveAssessment => "summaryCard.lpp.key3.value.protectiveAssessment"
      case POAReturnChargeFirstLPP | POAReturnChargeSecondLPP | POAReturnCharge => "summaryCard.lpp.key3.value.poaReturnCharge"
      case AAReturnChargeFirstLPP | AAReturnChargeSecondLPP | AAReturnCharge => "summaryCard.lpp.key3.value.aaReturnCharge"
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
      case (_, LPPPenaltyStatusEnum.Accruing) => renderTag(messages("status.estimate"))
      case (_, LPPPenaltyStatusEnum.Posted) if isPenaltyPaid(penalty) => renderTag(messages("status.paid"))
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
