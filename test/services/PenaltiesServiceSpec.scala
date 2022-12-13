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

package services

import base.SpecBase
import connectors.PenaltiesConnector
import connectors.httpParsers.{BadRequest, InvalidJson, UnexpectedFailure}
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, MainTransactionEnum}
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}
import org.mockito.Matchers.any
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
    latePaymentPenalty = None
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
    latePaymentPenalty = None
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
    latePaymentPenalty = None
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
    latePaymentPenalty = None
  )

  class Setup {
    val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)
    reset(mockPenaltiesConnector)
  }

  "getPenaltyDataFromEnrolmentKey" when  {
    s"$OK (Ok) is returned from the parser " should {
      "return a Right with the correct model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(any())(any(), any()))
          .thenReturn(Future.successful(Right(penaltyDetailsWithNoVATDue)))

        val result = await(service.getPenaltyDataFromEnrolmentKey("1234567890")(vatTraderUser, hc))
        result.isRight shouldBe true
        result shouldBe Right(penaltyDetailsWithNoVATDue)
      }
    }

    s"$NO_CONTENT (No content) is returned from the parser" should {
      "return an empty Right GetPenaltyDetails model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(any())(any(), any()))
          .thenReturn(Future.successful(Right(GetPenaltyDetails(None, None, None))))

        val result = await(service.getPenaltyDataFromEnrolmentKey("1234567890")(vatTraderUser, hc))
        result.isRight shouldBe true
        result shouldBe Right(GetPenaltyDetails(None, None, None))
      }

      s"$BAD_REQUEST (Bad request) is returned from the parser because of invalid json" should {
        "return a Left with status 400" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any())(any(), any()))
            .thenReturn(Future.successful(Left(InvalidJson)))

          val result = await(service.getPenaltyDataFromEnrolmentKey("1234567890")(vatTraderUser, hc))
          result.isLeft shouldBe true
          result shouldBe Left(InvalidJson)
          result.left.get.status shouldBe 400
          result.left.get.body shouldBe "Invalid JSON received"
        }
      }

      s"$BAD_REQUEST (Bad request) is returned from the parser" should {
        "return a Left with status 400" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any())(any(), any()))
            .thenReturn(Future.successful(Left(BadRequest)))

          val result = await(service.getPenaltyDataFromEnrolmentKey("1234567890")(vatTraderUser, hc))
          result.isLeft shouldBe true
          result shouldBe Left(BadRequest)
          result.left.get.status shouldBe 400
          result.left.get.body shouldBe "Incorrect JSON body sent"
        }
      }

      s"an unexpected error is returned from the parser" should {
        "return a Left with the status and message" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any())(any(), any()))
            .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))

          val result = await(service.getPenaltyDataFromEnrolmentKey("1234567890")(vatTraderUser, hc))
          result.isLeft shouldBe true
          result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
          result.left.get.status shouldBe 500
          result.left.get.body shouldBe "Unexpected response, status 500 returned"
        }
      }
    }
  }

  "findOverdueVATFromPayload" should {
    "return 0 when the payload does not have any VAT due" in new Setup {
      val result: BigDecimal = service.findOverdueVATFromPayload(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }
    "return total amount of VAT overdue when the VAT overview is present with elements" in new Setup {
      val result: BigDecimal = service.findOverdueVATFromPayload(penaltyDetailsWithVATOnly)
      result shouldBe 223.45
    }
  }

  "findEstimatedLPPsFromPayload" should {
    "return 0 when the user has no estimated LPP's due" in new Setup {
      val result: BigDecimal = service.findEstimatedLPPsFromPayload(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return the correct amount due of estimated penalties" in new Setup {
      val result: BigDecimal = service.findEstimatedLPPsFromPayload(penaltyDetailsWithEstimatedLPPs)
      result shouldBe 50.00
    }
  }

  "findCrystallisedLPPsFromPayload" should {
    "return 0 when the user has no LPP's" in new Setup {
      val result: BigDecimal = service.findCrystallisedLPPsFromPayload(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return the correct amount due of crystallised penalties" in new Setup {
      val result: BigDecimal = service.findCrystallisedLPPsFromPayload(penaltyDetailsWithCrystallisedLPPs)
      result shouldBe 50.00
    }
  }

  "findTotalLSPFromPayload" should {
    val penaltyDetailsWithLSPs: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = Some(
        Totalisations(
          LSPTotalValue = Some(400),
          penalisedPrincipalTotal = Some(0),
          LPPPostedTotal = Some(0),
          LPPEstimatedTotal = Some(0),
          totalAccountOverdue = None,
          totalAccountPostedInterest = None,
          totalAccountAccruingInterest = None
        )
      ),
      lateSubmissionPenalty = None, latePaymentPenalty = None
    )
    "return 0 when the payload does not have any LSP's" in new Setup {
      val result: BigDecimal = service.findTotalLSPFromPayload(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return total amount of LSP's" in new Setup {
      val result: BigDecimal = service.findTotalLSPFromPayload(penaltyDetailsWithLSPs)
      result shouldBe 400
    }
  }

  "estimatedVATInterest" should {
    "return 0 when the payload does not have any VAT overview field" ignore new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(penaltyDetailsWithNoVATDue)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return 0 when the payload contains VAT overview but has no crystalized and estimated interest" ignore new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(penaltyDetailsWithVATOnly)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return total estimated VAT interest when crystalized and estimated interest is present" ignore new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(penaltyDetailsWithVATOnly)
      result._1 shouldBe 40.00
      result._2 shouldBe true
    }

    "return total VAT interest when the VAT overview is present without estimated interest" ignore new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(penaltyDetailsWithVATOnly)
      result._1 shouldBe 20.00
      result._2 shouldBe false
    }
    "return total VAT interest when the VAT overview is present without crystalized interest" ignore new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(penaltyDetailsWithVATOnly)
      result._1 shouldBe 43.00
      result._2 shouldBe true
    }
  }

  "isAnyLSPUnpaidAndSubmissionIsDue" should {
    "return false" when {
      "there is no LSPs unpaid" in new Setup {
        val lspDetailsUnpaid: LSPDetails = LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
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
                taxReturnStatus = TaxReturnStatusEnum.Fulfilled
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Fulfilled
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Fulfilled
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Open
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Open
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Open
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
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
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
                taxReturnStatus = TaxReturnStatusEnum.Open
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
      val totalisationFieldWithOverdueVAT = Totalisations(
        totalAccountOverdue = Some(123.45),
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result = service.findUnpaidVATCharges(Some(totalisationFieldWithOverdueVAT))
      result shouldBe 123.45
    }

    "return 0 if no totalAccountOverdue field present" in new Setup {
      val totalisationFieldWithOverdueVAT = Totalisations(
        totalAccountOverdue = None,
        penalisedPrincipalTotal = Some(543.21),
        LPPPostedTotal = None,
        LPPEstimatedTotal = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None,
        LSPTotalValue = None
      )
      val result = service.findUnpaidVATCharges(Some(totalisationFieldWithOverdueVAT))
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
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )
    "return 0" when {
      "all the penalties have been appealed successfully" in new Setup {
        val allLPPsAppealedSuccessfully: LatePaymentPenalty = LatePaymentPenalty(
          Seq(sampleLPP.copy(appealInformation = Some(Seq(AppealInformationType(Some(AppealStatusEnum.Upheld), Some(AppealLevelEnum.HMRC))))))
        )
        val result = service.findNumberOfLatePaymentPenalties(Some(allLPPsAppealedSuccessfully))
        result shouldBe 0
      }

      "all the penalties have been paid" in new Setup {
        val allLPPsPaid: LatePaymentPenalty = LatePaymentPenalty(
          Seq(sampleLPP.copy(penaltyAmountOutstanding = Some(0)))
        )
        val result = service.findNumberOfLatePaymentPenalties(Some(allLPPsPaid))
        result shouldBe 0
      }

      "no penalties exist" in new Setup {
        val noneResult = service.findNumberOfLatePaymentPenalties(None)
        noneResult shouldBe 0
        val emptySeqResult = service.findNumberOfLatePaymentPenalties(Some(LatePaymentPenalty(Seq())))
        emptySeqResult shouldBe 0
      }
    }

    "return the amount of penalties that haven't been appealed successfully and are unpaid" in new Setup {
      val allLPPs: LatePaymentPenalty = LatePaymentPenalty(
        Seq(sampleLPP, sampleLPP)
      )
      val result = service.findNumberOfLatePaymentPenalties(Some(allLPPs))
      result shouldBe 2
    }
  }
}
