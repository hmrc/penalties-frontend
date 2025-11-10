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
import connectors.httpParsers.ComplianceDataParser._
import models.User
import models.compliance.CompliancePayload
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.SessionKeys

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ComplianceServiceSpec extends SpecBase {

  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])

  class Setup {
    val service: ComplianceService = new ComplianceService(mockPenaltiesConnector)

    def mockSuccessfulConnectorResponse(toDate: LocalDate,
                                        response: CompliancePayloadSuccessResponse): OngoingStubbing[Future[CompliancePayloadResponse]] =
      when(mockPenaltiesConnector.getObligationData(any(), ArgumentMatchers.eq(toDate.minusYears(2)), ArgumentMatchers.eq(toDate))(any()))
        .thenReturn(Future.successful(Right(response)))

    reset(mockPenaltiesConnector)
  }

  private val validPocAchievementDate          = LocalDate.of(2022, 1, 1)
  private val invalidDefaultDate               = LocalDate.of(9999, 12, 31)
  private val dateNow                          = LocalDate.now()
  private val user                             = User("123456789")
  private def userWithSession(date: LocalDate) = User("123456789")(fakeRequest.withSession(SessionKeys.pocAchievementDate -> date.toString))

  "getDESComplianceData" should {
    "return a successful response and pass the result back to the controller" when {
      "the date is provided as a parameter" in new Setup {
        mockSuccessfulConnectorResponse(validPocAchievementDate, CompliancePayloadSuccessResponse(sampleCompliancePayload))

        val result: Option[CompliancePayload] =
          await(service.getDESComplianceData(vrn)(HeaderCarrier(), user, implicitly, Some(validPocAchievementDate)))

        result.isDefined shouldBe true
        result.get shouldBe sampleCompliancePayload
      }
      "the date provided as a parameter is the HIP default with '9999' year and the service uses today's date instead" in new Setup {
        mockSuccessfulConnectorResponse(dateNow, CompliancePayloadSuccessResponse(sampleCompliancePayload))

        val result: Option[CompliancePayload] =
          await(service.getDESComplianceData(vrn)(HeaderCarrier(), user, implicitly, Some(invalidDefaultDate)))

        result.isDefined shouldBe true
        result.get shouldBe sampleCompliancePayload
      }

      "the date is provided from session data" in new Setup {
        mockSuccessfulConnectorResponse(validPocAchievementDate, CompliancePayloadSuccessResponse(sampleCompliancePayload))

        val result: Option[CompliancePayload] =
          await(service.getDESComplianceData(vrn)(HeaderCarrier(), userWithSession(validPocAchievementDate), implicitly, pocAchievementDate = None))

        result.isDefined shouldBe true
        result.get shouldBe sampleCompliancePayload
      }
      "the date provided from session data is the HIP default with '9999' year and the service uses today's date instead" in new Setup {
        mockSuccessfulConnectorResponse(dateNow, CompliancePayloadSuccessResponse(sampleCompliancePayload))

        val result: Option[CompliancePayload] =
          await(service.getDESComplianceData(vrn)(HeaderCarrier(), userWithSession(invalidDefaultDate), implicitly, pocAchievementDate = None))

        result.isDefined shouldBe true
        result.get shouldBe sampleCompliancePayload
      }
    }

    "return None when the session keys are not present" in new Setup {
      val result: Option[CompliancePayload] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), user, implicitly))
      result shouldBe None
    }

    "return an exception and pass the result back to the controller" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(), any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))
      val result: Exception = intercept[Exception](await(service.getDESComplianceData(vrn)(HeaderCarrier(), user, implicitly, Some(dateNow))))

      result.getMessage shouldBe "Upstream error"
    }
  }

}
