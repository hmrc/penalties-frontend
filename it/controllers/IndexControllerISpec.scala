/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.AppConfig
import models.breathingSpace.BreathingSpace
import org.jsoup.Jsoup
import play.api.http.{HeaderNames, Status}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub.{agentAuthorised, authorised, unauthorised}
import stubs.ComplianceStub.complianceDataStub
import stubs.PenaltiesStub._
import testUtils.{IntegrationSpecCommonBase, TestData}
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

class IndexControllerISpec extends IntegrationSpecCommonBase with TestData {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  val controller: IndexController = injector.instanceOf[IndexController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    authToken -> "12345",
    SessionKeys.pocAchievementDate -> "2022-01-01",
    SessionKeys.regimeThreshold -> "5"
  )
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    authToken -> "12345",
    SessionKeys.pocAchievementDate -> "2022-01-01",
    SessionKeys.regimeThreshold -> "5"
  )

  "GET /" should {
    "return 200 (OK) when the user is authorised" in {
      getPenaltyDetailsStub()
      complianceDataStub()
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
    }

    "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithAddedPoint))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 1 penalty point. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted a VAT Return late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent you a letter explaining why"
      parsedBody.select("header h4").text shouldBe "Penalty point 1: adjustment point"
      parsedBody.select("main strong").text shouldBe "active"
      val summaryCardBody = parsedBody.select(".app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Added on"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
      summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
      summaryCardBody.select("p.govuk-body a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe appConfig.adjustmentLink
      parsedBody.select(".app-summary-card footer div").text shouldBe "You cannot appeal this point"
    }

    "return 200 (OK) and render the view when there are removed points that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithRemovedPoints))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 1 penalty point. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 2 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("header h4").get(0).text shouldBe "Penalty point"
      parsedBody.select("main strong").get(0).text shouldBe "removed"
      val summaryCardBody = parsedBody.select(".app-summary-card__body").get(0)
      summaryCardBody.select("dt").get(0).text shouldBe "VAT period"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 31 January 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
      summaryCardBody.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
      summaryCardBody.select("p.govuk-body a").get(0).text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe appConfig.adjustmentLink
      parsedBody.select(".app-summary-card footer div").get(0).childrenSize() shouldBe 0 //No child nodes are within this div
    }

    "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
      getPenaltyDetailsStub(Some(getPenaltiesDataPayloadWith2PointsAndOneRemovedPoint))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 2 penalty points. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 3 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("main section h4").get(0).text shouldBe "Penalty point 2"
      parsedBody.select("main section h4").get(1).text shouldBe "Penalty point 1"
      parsedBody.select("main section h4").get(2).text shouldBe "Penalty point"
      parsedBody.select("main section strong").get(2).text shouldBe "removed"
    }

    "return 200 (OK) and render the view correctly when active points retrieved out of order" in {
      getPenaltyDetailsStub(Some(getPenaltiesDataPayloadOutOfOrder))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("main section .govuk-summary-list").get(0).select(".govuk-summary-list__value").get(0).text shouldBe "1 December 2020 to 31 December 2020"
      parsedBody.select("main section .govuk-summary-list").get(1).select(".govuk-summary-list__value").get(0).text shouldBe "1 November 2020 to 30 November 2020"
      parsedBody.select("main section .govuk-summary-list").get(2).select(".govuk-summary-list__value").get(0).text shouldBe "1 October 2020 to 31 October 2020"
    }

    "return 200 (OK) and render the view when there is a LSP with a penalty over the threshold with correct hidden text in header" in {
      complianceDataStub(Some(compliancePayload))
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithOverThreshold))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("header h4").get(0).text shouldBe "£200 penalty for late submission of VAT due on 7 March 2021"
    }

    "return 200 (OK) and render the view when there are LPPs paid that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltiesDataPayloadWithPaidLPP))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h4").get(0).ownText shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header h4 span").get(0).text shouldBe "for late payment of charge due on 7 March 2021"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 31 January 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "VAT due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "VAT paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      parsedBody.select("#late-payment-penalties footer a").get(1).ownText() shouldBe "Appeal this penalty"
    }

    "return 200 (OK) and render the what you owe section when relevant fields are present" in {
      complianceDataStub()
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#what-is-owed > h2").first.text shouldBe "Overview"
      parsedBody.select("#what-is-owed > p").first.text shouldBe "Your account has:"
      parsedBody.select("#what-is-owed > ul > li").first().text shouldBe "unpaid VAT charges"
      parsedBody.select("#what-is-owed > ul > li").get(1).text shouldBe "unpaid interest"
      parsedBody.select("#what-is-owed > ul > li").get(2).text shouldBe "a late payment penalty"
      parsedBody.select("#what-is-owed > ul > li").get(3).text shouldBe "late submission penalties"
      parsedBody.select("#what-is-owed > ul > li").get(4).text shouldBe "the maximum number of late submission penalty points"
      parsedBody.select("#what-is-owed > a").text shouldBe "Check amounts and pay"
      parsedBody.select("#main-content > h2").text shouldBe "Penalty and appeal details"
    }

    "return 200 (OK) and render the view when there are LPPs and additional penalties paid that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltiesDataPayloadWithLPPAndAdditionalPenalty))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h4").get(0).ownText shouldBe "£123.45 penalty"
      parsedBody.select("#late-payment-penalties section header h4 span").get(0).text shouldBe "for late payment of charge due on 7 March 2021"
      parsedBody.select("#late-payment-penalties section header strong").get(0).text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body").first()
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "Second penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 31 January 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "VAT due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "VAT paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      parsedBody.select("#late-payment-penalties footer li").text().contains("Appeal this penalty") shouldBe true
    }

    "return 200 (OK) and render the view when there are LPPs with VAT partially unpaid that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltiesDataPayloadWithLPPVATUnpaid))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h4").get(0).ownText shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header h4 span").text shouldBe "for late payment of charge due on 7 March 2021"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "£200 due"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 31 January 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "VAT due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "VAT paid"
      summaryCardBody.select("dd").get(3).text shouldBe "Payment not yet received"
      summaryCardBody.select(".govuk-details__summary-text").get(0).text shouldBe "Why you cannot appeal yet"
      summaryCardBody.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
      summaryCardBody.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your payment history. If you’ve already paid, keep checking back to see when the payment clears."
    }

    "return 200 (OK) and render the view when there are LPPs with VAT unpaid that are retrieved from the backend" in {
      complianceDataStub()
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(latePaymentPenalty = Some(unpaidLatePaymentPenalty))))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h4").get(0).ownText shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header h4 span").text shouldBe "for late payment of charge due on 7 March 2021"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "due"
    }

    "return 200 (OK) and render the view when there are appealed LPPs that are retrieved from the backend" in {
      getPenaltyDetailsStub(Some(getPenaltyPayloadWithLPPAppeal))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h4").get(0).ownText() shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header h4 span").text shouldBe "for late payment of charge due on 7 March 2021"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 31 January 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "VAT due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "VAT paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      summaryCardBody.select("dt").get(4).text shouldBe "Appeal status"
      summaryCardBody.select("dd").get(4).text shouldBe "Under review by HMRC"
    }

    "return 200 (OK) and add the latest lsp creation date and penalty threshold to the session" in {
      complianceDataStub()
      getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(latePaymentPenalty = Some(paidLatePaymentPenalty))))
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      await(request).session(fakeRequest).get(SessionKeys.pocAchievementDate).isDefined shouldBe true
      await(request).session(fakeRequest).get(SessionKeys.pocAchievementDate).get shouldBe "2022-01-01"
    }

    "agent view" must {
      "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
        agentAuthorised()
        getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithAddedPoint), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 1 penalty point. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted a VAT Return late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent them a letter explaining why"
        parsedBody.select("header h4").text shouldBe "Penalty point 1: adjustment point"
        parsedBody.select("main strong").text shouldBe "active"
        val summaryCardBody = parsedBody.select(".app-summary-card__body")
        summaryCardBody.select("dt").get(0).text shouldBe "Added on"
        summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
        summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
        summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
        summaryCardBody.select("p.govuk-body a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        summaryCardBody.select("p.govuk-body a").attr("href") shouldBe appConfig.adjustmentLink
        parsedBody.select(".app-summary-card footer div").text shouldBe "You cannot appeal this point"
      }

      "return 200 (OK) and render the view when there are removed points that are retrieved from the backend" in {
        agentAuthorised()
        getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithRemovedPoints), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 1 penalty point. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 2 VAT Returns late"
        parsedBody.select("header h4").get(0).text shouldBe "Penalty point"
        parsedBody.select("main strong").get(0).text shouldBe "removed"
        val summaryCardBody = parsedBody.select(".app-summary-card__body").get(0)
        summaryCardBody.select("dt").get(0).text shouldBe "VAT period"
        summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 31 January 2021"
        summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
        summaryCardBody.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
        summaryCardBody.select("p.govuk-body a").get(0).text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        summaryCardBody.select("p.govuk-body a").attr("href") shouldBe appConfig.adjustmentLink
        parsedBody.select(".app-summary-card footer div").get(0).childrenSize() shouldBe 0 //No child nodes are within this div
      }

      "return 200 (OK) and render the view when user is in breathing space" in {
        authorised()
        setFeatureDate(Some(sampleDate1))
        val penaltyDetailsWithBreathingSpace = getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(
          breathingSpace = Some(Seq(BreathingSpace(sampleDate1, sampleDate1)))
        )
        getPenaltyDetailsStub(Some(penaltyDetailsWithBreathingSpace))
        val request = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties a").get(0).text shouldBe "Read the guidance about late submission penalties (opens in a new tab)"
        parsedBody.select("#late-payment-penalties a").get(0).text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"
        parsedBody.select("#what-is-owed .govuk-button").get(0).text shouldBe "Check what you owe"
        setFeatureDate(None)
      }

      "return 200 (OK) and render the view when user is in breathing space for agents" in {
        agentAuthorised()
        setFeatureDate(Some(sampleDate1))
        val penaltyDetailsWithBreathingSpace = getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(
          breathingSpace = Some(Seq(BreathingSpace(sampleDate1, sampleDate1)))
        )
        getPenaltyDetailsStub(Some(penaltyDetailsWithBreathingSpace), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties a").get(0).text shouldBe "Read the guidance about late submission penalties (opens in a new tab)"
        parsedBody.select("#late-payment-penalties a").get(0).text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"
        parsedBody.select("#what-is-owed .govuk-button").get(0).text shouldBe "Check what your client owes"
      }

      "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
        agentAuthorised()
        getPenaltyDetailsStub(Some(getPenaltiesDataPayloadWith2PointsAndOneRemovedPoint), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 2 penalty points. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 3 VAT Returns late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent them a letter explaining why"
        parsedBody.select("main section h4").get(0).text shouldBe "Penalty point 2"
        parsedBody.select("main section h4").get(1).text shouldBe "Penalty point 1"
        parsedBody.select("main section h4").get(2).text shouldBe "Penalty point"
        parsedBody.select("main section strong").get(2).text shouldBe "removed"
      }

      "return 200 (OK) and render the view when there is outstanding payments for the client" in {
        complianceDataStub()
        agentAuthorised()
        getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#what-is-owed > h2").first.text shouldBe "Overview"
        parsedBody.select("#what-is-owed > p").first.text shouldBe "Your client’s account has:"
        parsedBody.select("#what-is-owed > ul > li").first().text shouldBe "unpaid VAT charges"
        parsedBody.select("#what-is-owed > ul > li").get(1).text shouldBe "unpaid interest"
        parsedBody.select("#what-is-owed > ul > li").get(2).text shouldBe "a late payment penalty"
        parsedBody.select("#what-is-owed > ul > li").get(3).text shouldBe "late submission penalties"
        parsedBody.select("#what-is-owed > ul > li").get(4).text shouldBe "the maximum number of late submission penalty points"
        parsedBody.select("#what-is-owed > a").text shouldBe "Check amounts"
        parsedBody.select("#main-content > h2").text shouldBe "Penalty and appeal details"
      }

      "return 200 (OK) and add the latest lsp creation date and the penalty threshold to the session" in {
        complianceDataStub()
        agentAuthorised()
        getPenaltyDetailsStub(Some(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue
          .copy(latePaymentPenalty = Some(paidLatePaymentPenalty))), isAgent = true)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        await(request).session(fakeAgentRequest).get(SessionKeys.pocAchievementDate).isDefined shouldBe true
        await(request).session(fakeAgentRequest).get(SessionKeys.pocAchievementDate).get shouldBe "2022-01-01"
      }
    }

    "return 200 (OK) and render the view when there are LSPs with multiple penalty period" in {
      getPenaltyDetailsStub(Some(getPenaltiesDetailsPayloadWithMultiplePenaltyPeriodInLSP))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      val summaryCardBody = parsedBody.select(" #late-submission-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT period"
      summaryCardBody.select("dd").get(0).text shouldBe "1 January 2021 to 15 January 2021"
      summaryCardBody.select("dt").get(1).text shouldBe "VAT Return due"
      summaryCardBody.select("dd").get(1).text shouldBe "8 May 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "Return submitted"
      summaryCardBody.select("dd").get(2).text shouldBe "13 May 2021"
      summaryCardBody.select("p.govuk-body").text() shouldBe "The VAT Return due on 24 May 2021 was also submitted late. HMRC only applies 1 penalty for late submission in each month."
    }

    "return 200 (OK) and render the view removing an LSP if it is an applicable expiryReason" in {
      getPenaltyDetailsStub(Some(getPenaltiesDetailsPayloadWithExpiredPoints))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      val summaryCardBody = parsedBody.select(" #late-submission-penalties .app-summary-card__body")
      summaryCardBody.size() shouldBe 2
      summaryCardBody.get(1).select("dt").get(0).text shouldBe "VAT period"
      summaryCardBody.get(1).select("dd").get(0).text shouldBe "1 January 2021 to 31 January 2021"
      summaryCardBody.get(0).select("dt").get(0).text shouldBe "VAT period"
      summaryCardBody.get(0).select("dd").get(0).text shouldBe "1 February 2021 to 28 February 2021"
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      unauthorised()
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-penalty" should {
    "redirect the user to the appeals service when the penalty is a LSP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = false,
        isObligation = false,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=false&isAdditional=false"
    }

    "redirect the user to the appeals service when the penalty is a LPP1" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = false,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=false"
    }

    "redirect the user to the appeals service when the penalty is a LPP2" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = false,
        isAdditional = true)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=true"
    }

    "redirect the user to the obligations appeals service when the penalty is a LSP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isObligation = true)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234"
    }
  }
}
