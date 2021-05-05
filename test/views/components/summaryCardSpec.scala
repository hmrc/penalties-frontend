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

class summaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  val summaryCardHtml: summaryCard = injector.instanceOf[summaryCard]

  val summaryCardModelWithAddedPoint: SummaryCard = summaryCardHelper.populateCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "2",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Added,
    None,
    PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        None,
        SubmissionStatusEnum.Submitted
      )
    ),
    Seq.empty
  ))).head

  val summaryCardModelWithRemovedPoint: SummaryCard = summaryCardHelper.populateCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "2",
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Removed,
    Some("A really great reason."),
    PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        None,
        SubmissionStatusEnum.Overdue
      )
    ),
    Seq.empty
  ))).head


  "summaryCard" when {
    "given an added point" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPoint))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h3").text() shouldBe "Penalty point 2: adjustment point"
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
        doc.select("h3").text() shouldBe "Penalty point 2"
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
  }

}
