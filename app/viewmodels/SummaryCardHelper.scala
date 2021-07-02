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

import java.time.LocalDateTime
import models.penalty.PenaltyPeriod
import models.point.PointStatusEnum.{Active, Due, Paid, Rejected, Removed}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.SubmissionStatusEnum.{Overdue, Submitted, Under_Review}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.{ImplicitDateFormatter, ViewUtils}

import javax.inject.Inject

class SummaryCardHelper @Inject()(link: views.html.components.link) extends ImplicitDateFormatter with ViewUtils {

  def populateCard(penalties: Seq[PenaltyPoint], threshold: Int, activePoints: Int)(implicit messages: Messages): Seq[SummaryCard] = {
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
      }
    }
  }

  def findAndReindexPointIfIsActive(indexedActivePoints: Seq[(PenaltyPoint, Int)], penaltyPoint: PenaltyPoint): PenaltyPoint = {
    if(indexedActivePoints.map(_._1).contains(penaltyPoint)) {
      val numberOfPoint = indexedActivePoints.find(_._1 == penaltyPoint).get._2 + 1
      penaltyPoint.copy(number = s"$numberOfPoint")
    } else {
      penaltyPoint
    }
  }

  private def addedPointCard(penalty: PenaltyPoint, thresholdMet: Boolean)(implicit messages: Messages): SummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.addedOnKey"),
        Html(
          dateTimeToString(penalty.dateCreated)
        )
      )
      ),
      penalty.dateExpired.fold[Option[SummaryListRow]](None)(x => {
        if(!thresholdMet) {
          Some(summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(x))))
        } else {
          None
        }
      })
    ).collect {
      case Some(x) => x
    }

    buildSummaryCard(rows, penalty, isAnAddedPoint = true, isAnAdjustedPoint = true)
  }

  private def removedPointCard(penalty: PenaltyPoint)(implicit messages: Messages): SummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(penalty.period.get.startDate),
            dateTimeToString(penalty.period.get.endDate)
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

    buildSummaryCard(rows, penalty, isAnAdjustedPoint = true)
  }

  private def buildSummaryCard(rows: Seq[SummaryListRow], penalty: PenaltyPoint, isAnAddedPoint: Boolean = false,
                               isAnAdjustedPoint: Boolean = false)(implicit messages: Messages): SummaryCard = {

    val isReturnSubmitted = penalty.period.fold(false)(_.submission.submittedDate.isDefined)

    SummaryCard(
      rows,
      tagStatus(penalty),
      if(!isAnAdjustedPoint || isAnAddedPoint) penalty.number else "",
      penalty.id,
      isReturnSubmitted,
      isAddedPoint = isAnAddedPoint,
      isAppealedPoint = penalty.appealStatus.isDefined,
      appealStatus = penalty.appealStatus,
      isAdjustedPoint = isAnAdjustedPoint
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
            dateTimeToString(period.get.startDate),
            dateTimeToString(period.get.endDate)
          )
        )
      ),
      summaryListRow(messages("summaryCard.key2"), Html(dateTimeToString(period.get.submission.dueDate))),
      summaryListRow(messages("summaryCard.key3"), Html(dateTimeToString(period.get.submission.submittedDate.get)))
    )

    if(penalty.dateExpired.isDefined && !thresholdMet && !penalty.appealStatus.contains(AppealStatusEnum.Accepted)) {
      base :+ summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(penalty.dateExpired.get)))
    } else {
      base
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

  def pointSummaryCard(penalty: PenaltyPoint, thresholdMet: Boolean)(implicit messages: Messages): SummaryCard = {
    val cardBody = penalty.period.get.submission.submittedDate match {
      case Some(_: LocalDateTime) => returnSubmittedCardBody(penalty, thresholdMet)
      case None => returnNotSubmittedCardBody(penalty.period.get)
    }

    if(penalty.appealStatus.isDefined) {
      buildSummaryCard(cardBody :+ summaryListRow(
        messages("summaryCard.appeal.status"),
        returnAppealStatusMessageBasedOnPenalty(penalty)
      ), penalty)
    } else {
      buildSummaryCard(cardBody, penalty)
    }
  }

  //scalastyle:off
  def financialSummaryCard(penalty: PenaltyPoint, threshold: Int)(implicit messages: Messages): SummaryCard = {
    val base = Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(penalty.period.get.startDate),
            dateTimeToString(penalty.period.get.endDate)
          )
        )
      ),
      summaryListRow(
        messages("summaryCard.key2"),
        Html(
          dateTimeToString(penalty.period.get.submission.dueDate)
        )
      ),
      penalty.period.get.submission.submittedDate.fold(
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

    SummaryCard(
      if(penalty.appealStatus.isDefined) {
        base :+ summaryListRow(
          messages("summaryCard.appeal.status"),
          returnAppealStatusMessageBasedOnPenalty(penalty)
        )
      } else base,
      tagStatus(penalty),
      getPenaltyNumberBasedOnThreshold(penalty.number, threshold),
      penalty.id,
      penalty.period.fold(false)(_.submission.submittedDate.isDefined),
      isFinancialPoint = penalty.`type` == PenaltyTypeEnum.Financial,
      isAppealedPoint = penalty.appealStatus.isDefined,
      appealStatus = penalty.appealStatus,
      amountDue = penalty.financial.get.amountDue
    )
  }

  def getPenaltyNumberBasedOnThreshold(penaltyNumberAsString: String, threshold: Int): String = {
    if(penaltyNumberAsString.toInt > threshold) "" else penaltyNumberAsString
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

  def tagStatus(penalty: PenaltyPoint)(implicit messages: Messages): Tag = {

    val periodSubmissionStatus = penalty.period.map(_.submission.status)
    val penaltyPointStatus = penalty.status
    val penaltyAppealStatus = penalty.appealStatus

    (penaltyAppealStatus, periodSubmissionStatus, penaltyPointStatus) match {
      case (Some(AppealStatusEnum.Accepted), _, _)          => renderTag(messages("status.cancelled"))
      case (Some(AppealStatusEnum.Reinstated), _, _)        => renderTag(messages("status.reinstated"))
      case (_, None, _)                                     => renderTag(messages("status.active"))
      case (_, Some(_), Removed)                            => renderTag(messages("status.removed"))
      case (_, Some(_), Paid)                               => renderTag(messages("status.paid"))
      case (_, Some(Overdue), _)                            => renderTag(messages("status.due"), "penalty-due-tag")
      case (Some(AppealStatusEnum.Under_Review), _, _)      => renderTag(messages("status.active"))
      case (_, Some(Submitted), Active)                     => renderTag(messages("status.active"))
      case (_, Some(Submitted), Rejected)                   => renderTag(messages("status.rejected"))
      case (_, Some(Submitted), Due)                        => renderTag(messages("status.due"), "penalty-due-tag")
      case (_, _, _)                                        => renderTag(messages("status.active")) // Temp solution
    }
  }

  def pointsThresholdMet(threshold: Int, activePoints: Int):Boolean = activePoints >= threshold

  private def returnAppealStatusMessageBasedOnPenalty(penaltyPoint: PenaltyPoint)(implicit messages: Messages): Html = {
    penaltyPoint.appealStatus.get match {
      case AppealStatusEnum.Accepted | AppealStatusEnum.Rejected => {
        html(
          Html(messages(s"summaryCard.appeal.${penaltyPoint.appealStatus.get.toString}")),
          Html("<br>"),
          link("#", "summaryCard.appeal.readMessage")
        )
      }
      case AppealStatusEnum.Reinstated => {
        html(
          Html(messages(s"summaryCard.appeal.${penaltyPoint.appealStatus.get.toString}")),
          Html("<br>"),
          link("#", "summaryCard.appeal.readMessageReinstated")
        )
      }
      case _ => {
        Html(
          messages(
            s"summaryCard.appeal.${penaltyPoint.appealStatus.get.toString}"
          )
        )
      }
    }
  }
}
