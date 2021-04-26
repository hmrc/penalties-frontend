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

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import play.api.i18n.Messages
import models._
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

class SummaryCardHelper {


  def populateCard(penalties: Seq[PenaltyModel])(implicit messages: Messages): Seq[SummaryCard] = {
    penalties.map(penalty => summaryCard(penalty))
  }

  // will probably return Penalty details from service then for each 'penaltyPoints' print a summary card.

  def summaryCard(penalty: PenaltyModel)(implicit messages: Messages): SummaryCard = SummaryCard(
    Seq(
      summaryListRow(messages("summaryCard.Key1"), Html(messages("summaryCard.value1", penalty.penaltyPeriod.startDate, penalty.penaltyPeriod.endDate))),
      summaryListRow(messages("summaryCard.Key2"), Html(penalty.penaltyPeriod.submission.dueDate.toString)),
      summaryListRow(messages("summaryCard.Key3"), Html(penalty.penaltyPeriod.submission.submissionDate.toString)),
      summaryListRow(messages("summaryCard.Key4"), Html(penalty.dateExpired.toString)),
    ),
    penalty.status,
    penalty.number
  )

  def summaryListRow(label: String, value: Html): SummaryListRow = {
    SummaryListRow(
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
  }

}
