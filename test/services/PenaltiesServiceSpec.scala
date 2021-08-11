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
import models.financial.{AmountTypeEnum, OverviewElement}
import models.penalty.{LatePaymentPenalty, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
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

  class Setup {
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)

    reset(mockPenaltiesConnector)
  }

  "getLspDataWithVrn" should {
    s"return a successful response and pass the result back to the controller" in new Setup {

      when(mockPenaltiesConnector.getPenaltiesData(any())(any())).thenReturn(Future.successful(sampleLspData))

      val result = await(service.getLspDataWithVrn(vrn)(HeaderCarrier()))

      result shouldBe sampleLspData
    }

    s"return an exception and pass the result back to the controller" in new Setup {

      when(mockPenaltiesConnector.getPenaltiesData(any())(any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result = intercept[Exception](await(service.getLspDataWithVrn(vrn)(HeaderCarrier())))

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
}
