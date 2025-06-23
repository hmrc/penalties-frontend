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

package services

import base.SpecBase
import connectors.PenaltiesConnector
import connectors.httpParsers.{BadRequest, InvalidJson, UnexpectedFailure}
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, MainTransactionEnum}
import models.lsp._
import models.{GetPenaltyDetails, Id, IdType, Regime, Totalisations}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.http.Status._
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import java.time.LocalDate
import scala.concurrent.Future

class PenaltiesServiceSpec extends SpecBase {

  val penaltyDetailsWithNoVATDue: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(0),
      penalisedPrincipalTotal = Some(0),
      LPPPostedTotal = Some(0),
      LPPEstimatedTotal = Some(0),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val penaltyDetailsWithVATOnly: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(0),
      penalisedPrincipalTotal = Some(223.45),
      LPPPostedTotal = Some(0),
      LPPEstimatedTotal = Some(0),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val penaltyDetailsWithEstimatedLPPs: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(0),
      penalisedPrincipalTotal = Some(0),
      LPPPostedTotal = Some(0),
      LPPEstimatedTotal = Some(50),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val penaltyDetailsWithCrystallisedLPPs: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(0),
      penalisedPrincipalTotal = Some(0),
      LPPPostedTotal = Some(50),
      LPPEstimatedTotal = Some(0),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  class Setup {
    val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)
    reset(mockPenaltiesConnector)
  }

  "getPenaltyData" when  {
    s"$OK (Ok) is returned from the parser " should {
      "return a Right with the correct model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(Regime(any[String]()), IdType(any[String]()), Id(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Right(penaltyDetailsWithNoVATDue)))

        val result = await(service.getPenaltyData(Regime.VATC, IdType.VRN, Id("1234567890"))(vatTraderUser, hc))
        result.isRight shouldBe true
        result shouldBe Right(penaltyDetailsWithNoVATDue)
      }
    }

    s"$NO_CONTENT (No content) is returned from the parser" should {
      "return an empty Right GetPenaltyDetails model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(Regime(any[String]()), IdType(any[String]()), Id(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Right(GetPenaltyDetails(None, None, None, None))))

        val result = await(service.getPenaltyData(Regime.VATC, IdType.VRN, Id("1234567890"))(vatTraderUser, hc))
        result.isRight shouldBe true
        result shouldBe Right(GetPenaltyDetails(None, None, None, None))
      }
    }

    s"$BAD_REQUEST (Bad request) is returned from the parser because of invalid json" should {
      "return a Left with status 400" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(Regime(any[String]()), IdType(any[String]()), Id(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Left(InvalidJson)))

        val result = await(service.getPenaltyData(Regime.VATC, IdType.VRN, Id("1234567890"))(vatTraderUser, hc))
        result.isLeft shouldBe true
        result shouldBe Left(InvalidJson)
        for(left <- result.left) yield left.status shouldBe 400
        for(left <- result.left) yield left.body shouldBe "Invalid JSON received"
      }
    }

    s"$BAD_REQUEST (Bad request) is returned from the parser" should {
      "return a Left with status 400" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(Regime(any[String]()), IdType(any[String]()), Id(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Left(BadRequest)))

        val result = await(service.getPenaltyData(Regime.VATC, IdType.VRN, Id("1234567890"))(vatTraderUser, hc))
        result.isLeft shouldBe true
        result shouldBe Left(BadRequest)
        for(left <- result.left) yield left.status shouldBe 400
        for(left <- result.left) yield left.body shouldBe "Incorrect JSON body sent"
      }
    }

    s"an unexpected error is returned from the parser" should {
      "return a Left with the status and message" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(Regime(any[String]()), IdType(any[String]()), Id(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))

        val result = await(service.getPenaltyData(Regime.VATC, IdType.VRN, Id("1234567890"))(vatTraderUser, hc))
        result.isLeft shouldBe true
        result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
        for(left <- result.left) yield left.status shouldBe 500
        for(left <- result.left) yield left.body shouldBe "Unexpected response, status 500 returned"
      }
    }
  }

  "findInterestOnAccount" should {
    "return 0 when the payload does not have any totalisations field" in new Setup {
      val result: BigDecimal = service.findInterestOnAccount(None)
      result shouldBe 0.00
    }

    "return 0 when the payload contains totalisations but has no posted and accruing interest" in new Setup {
      val totalisations = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findInterestOnAccount(Some(totalisations))
      result shouldBe 0.00
    }

    "return the total when the payload contains totalisations and has posted and accruing interest" in new Setup {
      val totalisations = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = Some(100),
        totalAccountAccruingInterest = Some(10),
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findInterestOnAccount(Some(totalisations))
      result shouldBe 110.00
    }

    "return the total when the payload contains totalisations and has posted but not accruing interest" in new Setup {
      val totalisations = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = Some(100),
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findInterestOnAccount(Some(totalisations))
      result shouldBe 100.00
    }

    "return the total when the payload contains totalisations and has accruing but not posted interest" in new Setup {
      val totalisations = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = Some(10),
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findInterestOnAccount(Some(totalisations))
      result shouldBe 10.00
    }
  }

  "isAnyLSPUnpaidAndSubmissionIsDue" should {
    "return false" when {
      "there is no LSPs unpaid" in new Setup {
        val lspDetailsUnpaid: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            )
          ),
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(0),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaidAndSubmissionIsDue(Seq(lspDetailsUnpaid))
        result shouldBe false
      }

      "there is no LSPs where the VAT has not been submitted" in new Setup {
        val lspDetailsUnsubmitted: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            )
          ),
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(100),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaidAndSubmissionIsDue(Seq(lspDetailsUnsubmitted))
        result shouldBe false
      }

      "there is LSPs that meet the condition but have been appealed successfully" in new Setup {
        val lspDetailsAppealed: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            )
          ),
          appealInformation = Some(
            Seq(
              AppealInformationType(
                appealStatus = Some(AppealStatusEnum.Upheld), appealLevel = Some(AppealLevelEnum.HMRC)
              )
            )
          ),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(0),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaidAndSubmissionIsDue(Seq(lspDetailsAppealed))
        result shouldBe false
      }
    }

    "return true" when {
      "there is an LSP that is unpaid and the submission is due and has not been appealed successfully" in new Setup {
        val lspDetails: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = None,
                taxReturnStatus = Some(TaxReturnStatusEnum.Open)
              )
            )
          ),
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(10),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaidAndSubmissionIsDue(Seq(lspDetails))
        result shouldBe true
      }
    }
  }

  "isAnyLSPUnpaid" should {
    "return false" when {
      "the LSP is paid" in new Setup {
        val lspDetailsPaid: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = None,
                taxReturnStatus = Some(TaxReturnStatusEnum.Open)
              )
            )
          ),
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(0),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaid(Seq(lspDetailsPaid))
        result shouldBe false
      }

      //May never happen in reality as user would appeal obligation
      "the LSP is unpaid but has been appealed successfully" in new Setup {
        val lspDetailsAppealed: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = None,
                taxReturnStatus = Some(TaxReturnStatusEnum.Open)
              )
            )
          ),
          appealInformation = Some(
            Seq(
              AppealInformationType(
                appealStatus = Some(AppealStatusEnum.Upheld), appealLevel = Some(AppealLevelEnum.HMRC)
              )
            )
          ),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(10),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaid(Seq(lspDetailsAppealed))
        result shouldBe false
      }
    }

    "return true" when {
      "the LSP is unpaid and not appealed" in new Setup {
        val lspDetailsAppealed: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                returnReceiptDate = None,
                taxReturnStatus = Some(TaxReturnStatusEnum.Open)
              )
            )
          ),
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(10),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
        val result = service.isAnyLSPUnpaid(Seq(lspDetailsAppealed))
        result shouldBe true
      }
    }
  }

  "findUnpaidVATCharges" should {
    "find the totalAccountOverdue in the totalisation field and return the value if present" in new Setup {
      val totalisationFieldWithOverdueVAT: Totalisations = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findUnpaidVATCharges(Some(totalisationFieldWithOverdueVAT))
      result shouldBe 123.45
    }

    "return 0 if no totalAccountOverdue field present" in new Setup {
      val totalisationFieldWithOverdueVAT: Totalisations = Totalisations(
        totalAccountOverdue = None,
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result: BigDecimal = service.findUnpaidVATCharges(Some(totalisationFieldWithOverdueVAT))
      result shouldBe 0
    }
  }

  "findNumberOfLatePaymentPenalties" should {
    val sampleLPP: LPPDetails = LPPDetails(principalChargeReference = "123456789",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyChargeCreationDate = Some(LocalDate.of(2022, 1, 1)),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = Some(BigDecimal(400)),
      penaltyAmountOutstanding = Some(BigDecimal(10)),
      penaltyAmountPosted = 410,
      penaltyAmountAccruing = 0,
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("30"),
      LPP2Days = None,
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP1LRPercentage = Some(BigDecimal(0.02)),
      LPP1HRPercentage = Some(BigDecimal(0.02)),
      LPP2Percentage = None,
      communicationsDate = Some(LocalDate.of(2022, 1, 1)),
      penaltyChargeDueDate = Some(LocalDate.of(2022, 1, 1)),
      appealInformation = None,
      principalChargeBillingFrom = LocalDate.of(2022, 1, 1),
      principalChargeBillingTo = LocalDate.of(2022, 1, 1).plusMonths(1),
      principalChargeDueDate = LocalDate.of(2022, 1, 1).plusMonths(2).plusDays(6),
      penaltyChargeReference = Some("123456789"),
      principalChargeLatestClearing = Some(LocalDate.of(2022, 1, 1).plusMonths(2).plusDays(7)),
      vatOutstandingAmount = Some(BigDecimal(123.45)),
        LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None)
    )
    "return 0" when {
      "all the penalties have been appealed successfully" in new Setup {
        val allLPPsAppealedSuccessfully: LatePaymentPenalty = LatePaymentPenalty(
          Seq(sampleLPP.copy(appealInformation = Some(Seq(AppealInformationType(Some(AppealStatusEnum.Upheld), Some(AppealLevelEnum.HMRC))))))
        )
        val result: Int = service.findNumberOfLatePaymentPenalties(Some(allLPPsAppealedSuccessfully))
        result shouldBe 0
      }

      "all the penalties have been paid" in new Setup {
        val allLPPsPaid: LatePaymentPenalty = LatePaymentPenalty(
          Seq(sampleLPP.copy(penaltyAmountOutstanding = None))
        )
        val result: Int = service.findNumberOfLatePaymentPenalties(Some(allLPPsPaid))
        result shouldBe 0
      }

      "no penalties exist" in new Setup {
        val noneResult: Int = service.findNumberOfLatePaymentPenalties(None)
        noneResult shouldBe 0
        val emptySeqResult: Int = service.findNumberOfLatePaymentPenalties(Some(LatePaymentPenalty(Seq())))
        emptySeqResult shouldBe 0
      }
    }

    "return the amount of penalties that haven't been appealed successfully and are unpaid" in new Setup {
      val allLPPs: LatePaymentPenalty = LatePaymentPenalty(
        Seq(sampleLPP, sampleLPP)
      )
      val result: Int = service.findNumberOfLatePaymentPenalties(Some(allLPPs))
      result shouldBe 2
    }
  }

  "findNumberOfLateSubmissionPenalties" should {
    val sampleLSP: LSPDetails = LSPDetails(
      penaltyNumber = "123456789",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = None,
      penaltyCreationDate = LocalDate.of(2022, 1, 1),
      penaltyExpiryDate = LocalDate.of(2024, 1, 1),
      expiryReason = None,
      communicationsDate = Some(LocalDate.of(2022, 1, 1)),
      lateSubmissions = Some(
        Seq(
          LateSubmission(
            taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
            taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
            returnReceiptDate = None,
            taxReturnStatus = Some(TaxReturnStatusEnum.Open)
          )
        )
      ),
      appealInformation = None,
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(10),
      chargeDueDate = Some(LocalDate.of(2022, 1, 1))
    )
    val sampleSummary = LSPSummary(activePenaltyPoints = 3,
      inactivePenaltyPoints = 0,
      regimeThreshold = 4,
      penaltyChargeAmount = 200,
      PoCAchievementDate = Some(LocalDate.of(2022, 1, 1)))
    "return 0" when {
      "all the penalties have been appealed successfully" in new Setup {
        val allLSPsAppealedSuccessfully: LateSubmissionPenalty = LateSubmissionPenalty(
          sampleSummary,
          details = Seq(sampleLSP.copy(appealInformation = Some(Seq(AppealInformationType(Some(AppealStatusEnum.Upheld), Some(AppealLevelEnum.HMRC))))))
        )
        val result = service.findNumberOfLateSubmissionPenalties(Some(allLSPsAppealedSuccessfully))
        result shouldBe 0
      }

      "all the penalties have been paid" in new Setup {
        val allLSPsPaid = LateSubmissionPenalty(
          summary = sampleSummary,
          Seq(sampleLSP.copy(chargeOutstandingAmount = Some(0)))
        )
        val result = service.findNumberOfLateSubmissionPenalties(Some(allLSPsPaid))
        result shouldBe 0
      }

      "no penalties exist" in new Setup {
        val noneResult = service.findNumberOfLateSubmissionPenalties(None)
        noneResult shouldBe 0
        val emptySeqResult = service.findNumberOfLateSubmissionPenalties(Some(LateSubmissionPenalty(sampleSummary, Seq())))
        emptySeqResult shouldBe 0
      }
    }

    "return the amount of peantlies that haven't been appealed successfully and are unpaid" in new Setup {
      val allLSPs = LateSubmissionPenalty(
        sampleSummary, Seq(sampleLSP, sampleLSP)
      )
      val result = service.findNumberOfLateSubmissionPenalties(Some(allLSPs))
      result shouldBe 2
    }
  }

  "findActiveLateSubmissionPenaltyPoints" should {
    "return Some" when {
      "the payload has an entry for active penalty points" in new Setup {
        val lateSubmissionPenalty: LateSubmissionPenalty = LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 1, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 0, PoCAchievementDate = Some(LocalDate.of(9999, 1, 1))
          ), details = Seq.empty
        )
        val result: Option[Int] = service.findActiveLateSubmissionPenaltyPoints(Some(lateSubmissionPenalty))
        result.isDefined shouldBe true
        result.get shouldBe 1
      }
    }

    "return None" when {
      "the payload does not have a summary section for LSP" in new Setup {
        val result: Option[Int] = service.findActiveLateSubmissionPenaltyPoints(None)
        result.isEmpty shouldBe true
      }
    }
  }

  "getRegimeThreshold" should {
    "return Some" when {
      "the payload has an entry for regime threshold" in new Setup {
        val lateSubmissionPenalty: LateSubmissionPenalty = LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 0, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 0, PoCAchievementDate = None
          ), details = Seq.empty
        )
        val result: Option[Int] = service.getRegimeThreshold(Some(lateSubmissionPenalty))
        result.isDefined shouldBe true
        result.get shouldBe 4
      }
    }

    "return None" when {
      "the payload does not have a summary section for LSP" in new Setup {
        val result: Option[Int] = service.findActiveLateSubmissionPenaltyPoints(None)
        result.isEmpty shouldBe true
      }
    }
  }

  "getContentForLSP" should {
    "return None" when {
      "the active LSP amount is 0" in new Setup {
        val result: Option[String] = service.getContentForLSPPoints(amountOfLSPs = 0, regimeThreshold = 4)
        result.isEmpty shouldBe true
      }

      //Theoretically should never happen but worth guarding
      "the regime threshold is 0" in new Setup {
        val result: Option[String] = service.getContentForLSPPoints(amountOfLSPs = 4, regimeThreshold = 0)
        result.isEmpty shouldBe true
      }
    }

    "return Some and the correct message" when {
      "the amount of LSPs is 1" in new Setup {
        val result: Option[String] = service.getContentForLSPPoints(amountOfLSPs = 1, regimeThreshold = 4)
        result.isDefined shouldBe true
        result.get shouldBe "1 late submission penalty point"
      }

      "the amount of LSPs is > 1" in new Setup {
        val result: Option[String] = service.getContentForLSPPoints(amountOfLSPs = 3, regimeThreshold = 4)
        result.isDefined shouldBe true
        result.get shouldBe "3 late submission penalty points"
      }

      "the amount of LSPs is at the threshold" in new Setup {
        val result: Option[String] = service.getContentForLSPPoints(amountOfLSPs = 4, regimeThreshold = 4)
        result.isDefined shouldBe true
        result.get shouldBe "the maximum number of late submission penalty points"
      }
    }
  }
}
