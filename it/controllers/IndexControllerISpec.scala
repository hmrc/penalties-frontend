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
import models.{GetPenaltyDetails, Totalisations, appealInfo, lpp}
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, MainTransactionEnum}
import models.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LateSubmission, LateSubmissionPenalty, TaxReturnStatusEnum}
import org.jsoup.Jsoup
import play.api.http.{HeaderNames, Status}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

import java.time.{LocalDate, LocalDateTime}

class IndexControllerISpec extends IntegrationSpecCommonBase {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleDate1V2: LocalDate = sampleDate1.toLocalDate
  val sampleDate2: LocalDateTime = LocalDateTime.of(2021, 2, 1, 1, 1, 1)
  val sampleDate2V2: LocalDate = sampleDate2.toLocalDate
  val sampleDate3: LocalDateTime = LocalDateTime.of(2021, 3, 1, 1, 1, 1)
  val sampleDate3V2: LocalDate = sampleDate3.toLocalDate
  val sampleDate4: LocalDateTime = LocalDateTime.of(2021, 4, 1, 1, 1, 1)
  val sampleDate4V2: LocalDate = sampleDate4.toLocalDate
  val controller: IndexController = injector.instanceOf[IndexController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    authToken -> "12345"
  )
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    authToken -> "12345"
  )

  val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1,
        regimeThreshold = 4,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 0
      ),
      details = Seq(
        LSPDetails(
          penaltyNumber = "1234567890",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = None,
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      )
    )
    ),
    latePaymentPenalty = None
  )

  val getPenaltyDetailsPayloadWithRemovedPoints = GetPenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1,
        regimeThreshold = 4,
        inactivePenaltyPoints = 1,
        penaltyChargeAmount = 0
      ),
      details = Seq(
        LSPDetails(
          penaltyNumber = "1234567890",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          FAPIndicator = None,
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = Some("FAP"),
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2.plusMonths(1)),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        ),
        LSPDetails(
          penaltyNumber = "1234567891",
          penaltyOrder = "02",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          FAPIndicator = Some("X"),
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = Some("FAP"),
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      )
    )),
    latePaymentPenalty = None
  )

  val getPenaltiesDataPayloadWith2PointsandOneRemovedPoint: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 2,
        regimeThreshold = 4,
        inactivePenaltyPoints = 1,
        penaltyChargeAmount = 0
      ),
      details = Seq(
        LSPDetails(
          penaltyNumber = "1234567893",
          penaltyOrder = "03",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = None,
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        ),
        LSPDetails(
          penaltyNumber = "1234567893",
          penaltyOrder = "02",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = None,
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        ),
        LSPDetails(
          penaltyNumber = "1234567891",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          FAPIndicator = Some("X"),
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = Some("FAP"),
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      ))
    ),
    latePaymentPenalty = None
  )

  val paidLatePaymentPenaltyV2: LatePaymentPenalty = lpp.LatePaymentPenalty(
    details = Seq(
      LPPDetails(principalChargeReference = "123456789",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyChargeCreationDate = sampleDate1V2,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = Some(BigDecimal(400)),
      penaltyAmountOutstanding = Some(BigDecimal(0)),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("30"),
      LPP2Days = None,
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP1LRPercentage = Some(BigDecimal(0.02)),
      LPP1HRPercentage = Some(BigDecimal(0.02)),
      LPP2Percentage = None,
      communicationsDate = sampleDate1V2,
      penaltyChargeDueDate = sampleDate1V2,
      appealInformation = None,
      principalChargeBillingFrom = sampleDate1V2,
      principalChargeBillingTo = sampleDate1V2.plusMonths(1),
      principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
      penaltyChargeReference = Some("123456789"),
      principalChargeLatestClearing = Some(sampleDate1V2.plusMonths(2).plusDays(7)),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99)
        )
      )))

  val latePaymentPenaltyWithAdditionalPenaltyV2: LatePaymentPenalty = lpp.LatePaymentPenalty(
    details = Seq(
      LPPDetails(
        principalChargeReference = "123456789",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyChargeCreationDate = sampleDate1V2,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(BigDecimal(123.45)),
        penaltyAmountOutstanding = Some(BigDecimal(0)),
        LPP1LRDays = None,
        LPP1HRDays = None,
        LPP2Days = Some("30"),
        LPP1LRCalculationAmount = None,
        LPP1HRCalculationAmount = None,
        LPP1LRPercentage =None,
        LPP1HRPercentage = None,
        LPP2Percentage =  Some(BigDecimal(0.02)),
        communicationsDate = sampleDate1V2,
        penaltyChargeDueDate = sampleDate1V2,
        appealInformation = None,
        principalChargeBillingFrom = sampleDate1V2,
        principalChargeBillingTo = sampleDate1V2.plusMonths(1),
        principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
        penaltyChargeReference = Some("123456789"),
        principalChargeLatestClearing = Some(sampleDate1V2.plusMonths(2).plusDays(7)),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ),
      LPPDetails(
        principalChargeReference = "123456789",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyChargeCreationDate = sampleDate1V2,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(BigDecimal(200)),
        penaltyAmountOutstanding = Some(BigDecimal(200)),
        LPP1LRDays = Some("15"),
        LPP1HRDays = None,
        LPP2Days = None,
        LPP1LRCalculationAmount = None,
        LPP1HRCalculationAmount = None,
        LPP1LRPercentage = Some(BigDecimal(0.02)),
        LPP1HRPercentage = None,
        LPP2Percentage = None,
        communicationsDate = sampleDate1V2,
        penaltyChargeDueDate = sampleDate1V2,
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(appealInfo.AppealStatusEnum.Unappealable),
          appealLevel = None
        ))),
        principalChargeBillingFrom = sampleDate1V2,
        principalChargeBillingTo = sampleDate1V2.plusMonths(1),
        principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
        penaltyChargeReference = Some("123456789"),
        principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      )
    )
  )

  val latePaymentPenaltyVATUnpaidV2: LatePaymentPenalty = lpp.LatePaymentPenalty(
    details = Seq(
      LPPDetails(principalChargeReference = "123456789",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyChargeCreationDate = sampleDate1V2,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = Some(BigDecimal(200)),
      penaltyAmountOutstanding = Some(BigDecimal(200)),
      LPP1LRDays = Some("15"),
      LPP1HRDays = None,
      LPP2Days = None,
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP1LRPercentage = Some(BigDecimal(0.02)),
      LPP1HRPercentage = None,
      LPP2Percentage = None,
      communicationsDate = sampleDate1V2,
      penaltyChargeDueDate = sampleDate1V2,
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(appealInfo.AppealStatusEnum.Unappealable),
        appealLevel = None
      ))),
      principalChargeBillingFrom = sampleDate1V2,
      principalChargeBillingTo = sampleDate1V2.plusMonths(1),
      principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
      penaltyChargeReference = Some("123456789"),
      principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )))
  )

  val latePaymentPenaltyWithAppealV2 = Some(
    lpp.LatePaymentPenalty(
    details = Seq(LPPDetails(principalChargeReference = "123456789",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyChargeCreationDate = sampleDate1V2,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = Some(BigDecimal(400)),
      penaltyAmountOutstanding = Some(BigDecimal(0)),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("30"),
      LPP2Days = None,
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP1LRPercentage = Some(BigDecimal(0.02)),
      LPP1HRPercentage = Some(BigDecimal(0.02)),
      LPP2Percentage = None,
      communicationsDate = sampleDate1V2,
      penaltyChargeDueDate = sampleDate1V2,
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(appealInfo.AppealStatusEnum.Under_Appeal),
        appealLevel = Some(AppealLevelEnum.HMRC)))),
      principalChargeBillingFrom = sampleDate1V2,
      principalChargeBillingTo = sampleDate1V2.plusMonths(1),
      principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
      penaltyChargeReference = Some("123456789"),
      principalChargeLatestClearing = Some(sampleDate1V2.plusMonths(2).plusDays(7)),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99)
      ))))
  )

  val getPenaltiesDataPayloadWithPaidLPP: GetPenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(paidLatePaymentPenaltyV2),
  )

  val getPenaltiesDataPayloadWithLPPAndAdditionalPenalty: GetPenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(latePaymentPenaltyWithAdditionalPenaltyV2)
  )

  val getPenaltiesDataPayloadWithLPPVATUnpaid: GetPenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(latePaymentPenaltyVATUnpaidV2)
  )

  val getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue: GetPenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(400),
      penalisedPrincipalTotal = Some(121.40),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(46.55),
      LPIPostedTotal = Some(40.55),
      LPIEstimatedTotal = Some(6)
    )),

    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 2,
        regimeThreshold = 4,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 0
      ),
      details = Seq(
        LSPDetails(
          penaltyNumber = "1234567890",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = None,
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = Some(BigDecimal(100)),
          chargeDueDate = None
        ),
        LSPDetails(
          penaltyNumber = "1234567890",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = sampleDate1V2,
          penaltyExpiryDate = sampleDate1V2.plusMonths(1).plusYears(2),
          expiryReason = None,
          communicationsDate = sampleDate1V2,
          lateSubmissions = Some(Seq(LateSubmission(
            taxPeriodStartDate = Some(sampleDate1V2),
            taxPeriodEndDate = Some(sampleDate1V2),
            taxPeriodDueDate = Some(sampleDate1V2),
            returnReceiptDate = Some(sampleDate1V2),
            taxReturnStatus = TaxReturnStatusEnum.Fulfilled))),
          appealInformation = Some(Seq(AppealInformationType(Some(appealInfo.AppealStatusEnum.Unappealable), None))),
          chargeAmount = None,
          chargeOutstandingAmount = Some(BigDecimal(100)),
          chargeDueDate = None
        )
      )
    )
    ),
     latePaymentPenalty = Some(latePaymentPenaltyVATUnpaidV2)
  )

  val getPenaltyPayloadWithLPPAppeal: GetPenaltyDetails = getPenaltiesDataPayloadWithPaidLPP.copy(
    latePaymentPenalty = latePaymentPenaltyWithAppealV2
  )

  val unpaidLatePaymentPenaltyV2: LatePaymentPenalty = lpp.LatePaymentPenalty(
    details = Seq(LPPDetails(principalChargeReference = "123456789",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyChargeCreationDate = sampleDate1V2,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = Some(BigDecimal(0)),
      penaltyAmountOutstanding = Some(BigDecimal(400)),
      LPP1LRDays = Some("15"),
      LPP1HRDays = None,
      LPP2Days = None,
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP1LRPercentage = Some(BigDecimal(0.02)),
      LPP1HRPercentage = None,
      LPP2Percentage = None,
      communicationsDate = sampleDate1V2,
      penaltyChargeDueDate = sampleDate1V2,
      appealInformation = None,
      principalChargeBillingFrom = sampleDate1V2,
      principalChargeBillingTo = sampleDate1V2.plusMonths(1),
      principalChargeDueDate = sampleDate1V2.plusMonths(2).plusDays(6),
      penaltyChargeReference = Some("123456789"),
      principalChargeLatestClearing = None,
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99)
      )))
  )

  val getPenaltiesDetailsPayloadWithMultiplePenaltyPeriodInLSP: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 1,
          inactivePenaltyPoints = 0,
          regimeThreshold = 4,
          penaltyChargeAmount = 200
        ),
        details = Seq(
          LSPDetails(
            penaltyNumber = "12345678901235",
            penaltyOrder = "01",
            penaltyCategory = LSPPenaltyCategoryEnum.Point,
            penaltyStatus = LSPPenaltyStatusEnum.Active,
            FAPIndicator = None,
            penaltyCreationDate = LocalDate.parse("2069-10-30"),
            penaltyExpiryDate = LocalDate.parse("2069-10-30"),
            expiryReason = None,
            communicationsDate = LocalDate.parse("2069-10-30"),
            lateSubmissions = Some(Seq(
              LateSubmission(
                taxPeriodStartDate = Some(sampleDate1V2),
                taxPeriodEndDate = Some(sampleDate1V2.plusDays(14)),
                taxPeriodDueDate = Some(sampleDate1V2.plusMonths(4).plusDays(7)),
                returnReceiptDate = Some(sampleDate1V2.plusMonths(4).plusDays(12)),
                taxReturnStatus = TaxReturnStatusEnum.Fulfilled
              ),
              LateSubmission(
                taxPeriodStartDate = Some(sampleDate1V2.plusDays(16)),
                taxPeriodEndDate = Some(sampleDate1V2.plusDays(31)),
                taxPeriodDueDate = Some(sampleDate1V2.plusMonths(4).plusDays(23)),
                returnReceiptDate = Some(sampleDate1V2.plusMonths(4).plusDays(25)),
                taxReturnStatus = TaxReturnStatusEnum.Fulfilled
              )
            )),
            appealInformation = None,
            chargeAmount = None,
            chargeOutstandingAmount = None,
            chargeDueDate = None
          )
        )
      )
    ),
    latePaymentPenalty = None
  )

  "GET /" should {
    "return 200 (OK) when the user is authorised" in {
      getPenaltyDetailsStub
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
    }

    "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltyDetailsPayloadWithAddedPoint)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 1 penalty point. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted a VAT Return late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent you a letter explaining why"
      parsedBody.select("header h3").text shouldBe "Penalty point 1: adjustment point"
      parsedBody.select("main strong").text shouldBe "active"
      val summaryCardBody = parsedBody.select(".app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Added on"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
      summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
      summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
      //TODO: Change to external guidance when available
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
      parsedBody.select(".app-summary-card footer div").text shouldBe "You cannot appeal this point"
    }

    "return 200 (OK) and render the view when there are removed points that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltyDetailsPayloadWithRemovedPoints)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 1 penalty point. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 2 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("header h3").get(0).text shouldBe "Penalty point"
      parsedBody.select("main strong").get(0).text shouldBe "removed"
      val summaryCardBody = parsedBody.select(".app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT period"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
      summaryCardBody.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
      summaryCardBody.select("p.govuk-body a").get(0).text() shouldBe "Find out more about adjustment points (opens in a new tab)"
      //TODO: Change to external guidance when available
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
      parsedBody.select(".app-summary-card footer div").text shouldBe ""
      parsedBody.select(".app-summary-card footer a").text shouldBe ""
    }

    "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
      returnPenaltyDetailsStub(getPenaltiesDataPayloadWith2PointsandOneRemovedPoint)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 2 penalty points. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 3 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("main section h3").get(0).text shouldBe "Penalty point 2"
      parsedBody.select("main section h3").get(1).text shouldBe "Penalty point 1"
      parsedBody.select("main section h3").get(2).text shouldBe "Penalty point"
      parsedBody.select("main section strong").get(2).text shouldBe "removed"
    }

    "return 200 (OK) and render the view when there are LPPs paid that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltiesDataPayloadWithPaidLPP)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h3").get(0).text shouldBe "??400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      //      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "Charge due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "Date paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      parsedBody.select("#late-payment-penalties footer li").get(1).text() shouldBe "Appeal this penalty"
    }

    //TODO Changes are being made to overview section based on API restrictions
    "return 200 (OK) and render the view when there is outstanding payments" ignore {
//      returnLSPDataStub(etmpPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#what-is-owed > p").first.text shouldBe "You owe:"
      parsedBody.select("#what-is-owed > ul > li").first().text shouldBe "??121.40 in late VAT"
      parsedBody.select("#what-is-owed > ul > li").get(1).text shouldBe "??93.10 in estimated VAT interest"
      parsedBody.select("#what-is-owed > ul > li").get(2).text shouldBe "??200 in late payment penalties"
      parsedBody.select("#what-is-owed > ul > li").get(3).text shouldBe "??99.55 in estimated interest on penalties"
      parsedBody.select("#what-is-owed > ul > li").get(4).text shouldBe "??400 fixed penalties for late submission"
      parsedBody.select("#what-is-owed > ul > li").get(5).text shouldBe "other penalties not related to late submission or late payment"
      parsedBody.select("#main-content h2:nth-child(3)").text shouldBe "Penalty and appeal details"
      parsedBody.select("#what-is-owed > a").text shouldBe "Check amounts and pay"
      parsedBody.select("#what-is-owed > h2").text shouldBe "If you cannot pay today"
    }

    //TODO Changes are being made to overview section based on API restrictions
    "return 200 (OK) and render the view when there is outstanding estimate payments" ignore {
//      returnLSPDataStub(etmpPayloadWithEstimates)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#what-is-owed > ul > li").first().text shouldBe "??232.12 in estimated late payment penalties"
      parsedBody.select("#what-is-owed > ul > li").get(1).text shouldBe "??53 in estimated interest on penalties"
      parsedBody.select("#main-content h2:nth-child(3)").text shouldBe "Penalty and appeal details"
      parsedBody.select("#what-is-owed > a").text shouldBe "Check amounts and pay"
      parsedBody.select("#what-is-owed > h2").text shouldBe "If you cannot pay today"
    }

    "return 200 (OK) and render the view when there are LPPs and additional penalties paid that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltiesDataPayloadWithLPPAndAdditionalPenalty)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h3").get(0).text shouldBe "??123.45 penalty"
      parsedBody.select("#late-payment-penalties section header strong").get(0).text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body").first()
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "Second penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      //      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "Charge due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "Date paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      parsedBody.select("#late-payment-penalties footer li").text().contains("Appeal this penalty") shouldBe true
    }

    "return 200 (OK) and render the view when there are LPPs with VAT partially unpaid that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltiesDataPayloadWithLPPVATUnpaid)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "??400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "??200 due"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      //      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "Charge due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "Date paid"
      summaryCardBody.select("dd").get(3).text shouldBe "Payment not yet received"
      parsedBody.select("#late-payment-penalties footer li").get(1).text() shouldBe "Check if you can appeal"
    }

    "return 200 (OK) and render the view when there are LPPs with VAT unpaid that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(latePaymentPenalty = Some(unpaidLatePaymentPenaltyV2)))
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "??400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "due"
    }

    "return 200 (OK) and render the view when there are appealed LPPs that are retrieved from the backend" in {
      returnPenaltyDetailsStub(getPenaltyPayloadWithLPPAppeal)
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "??400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Penalty type"
      summaryCardBody.select("dd").get(0).text shouldBe "First penalty for late payment"
      summaryCardBody.select("dt").get(1).text shouldBe "Overdue charge"
      //      summaryCardBody.select("dd").get(1).text shouldBe "VAT for period 1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(2).text shouldBe "Charge due"
      summaryCardBody.select("dd").get(2).text shouldBe "7 March 2021"
      summaryCardBody.select("dt").get(3).text shouldBe "Date paid"
      summaryCardBody.select("dd").get(3).text shouldBe "8 March 2021"
      summaryCardBody.select("dt").get(4).text shouldBe "Appeal status"
      summaryCardBody.select("dd").get(4).text shouldBe "Under review by HMRC"
    }

    "return 200 (OK) and add the latest lsp creation date and penalty threshold to the session" in {
      returnPenaltyDetailsStub(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue.copy(latePaymentPenalty = Some(paidLatePaymentPenaltyV2)))
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      await(request).session(fakeRequest).get(SessionKeys.latestLSPCreationDate).isDefined shouldBe true
      await(request).session(fakeRequest).get(SessionKeys.latestLSPCreationDate).get shouldBe sampleDate1V2.toString
      await(request).session(fakeRequest).get(SessionKeys.pointsThreshold).isDefined shouldBe true
      await(request).session(fakeRequest).get(SessionKeys.pointsThreshold).get shouldBe "4"
    }

    "agent view" must {
      "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
        AuthStub.agentAuthorised()
        returnPenaltyDetailsStubAgent(getPenaltyDetailsPayloadWithAddedPoint)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 1 penalty point. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted a VAT Return late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent them a letter explaining why"
        parsedBody.select("header h3").text shouldBe "Penalty point 1: adjustment point"
        parsedBody.select("main strong").text shouldBe "active"
        val summaryCardBody = parsedBody.select(".app-summary-card__body")
        summaryCardBody.select("dt").get(0).text shouldBe "Added on"
        summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
        summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
        summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
        summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
        //TODO: Change to external guidance when available
        summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
        parsedBody.select(".app-summary-card footer div").text shouldBe "You cannot appeal this point"
      }

      "return 200 (OK) and render the view when there are removed points that are retrieved from the backend" in {
        AuthStub.agentAuthorised()
        returnPenaltyDetailsStubAgent(getPenaltyDetailsPayloadWithRemovedPoints)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 1 penalty point. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 2 VAT Returns late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent them a letter explaining why"
        parsedBody.select("header h3").get(0).text shouldBe "Penalty point"
        parsedBody.select("main strong").get(0).text shouldBe "removed"
        val summaryCardBody = parsedBody.select(".app-summary-card__body")
        summaryCardBody.select("dt").get(0).text shouldBe "VAT period"
        summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 1 February 2021"
        summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
        summaryCardBody.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
        summaryCardBody.select("p.govuk-body a").get(0).text() shouldBe "Find out more about adjustment points (opens in a new tab)"
        //TODO: Change to external guidance when available
        summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
        parsedBody.select(".app-summary-card footer div").text shouldBe ""
        parsedBody.select(".app-summary-card footer a").text shouldBe ""
      }

      "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
        AuthStub.agentAuthorised()
        returnPenaltyDetailsStubAgent(getPenaltiesDataPayloadWith2PointsandOneRemovedPoint)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 2 penalty points. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 3 VAT Returns late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent them a letter explaining why"
        parsedBody.select("main section h3").get(0).text shouldBe "Penalty point 2"
        parsedBody.select("main section h3").get(1).text shouldBe "Penalty point 1"
        parsedBody.select("main section h3").get(2).text shouldBe "Penalty point"
        parsedBody.select("main section strong").get(2).text shouldBe "removed"
      }

      "return 200 (OK) and render the view when there is outstanding payments for the client" in {
        AuthStub.agentAuthorised()
        returnPenaltyDetailsStubAgent(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue
          .copy(latePaymentPenalty = Some(paidLatePaymentPenaltyV2)))
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#what-is-owed > p").first.text shouldBe "Your client owes:"
        parsedBody.select("#what-is-owed > ul > li").first().text shouldBe "??121.40 in late VAT"
        //parsedBody.select("#what-is-owed > ul > li").get(1).text shouldBe "??93.10 in estimated VAT interest"
        parsedBody.select("#what-is-owed > ul > li").get(3).text shouldBe "??46.55 in estimated interest on penalties"
        parsedBody.select("#what-is-owed > ul > li").get(4).text shouldBe "??400 fixed penalties for late submission"
        //parsedBody.select("#what-is-owed > ul > li").get(4).text shouldBe "other penalties not related to late submission or late payment"
        parsedBody.select("#main-content h2:nth-child(3)").text shouldBe "Penalty and appeal details"
        parsedBody.select("#what-is-owed > a").text shouldBe "Check amounts"
        parsedBody.select("#main-content .govuk-details__summary-text").text shouldBe "Payment help"
      }

      "return 200 (OK) and add the latest lsp creation date and the penalty threshold to the session" in {
        AuthStub.agentAuthorised()
        returnPenaltyDetailsStubAgent(getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue
          .copy(latePaymentPenalty = Some(paidLatePaymentPenaltyV2)))
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        await(request).session(fakeAgentRequest).get(SessionKeys.latestLSPCreationDate).isDefined shouldBe true
        await(request).session(fakeAgentRequest).get(SessionKeys.latestLSPCreationDate).get shouldBe sampleDate1V2.toString
        await(request).session(fakeAgentRequest).get(SessionKeys.pointsThreshold).isDefined shouldBe true
        await(request).session(fakeAgentRequest).get(SessionKeys.pointsThreshold).get shouldBe "4"
      }
    }

    "return 200 (OK) and render the view when there are LSPs with multiple penalty period" in {
      returnPenaltyDetailsStub(getPenaltiesDetailsPayloadWithMultiplePenaltyPeriodInLSP)
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

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = controller.onPageLoad()(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-penalty" should {
    "redirect the user to the appeals service when the penalty is not a LPP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = false,
        isObligation = false,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=false&isAdditional=false"
    }

    "redirect the user to the appeals service when the penalty is a LPP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = false,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=false"
    }

    "redirect the user to the appeals service when the penalty is a LPP - Additional" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = false,
        isAdditional = true)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=true"
    }

    "redirect the user to the obligations appeals service when the penalty is not a LPP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = false,
        isObligation = true,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234&isLPP=false&isAdditional=false"
    }

    "redirect the user to the obligations appeals service when the penalty is a LPP" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = true,
        isAdditional = false)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234&isLPP=true&isAdditional=false"
    }

    "redirect the user to the obligations appeals service when the penalty is a LPP - Additional" in {
      val request = controller.redirectToAppeals(
        penaltyId = "1234",
        isLPP = true,
        isObligation = true,
        isAdditional = true)(FakeRequest("GET", "/").withSession(
        authToken -> "1234"
      ))
      status(request) shouldBe Status.SEE_OTHER
      headers(request)(implicitly)(HeaderNames.LOCATION) shouldBe
        "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234&isLPP=true&isAdditional=true"
    }
  }
}
