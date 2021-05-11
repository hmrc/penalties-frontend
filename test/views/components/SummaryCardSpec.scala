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

package views.components

import base.{BaseSelectors, SpecBase}
import models.financial.Financial
import models.penalty.PenaltyPeriod
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.nodes.Document
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import viewmodels.{SummaryCard, SummaryCardHelper}
import views.behaviours.ViewBehaviours
import views.html.components.summaryCard

import java.time.LocalDateTime

class SummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  val summaryCardHtml: summaryCard = injector.instanceOf[summaryCard]

  val summaryCardModelWithAddedPoint: SummaryCard = summaryCardHelper.populateCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Added,
    None,
    None,
    Seq.empty
  )), quarterlyThreshold).head

  val summaryCardModelWithRemovedPoint: SummaryCard = summaryCardHelper.populateCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "2",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Removed,
    Some("A really great reason."),
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        None,
        SubmissionStatusEnum.Overdue
      )
    )),
    Seq.empty
  )), quarterlyThreshold).head

  val summaryCardModelWithFinancialPointBelowThreshold: SummaryCard = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointAboveThreshold: SummaryCard = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "3",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), annualThreshold)


  "summaryCard" when {
    "given an added point" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPoint))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h3").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in {
        doc.select("a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
        //TODO: change this once we have the adjustment point info page
        doc.select("a").attr("href") shouldBe "#"
      }

      "display the 'active' status for an added point" in {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "1 January 2020"
      }

      "display when the point is due to expire" in {
        doc.select("dt").get(1).text() shouldBe "Point due to expire"
        doc.select("dd").get(1).text() shouldBe "February 2020"
      }

      "display that the user can not appeal an added point" in {
        doc.select("footer li").text() shouldBe "You cannot appeal this point"
      }
    }

    "given a removed point" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPoint))
      "display that the point number as usual" in {
        doc.select("h3").text() shouldBe "Penalty point"
      }

      "display the VAT period the point was removed from" in {
        doc.select("dt").get(0).text() shouldBe "VAT Period"
        doc.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the reason why the point was removed" in {
        doc.select("dt").get(1).text() shouldBe "Reason"
        doc.select("dd").get(1).text() shouldBe "A really great reason."
      }

      "not display any footer text" in {
        doc.select("footer li").hasText shouldBe false
      }
    }

    "given a financial point" should {
      val docWithFinancialPointBelowThreshold: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThreshold))
      val docWithFinancialPointAboveThreshold: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointAboveThreshold))

      "shows the financial heading with point number when the point is below/at threshold for filing frequency" in {
        docWithFinancialPointBelowThreshold.select(".app-summary-card__title").get(0).text shouldBe "Penalty point 1: £200 penalty"
      }

      "shows the financial heading WITHOUT point number when the point is above threshold for filing frequency and a rewording of the appeal text" in {
        docWithFinancialPointAboveThreshold.select(".app-summary-card__title").get(0).text shouldBe "£200 penalty"
        docWithFinancialPointAboveThreshold.select(".app-summary-card__footer a").get(0).text shouldBe "Appeal this penalty"
      }
    }
  }

}
