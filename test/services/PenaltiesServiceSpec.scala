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

package services

import base.SpecBase
import connectors.PenaltiesConnector
import models.ETMPPayload
import models.financial.{AmountTypeEnum, Financial, OverviewElement}
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import java.time.LocalDateTime

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

  class Setup {
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)

    reset(mockPenaltiesConnector)
  }

  "getLspDataWithVrn" should {
    s"return a successful response and pass the result back to the controller" in new Setup {

      when(mockPenaltiesConnector.getPenaltiesData(any())(any(), any())).thenReturn(Future.successful(sampleLspData))

      val result = await(service.getETMPDataFromEnrolmentKey(vrn)(vatTraderUser, HeaderCarrier()))

      result shouldBe sampleLspData
    }

    s"return an exception and pass the result back to the controller" in new Setup {

      when(mockPenaltiesConnector.getPenaltiesData(any())(any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result = intercept[Exception](await(service.getETMPDataFromEnrolmentKey(vrn)(vatTraderUser, HeaderCarrier())))

      result.getMessage shouldBe "Upstream error"
    }
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
        period = Some(PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      )
    )
    s"return true when there is a ${PenaltyTypeEnum.Financial} penalty point and it is not paid" in new Setup {
      val result = service.isAnyLSPUnpaid(sampleFinancialPenaltyPointUnpaid)
      result shouldBe true
    }

    s"return false when there is a ${PenaltyTypeEnum.Financial} penalty point and IT IS paid" in new Setup {
      val result = service.isAnyLSPUnpaid(Seq(sampleFinancialPenaltyPointUnpaid.head.copy(status = PointStatusEnum.Paid)))
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
        period = Some(PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            status = SubmissionStatusEnum.Overdue
          )
        )),
        communications = Seq.empty,
        financial = None
      )
    )

    val sampleFinancialPenaltyPointUnpaidAndSubmitted: Seq[PenaltyPoint] = Seq(
      sampleFinancialPenaltyPointUnpaidAndNotSubmitted.head.copy(period = Some(PenaltyPeriod(
        startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
        endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
        submission = Submission(
          dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
          submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
          status = SubmissionStatusEnum.Submitted
        )
      )))
    )

    s"return true when there is a ${PenaltyTypeEnum.Financial} penalty point, it is due and there is no submission" in new Setup {
      val result = service.isAnyLSPUnpaidAndSubmissionIsDue(sampleFinancialPenaltyPointUnpaidAndNotSubmitted)
      result shouldBe true
    }

    s"return false when there is a ${PenaltyTypeEnum.Financial} penalty point, it is due BUT there is a submission" in new Setup {
      val result = service.isAnyLSPUnpaidAndSubmissionIsDue(sampleFinancialPenaltyPointUnpaidAndSubmitted)
      result shouldBe false
    }
  }

  "findOverdueVATFromPayload" should {
    "return 0 when the payload does not have any VAT overview field" in new Setup {
      val result = service.findOverdueVATFromPayload(sampleLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains VAT overview but has no elements" in new Setup {
      val result = service.findOverdueVATFromPayload(sampleLspDataWithVATOverviewNoElements)
      result shouldBe 0
    }

    "return total amount of VAT overdue when the VAT overview is present with elements" in new Setup {
      val result = service.findOverdueVATFromPayload(sampleLspDataWithVATOverview)
      result shouldBe 223.45
    }
  }

  "isOtherUnrelatedPenalties" should {
    "return false when the payload does not have the 'otherPenalties' field" in new Setup {
      val result = service.isOtherUnrelatedPenalties(sampleLspData.copy(otherPenalties = None))
      result shouldBe false
    }

    "return false when the payload has the 'otherPenalties' field and it's false" in new Setup {
      val result = service.isOtherUnrelatedPenalties(sampleLspData)
      result shouldBe false
    }

    "return true when the payload has the 'otherPenalties' field and it's true" in new Setup {
      val result = service.isOtherUnrelatedPenalties(sampleLspData.copy(otherPenalties = Some(true)))
      result shouldBe true
    }
  }

  "findEstimatedLPPsFromPayload" should {
    "return 0 when the user has no LPP's" in new Setup {
      val result = service.findEstimatedLPPsFromPayload(sampleLspData)
      result._1 shouldBe 0
      result._2 shouldBe false
    }

    "return the amount of crystallised penalties and false - indicating no estimate" in new Setup {
      val result = service.findEstimatedLPPsFromPayload(sampleLppDataNoAdditionalPenalties)
      result._1 shouldBe 50.00
      result._2 shouldBe false
    }

    "return the amount of crystallised penalties and true - indicating additional penalties / estimates" in new Setup {
      val result = service.findEstimatedLPPsFromPayload(sampleLppDataWithAdditionalPenalties)
      result._1 shouldBe 100.00
      result._2 shouldBe true
    }
  }

  "findTotalLSPFromPayload" should {
    "return 0 when the payload does not have any LSPP's" in new Setup {
      val result = service.findTotalLSPFromPayload(sampleLspData)
      result shouldBe (0, 0)
    }

    "return 0 when the payload does not have any LSP's" in new Setup {
      val result = service.findTotalLSPFromPayload(etmpDataWithOneLSP)
      result shouldBe (0, 0)
    }

    "return total amount of VAT overdue when the VAT overview is present with elements" in new Setup {
      val result = service.findTotalLSPFromPayload(sampleLspDataWithDueFinancialPenalties)
      result shouldBe (400.00, 2)
    }
  }

  "estimatedVATInterest" should {
    "return 0 when the payload does not have any VAT overview field" in new Setup {
      val result = service.findEstimatedVATInterest(sampleLspData)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return 0 when the payload contains VAT overview but has no crystalized and estimated interest" in new Setup {
      val result = service.findEstimatedVATInterest(sampleLspDataWithVATOverviewNoElements)
      result._1 shouldBe 0.00
      result._2 shouldBe false
    }

    "return total estimated VAT interest when  crystalized and estimated interest is present" in new Setup {
      val result = service.findEstimatedVATInterest(sampleLspDataWithVATOverview)
      result._1 shouldBe 40.00
      result._2 shouldBe true
    }

    "return total VAT interest when the VAT overview is present without estimated interest" in new Setup {
      val result = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutEstimatedInterest)
      result._1 shouldBe 20.00
      result._2 shouldBe false
    }
    "return total VAT interest when the VAT overview is present without crystalized interest" in new Setup {
      val result = service.findEstimatedVATInterest(samplePayloadWithVATOverviewWithoutCrystalizedInterest)
      result._1 shouldBe 43.00
      result._2 shouldBe true
    }
  }

  "findCrystalizedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LPS or LPP" in new Setup {
      val result = service.findCrystalizedPenaltiesInterest(sampleLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain crystalized interest penalties for LSP and LPP" in new Setup {
      val result = service.findCrystalizedPenaltiesInterest(sampleLspDataWithNoFinancialElements)
      result shouldBe 0
    }

    "return total amount when the payload contains crystalized interest penalties for LSP and LPP" in new Setup {
      val result = service.findCrystalizedPenaltiesInterest(sampleLspDataWithFinancialElements)
      result shouldBe 40
    }
  }

  "findEstimatedPenaltiesInterest" should {
    "return 0 when the payload does not have any financial penalties for LPS or LPP" in new Setup {
      val result = service.findEstimatedPenaltiesInterest(sampleLspData)
      result shouldBe 0
    }

    "return 0 when the payload contains financial penalties but does not contain estimated interest penalties for LSP and LPP" in new Setup {
      val result = service.findEstimatedPenaltiesInterest(sampleLspDataWithNoFinancialElements)
      result shouldBe 0
    }

    "return total amount when the payload contains estimated interest penalties for LSP and LPP" in new Setup {
      val result = service.findEstimatedPenaltiesInterest(sampleLspDataWithFinancialElements)
      result shouldBe 30
    }
  }
}
