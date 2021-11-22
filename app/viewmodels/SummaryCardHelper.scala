/*
 * Copyright 2021 HM Revenue & Customs
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

import models.User
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentStatusEnum, PenaltyPeriod}
import models.point.PointStatusEnum.{Active, Due, Paid, Rejected, Removed}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.SubmissionStatusEnum.{Overdue, Submitted}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.{ImplicitDateFormatter, PenaltyPeriodHelper, ViewUtils}

import java.time.LocalDateTime
import javax.inject.Inject

class SummaryCardHelper @Inject()(link: views.html.components.link) extends ImplicitDateFormatter with ViewUtils {

  def populateLateSubmissionPenaltyCard(penalties: Seq[PenaltyPoint],
                                        threshold: Int, activePoints: Int)
                                       (implicit messages: Messages, user: User[_]): Seq[LateSubmissionPenaltySummaryCard] = {
    val thresholdMet: Boolean = pointsThresholdMet(threshold, activePoints)
    val filteredActivePenalties: Seq[PenaltyPoint] = penalties.filter(_.status != PointStatusEnum.Removed).reverse
    val indexedActivePoints = filteredActivePenalties.zipWithIndex
    penalties.map { penalty =>
      val newPenalty = findAndReindexPointIfIsActive(indexedActivePoints, penalty)
      (newPenalty.`type`, newPenalty.status, newPenalty.appealStatus) match {
        case (PenaltyTypeEnum.Financial, _, _) => financialSummaryCard(newPenalty, threshold)
        case (PenaltyTypeEnum.Point, PointStatusEnum.Added, _) => addedPointCard(newPenalty, thresholdMet)
        case (PenaltyTypeEnum.Point, PointStatusEnum.Removed, None) => removedPointCard(newPenalty)
        case (PenaltyTypeEnum.Point, _, _) => pointSummaryCard(newPenalty, thresholdMet)
        case _ => throw new Exception("[SummaryCardHelper][populateLateSubmissionPenaltyCard] Incorrect values provided")
      }
    }
  }

  def populateLatePaymentPenaltyCard(lpp: Option[Seq[LatePaymentPenalty]])
                                    (implicit messages: Messages, user: User[_]): Option[Seq[LatePaymentPenaltySummaryCard]] = {
    lpp.map {
      _.map(penalty => lppSummaryCard(penalty))
    }
  }

  def findAndReindexPointIfIsActive(indexedActivePoints: Seq[(PenaltyPoint, Int)], penaltyPoint: PenaltyPoint): PenaltyPoint = {
    if (indexedActivePoints.map(_._1).contains(penaltyPoint)) {
      val numberOfPoint = indexedActivePoints.find(_._1 == penaltyPoint).get._2 + 1
      penaltyPoint.copy(number = s"$numberOfPoint")
    } else {
      penaltyPoint
    }
  }

  private def addedPointCard(penalty: PenaltyPoint, thresholdMet: Boolean)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.addedOnKey"),
        Html(
          dateTimeToString(penalty.dateCreated)
        )
      )
      ),
      penalty.dateExpired.fold[Option[SummaryListRow]](None)(x => {
        if (!thresholdMet) {
          Some(summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(x))))
        } else {
          None
        }
      })
    ).collect {
      case Some(x) => x
    }

    buildLSPSummaryCard(rows, penalty, isAnAddedPoint = true, isAnAdjustedPoint = true)
  }

  private def removedPointCard(penalty: PenaltyPoint)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(penalty.period.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.startDate).get),
            dateTimeToString(penalty.period.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.endDate).get)
          )
        )
      )),
      penalty.reason.fold[Option[SummaryListRow]](None)(x => {
        Some(summaryListRow(messages("summaryCard.removedReason"),
          Html(x)))
      })
    ).collect {
      case Some(x) => x
    }

    buildLSPSummaryCard(rows, penalty, isAnAdjustedPoint = true)
  }

  private def buildLSPSummaryCard(rows: Seq[SummaryListRow], penalty: PenaltyPoint, isAnAddedPoint: Boolean = false,
                                  isAnAdjustedPoint: Boolean = false)(implicit messages: Messages): LateSubmissionPenaltySummaryCard = {
    val isReturnSubmitted = penalty.period.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head).fold(false)(_.submission.submittedDate.isDefined)

    LateSubmissionPenaltySummaryCard(
      rows,
      tagStatus(Some(penalty), None),
      if (!isAnAdjustedPoint || isAnAddedPoint) penalty.number else "",
      penalty.id,
      isReturnSubmitted,
      isAddedPoint = isAnAddedPoint,
      isAppealedPoint = penalty.appealStatus.isDefined,
      appealStatus = penalty.appealStatus,
      isAdjustedPoint = isAnAdjustedPoint,
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty)
    )
  }

  private def buildLPPSummaryCard(rows: Seq[SummaryListRow],
                                  lpp: LatePaymentPenalty, isPaid: Boolean, isVatPaid: Boolean)
                                 (implicit messages: Messages): LatePaymentPenaltySummaryCard = {
    LatePaymentPenaltySummaryCard(
      cardRows = rows,
      status = tagStatus(None, Some(lpp)),
      penaltyId = lpp.id,
      isPenaltyPaid = isPaid,
      amountDue = lpp.financial.amountDue,
      appealStatus = lpp.appealStatus,
      isVatPaid = isVatPaid,
      isAdditionalPenalty = lpp.`type` == PenaltyTypeEnum.Additional
    )
  }

  def returnSubmittedCardBody(penalty: PenaltyPoint, thresholdMet: Boolean)(implicit messages: Messages): Seq[SummaryListRow] = {
    val period = penalty.period
    val base = Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.startDate),
            dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.endDate)
          )
        )
      ),
      summaryListRow(messages("summaryCard.key2"), Html(dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.submission.dueDate))),
      summaryListRow(messages("summaryCard.key3"), Html(dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(period.get).head.submission.submittedDate.get)))
    )

    if (penalty.dateExpired.isDefined && !thresholdMet && !penalty.appealStatus.contains(AppealStatusEnum.Accepted) &&
      !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal)) {
      base :+ summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(penalty.dateExpired.get)))
    } else {
      base
    }
  }

  private def lppCardBody(lpp: LatePaymentPenalty)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(summaryListRow(
      messages("summaryCard.key1"),
      Html(
        messages(
          "summaryCard.value1",
          dateTimeToString(lpp.period.startDate),
          dateTimeToString(lpp.period.endDate)
        )
      )
    ),
        summaryListRow(messages("summaryCard.lpp.key3"), Html(dateTimeToString(lpp.period.dueDate))),
        summaryListRow(messages("summaryCard.lpp.key4"), Html(messages(getVATPaymentDate(lpp)))),
        summaryListRow(messages("summaryCard.lpp.key2"), Html(messages(getLPPPenaltyReasonKey(lpp.reason))))

    )
  }

  private def lppAdditionalCardBody(lpp: LatePaymentPenalty)(implicit messages: Messages): Seq[SummaryListRow] ={
    val dueDatePlus31Days: String = dateTimeToString(lpp.period.dueDate.plusDays(31))
    Seq(
        summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(lpp.period.startDate),
            dateTimeToString(lpp.period.endDate)
          )
        )
      ),
      summaryListRow(messages("summaryCard.lpp.key2"), Html(messages(getLPPAdditionalPenaltyReasonKey(lpp.reason)))),
      summaryListRow(messages("summaryCard.lpp.additional.key"), Html(dueDatePlus31Days)))
  }

  private def getVATPaymentDate(lpp: LatePaymentPenalty): String = {
    if(lpp.period.paymentReceivedDate.isDefined) {
      dateTimeToString(lpp.period.paymentReceivedDate.get)
    } else {
      "summaryCard.lpp.key5"
    }
  }

  private def getLPPAdditionalPenaltyReasonKey(reason: PaymentPenaltyReasonEnum.Value): String = {
    reason match {
      case PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS => "summaryCard.lpp.additional.30days"
      case PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_AFTER_30_DAYS => "summaryCard.lpp.additional.30days.centralAssessment"
      case PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_AFTER_30_DAYS => "summaryCard.lpp.additional.30days.ecn"
      case PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_AFTER_30_DAYS => "summaryCard.lpp.additional.30days.officersAssessment"
    }
  }

  private def getLPPPenaltyReasonKey(reason: PaymentPenaltyReasonEnum.Value): String = {
    reason match {
      case PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS => "summaryCard.lpp.15days"
      case PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS => "summaryCard.lpp.30days"
      case PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS=> "summaryCard.lpp.15days.centralAssessment"
      case PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS => "summaryCard.lpp.30days.centralAssessment"
      case PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_15_DAYS => "summaryCard.lpp.15days.ecn"
      case PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_30_DAYS => "summaryCard.lpp.30days.ecn"
      case PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS => "summaryCard.lpp.15days.officersAssessment"
      case PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS => "summaryCard.lpp.30days.officersAssessment"
     }
  }

  def returnNotSubmittedCardBody(period: PenaltyPeriod)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    summaryListRow(
      messages("summaryCard.key1"),
      Html(
        messages(
          "summaryCard.value1",
          dateTimeToString(period.startDate),
          dateTimeToString(period.endDate)
        )
      )
    ),
    summaryListRow(messages("summaryCard.key2"), Html(dateTimeToString(period.submission.dueDate))),
    summaryListRow(messages("summaryCard.key3"), Html(messages("summaryCard.key3.defaultValue")))
  )

  def pointSummaryCard(penalty: PenaltyPoint, thresholdMet: Boolean)(implicit messages: Messages, user: User[_]): LateSubmissionPenaltySummaryCard = {
    val cardBody = PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head.submission.submittedDate match {
      case Some(_: LocalDateTime) => returnSubmittedCardBody(penalty, thresholdMet)
      case None => returnNotSubmittedCardBody(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head)
    }

    if (penalty.appealStatus.isDefined) {
      buildLSPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(Some(penalty), None)
      ), penalty)
    } else {
      buildLSPSummaryCard(cardBody, penalty)
    }
  }

  def lppSummaryCard(lpp: LatePaymentPenalty)(implicit messages: Messages, user: User[_]): LatePaymentPenaltySummaryCard = {
    val cardBody = if (lpp.`type` == PenaltyTypeEnum.Additional) lppAdditionalCardBody(lpp) else lppCardBody(lpp)
    val isPaid = lpp.status == Paid
    val isVatPaid = lpp.period.paymentStatus == PaymentStatusEnum.Paid
    if (lpp.appealStatus.isDefined) {
      buildLPPSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(None, Some(lpp))
      ), lpp, isPaid, isVatPaid)
    } else {
      buildLPPSummaryCard(cardBody, lpp, isPaid, isVatPaid)
    }
  }

  //scalastyle:off
  def financialSummaryCard(penalty: PenaltyPoint, threshold: Int)(implicit messages: Messages, user: User[_]): LateSubmissionPenaltySummaryCard = {
    val base = Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head.startDate),
            dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head.endDate)
          )
        )
      ),
      summaryListRow(
        messages("summaryCard.key2"),
        Html(
          dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head.submission.dueDate)
        )
      ),
      PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).head.submission.submittedDate.fold(
        summaryListRow(
          messages("summaryCard.key3"),
          Html(
            messages("summaryCard.notYetSubmitted")
          )
        )
      )(dateSubmitted =>
        summaryListRow(
          messages("summaryCard.key3"),
          Html(
            dateTimeToString(dateSubmitted)
          )
        )
      )
    )

    LateSubmissionPenaltySummaryCard(
      if (penalty.appealStatus.isDefined) {
        base :+ summaryListRow(
          messages("summaryCard.appeal.status"),
          returnAppealStatusMessageBasedOnPenalty(Some(penalty), None)
        )
      } else base,
      tagStatus(Some(penalty), None),
      getPenaltyNumberBasedOnThreshold(penalty.number, threshold),
      penalty.id,
      penalty.period.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head).fold(false)(_.submission.submittedDate.isDefined),
      isFinancialPoint = penalty.`type` == PenaltyTypeEnum.Financial,
      isAppealedPoint = penalty.appealStatus.isDefined,
      appealStatus = penalty.appealStatus,
      amountDue = penalty.financial.get.amountDue,
      multiplePenaltyPeriod = getMultiplePenaltyPeriodMessage(penalty)
    )
  }
  def getPenaltyNumberBasedOnThreshold(penaltyNumberAsString: String, threshold: Int): String = {
    if (penaltyNumberAsString.toInt > threshold) "" else penaltyNumberAsString
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

  def renderTag(status: String, cssClass: String = ""): Tag = Tag(
    content = Text(status),
    classes = s"govuk-tag $cssClass"
  )

  def tagStatus(penalty: Option[PenaltyPoint], lpp: Option[LatePaymentPenalty])(implicit messages: Messages): Tag = {

    if (penalty.isDefined) {
      val periodSubmissionStatus = penalty.get.period.map(penaltyPeriod => PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriod).head.submission.status)
      val penaltyPointStatus = penalty.get.status
      val penaltyAppealStatus = penalty.get.appealStatus

      (penaltyAppealStatus, periodSubmissionStatus, penaltyPointStatus) match {
        case (Some(AppealStatusEnum.Accepted | AppealStatusEnum.Accepted_By_Tribunal), _, _) => renderTag(messages("status.cancelled"))
        case (_, Some(Submitted), Due) => showDueOrPartiallyPaidDueTag(penalty.get.financial)
        case (_, Some(_), Paid) => renderTag(messages("status.paid"))
        case (_, Some(Overdue), _) => showDueOrPartiallyPaidDueTag(penalty.get.financial)
        case (Some(AppealStatusEnum.Reinstated), _, _) => renderTag(messages("status.reinstated"))
        case (Some(AppealStatusEnum.Tribunal_Rejected), _, _) => renderTag(messages("status.active"))
        case (Some(AppealStatusEnum.Under_Review | AppealStatusEnum.Under_Tribunal_Review), _, _) => renderTag(messages("status.active"))
        case (_, None, _) => renderTag(messages("status.active"))
        case (_, Some(_), Removed) => renderTag(messages("status.removed"))
        case (_, Some(Submitted), Active) => renderTag(messages("status.active"))
        case (_, Some(Submitted), Rejected) => renderTag(messages("status.rejected"))
        case (_, _, _) => renderTag(messages("status.active")) // Temp solution
      }
    } else {
      val latePaymentPenaltyStatus = lpp.get.status
      val latePaymentPenaltyAppealStatus = lpp.get.appealStatus

      (latePaymentPenaltyAppealStatus, latePaymentPenaltyStatus) match {
        case (Some(AppealStatusEnum.Accepted | AppealStatusEnum.Accepted_By_Tribunal), _) => renderTag(messages("status.cancelled"))
        case (_, PointStatusEnum.Estimated) => renderTag(messages("status.estimated"))
        case (_, PointStatusEnum.Paid) => renderTag(messages("status.paid"))
        case (_, _) => showDueOrPartiallyPaidDueTag(Some(lpp.get.financial))
      }
    }
  }

  def pointsThresholdMet(threshold: Int, activePoints: Int): Boolean = activePoints >= threshold

  private def returnAppealStatusMessageBasedOnPenalty(penaltyPoint: Option[PenaltyPoint], lpp: Option[LatePaymentPenalty])(implicit messages: Messages, user: User[_]): Html = {
    val penalty = if (penaltyPoint.isDefined) penaltyPoint.get.appealStatus else lpp.get.appealStatus
    penalty.get match {
      case AppealStatusEnum.Accepted | AppealStatusEnum.Rejected | AppealStatusEnum.Tribunal_Rejected | AppealStatusEnum.Accepted_By_Tribunal =>
        html(
          Html(messages(s"summaryCard.appeal.${penalty.get.toString}")),
          Html("<br>"),
          if (!user.isAgent) link("#", "summaryCard.appeal.readMessage") else HtmlFormat.empty
        )
      case AppealStatusEnum.Reinstated =>
        html(
          Html(messages(s"summaryCard.appeal.${penalty.get.toString}")),
          Html("<br>"),
          if (!user.isAgent) link("#", "summaryCard.appeal.readMessageReinstated") else HtmlFormat.empty
        )
      case _ =>
        Html(
          messages(
            s"summaryCard.appeal.${penalty.get.toString}"
          )
        )
    }
  }

  def showDueOrPartiallyPaidDueTag(financial: Option[Financial])(implicit messages: Messages): Tag = financial match {
    case Some(financial) if financial.outstandingAmountDue == 0 => renderTag(messages("status.paid"))
    case Some(financial) if financial.outstandingAmountDue < financial.amountDue =>
      renderTag(messages("status.partialPayment.due", financial.outstandingAmountDue), "penalty-due-tag")
    case _ => renderTag(messages("status.due"), "penalty-due-tag")
  }

  private def getMultiplePenaltyPeriodMessage(penalty : PenaltyPoint)(implicit messages: Messages): Option[Html]={
    if(penalty.period.getOrElse(Seq.empty).size > 1)
      Some(Html(messages("lsp.multiple.penaltyPeriod", dateTimeToString(PenaltyPeriodHelper.sortedPenaltyPeriod(penalty.period.get).last.submission.dueDate))))
    else None
  }
}
