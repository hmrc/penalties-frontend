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

import models._
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

class SummaryCardHelper extends ImplicitDateFormatter{

  def populateCard(penalties: Seq[PenaltyModel])(implicit messages: Messages): Seq[SummaryCard] = {
    penalties.map(penalty => summaryCard(penalty))
  }

  def summaryCard(penalty: PenaltyModel)(implicit messages: Messages): SummaryCard = SummaryCard(
    Seq(
      summaryListRow(
        messages("summaryCard.key1"),
        Html(
          messages(
            "summaryCard.value1",
            dateTimeToString(penalty.penaltyPeriod.startDate),
            dateTimeToString(penalty.penaltyPeriod.endDate)
          )
        )
      ),
      summaryListRow(messages("summaryCard.key2"), Html(dateTimeToString(penalty.penaltyPeriod.submission.dueDate))),
      summaryListRow(messages("summaryCard.key3"), Html(dateTimeToString(penalty.penaltyPeriod.submission.submissionDate))),
      summaryListRow(messages("summaryCard.key4"), Html(dateTimeToMonthYearString(penalty.dateExpired))),
    ),
    tagStatus(penalty.status),
    penalty.number
  )

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

  def tagStatus(statusText: String)(implicit messages: Messages): Tag = {
    val tagCssClass = statusText match {
      case _: String if statusText.equalsIgnoreCase("due") => "penalty-due-tag"
      case _ => ""
    }

    Tag(
      content = Text(statusText),
      classes = s"govuk-tag $tagCssClass"
    )
  }

}
