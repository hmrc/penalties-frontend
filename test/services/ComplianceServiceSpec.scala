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
import connectors.ComplianceConnector
import models.compliance.CompliancePayload
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

class ComplianceServiceSpec extends SpecBase {

  val mockComplianceConnector: ComplianceConnector = mock(classOf[ComplianceConnector])

  class Setup {
    val service: ComplianceService = new ComplianceService(mockComplianceConnector)

    reset(mockComplianceConnector)
  }

  "getComplianceDataWithVrn" should {
    s"return a successful response and pass the result back to the controller" in new Setup {

      when(mockComplianceConnector.getComplianceData(any())(any())).thenReturn(Future.successful(sampleComplianceData))

      val result: CompliancePayload = await(service.getComplianceDataWithEnrolmentKey(vrn)(HeaderCarrier()))

      result shouldBe sampleComplianceData
    }

    s"return an exception and pass the result back to the controller" in new Setup {

      when(mockComplianceConnector.getComplianceData(any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result: Exception = intercept[Exception](await(service.getComplianceDataWithEnrolmentKey(vrn)(HeaderCarrier())))

      result.getMessage shouldBe "Upstream error"
    }
  }
}
