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

import assets.messages.IndexMessages._
import base.SpecBase
import models.ETMPPayload
import models.financial.{AmountTypeEnum, Financial, OverviewElement}
import models.penalty._
import models.point._
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
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
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithNoActivePenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the singular wording when there is only one penalty point" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the plural wording when there is multiple penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "user is agent - show what happens when next submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLateForAgent
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }

      "user is agent - show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 2, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplicationForAgent
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 2, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the summary of penalty points" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 1, lateSubmissions = 1, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 2, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "user is agent - show some warning text explaining what will happen if another submission is late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }

      "user is agent - show a summary of amount of points accrued and returns submitted late" in {
        val etmpPayloadModelWithActivePenaltyPointsBelowThreshold: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 3, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(etmpPayloadModelWithActivePenaltyPointsBelowThreshold)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(3, 3)
      }
    }

    "points are at or above the threshold" should {
      val etmpPayloadModelWithActivePenaltyPointsOnOrAboveThreshold: ETMPPayload = ETMPPayload(
        pointsTotal = 4, lateSubmissions = 4, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
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
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
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
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
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
          pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
      }

      "have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - have a breakdown of how the points were calculated" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 2, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(sampleAddedPenaltyPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val sampleAddedPenaltyPoints: ETMPPayload = ETMPPayload(
          pointsTotal = 3, lateSubmissions = 4, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
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
          pointsTotal = 3, lateSubmissions = 4, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = quarterlyThreshold, penaltyPoints = Seq.empty
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

  "getContentBasedOnLatePaymentPenaltiesFromModel" should {
    "no active payment penalties" should {
      "display a message in a <p> tag" in {
        val etmpPayloadModelWithNoActivePaymentPenalty: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = Some(Seq.empty)
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(etmpPayloadModelWithNoActivePaymentPenalty)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePaymentPenalty
      }
    }

    "display unpaid VAT text and 'how lpp calculated' link" when {
      "user has outstanding vat to pay" in {
        val etmpPayloadWithOutstandingVAT: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = Some(Seq(
            LatePaymentPenalty(
              `type` = PenaltyTypeEnum.Financial,
              id = "1234567891",
              reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
              dateCreated = sampleDate,
              status = PointStatusEnum.Due,
              appealStatus = None,
              period = PaymentPeriod(
                startDate = sampleDate,
                endDate = sampleDate,
                dueDate = sampleDate,
                paymentStatus = PaymentStatusEnum.Due
              ),
              communications = Seq.empty,
              financial = Financial(
                amountDue = 400.00,
                outstandingAmountDue = 11.00,
                dueDate = sampleDate
              )
            )
          )
          )
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(etmpPayloadWithOutstandingVAT)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe unpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        //TODO: change this when we have link to calculation page
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }

      "client has outstanding vat to pay" in {
        val etmpPayloadWithOutstandingVAT: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = Some(Seq(
            LatePaymentPenalty(
              `type` = PenaltyTypeEnum.Financial,
              id = "1234567891",
              reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
              dateCreated = sampleDate,
              status = PointStatusEnum.Due,
              appealStatus = None,
              period = PaymentPeriod(
                startDate = sampleDate,
                endDate = sampleDate,
                dueDate = sampleDate,
                paymentStatus = PaymentStatusEnum.Due
              ),
              communications = Seq.empty,
              financial = Financial(
                amountDue = 400.00,
                outstandingAmountDue = 11.00,
                dueDate = sampleDate
              )
            )
          )
          )
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(etmpPayloadWithOutstandingVAT)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe agentClientUnpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        //TODO: change this when we have link to calculation page
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }
    }

    "display 'how lpp calculated' link" when {
      "user has no outstanding vat to pay" in {
        val etmpPayloadWithOutstandingVAT: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = Some(Seq(
            LatePaymentPenalty(
              `type` = PenaltyTypeEnum.Financial,
              id = "1234567891",
              reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
              dateCreated = sampleDate,
              status = PointStatusEnum.Active,
              appealStatus = None,
              period = PaymentPeriod(
                startDate = sampleDate,
                endDate = sampleDate,
                dueDate = sampleDate,
                paymentStatus = PaymentStatusEnum.Paid
              ),
              communications = Seq.empty,
              financial = Financial(
                amountDue = 400.00,
                outstandingAmountDue = 0.00,
                dueDate = sampleDate
              )
            )
          )
          )
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(etmpPayloadWithOutstandingVAT)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        //TODO: change this when we have link to calculation page
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }
    }
  }

  "getWhatYouOweBreakdown" should {
    "return None" when {
      "the user has no outstanding payments" in {
        val etmpPayloadWithNoOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None)
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithNoOutstandingPayments)
        result.isEmpty shouldBe true
      }
    }

    "return Some" when {
      "the user has outstanding VAT to pay" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None,
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                estimatedInterest = Some(12.04),
                crystalizedInterest = Some(11.23)
              )
            )
          ))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
      }

      "the user has outstanding LPP's to pay - no estimates" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty,
          latePaymentPenalties = Some(
            Seq(
              LatePaymentPenalty(
                `type` = PenaltyTypeEnum.Financial,
                id = "1234",
                reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
                dateCreated = sampleDate,
                status = PointStatusEnum.Due,
                appealStatus = None,
                period = PaymentPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  dueDate = sampleDate,
                  paymentStatus = PaymentStatusEnum.Paid
                ),
                communications = Seq.empty,
                financial = Financial(
                  amountDue = 100.34,
                  outstandingAmountDue = 50.12,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              )
            )
          ),
          vatOverview = None
          )
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£50.12 in late payment penalties") shouldBe true
      }

      "the user has outstanding LPP's to pay - with estimates" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty,
          latePaymentPenalties = Some(
            Seq(
              LatePaymentPenalty(
                `type` = PenaltyTypeEnum.Additional,
                id = "1234",
                reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
                dateCreated = sampleDate,
                status = PointStatusEnum.Estimated,
                appealStatus = None,
                period = PaymentPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  dueDate = sampleDate,
                  paymentStatus = PaymentStatusEnum.Paid
                ),
                communications = Seq.empty,
                financial = Financial(
                  amountDue = 31.34,
                  outstandingAmountDue = 10.12,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              ),
              LatePaymentPenalty(
                `type` = PenaltyTypeEnum.Financial,
                id = "1234",
                reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
                dateCreated = sampleDate,
                status = PointStatusEnum.Estimated,
                appealStatus = None,
                period = PaymentPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  dueDate = sampleDate,
                  paymentStatus = PaymentStatusEnum.Paid
                ),
                communications = Seq.empty,
                financial = Financial(
                  amountDue = 100.34,
                  outstandingAmountDue = 50.12,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              )
            )
          ),
          vatOverview = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£60.24 in estimated late payment penalties") shouldBe true
      }

      "the user has outstanding VAT and outstanding LPP's" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty,
          latePaymentPenalties = Some(
            Seq(
              LatePaymentPenalty(
                `type` = PenaltyTypeEnum.Additional,
                id = "1234",
                reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
                dateCreated = sampleDate,
                status = PointStatusEnum.Estimated,
                appealStatus = None,
                period = PaymentPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  dueDate = sampleDate,
                  paymentStatus = PaymentStatusEnum.Paid
                ),
                communications = Seq.empty,
                financial = Financial(
                  amountDue = 31.34,
                  outstandingAmountDue = 21.34,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              ),
              LatePaymentPenalty(
                `type` = PenaltyTypeEnum.Financial,
                id = "1234",
                reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
                dateCreated = sampleDate,
                status = PointStatusEnum.Estimated,
                appealStatus = None,
                period = PaymentPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  dueDate = sampleDate,
                  paymentStatus = PaymentStatusEnum.Paid
                ),
                communications = Seq.empty,
                financial = Financial(
                  amountDue = 100.34,
                  outstandingAmountDue = 50.23,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              )
            )
          ),
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                estimatedInterest = Some(12.04),
                crystalizedInterest = Some(11.23)
              )
            )
          )
        )
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£71.57 in estimated late payment penalties") shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
      }

      "the user has outstanding VAT to pay and has other unrelated penalties" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None,
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                estimatedInterest = Some(12.04),
                crystalizedInterest = Some(11.23)
              )
            )
          ),
          otherPenalties = Some(true))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
        result.get.body.contains("other penalties not related to late submission or late payment") shouldBe true
      }

      "the user has other unrelated penalties only" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None,
          vatOverview = None, otherPenalties = Some(true))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe false
      }

      "the user has outstanding VAT to pay and outstanding LSP's" in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
          penaltyPoints = Seq(
            PenaltyPoint(
              `type` = PenaltyTypeEnum.Financial,
              id = "1236",
              number = "3",
              appealStatus = None,
              dateCreated = sampleDate,
              dateExpired = Some(sampleDate),
              status = PointStatusEnum.Due,
              reason = None,
              period = Some(
                Seq(PenaltyPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  submission = Submission(
                    dueDate = sampleDate,
                    submittedDate = Some(sampleDate),
                    status = SubmissionStatusEnum.Submitted
                  )
                )
              )),
              communications = Seq.empty,
              financial = Some(
                Financial(
                  amountDue = 200.00,
                  outstandingAmountDue = 200.00,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              )
            ),
            PenaltyPoint(
              `type` = PenaltyTypeEnum.Financial,
              id = "1235",
              number = "2",
              appealStatus = None,
              dateCreated = sampleDate,
              dateExpired = Some(sampleDate),
              status = PointStatusEnum.Due,
              reason = None,
              period = Some(
                Seq(PenaltyPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  submission = Submission(
                    dueDate = sampleDate,
                    submittedDate = Some(sampleDate),
                    status = SubmissionStatusEnum.Submitted
                  )
                )
              )),
              communications = Seq.empty,
              financial = Some(
                Financial(
                  amountDue = 200.00,
                  outstandingAmountDue = 200.00,
                  dueDate = sampleDate,
                  estimatedInterest = None,
                  crystalizedInterest = None
                )
              )
            ),
            PenaltyPoint(
              `type` = PenaltyTypeEnum.Point,
              id = "1234",
              number = "1",
              appealStatus = None,
              dateCreated = sampleDate,
              dateExpired = Some(sampleDate),
              status = PointStatusEnum.Active,
              reason = None,
              period = Some(
                Seq(PenaltyPeriod(
                  startDate = sampleDate,
                  endDate = sampleDate,
                  submission = Submission(
                    dueDate = sampleDate,
                    submittedDate = Some(sampleDate),
                    status = SubmissionStatusEnum.Submitted
                  )
                )
              )),
              communications = Seq.empty,
              financial = None
            )
          ),
          latePaymentPenalties = None,
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                estimatedInterest = Some(12.04),
                crystalizedInterest = Some(11.23)
              )
            )
          ))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
        result.get.body.contains("£400 fixed penalties for late submission") shouldBe true
        result.get.body.contains("£43.27 in estimated VAT interest") shouldBe true
      }

      "the user has outstanding LSP's" in {
        val result = pageHelper.getWhatYouOweBreakdown(sampleLspDataWithDueFinancialPenalties)
        result.isDefined shouldBe true
        result.get.body.contains("£400 fixed penalties for late submission") shouldBe true
      }

      "the user has a single outstanding LSP" in {
        val etmpPayloadWithSingleLSP: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
          penaltyPoints = Seq(sampleFinancialPenaltyPoint.copy(financial = Some(
            Financial(
              amountDue = 200.00,
              outstandingAmountDue = 200.00,
              dueDate = LocalDateTime.now(),
              estimatedInterest = None,
              crystalizedInterest = Some(23.00)
            )
          ))),
          latePaymentPenalties = None,
          vatOverview = None)
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithSingleLSP)
        result.isDefined shouldBe true
        result.get.body.contains("£200 fixed penalty for late submission") shouldBe true
      }

      "the user has outstanding VAT Interest to pay - no estimated interest " in {
        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
          penaltyPoints = Seq.empty,
          latePaymentPenalties = None,
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                crystalizedInterest = Some(11.23)
              )
            )
          ))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£21.23 in VAT interest") shouldBe true
      }

      "the user has crystalized and estimated interest on penalties" in {
        val etmpPayloadWithInterestOnPenalties: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
          penaltyPoints = Seq(sampleFinancialPenaltyPoint.copy(financial = Some(
            Financial(
              amountDue = 0,
              outstandingAmountDue = 0,
              dueDate = LocalDateTime.now(),
              estimatedInterest = Some(16.10),
              crystalizedInterest = Some(23.00)
            )
          )),
            sampleFinancialPenaltyPoint.copy(financial = Some(
              Financial(
                amountDue = 0,
                outstandingAmountDue = 0,
                dueDate = LocalDateTime.now(),
                estimatedInterest = Some(14.05),
                crystalizedInterest = Some(23.00)
              )
            ))
          ),
          latePaymentPenalties = Some(Seq(sampleLatePaymentPenaltyDue.copy(financial =
            Financial(
              amountDue = 0,
              outstandingAmountDue = 0,
              dueDate = LocalDateTime.now(),
              estimatedInterest = Some(15.00),
              crystalizedInterest = Some(22.00)
            )
          ),
            sampleLatePaymentPenaltyDue.copy(financial =
              Financial(
                amountDue = 0,
                outstandingAmountDue = 0,
                dueDate = LocalDateTime.now(),
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(22.00)
              )
            )
          )),
          vatOverview = None)
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithInterestOnPenalties)
        result.isDefined shouldBe true
        result.get.body.contains("£145.15 in estimated interest on penalties") shouldBe true
      }

      "the user has just crystalized interest on penalties" in {
        val etmpPayloadWithCrystalizedButNoEstimatedInterestOnPenalties: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
          penaltyPoints = Seq(sampleFinancialPenaltyPoint.copy(financial = Some(
              Financial(
                amountDue = 0,
                outstandingAmountDue = 0,
                dueDate = LocalDateTime.now(),
                estimatedInterest = None,
                crystalizedInterest = Some(23.00)
              )
            ))
          ),
          latePaymentPenalties = Some(Seq(sampleLatePaymentPenaltyDue.copy(financial =
            Financial(
              amountDue = 0,
              outstandingAmountDue = 0,
              dueDate = LocalDateTime.now(),
              estimatedInterest = None,
              crystalizedInterest = Some(22.10)
            )
          ))
          ),
          vatOverview = None)
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithCrystalizedButNoEstimatedInterestOnPenalties)
        result.isDefined shouldBe true
        result.get.body.contains("£45.10 in interest on penalties") shouldBe true

      }

      "the user has no estimated or crystalized interest on penalties" in {
        val etmpPayloadWithNoInterestPayments: ETMPPayload = ETMPPayload(
          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None,
          vatOverview = Some(
            Seq(
              OverviewElement(
                `type` = AmountTypeEnum.VAT,
                amount = 100.00,
                estimatedInterest = Some(10.00),
                crystalizedInterest = Some(10.00)
              ),
              OverviewElement(
                `type` = AmountTypeEnum.Central_Assessment,
                amount = 123.45,
                estimatedInterest = Some(12.04),
                crystalizedInterest = Some(11.20)
              )
            )
          ))
        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithNoInterestPayments)
        result.isDefined shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
        result.get.body.contains("in interest on penalties") shouldBe false
        result.get.body.contains("in estimated interest on penalties") shouldBe false
      }

    }
  }
}
