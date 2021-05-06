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
import models.penalty.PenaltyPeriod
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.LocalDateTime
import scala.concurrent.Future

class PenaltiesServiceSpec extends SpecBase with MockitoSugar {

  val mocPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]

  class Setup {
    val service: PenaltiesService = new PenaltiesService(mocPenaltiesConnector)

    reset(mocPenaltiesConnector)
  }

  "getLspDataWithVrn" should {
    s"return a successful response and pass the result back to the controller" in new Setup {

      when(mocPenaltiesConnector.getPenaltiesData(any())(any())).thenReturn(Future.successful(sampleLspData))

      val result = await(service.getLspDataWithVrn(vrn)(HeaderCarrier()))

      result shouldBe sampleLspData
    }

    s"return an exception and pass the result back to the controller" in new Setup {

      when(mocPenaltiesConnector.getPenaltiesData(any())(any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result = intercept[Exception](await(service.getLspDataWithVrn(vrn)(HeaderCarrier())))

      result.getMessage shouldBe "Upstream error"
    }
  }

  "isAnyLSPUnpaid" should {
    val sampleFinancialPenaltyPointUnpaid: Seq[PenaltyPoint] = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        number = "1",
        dateCreated = LocalDateTime.of(2021, 3, 8, 0, 0),
        dateExpired = Some(LocalDateTime.of(2023, 1, 1, 0, 0)),
        status = PointStatusEnum.Due,
        period = PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
            status = SubmissionStatusEnum.Submitted
          )
        ),
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
        number = "1",
        dateCreated = LocalDateTime.of(2021, 3, 8, 0, 0),
        dateExpired = Some(LocalDateTime.of(2023, 1, 1, 0, 0)),
        status = PointStatusEnum.Due,
        period = PenaltyPeriod(
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
          endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
          submission = Submission(
            dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
            status = SubmissionStatusEnum.Overdue
          )
        ),
        communications = Seq.empty,
        financial = None
      )
    )

    val sampleFinancialPenaltyPointUnpaidAndSubmitted: Seq[PenaltyPoint] = Seq(
      sampleFinancialPenaltyPointUnpaidAndNotSubmitted.head.copy(period = PenaltyPeriod(
        startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
        endDate = LocalDateTime.of(2021, 2, 1, 0, 0),
        submission = Submission(
          dueDate = LocalDateTime.of(2021, 3, 7, 0, 0),
          submittedDate = Some(LocalDateTime.of(2021, 3, 9, 0, 0)),
          status = SubmissionStatusEnum.Submitted
        )
      ))
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
}
