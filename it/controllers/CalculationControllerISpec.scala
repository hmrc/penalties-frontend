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
import config.featureSwitches.FeatureSwitching
import models.GetPenaltyDetails
import models.breathingSpace.BreathingSpace
import models.lpp._
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub.{agentAuthorised, unauthorised}
import stubs.PenaltiesStub.getPenaltyDetailsStub
import testUtils.{IntegrationSpecCommonBase, TestData}
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

import java.time.LocalDate

class CalculationControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching with TestData {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  val controller: CalculationController = injector.instanceOf[CalculationController]
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    authToken -> "1234"
  )
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    authToken -> "1234"
  )

  val penaltyDetailsWithDay15Charge: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400.00,
          LPP1LRDays = Some("15"),
          LPP1HRDays = None,
          LPP2Days = None,
          LPP1LRCalculationAmount = Some(20000.00),
          LPP1HRCalculationAmount = None,
          LPP2Percentage = None,
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = None,
          principalChargeLatestClearing = None,
          penaltyChargeReference = None
        )
      ))))

  val penaltyDetailsWithDay15ChargePosted: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          LPP1LRDays = Some("15"),
          LPP1HRDays = None,
          LPP2Days = None,
          LPP1LRCalculationAmount = Some(20000.00),
          LPP1HRCalculationAmount = None,
          LPP2Percentage = None,
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = None,
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890")
        )
      ))))

  val penaltyDetailsWithDueDateMoreThan30days: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("31"),
          LPP2Days = None,
          LPP1LRCalculationAmount = Some(10000.00),
          LPP1HRCalculationAmount = Some(10000.00),
          LPP2Percentage = None,
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.parse("2021-03-08")
        )
      ))))

  val penaltyDetailsWithDueDateMoreThan30daysAccruing: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400.00,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("31"),
          LPP2Days = None,
          LPP1LRCalculationAmount = Some(10000.00),
          LPP1HRCalculationAmount = Some(10000.00),
          LPP2Percentage = None,
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = None,
          penaltyChargeReference = None
        )
      ))))

  val penaltyDetailsWithAdditionalPenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
          penaltyAmountPaid = Some(113.45),
          penaltyAmountOutstanding = Some(10.00),
          penaltyAmountPosted = 123.45,
          penaltyAmountAccruing = 0,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("30"),
          LPP2Days = Some("31"),
          LPP1LRCalculationAmount = Some(3086.25),
          LPP1HRCalculationAmount = Some(3086.25),
          LPP2Percentage = Some(4.00),
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.now().minusDays(40)
        )
      ))))

  val penaltyDetailsWithAdditionalDuePenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("30"),
          LPP2Days = Some("31"),
          LPP1LRCalculationAmount = Some(3086.25),
          LPP1HRCalculationAmount = Some(3086.25),
          LPP2Percentage = Some(4.00),
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.now().minusDays(40)
        )
  ))))

  val penaltyDetailsWithAdditionalDuePenaltyTTPActive: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("30"),
          LPP2Days = Some("31"),
          LPP1LRCalculationAmount = Some(3086.25),
          LPP1HRCalculationAmount = Some(3086.25),
          LPP2Percentage = Some(4.00),
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.now().minusDays(40),
          vatOutstandingAmount = Some(BigDecimal(123.45)),
            LPPDetailsMetadata = LPPDetailsMetadata(
            mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
            outstandingAmount = Some(99),
            timeToPay = Some(
              Seq(
                TimeToPay(TTPStartDate = Some(LocalDate.parse("2021-01-01")), TTPEndDate = Some(LocalDate.parse("2021-02-01")))
              )
            ),
          )
        )
    ))))

  val penaltyDetailsWithAdditionalDuePenaltyTTPActiveBreathingSpaceActive: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("30"),
          LPP2Days = Some("31"),
          LPP1LRCalculationAmount = Some(3086.25),
          LPP1HRCalculationAmount = Some(3086.25),
          LPP2Percentage = Some(4.00),
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.now().minusDays(40),
          vatOutstandingAmount = Some(BigDecimal(123.45)),
            LPPDetailsMetadata = LPPDetailsMetadata(
            mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
            outstandingAmount = Some(99),
            timeToPay = Some(
              Seq(
                TimeToPay(TTPStartDate = Some(LocalDate.parse("2021-01-01")), TTPEndDate = Some(LocalDate.parse("2021-02-01")))
              )
            ),
          )
        ))
    )),
    breathingSpace = Some(Seq(
      BreathingSpace(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"))
    ))
  )

  val penaltyDetailsWithAdditionalDuePenaltyBreathingSpaceActive: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400,
          LPP1LRDays = Some("15"),
          LPP1HRDays = Some("30"),
          LPP2Days = Some("31"),
          LPP1LRCalculationAmount = Some(3086.25),
          LPP1HRCalculationAmount = Some(3086.25),
          LPP2Percentage = Some(4.00),
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = Some(2.00),
          principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
          penaltyChargeReference = Some("1234567890"),
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.now().minusDays(40),
          vatOutstandingAmount = Some(BigDecimal(123.45)),
            LPPDetailsMetadata = LPPDetailsMetadata(
            mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
            outstandingAmount = Some(99),
            timeToPay = None
          )
        ))
    )),
    breathingSpace = Some(Seq(
      BreathingSpace(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01"))
    ))
  )

  val penaltyDetailsWithDay15ChargeTTPActive: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(
        sampleLPPPosted.copy(
          penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
          penaltyStatus = LPPPenaltyStatusEnum.Accruing,
          penaltyAmountPaid = None,
          penaltyAmountOutstanding = None,
          penaltyAmountPosted = 0,
          penaltyAmountAccruing = 400.00,
          LPP1LRDays = Some("15"),
          LPP1HRDays = None,
          LPP2Days = None,
          LPP1LRCalculationAmount = Some(20000.00),
          LPP1HRCalculationAmount = None,
          LPP2Percentage = None,
          LPP1LRPercentage = Some(2.00),
          LPP1HRPercentage = None,
          principalChargeLatestClearing = None,
          penaltyChargeReference = None,
          vatOutstandingAmount = Some(BigDecimal(123.45)),
            LPPDetailsMetadata = LPPDetailsMetadata(
            mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
            outstandingAmount = Some(99),
            timeToPay = Some(
              Seq(
                TimeToPay(TTPStartDate = Some(LocalDate.parse("2021-01-01")), TTPEndDate = Some(LocalDate.parse("2021-02-01")))
              )
            ),
          )
        )
    ))))

  "GET /calculation when it is not an additional penalty" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithDay15ChargePosted))
      val request = controller.onPageLoad(
        "12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select(".penalty-information-caption span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£277.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£123.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID (after 30 days)" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithDueDateMoreThan30days))
      val request = controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select(".penalty-information-caption span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 30 days."
      parsedBody.select("#30-day-calculation").text() shouldBe "It is made up of 2 parts:"
      parsedBody.select("#main-content > div > div > ul > li:nth-child(1)").text() shouldBe "2% of £10,000.00 (the unpaid VAT 15 days after the due date) = £200.00"
      parsedBody.select("#main-content > div > div > ul > li:nth-child(2)").text() shouldBe "2% of £10,000.00 (the unpaid VAT 30 days after the due date) = £200.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Due date"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "8 March 2021"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£277.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£123.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      getPenaltyDetailsStub(Some(samplePenaltyDetails))
      val request = controller.onPageLoad("1234567890", "LPP1")(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      unauthorised()

      val request = controller.onPageLoad("12345", "LPP1")(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }

  "GET /calculation when it is not an additional penalty and is estimated" should {
    "return 200 (OK) and render the view correctly when the use has specified a valid penalty ID" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithDay15Charge))
      val request = controller.onPageLoad(
        "12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select(".penalty-information-caption span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
      parsedBody.select(".ttp-content").isEmpty shouldBe true
    }

    "the user has specified a valid penalty ID (parses decimals correctly)" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithDay15Charge))
      val request = controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select(".penalty-information-caption span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (TTP Active)" in {
      setFeatureDate(Some(LocalDate.of(2021, 1, 31)))
      getPenaltyDetailsStub(Some(penaltyDetailsWithDay15ChargeTTPActive))
      val request = controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select(".penalty-information-caption span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#ttp-inset-text").text() shouldBe "You’ve asked HMRC if you can set up a payment plan. If a payment plan has been agreed, and you keep up with all payments, this penalty will not increase further."
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(2).text() shouldBe "Penalties will show as estimates until you make all payments due under the payment plan."
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      getPenaltyDetailsStub(Some(samplePenaltyDetails))
      val request = controller.onPageLoad("1234567890", "LPP1")(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      unauthorised()
      val request = controller.onPageLoad("12345", "LPP1")(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }

  "GET /calculation when it is an additional penalty" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalPenalty))
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total increases daily based on the amount of unpaid VAT for the period."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Due date"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "8 March 2021"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£123.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£113.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£10.00"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalDuePenalty))
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total builds up daily until you pay your VAT or set up a payment plan."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select(".ttp-content").isEmpty shouldBe true
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(3).text() shouldBe "Penalties and interest will show as estimates until you pay the charge they relate to."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (TTP Active)" in {
      setFeatureDate(Some(LocalDate.of(2021, 1, 31)))
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalDuePenaltyTTPActive))
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total builds up daily until you pay your VAT or set up a payment plan."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#ttp-inset-text").get(0).text() shouldBe "You’ve asked HMRC if you can set up a payment plan. If a payment plan has been agreed, and you keep up with all payments, this penalty will not increase further."
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(3).text() shouldBe "Penalties will show as estimates until you make all payments due under the payment plan."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (TTP Active - user in Breathing Space)" in {
      setFeatureDate(Some(LocalDate.of(2021, 1, 31)))
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalDuePenaltyTTPActiveBreathingSpaceActive))
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total builds up daily until you pay your VAT or set up a payment plan. However, when we calculate your penalty we do not count the days you are in Breathing Space."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#ttp-inset-text").get(0).text() shouldBe "You’ve asked HMRC if you can set up a payment plan. If a payment plan has been agreed, and you keep up with all payments, this penalty will not increase further."
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(3).text() shouldBe "Penalties will show as estimates until:"
      parsedBody.select("#main-content .govuk-list--bullet li").get(0).text() shouldBe "you make all payments due under the payment plan, and"
      parsedBody.select("#main-content .govuk-list--bullet li").get(1).text() shouldBe "Breathing Space ends"
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (User in Breathing Space - TTP not active)" in {
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalDuePenaltyBreathingSpaceActive))
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total builds up daily until you pay your VAT or set up a payment plan. However, when we calculate your penalty we do not count the days you are in Breathing Space."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select(".ttp-content").isEmpty shouldBe true
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(3).text() shouldBe "Penalties and interest will show as estimates until:"
      parsedBody.select("#main-content .govuk-list--bullet li").get(0).text() shouldBe "you pay the charge they relate to, and"
      parsedBody.select("#main-content .govuk-list--bullet li").get(1).text() shouldBe "Breathing Space ends"
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (user is agent)" in {
      agentAuthorised()
      getPenaltyDetailsStub(Some(penaltyDetailsWithAdditionalDuePenalty), isAgent = true)
      val request = controller.onPageLoad("12345678901234", "LPP2")(fakeAgentRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select(".penalty-information-caption > .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select(".penalty-information-caption").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(0).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(1).text() shouldBe "The total builds up daily until your client pays their VAT or sets up a payment plan."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£0.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£400.00"
      parsedBody.select("h2").get(1).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(3).text() shouldBe "Penalties and interest will show as estimates until your client pays the charge they relate to."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      getPenaltyDetailsStub(Some(samplePenaltyDetails))
      val request = controller.onPageLoad("123456800", "LPP2")(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      unauthorised()
      val request = controller.onPageLoad("123456800", "LPP2")(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }
}
