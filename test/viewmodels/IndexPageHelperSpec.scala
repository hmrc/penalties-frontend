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

  "getPluralOrSingularContentForOverview" should {
    "show the singular wording" when {
      "there is only one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, vatTraderUser)
        result.body shouldBe singularOverviewText
      }

      "user is agent - there is only one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, agentUser)
        result.body shouldBe singularAgentOverviewText
      }
    }

    "show the plural wording" when {
      "there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, vatTraderUser)
        result.body shouldBe pluralOverviewText
      }

      "user is agent - there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, agentUser)
        result.body shouldBe pluralAgentOverviewText
      }
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(1, 1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.singular"
      }

      "user is agent - there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(1, 1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.singular"
      }
    }

    "show the plural wording" when {
      "there is more than one total passed in" in {
        val result = pageHelper.getPluralOrSingular(2, 2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.plural"
      }

      "user is agent - there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(2, 2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.plural"
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

  "getGuidanceLink" should {
    "show the text 'Read the guidance about late submission penalties (opens in a new tab)' and have a link to external guidance which opens in a new tab" in {
      val result = pageHelper.getGuidanceLink
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("#guidance-link").text shouldBe externalGuidanceLinkText
      //TODO: change this when we have a GOV.UK guidance page
      parsedHtmlResult.select("#guidance-link").attr("href") shouldBe "#"
      parsedHtmlResult.select("#guidance-link").attr("target") shouldBe "_blank"
    }
  }


  "getContentBasedOnPointsFromModel" should {
    "no active penalty points" should {
      "display a message in a <p> tag" in {
        val etmpPayloadModelWithNoActivePenaltyPoints: ETMPPayload = ETMPPayload(
          0, 0, 0, 0, 0, 3, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithNoActivePenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "user is agent - show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLateForAgent
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }

      "user is agent - show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplicationForAgent
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = 2, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, 0, 0, 0, penaltyPointsThreshold = 2, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "user is agent - show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }

      "user is agent - show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(3, 3)
      }
    }

    "points are at or above the threshold" should {
      val etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold: ETMPPayload = ETMPPayload(
        pointsTotal = 4, lateSubmissions = 4, 0, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
      )
      val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold)(implicitly, vatTraderUser)
      val parsedHtmlResult = Jsoup.parse(result.body)

      val resultForAgent = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold)(implicitly, agentUser)
      val parsedHtmlResultForAgent = Jsoup.parse(resultForAgent.body)

      "show the financial penalty threshold reached text" in {
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe thresholdReached
        parsedHtmlResult.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "user is agent - show the financial penalty threshold reached text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).text shouldBe thresholdReachedAgent
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "show the penalty amount until account is updated text" in {
        parsedHtmlResult.select("p.govuk-body").get(1).text shouldBe lateReturnPenalty
        parsedHtmlResult.select("ul li").get(0).text shouldBe lateReturnPenaltyBullet1
        parsedHtmlResult.select("ul li").get(1).text shouldBe lateReturnPenaltyBullet2
      }

      "user is agent - show the penalty amount until account is updated text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(1).text shouldBe lateReturnPenaltyAgent
        parsedHtmlResultForAgent.select("ul li").get(0).text shouldBe lateReturnPenaltyBullet1Agent
        parsedHtmlResultForAgent.select("ul li").get(1).text shouldBe lateReturnPenaltyBullet2Agent
      }

      "show the guidance link text" in {
        parsedHtmlResult.select("a.govuk-link").text shouldBe bringAccountUpToDate
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad().url
      }
    }

    "points have been added" should {
      "show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = 1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }
    }

    "points have been removed" should {
      "show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 4, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 4, adjustmentPointsTotal = -1, 0, 0, penaltyPointsThreshold = quarterlyThreshold, Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }
    }
  }
}
