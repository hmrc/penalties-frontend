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
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
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

class SummaryCardHelper @Inject()(link: views.html.components.link) extends ImplicitDateFormatter with ViewUtils{

  def populateLateSubmissionPenaltyCard(penalties: Seq[LSPDetails],
                                        threshold: Int, activePoints: Int)
                                       (implicit messages: Messages, user: User[_]): Seq[LateSubmissionPenaltySummaryCard] = {

    val thresholdMet: Boolean = pointsThresholdMet(threshold, activePoints)
    val filteredActivePenalties: Seq[LSPDetails] = penalties.filter(_.penaltyStatus != LSPPenaltyStatusEnum.Inactive).reverse
    val indexedActivePoints = filteredActivePenalties.zipWithIndex
    penalties.map { penalty =>
      val newPenalty = findAndReindexPointIfIsActive(indexedActivePoints, penalty)
      (newPenalty.penaltyCategory, newPenalty.appealInformation.map(_.head.appealStatus)) match {
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.FAPIndicator.contains("X") & newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Active => addedPointCard(newPenalty, thresholdMet)//Added FAP
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.FAPIndicator.contains("X") & newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive => removedPointCard(newPenalty)//Added FAP
        case (LSPPenaltyCategoryEnum.Point, _) if newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive & newPenalty.expiryReason.isDefined => removedPointCard(newPenalty)//Removed FAP
        case (LSPPenaltyCategoryEnum.Point, Some(AppealStatusEnum.Upheld)) if newPenalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive => removedPointCard(newPenalty)//Removed FAP
        case (LSPPenaltyCategoryEnum.Point, _) => pointSummaryCard(newPenalty, thresholdMet)// normal points
        case (LSPPenaltyCategoryEnum.Threshold, _) => financialSummaryCard(newPenalty, threshold)//normal threshold
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
      .fold(false)(_.taxReturnStatus.equals(TaxReturnStatusEnum.Open))
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

  private def getMultiplePenaltyPeriodMessage(penalty : LSPDetails)(implicit messages: Messages): Option[Html]={
    if(penalty.lateSubmissions.getOrElse(Seq.empty).size > 1)
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
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    val appealLevel = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealLevel))

    LateSubmissionPenaltySummaryCard(
      if (appealStatus.isDefined) {
        base :+ summaryListRow(
          messages("summaryCard.appeal.status"),
          returnAppealStatusMessageBasedOnPenalty(Some(penalty), None)
        )
      } else base,
      tagStatus(Some(penalty), None),
      getPenaltyNumberBasedOnThreshold(penalty.penaltyOrder, threshold),
      penalty.penaltyNumber,
      penalty.lateSubmissions.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head).fold(false)(_.returnReceiptDate.isDefined),
      isFinancialLSP = penalty.penaltyCategory == LSPPenaltyCategoryEnum.Charge,
      isThresholdPoint = penalty.penaltyCategory == LSPPenaltyCategoryEnum.Threshold,
      isAppealedPoint = appealStatus.isDefined,
      appealStatus = appealStatus,
      appealLevel = appealLevel,
      amountDue = penalty.chargeOutstandingAmount.getOrElse(BigDecimal(0)),
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
    val appealStatus = penalty.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
    if (appealStatus.isDefined) {
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
      penalty.expiryReason.fold[Option[SummaryListRow]](None)(x => {
        Some(summaryListRow(messages("summaryCard.removedReason"),
          Html(x)))
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
    val isPaid = lpp.penaltyAmountOutstanding.contains(0)
    val isVatPaid = lpp.penaltyStatus == LPPPenaltyStatusEnum.Posted
    if (lpp.appealInformation.isDefined) {
      buildLPPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(None, Some(lpp))
      ), lpp, isPaid, isVatPaid)
    } else {
      buildLPPSummaryCard(cardBody, lpp, isPaid, isVatPaid)
    }
  }

  private def returnAppealStatusMessageBasedOnPenalty(penaltyPoint: Option[LSPDetails], lpp: Option[LPPDetails])(implicit messages: Messages, user: User[_]): Html = {
    val seqAppealInformation = if (penaltyPoint.isDefined) penaltyPoint.get.appealInformation else lpp.get.appealInformation
    val appealStatus = seqAppealInformation.get.headOption.flatMap(_.appealStatus).get
    val appealLevel = seqAppealInformation.get.headOption.flatMap(_.appealLevel).get
    (appealStatus, appealLevel) match {
      case (AppealStatusEnum.Unappealable, _) => html(
        Html(messages(s"summaryCard.appealCheck"))
      )
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
      penaltyId = lpp.principalChargeReference, // TODO: Change to penaltyChargeReference
      isPenaltyPaid = isPaid,
      amountDue,
      appealStatus,
      appealLevel,
      isVatPaid = isVatPaid,
      isAdditionalPenalty = lpp.penaltyCategory == LPPPenaltyCategoryEnum.LPP2
    )
  }

  private def lppCardBody(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(summaryListRow(
      messages("summaryCard.key1"),
      Html(
        messages(
          "summaryCard.value1",
          dateToString(lpp.principalChargeBillingFrom),
          dateToString(lpp.principalChargeBillingTo)
        )
      )
    ),
      summaryListRow(messages("summaryCard.lpp.key3"), Html(dateToString(lpp.principalChargeDueDate))),
      summaryListRow(messages("summaryCard.lpp.key4"), Html(messages(getVATPaymentDate(lpp)))),
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages(getLPPPenaltyReasonKey("lpp.reason"))))
    )
  }

  private def lppAdditionalCardBody(lpp: LPPDetails)(implicit messages: Messages): Seq[SummaryListRow] ={
    val dueDatePlus31Days: String = dateToString(lpp.principalChargeDueDate.plusDays(31))
    Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateToString(lpp.principalChargeBillingFrom),
            dateToString(lpp.principalChargeBillingTo)
          )
        )
      ),
      //TODO: get the reason from New API Call
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages(getLPPAdditionalPenaltyReasonKey("reason TODO")))),
      summaryListRow(messages("summaryCard.lpp.additional.key"), Html(dueDatePlus31Days)))
  }

  private def getVATPaymentDate(lpp: LPPDetails)(implicit messages: Messages): String = {
    //  TODO: more info on paymentReceivedDate
    //    if(lpp.period.paymentReceivedDate.isDefined) {
    //      dateTimeToString(lpp.period.paymentReceivedDate.get)
    //    } else {
      "summaryCard.lpp.key5"
    //}
  }

  private def getLPPAdditionalPenaltyReasonKey(reason: String): String = {"LPPAdditionalPenaltyReasonKey"} // TODO: New API call for its implementation

  private def getLPPPenaltyReasonKey(reason: String): String = {"LPPPenaltyReasonKey"} // TODO: New API call for its implementation

  def tagStatus(penalty: Option[LSPDetails], lpp: Option[LPPDetails])(implicit messages: Messages): Tag = {

    if (penalty.isDefined) {
      val periodSubmissionStatus = penalty.get.lateSubmissions.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.taxReturnStatus)
      val penaltyPointStatus = penalty.get.penaltyStatus
      val appealStatus = penalty.get.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
      val isVATSubmitted = periodSubmissionStatus.contains(TaxReturnStatusEnum.Fulfilled)
      val penaltyAmountPaid = penalty.get.chargeAmount.getOrElse(BigDecimal(0)) - penalty.get.chargeOutstandingAmount.getOrElse(BigDecimal(0))
      val penaltyAmount = penalty.get.chargeAmount.getOrElse(BigDecimal(0))
      penaltyPointStatus match {
        case LSPPenaltyStatusEnum.Inactive if appealStatus.contains(AppealStatusEnum.Upheld) => renderTag(messages("status.cancelled"))
        case LSPPenaltyStatusEnum.Inactive => renderTag(messages("status.removed"))
        case LSPPenaltyStatusEnum.Active if penaltyAmount > BigDecimal(0) =>  showDueOrPartiallyPaidDueTag(penalty.get.chargeOutstandingAmount, penaltyAmountPaid)
        case LSPPenaltyStatusEnum.Active if appealStatus.contains(AppealStatusEnum.Rejected) & isVATSubmitted =>  renderTag(messages("status.rejected"))
        case _ => renderTag(messages("status.active"))
      }
    } else {
      val latePaymentPenaltyStatus = lpp.get.penaltyStatus
      val latePaymentPenaltyAppealStatus = lpp.get.appealInformation.flatMap(_.headOption.flatMap(_.appealStatus))
      (latePaymentPenaltyAppealStatus, latePaymentPenaltyStatus) match {
        case (Some(AppealStatusEnum.Upheld), _) => renderTag(messages("status.cancelled"))
        case (_, LPPPenaltyStatusEnum.Accruing) => renderTag(messages("status.estimated"))
        case (_, LPPPenaltyStatusEnum.Posted) if lpp.get.penaltyAmountOutstanding.contains(BigDecimal(0))=> renderTag(messages("status.paid"))
        case (_, _) => showDueOrPartiallyPaidDueTag(lpp.get.penaltyAmountOutstanding, lpp.get.penaltyAmountPaid.getOrElse(BigDecimal(0)))
      }
    }
  }

  def renderTag(status: String, cssClass: String = ""): Tag = Tag(
    content = Text(status),
    classes = s"$cssClass"
  )

  def showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding: Option[BigDecimal], penaltyAmountPaid: BigDecimal)(implicit messages: Messages): Tag = (penaltyAmountOutstanding,penaltyAmountPaid) match {
    case (Some(outstanding), _) if outstanding == 0 => renderTag(messages("status.paid"))
    case (Some(outstanding), paid) if paid > 0 =>
      renderTag(messages("status.partialPayment.due", CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(outstanding)), "penalty-due-tag")
    case _ => renderTag(messages("status.due"), "penalty-due-tag")
  }

}
