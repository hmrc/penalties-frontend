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
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
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

    reset(mockPenaltiesConnector)
  }

  "getDESComplianceData" should {
    s"return a successful response and pass the result back to the controller (date provided as parameter)" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(),
        Matchers.eq(LocalDate.of(2020, 1, 1)),
        Matchers.eq(LocalDate.of(2022, 1, 1)))(any())).thenReturn(Future.successful(Right(CompliancePayloadSuccessResponse(sampleCompliancePayload))))
      val result: Option[CompliancePayload] = await(service.getDESComplianceData(vrn)(HeaderCarrier(),
        User("123456789"), implicitly, Some(LocalDate.of(2022, 1, 1))))
      result.isDefined shouldBe true
      result.get shouldBe sampleCompliancePayload
    }

    s"return a successful response and pass the result back to the controller (date in session)" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(),
        Matchers.eq(LocalDate.of(2020, 1, 1)),
        Matchers.eq(LocalDate.of(2022, 1, 1)))(any())).thenReturn(Future.successful(Right(CompliancePayloadSuccessResponse(sampleCompliancePayload))))
      val result: Option[CompliancePayload] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest.withSession(
        SessionKeys.pocAchievementDate -> "2022-01-01"
      )), implicitly))
      result.isDefined shouldBe true
      result.get shouldBe sampleCompliancePayload
    }

    "return None when the session keys are not present" in new Setup {
      val result: Option[CompliancePayload] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest), implicitly))
      result shouldBe None
    }

    s"return an exception and pass the result back to the controller" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(), any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))
      val result: Exception = intercept[Exception](await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest.withSession(
        SessionKeys.pocAchievementDate -> "2022-01-01"
      )), implicitly)))
      result.getMessage shouldBe "Upstream error"
    }
  }
}
