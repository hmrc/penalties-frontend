/*
 * Copyright 2022 HM Revenue & Customs
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
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub.{returnPenaltyDetailsStub, returnPenaltyDetailsStubAgent}
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

import java.time.{LocalDate, LocalDateTime}

class CalculationControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  val controller: CalculationController = injector.instanceOf[CalculationController]
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    authToken -> "1234"
  )
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    authToken -> "1234"
  )
  
  val samplePenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(BigDecimal(200)),
      penalisedPrincipalTotal = Some(BigDecimal(2000)),
      LPPPostedTotal = Some(BigDecimal(165.25)),
      LPPEstimatedTotal = Some(BigDecimal(15.26)),
      LPIPostedTotal = Some(BigDecimal(1968.2)),
      LPIEstimatedTotal = Some(BigDecimal(7))
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some("FAP"),
          communicationsDate = LocalDate.parse("2069-10-30"),
          lateSubmissions = Some(Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
              returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
              taxReturnStatus = TaxReturnStatusEnum.Fulfilled
            )
          )),
          appealInformation = Some(Seq(
            AppealInformationType(
              appealStatus = Some(AppealStatusEnum.Unappealable),
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(10000.00),
        LPP1HRCalculationAmount = Some(10000.00),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(2.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel =  Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  val penaltyDetailsWithDay15Charge: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = None,
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(20000.00),
        LPP1HRCalculationAmount = None,
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = None,
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    )))

  val penaltyDetailsWithDay15ChargePosted: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = None,
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(20000.00),
        LPP1HRCalculationAmount = None,
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = None,
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    )))

  val penaltyDetailsWithDueDateMoreThan30days: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(10000.00),
        LPP1HRCalculationAmount = Some(10000.00),
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(2.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    )))

  val penaltyDetailsWithDueDateMoreThan30daysAccruing: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(10000.00),
        LPP1HRCalculationAmount = Some(10000.00),
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(2.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    )))

  val penaltyDetailsWithAdditionalPenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "54312345678901",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(113.45),
        penaltyAmountOutstanding = Some(10.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = Some(123.00),
        LPP2Percentage = Some(2.00),
        LPP1LRPercentage = Some(1.00),
        LPP1HRPercentage = Some(1.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.now().minusDays(40),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  val penaltyDetailsWithAdditionalDuePenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "65431234567890",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(113.45),
        penaltyAmountOutstanding = Some(10.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = Some(123.00),
        LPP2Percentage = Some(2.00),
        LPP1LRPercentage = Some(1.00),
        LPP1HRPercentage = Some(1.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.now().minusDays(40),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )
  
  "GET /calculation when it is not an additional penalty" should {
      "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in {
        returnPenaltyDetailsStub(penaltyDetailsWithDay15ChargePosted)
        val request = controller.onPageLoad(
          "12345678901234", "LPP1")(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
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
        returnPenaltyDetailsStub(penaltyDetailsWithDueDateMoreThan30days)
        val request = controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
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
        returnPenaltyDetailsStub(samplePenaltyDetails)

        val request = controller.onPageLoad("1234567890", "LPP1")(fakeRequest)
        status(request) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return 303 (SEE_OTHER) when the user is not authorised" in {
        AuthStub.unauthorised()

        val request = controller.onPageLoad("12345", "LPP1")(fakeRequest)
        status(request) shouldBe Status.SEE_OTHER
      }
  }

  "GET /calculation when it is not an additional penalty and is estimated" should {
    "return 200 (OK) and render the view correctly when the use has specified a valid penalty ID" in {
      returnPenaltyDetailsStub(penaltyDetailsWithDay15Charge)
      val request = controller.onPageLoad(
        "12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£277.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£123.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "the user has specified a valid penalty ID (parses decimals correctly)" in {
      returnPenaltyDetailsStub(penaltyDetailsWithDay15Charge)
      val request = controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
      parsedBody.select("#how-penalty-is-applied").text() shouldBe "This penalty applies if VAT has not been paid for 15 days."
      parsedBody.select("#15-day-calculation").text() shouldBe "The calculation we use is: 2% of £20,000.00 (the unpaid VAT 15 days after the due date)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£277.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£123.00"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      returnPenaltyDetailsStub(samplePenaltyDetails)

      val request = controller.onPageLoad("1234567890", "LPP1")(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()

      val request = controller.onPageLoad("12345", "LPP1")(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }

  "GET /calculation when it is an additional penalty" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in {
      returnPenaltyDetailsStub(penaltyDetailsWithAdditionalPenalty)
      val request = controller.onPageLoad("54312345678901", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(1).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The total increases daily based on the amount of unpaid VAT for the period."
      parsedBody.select("#main-content p").get(3).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
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
      returnPenaltyDetailsStub(penaltyDetailsWithAdditionalDuePenalty)
      val request = controller.onPageLoad("65431234567890", "LPP2")(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(1).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The total increases daily until you pay your VAT or set up a payment plan."
      parsedBody.select("#main-content p").get(3).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£113.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£10.00"
      parsedBody.select("h2").get(0).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(4).text() shouldBe "Penalties and interest will show as estimates until you pay the charge they relate to."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due (user is agent)" in {
      AuthStub.agentAuthorised()
      returnPenaltyDetailsStubAgent(penaltyDetailsWithAdditionalDuePenalty)
      val request = controller.onPageLoad("65431234567890", "LPP2")(fakeAgentRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(1).text() shouldBe "This penalty applies from day 31, if any VAT remains unpaid."
      parsedBody.select("#main-content p").get(2).text() shouldBe "The total increases daily until your client pays their VAT or sets up a payment plan."
      parsedBody.select("#main-content p").get(3).text() shouldBe "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "£113.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£10.00"
      parsedBody.select("h2").get(0).text() shouldBe "Estimates"
      parsedBody.select("#main-content p").get(4).text() shouldBe "Penalties and interest will show as estimates until your client pays the charge they relate to."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      returnPenaltyDetailsStub(samplePenaltyDetails)

      val request = controller.onPageLoad("123456800", "LPP2")(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = controller.onPageLoad("123456800", "LPP2")(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }
}
