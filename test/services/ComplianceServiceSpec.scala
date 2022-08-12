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
import connectors.ComplianceConnector
import models.compliance.ComplianceData
import models.{FilingFrequencyEnum, User}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.SessionKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ComplianceServiceSpec extends SpecBase {

  val mockComplianceConnector: ComplianceConnector = mock(classOf[ComplianceConnector])

  class Setup {
    val service: ComplianceService = new ComplianceService(mockComplianceConnector)

    reset(mockComplianceConnector)
  }

  "getDESComplianceData" should {
    s"return a successful response and pass the result back to the controller" in new Setup {
      when(mockComplianceConnector.getComplianceDataFromDES(any(), any(), any())(any())).thenReturn(Future.successful(sampleCompliancePayload))
      val result: Option[ComplianceData] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest.withSession(
        SessionKeys.latestLSPCreationDate -> "2020-01-01",
        SessionKeys.pointsThreshold -> "5"
      )), implicitly))
      val expectedResult = ComplianceData(
        sampleCompliancePayload,
        filingFrequency = FilingFrequencyEnum.monthly
      )
      result.isDefined shouldBe true
      result.get shouldBe expectedResult
    }

    s"return a successful response and pass the result back to the controller (when given a local date for latest LSP creation date)" in new Setup {
      when(mockComplianceConnector.getComplianceDataFromDES(any(), any(), any())(any())).thenReturn(Future.successful(sampleCompliancePayload))
      val result: Option[ComplianceData] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest.withSession(
        SessionKeys.latestLSPCreationDate -> "2020-01-01",
        SessionKeys.pointsThreshold -> "5"
      )), implicitly))
      val expectedResult = ComplianceData(
        sampleCompliancePayload,
        filingFrequency = FilingFrequencyEnum.monthly
      )
      result.isDefined shouldBe true
      result.get shouldBe expectedResult
    }

    "return None when the session keys are not present" in new Setup {
      val result: Option[ComplianceData] = await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest), implicitly))
      result shouldBe None
    }

    s"return an exception and pass the result back to the controller" in new Setup {
      when(mockComplianceConnector.getComplianceDataFromDES(any(), any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))
      val result: Exception = intercept[Exception](await(service.getDESComplianceData(vrn)(HeaderCarrier(), User("123456789")(fakeRequest.withSession(
        SessionKeys.latestLSPCreationDate -> "2020-01-01",
        SessionKeys.pointsThreshold -> "5"
      )), implicitly)))
      result.getMessage shouldBe "Upstream error"
    }
  }
}
