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
import models.point.{PenaltyPoint, PenaltyTypeEnum}
import models.submission.SubmissionStatusEnum.{Due, Submitted}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

class SummaryCardHelper extends ImplicitDateFormatter {

  def populateCard(penalties: Seq[PenaltyPoint])(implicit messages: Messages): Seq[SummaryCard] = {
    penalties.map { penalty =>
      penalty.`type` match {
        case PenaltyTypeEnum.Financial => financialSummaryCard(penalty)
        case PenaltyTypeEnum.Point => pointSummaryCard(penalty)
      }
    }
  }

  def returnSubmittedCardBody(penalty: PenaltyPoint)(implicit messages: Messages): Seq[SummaryListRow] = {

    val period = penalty.period
    Seq(
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
      summaryListRow(messages("summaryCard.key3"), Html(period.submission.submittedDate.map(date => dateTimeToString(date)))),
      summaryListRow(messages("summaryCard.key4"), Html(penalty.dateExpired.map(date => dateTimeToMonthYearString(date))))
    )
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
    val cardBody = penalty.period.submission.submittedDate match {
      case Some(_: LocalDateTime) => returnSubmittedCardBody(penalty)
      case None => returnNotSubmittedCardBody(penalty.period)
    }

    SummaryCard(
      cardBody,
      tagStatus(penalty),
      penalty.number
    )
  }

  def financialSummaryCard(penalty: PenaltyPoint)(implicit messages: Messages): SummaryCard = {
    SummaryCard(
      Seq(
        summaryListRow(
          messages("summaryCard.key1"),
          Html(
            messages(
              "summaryCard.value1",
              dateTimeToString(penalty.period.startDate),
              dateTimeToString(penalty.period.endDate)
            )
          )
        ),
        summaryListRow(
          messages("summaryCard.key2"),
          Html(
            dateTimeToString(penalty.period.submission.dueDate)
          )
        ),
        penalty.period.submission.submittedDate.fold(
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
      penalty.number,
      isFinancialPoint = penalty.`type` == PenaltyTypeEnum.Financial,
      amountDue = penalty.financial.get.amountDue
    )
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

  def tagStatus(penalty: PenaltyPoint)(implicit messages: Messages): Tag = {

    val submissionStatus = penalty.period.submission.status

    val tagType = submissionStatus match {
      case Submitted => penalty.status.toString
      case _ => submissionStatus.toString
    }

    val tagCssClass = submissionStatus match {
      case Due => "penalty-due-tag"
      case _ => ""
    }

    val renderedTag: (String, String) => Tag = (status, cssClass) => Tag(
      content = Text(status),
      classes = s"govuk-tag $cssClass"
    )

    renderedTag(tagType, tagCssClass)
  }
}
