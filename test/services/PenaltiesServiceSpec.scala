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
import models.ETMPPayload
import models.financial.{AmountTypeEnum, Financial, OverviewElement}
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import models.v3.lpp.{LatePaymentPenalty => NewLatePaymentPenalty}
import models.v3.{GetPenaltyDetails, Totalisations}
import models.v3.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LateSubmission, LateSubmissionPenalty, TaxReturnStatusEnum}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class PenaltiesServiceSpec extends SpecBase {

  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])

  val sampleLspDataWithVATOverview: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
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
          estimatedInterest = Some(10.00),
          crystalizedInterest = Some(10.00)
        )
      )
    ),
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )

  val samplePayloadWithVATOverviewWithoutEstimatedInterest: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
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
          crystalizedInterest = Some(10.00)
        )
      )
    ),
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )
  val samplePayloadWithVATOverviewWithoutCrystalizedInterest: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = Some(
      Seq(
        OverviewElement(
          `type` = AmountTypeEnum.VAT,
          amount = 100.00,
          estimatedInterest = Some(13.00)
        ),
        OverviewElement(
          `type` = AmountTypeEnum.Central_Assessment,
          amount = 123.45,
          estimatedInterest = Some(30.00)
        )
      )
    ),
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )

  val sampleLspDataWithVATOverviewNoElements: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = Some(Seq()),
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )

  val sampleLppDataNoAdditionalPenalties: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = None,
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq(
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
          amountDue = 123.45,
          outstandingAmountDue = 50.00,
          dueDate = sampleDate,
          estimatedInterest = Some(10.12),
          crystalizedInterest = Some(10.12)
        )
      )
    ))
  )

  val sampleLppDataWithAdditionalPenalties: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = None,
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq(
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
          amountDue = 100.00,
          outstandingAmountDue = 50.00,
          dueDate = sampleDate,
          estimatedInterest = None,
          crystalizedInterest = None
        )
      ),
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
          amountDue = 123.45,
          outstandingAmountDue = 50.00,
          dueDate = sampleDate,
          estimatedInterest = Some(10.12),
          crystalizedInterest = Some(10.12)
        )
      )
    ))
  )

  val sampleLspDataWithNoFinancialElements: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = Some(Seq.empty),
    penaltyPoints = Seq(sampleFinancialPenaltyPoint.copy(financial = Some(
      Financial(
        amountDue = 0,
        outstandingAmountDue = 0,
        dueDate = LocalDateTime.now(),
        estimatedInterest = None,
        crystalizedInterest = None
      )
    ))),
    latePaymentPenalties = Some(Seq(sampleLatePaymentPenaltyDue.copy(financial =
      Financial(
        amountDue = 0,
        outstandingAmountDue = 0,
        dueDate = LocalDateTime.now(),
        estimatedInterest = None,
        crystalizedInterest = None
      )
    )))
  )

  val sampleLspDataWithFinancialElements: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    vatOverview = Some(Seq.empty),
    penaltyPoints = Seq(sampleFinancialPenaltyPoint.copy(financial = Some(
      Financial(
        amountDue = 0,
        outstandingAmountDue = 0,
        dueDate = LocalDateTime.now(),
        estimatedInterest = Some(15),
        crystalizedInterest = Some(20)
      )
    ))),
    latePaymentPenalties = Some(Seq(sampleLatePaymentPenaltyDue.copy(financial =
      Financial(
        amountDue = 0,
        outstandingAmountDue = 0,
        dueDate = LocalDateTime.now(),
        estimatedInterest = Some(15),
        crystalizedInterest = Some(20)
      )
    )))
  )

  val samplePenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 200,
      penalisedPrincipalTotal = 2000,
      LPPPostedTotal = 165.25,
      LPPEstimatedTotal = 15.26,
      LPIPostedTotal = 1968.2,
      LPIEstimatedTotal = 7)),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25
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
              appealLevel =  Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(NewLatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(1001.45),
        penaltyAmountOutstanding = Some(99.99),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(99.99),
        LPP1HRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2069-10-30"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel =  Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  class Setup {
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector, appConfig)

    reset(mockPenaltiesConnector)
  }

  "isAnyLSPUnpaid" should {
    val sampleFinancialPenaltyPointUnpaid: Seq[PenaltyPoint] = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        number = "1",
        dateCreated = LocalDateTime.of(2021, 3, 8, 0, 0),
        dateExpired = Some(LocalDateTime.of(2023, 1, 1, 0, 0)),
        status = PointStatusEnum.Due,
        reason = None,
        period = Some(Seq(PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
            status = SubmissionStatusEnum.Submitted
          )
        ))),
        communications = Seq.empty,
        financial = None
      )
    )
    s"return true when there is a ${PenaltyTypeEnum.Financial} penalty point and it is not paid" in new Setup {
      val result: Boolean = service.isAnyLSPUnpaid(sampleFinancialPenaltyPointUnpaid)
      result shouldBe true
    }

    s"return false when there is a ${PenaltyTypeEnum.Financial} penalty point and IT IS paid" in new Setup {
      val result: Boolean = service.isAnyLSPUnpaid(Seq(sampleFinancialPenaltyPointUnpaid.head.copy(status = PointStatusEnum.Paid)))
      result shouldBe false
    }
  }

  "isAnyLSPUnpaidAndSubmissionIsDue" should {
    val sampleFinancialPenaltyPointUnpaidAndNotSubmitted: Seq[PenaltyPoint] = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        number = "1",
        dateCreated = LocalDateTime.of(2021, 3, 8, 0, 0),
        dateExpired = Some(LocalDateTime.of(2023, 1, 1, 0, 0)),
        status = PointStatusEnum.Due,
        reason = None,
        period = Some(Seq(PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            status = SubmissionStatusEnum.Overdue
          )
        ))),
        communications = Seq.empty,
        financial = None
      )
    )

    val sampleFinancialPenaltyPointUnpaidAndSubmitted: Seq[PenaltyPoint] = Seq(
      sampleFinancialPenaltyPointUnpaidAndNotSubmitted.head.copy(period = Some(Seq(PenaltyPeriod(
        startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
        endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
        submission = Submission(
          dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
          submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
          status = SubmissionStatusEnum.Submitted
        )
      ))))
    )

    s"return true when there is a ${PenaltyTypeEnum.Financial} penalty point, it is due and there is no submission" in new Setup {

      val result: Boolean = service.isAnyLSPUnpaidAndSubmissionIsDue(sampleFinancialPenaltyPointUnpaidAndNotSubmitted)
      result shouldBe true
    }

    s"return false when there is a ${PenaltyTypeEnum.Financial} penalty point, it is due BUT there is a submission" in new Setup {

      val result: Boolean = service.isAnyLSPUnpaidAndSubmissionIsDue(sampleFinancialPenaltyPointUnpaidAndSubmitted)
      result shouldBe false
    }
  }

  "findOverdueVATFromPayload" should {
    "return 0 when the payload does not have any VAT overview field" in new Setup {
      val result: BigDecimal = service.findOverdueVATFromPayload(sampleEmptyLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains VAT overview but has no elements" in new Setup {
      val result: BigDecimal = service.findOverdueVATFromPayload(sampleLspDataWithVATOverviewNoElements)
      result shouldBe 0
    }

    "return total amount of VAT overdue when the VAT overview is present with elements" in new Setup {
      val result: BigDecimal = service.findOverdueVATFromPayload(sampleLspDataWithVATOverview)
      result shouldBe 223.45
    }
  }

  "isOtherUnrelatedPenalties" should {
    "return false when the payload does not have the 'otherPenalties' field" in new Setup {
      val result: Boolean = service.isOtherUnrelatedPenalties(sampleEmptyLspData.copy(otherPenalties = None))
      result shouldBe false
    }

    "return false when the payload has the 'otherPenalties' field and it's false" in new Setup {
      val result: Boolean = service.isOtherUnrelatedPenalties(sampleEmptyLspData)
      result shouldBe false
    }

    "return true when the payload has the 'otherPenalties' field and it's true" in new Setup {
      val result: Boolean = service.isOtherUnrelatedPenalties(sampleEmptyLspData.copy(otherPenalties = Some(true)))
      result shouldBe true
    }
  }

  "findEstimatedLPPsFromPayload" should {
    "return 0 when the user has no LPP's" in new Setup {
      val result: BigDecimal = service.findEstimatedLPPsFromPayload(sampleEmptyLspData)
      result shouldBe 0
    }

    "return the correct amount due of estimated penalties" in new Setup {
      val result: BigDecimal = service.findEstimatedLPPsFromPayload(sampleLppDataWithAdditionalPenalties)
      result shouldBe 50.00
    }
  }

  "findCrystallisedLPPsFromPayload" should {
    "return 0 when the user has no LPP's" in new Setup {
      val result: BigDecimal = service.findCrystallisedLPPsFromPayload(sampleEmptyLspData)
      result shouldBe 0
    }

    "return the correct amount due of crystallised penalties" in new Setup {
      val result: BigDecimal = service.findCrystallisedLPPsFromPayload(sampleLppDataNoAdditionalPenalties)
      result shouldBe 50.00
    }
  }

  "findTotalLSPFromPayload" should {
    "return 0 when the payload does not have any LSPP's" in new Setup {
      val result: (BigDecimal, Int) = service.findTotalLSPFromPayload(sampleEmptyLspData)
      result shouldBe ((0, 0): (Int, Int))
    }

    "return 0 when the payload does not have any LSP's" in new Setup {
      val result: (BigDecimal, Int) = service.findTotalLSPFromPayload(etmpDataWithOneLSP)
      result shouldBe ((0, 0): (Int, Int))
    }

    "return total amount of VAT overdue when the VAT overview is present with elements" in new Setup {
      val result: (BigDecimal, Int) = service.findTotalLSPFromPayload(sampleLspDataWithDueFinancialPenalties)
      result shouldBe ((400.00, 2): (Double, Int))
    }
  }

  "estimatedVATInterest" should {
    "return 0 when the payload does not have any VAT overview field" in new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleEmptyLspData)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return 0 when the payload contains VAT overview but has no crystalized and estimated interest" in new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleLspDataWithVATOverviewNoElements)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return total estimated VAT interest when  crystalized and estimated interest is present" in new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(sampleLspDataWithVATOverview)
      result._1 shouldBe 40.00
      result._2 shouldBe true
    }

    "return total VAT interest when the VAT overview is present without estimated interest" in new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutEstimatedInterest)
      result._1 shouldBe 20.00
      result._2 shouldBe false
    }
    "return total VAT interest when the VAT overview is present without crystalized interest" in new Setup {
      val result: (BigDecimal, Boolean) = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutCrystalizedInterest)
      result._1 shouldBe 43.00
      result._2 shouldBe true
    }
  }

  "findCrystalizedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LPS or LPP" in new Setup {
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(sampleEmptyLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain crystalized interest penalties for LSP and LPP" in new Setup {
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(sampleLspDataWithNoFinancialElements)
      result shouldBe 0
    }

    "return total amount when the payload contains crystalized interest penalties for LSP and LPP" in new Setup {
      val result: BigDecimal = service.findCrystalizedPenaltiesInterest(sampleLspDataWithFinancialElements)
      result shouldBe 40
    }
  }

  "findEstimatedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LPS or LPP" in new Setup {
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(sampleEmptyLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain estimated interest penalties for LSP and LPP" in new Setup {
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(sampleLspDataWithNoFinancialElements)
      result shouldBe 0
    }

    "return total amount when the payload contains estimated interest penalties for LSP and LPP" in new Setup {
      val result: BigDecimal = service.findEstimatedPenaltiesInterest(sampleLspDataWithFinancialElements)
      result shouldBe 30
    }
  }

  "getLatestLSPCreationDate" should {
    "return Some" when {
      "the user has LSP's" in new Setup {
        val sampleLspDataWithDueFinancialPenalties: ETMPPayload = ETMPPayload(
          pointsTotal = 3,
          lateSubmissions = 3,
          adjustmentPointsTotal = 0,
          fixedPenaltyAmount = 400.0,
          penaltyAmountsTotal = 0.0,
          penaltyPointsThreshold = 2,
          vatOverview = None,
          penaltyPoints = Seq(
            PenaltyPoint(
              `type` = PenaltyTypeEnum.Financial,
              id = "1236",
              number = "3",
              appealStatus = None,
              dateCreated = sampleDate.plusMonths(3),
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
          latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
        )
        val result: Option[LocalDateTime] = service.getLatestLSPCreationDate(sampleLspDataWithDueFinancialPenalties)
        result.isDefined shouldBe true
        result.get shouldBe sampleDate.plusMonths(3)
      }

      "the user has appealed points - return the next valid point" in new Setup {
        val acceptedPoint: PenaltyPoint = samplePenaltyPointAppealedAccepted.copy(dateCreated = sampleDate, `type` = PenaltyTypeEnum.Financial)
        val appealUnderReviewPoint: PenaltyPoint = samplePenaltyPointAppealedUnderReview.copy(dateCreated = sampleDate.minusMonths(3),
          `type` = PenaltyTypeEnum.Financial)
        val dataWithAppealedPoint: ETMPPayload = ETMPPayload(
          pointsTotal = 1,
          lateSubmissions = 2,
          adjustmentPointsTotal = 0,
          fixedPenaltyAmount = 0,
          penaltyAmountsTotal = 0,
          penaltyPointsThreshold = 4,
          otherPenalties = None,
          vatOverview = None,
          penaltyPoints = Seq(
            acceptedPoint,
            appealUnderReviewPoint
          ),
          latePaymentPenalties = None
        )
        val result: Option[LocalDateTime] = service.getLatestLSPCreationDate(dataWithAppealedPoint)
        result.isDefined shouldBe true
        result.get shouldBe appealUnderReviewPoint.dateCreated
      }
    }

    "return None" when {

      "the user has no penalties" in new Setup {
        val result: Option[LocalDateTime] = service.getLatestLSPCreationDate(sampleEmptyLspData)
        result.isEmpty shouldBe true
      }

      "the user only has no LSPs" in new Setup {
        val result: Option[LocalDateTime] = service.getLatestLSPCreationDate(etmpDataWithOneLSP)
        result.isEmpty shouldBe true
      }
    }
  }
}
