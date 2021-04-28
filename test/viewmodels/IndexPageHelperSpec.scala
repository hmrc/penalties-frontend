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

import assets.messages.IndexMessages._
import base.SpecBase
import models.ETMPPayload
import org.jsoup.Jsoup
import org.scalatest.{Matchers, WordSpec}

class IndexPageHelperSpec extends SpecBase {
  val pageHelper: IndexPageHelper = injector.instanceOf[IndexPageHelper]

  "getPluralOrSingularBasedOnCurrentPenaltyPoints" should {
    "show the singular wording" when {
      "there is only one current point" in {
        val result = pageHelper.getPluralOrSingularBasedOnCurrentPenaltyPoints(1, 1)
        result.body shouldBe singularOverviewText
      }
    }

    "show the plural wording" when {
      "there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularBasedOnCurrentPenaltyPoints(2, 2)
        result.body shouldBe pluralOverviewText
      }
    }
  }

  "renderPointsTotal" should {
    "show the text 'Penalty points total' and have the total amount in a span (with a bold class name)" in {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p.govuk-body").text().contains(penaltyPointsTotal) shouldBe true
      parsedHtmlResult.select("span").text shouldBe "1"
      parsedHtmlResult.select("span").hasClass("govuk-!-font-weight-bold") shouldBe true
    }

    "the p class should have a larger font i.e. 27pt" in {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p").hasClass("govuk-!-font-size-27") shouldBe true
    }
  }

  "getContentBasedOnPointsFromModel" should {
    "no active penalty points" should {
      "display a message in a <p> tag" in {
        val etmpPayloadModelWithNoActivePenaltyPoints: ETMPPayload = ETMPPayload(
          0, 0, 0, 0, 0, 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithNoActivePenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "points are below threshold" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "show the (threshold + 1) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }
    }

    "points are at threshold" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }
    }
  }
}
