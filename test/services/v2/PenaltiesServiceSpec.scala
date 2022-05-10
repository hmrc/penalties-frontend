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
import models.v3.{GetPenaltyDetails, Totalisations}

class PenaltiesServiceSpec extends SpecBase {

  val penaltyDetailsWithNoVATDue: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 0,
      penalisedPrincipalTotal = 0,
      LPPPostedTotal = 0,
      LPPEstimatedTotal = 0,
      LPIPostedTotal = 0,
      LPIEstimatedTotal = 0
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  val penaltyDetailsWithVATOnly: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 0,
      penalisedPrincipalTotal = 223.45,
      LPPPostedTotal = 0,
      LPPEstimatedTotal = 0,
      LPIPostedTotal = 0,
      LPIEstimatedTotal = 0
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  val penaltyDetailsWithEstimatedLPPs: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 0,
      penalisedPrincipalTotal = 0,
      LPPPostedTotal = 0,
      LPPEstimatedTotal = 50,
      LPIPostedTotal = 0,
      LPIEstimatedTotal = 0
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  val penaltyDetailsWithCrystallisedLPPs: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 0,
      penalisedPrincipalTotal = 0,
      LPPPostedTotal = 50,
      LPPEstimatedTotal = 0,
      LPIPostedTotal = 0,
      LPIEstimatedTotal = 0
    )),
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  class Setup {
    val service: PenaltiesService = new PenaltiesService()
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
          LSPTotalValue = 400,
          penalisedPrincipalTotal = 0,
          LPPPostedTotal = 0,
          LPPEstimatedTotal = 0,
          LPIPostedTotal = 0,
          LPIEstimatedTotal = 0
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

//  "estimatedVATInterest" should {
//    "return 0 when the payload does not have any VAT overview field" in new Setup {
//      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleEmptyLspData)
//      result._1 shouldBe 0.00
//      result._2 shouldBe false
//    }
//
//    "return 0 when the payload contains VAT overview but has no crystalized and estimated interest" in new Setup {
//      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleLspDataWithVATOverviewNoElements)
//      result._1 shouldBe 0.00
//      result._2 shouldBe false
//    }
//
//    "return total estimated VAT interest when  crystalized and estimated interest is present" in new Setup {
//      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleLspDataWithVATOverview)
//      result._1 shouldBe 40.00
//      result._2 shouldBe true
//    }
//
//    "return total VAT interest when the VAT overview is present without estimated interest" in new Setup {
//      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutEstimatedInterest)
//      result._1 shouldBe 20.00
//      result._2 shouldBe false
//    }
//    "return total VAT interest when the VAT overview is present without crystalized interest" in new Setup {
//      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutCrystalizedInterest)
//      result._1 shouldBe 43.00
//      result._2 shouldBe true
//    }
//  }

  "findCrystalizedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LSP or LPP" in new Setup {
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(penaltyDetailsWithNoVATDue)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain crystalized interest penalties for LPP" in new Setup {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = 400,
            penalisedPrincipalTotal = 2000.23,
            LPPPostedTotal = 100,
            LPPEstimatedTotal = 0,
            LPIPostedTotal = 0,
            LPIEstimatedTotal = 0
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
            LSPTotalValue = 400,
            penalisedPrincipalTotal = 2000.23,
            LPPPostedTotal = 100,
            LPPEstimatedTotal = 0,
            LPIPostedTotal = 40,
            LPIEstimatedTotal = 0
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
            LSPTotalValue = 400,
            penalisedPrincipalTotal = 2000.23,
            LPPPostedTotal = 100,
            LPPEstimatedTotal = 23.45,
            LPIPostedTotal = 40,
            LPIEstimatedTotal = 0
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
            LSPTotalValue = 400,
            penalisedPrincipalTotal = 2000.23,
            LPPPostedTotal = 100,
            LPPEstimatedTotal = 23.45,
            LPIPostedTotal = 40,
            LPIEstimatedTotal = 30
          )
        ),
        lateSubmissionPenalty = None,
        latePaymentPenalty = None
      )
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(penaltyDetails)
      result shouldBe 30
    }
  }
}
