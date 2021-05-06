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

class IndexPageHelperSpec extends SpecBase {
  val pageHelper: IndexPageHelper = injector.instanceOf[IndexPageHelper]

  private val quarterlyThreshold:Int = 4

  "getPluralOrSingularContentForOverview" should {
    "show the singular wording" when {
      "there is only one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)
        result.body shouldBe singularOverviewText
      }
    }

    "show the plural wording" when {
      "there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)
        result.body shouldBe pluralOverviewText
      }
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(1, 1)("this.is.a.message.singular", "this.is.a.message.plural")
        result.body shouldBe "this.is.a.message.singular"
      }
    }

    "show the plural wording" when {
      "there is more than one total passed in" in {
        val result = pageHelper.getPluralOrSingular(2, 2)("this.is.a.message.singular", "this.is.a.message.plural")
        result.body shouldBe "this.is.a.message.plural"
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

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = 2, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }
    }

    "points are at or above the threshold" should {
      val etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold: ETMPPayload = ETMPPayload(
        pointsTotal = 4, lateSubmissions = 4, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
      )
      val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold)
      val parsedHtmlResult = Jsoup.parse(result.body)

      "show the financial penalty threshold reached text" in {
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe thresholdReached
        parsedHtmlResult.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "show the penalty amount until account is updated text" in {
        parsedHtmlResult.select("p.govuk-body").get(1).text shouldBe lateReturnPenalty
      }

      "show the guidance link text" in {
        parsedHtmlResult.select("a.govuk-link").text shouldBe bringAccountUpToDate
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }
    }

    "points have been added" should {
      "show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }
    }

    "points have been removed" should {
      "show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 4, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }
    }
  }
}
