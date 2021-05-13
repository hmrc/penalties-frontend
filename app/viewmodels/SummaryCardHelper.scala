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
import models.point.PointStatusEnum.{Active, Due, Rejected, Removed, Paid}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.SubmissionStatusEnum.{Overdue, Submitted}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

class SummaryCardHelper extends ImplicitDateFormatter {

  def populateCard(penalties: Seq[PenaltyPoint], threshold: Int)(implicit messages: Messages): Seq[SummaryCard] = {
    val filteredActivePenalties: Seq[PenaltyPoint] = penalties.filter(_.status != PointStatusEnum.Removed).reverse
    val indexedActivePoints = filteredActivePenalties.zipWithIndex
    penalties.map { penalty =>
      val newPenalty = findAndReindexPointIfIsActive(indexedActivePoints, penalty)
      (newPenalty.`type`, newPenalty.status) match {
        case (PenaltyTypeEnum.Financial, _) => financialSummaryCard(newPenalty, threshold)
        case (PenaltyTypeEnum.Point, PointStatusEnum.Added) => addedPointCard(newPenalty)
        case (PenaltyTypeEnum.Point, PointStatusEnum.Removed) => removedPointCard(newPenalty)
        case (PenaltyTypeEnum.Point, _) => pointSummaryCard(newPenalty)
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

  private def addedPointCard(penalty: PenaltyPoint)(implicit messages: Messages): SummaryCard = {
    val rows = Seq(
      Some(summaryListRow(
        messages("summaryCard.addedOnKey"),
        Html(
          dateTimeToString(penalty.dateCreated)
        )
      )
      ),
      penalty.dateExpired.fold[Option[SummaryListRow]](None)(x => {
        Some(summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(x))))
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
      isAdjustedPoint = isAnAdjustedPoint
    )
  }

  def returnSubmittedCardBody(penalty: PenaltyPoint)(implicit messages: Messages): Seq[SummaryListRow] = {
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

    if(penalty.dateExpired.isDefined) {
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

  def pointSummaryCard(penalty: PenaltyPoint)(implicit messages: Messages): SummaryCard = {
    val cardBody = penalty.period.get.submission.submittedDate match {
      case Some(_: LocalDateTime) => returnSubmittedCardBody(penalty)
      case None => returnNotSubmittedCardBody(penalty.period.get)
    }

    buildSummaryCard(cardBody, penalty)
  }

  def financialSummaryCard(penalty: PenaltyPoint, threshold: Int)(implicit messages: Messages): SummaryCard = {
    SummaryCard(
      Seq(
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
      ),
      tagStatus(penalty),
      getPenaltyNumberBasedOnThreshold(penalty.number, threshold),
      penalty.id,
      penalty.period.fold(false)(_.submission.submittedDate.isDefined),
      isFinancialPoint = penalty.`type` == PenaltyTypeEnum.Financial,
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

    (periodSubmissionStatus, penaltyPointStatus) match {
      case (None, _)                    => renderTag(messages("status.active"))
      case (Some(_), Removed)           => renderTag(messages("status.removed"))
      case (Some(_), Paid)              => renderTag(messages("status.paid"))
      case (Some(Overdue), _)           => renderTag(messages("status.due"), "penalty-due-tag")
      case (Some(Submitted), Active)    => renderTag(messages("status.active"))
      case (Some(Submitted), Rejected)  => renderTag(messages("status.rejected"))
      case (Some(Submitted), Due)       => renderTag(messages("status.due"), "penalty-due-tag")
      case (_, _)                       => renderTag(messages("status.active")) // Temp solution
    }
  }
}
