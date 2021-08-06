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

package connectors

import base.SpecBase
import config.AppConfig
import models.compliance.CompliancePayload
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class ComplianceConnectorSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)

    val connector: ComplianceConnector = new ComplianceConnector(mockHttpClient, mockAppConfig)
    when(mockAppConfig.penaltiesUrl).thenReturn("/")
  }

  "getComplianceData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in new Setup {
      when(mockHttpClient.GET[CompliancePayload](any(),
      any(),
      any())
        (any(),
        any(),
        any())).thenReturn(Future.successful(sampleComplianceData))

      val result: CompliancePayload = await(connector.getComplianceData(vrn)(HeaderCarrier()))
      result shouldBe sampleComplianceData
    }
  }

  "return an error when an error occurs upstream" in new Setup {
    when(mockHttpClient.GET[CompliancePayload](any(),
    any(),
    any())
      (any(),
      any(),
      any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", NOT_FOUND)))

    val result: Exception = intercept[Exception](await(connector.getComplianceData("123456789")(HeaderCarrier())))
    result.getMessage shouldBe "Upstream error"
  }
}
