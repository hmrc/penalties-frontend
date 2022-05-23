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

package services.v2

import base.SpecBase
import connectors.PenaltiesConnector
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.v3.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LateSubmission, TaxReturnStatusEnum}
import models.v3.{GetPenaltyDetails, Totalisations}
import org.mockito.Mockito.{mock, reset}

import java.time.LocalDate

class PenaltiesServiceSpec extends SpecBase {

  val penaltyDetailsWithNoVATDue: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(0),
      penalisedPrincipalTotal = Some(0),
      LPPPostedTotal = Some(0),
      LPPEstimatedTotal = Some(0),
      LPIPostedTotal = Some(0),
      LPIEstimatedTotal = Some(0)
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
      LPIPostedTotal = Some(0),
      LPIEstimatedTotal = Some(0)
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
      LPIPostedTotal = Some(0),
      LPIEstimatedTotal = Some(0)
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
      LPIPostedTotal = Some(0),
      LPIEstimatedTotal = Some(0)
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  class Setup {
    val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)
    reset(mockPenaltiesConnector)
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
          LPIPostedTotal = Some(0),
          LPIEstimatedTotal = Some(0)
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

  "findCrystalizedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LSP or LPP" in new Setup {
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain crystalized interest penalties for LPP" in new Setup {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(2000.23),
            LPPPostedTotal = Some(100),
            LPPEstimatedTotal = Some(0),
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
          )
        ),
        lateSubmissionPenalty = None,
        latePaymentPenalty = None
      )
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(penaltyDetails)
      result shouldBe 0
    }

    "return total amount when the payload contains crystalized interest penalties for LSP and LPP" in new Setup {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(2000.23),
            LPPPostedTotal = Some(100),
            LPPEstimatedTotal = Some(0),
            LPIPostedTotal = Some(40),
            LPIEstimatedTotal = Some(0)
          )
        ),
        lateSubmissionPenalty = None,
        latePaymentPenalty = None
      )
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(penaltyDetails)
      result shouldBe 40
    }
  }

  "findEstimatedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LPS or LPP" in new Setup {
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain estimated interest penalties for LSP and LPP" in new Setup {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(2000.23),
            LPPPostedTotal = Some(100),
            LPPEstimatedTotal = Some(23.45),
            LPIPostedTotal = Some(40),
            LPIEstimatedTotal = Some(0)
          )
        ),
        lateSubmissionPenalty = None,
        latePaymentPenalty = None
      )
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(penaltyDetails)
      result shouldBe 0
    }

    "return total amount when the payload contains estimated interest penalties for LPP" in new Setup {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(2000.23),
            LPPPostedTotal = Some(100),
            LPPEstimatedTotal = Some(23.45),
            LPIPostedTotal = Some(40),
            LPIEstimatedTotal = Some(30)
          )
        ),
        lateSubmissionPenalty = None,
        latePaymentPenalty = None
      )
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(penaltyDetails)
      result shouldBe 30
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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
          communicationsDate = LocalDate.of(2022, 1, 1),
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

  "getLatestLSPCreationDate" should {
    "find the latest LSP creation date for all LSPs not appealed successfully" in new Setup {
      val lspDetailsNonAppealed: Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "123456790",
          penaltyOrder = "2",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 2),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
        ),
        LSPDetails(
          penaltyNumber = "123456791",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 3),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
        ),
        LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
      )
      val result = service.getLatestLSPCreationDate(lspDetailsNonAppealed)
      result.get shouldBe LocalDate.of(2022, 1, 3)
    }

    "find the latest LSP creation date for all LSPs with some appealed successfully" in new Setup {
      val lspDetailsAppealed: Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "123456790",
          penaltyOrder = "2",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 2),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
        ),
        LSPDetails(
          penaltyNumber = "123456791",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 3),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
        ),
        LSPDetails(
          penaltyNumber = "123456789",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Charge,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2024, 1, 1),
          expiryReason = None,
          communicationsDate = LocalDate.of(2022, 1, 1),
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
      )
      val result = service.getLatestLSPCreationDate(lspDetailsAppealed)
      result.get shouldBe LocalDate.of(2022, 1, 2)
    }
  }
}
