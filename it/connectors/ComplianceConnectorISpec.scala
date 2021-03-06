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

package connectors

import config.AppConfig
import play.api.test.Helpers._
import stubs.ComplianceStub
import stubs.ComplianceStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.HeaderCarrier

class ComplianceConnectorISpec extends IntegrationSpecCommonBase {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val connector: ComplianceConnector = app.injector.instanceOf[ComplianceConnector]

  "getComplianceDataFromDES" should {
    "generate a CompliancePayload when valid JSON is returned from penalties" in {
      ComplianceStub.complianceDataStub()
      val result = await(connector.getComplianceDataFromDES("123456789", startDate, endDate))
      result shouldBe sampleCompliancePayload
    }

    "throw an exception when invalid JSON is returned from penalties" in {
      wireMockServer.editStubMapping(invalidComplianceDataStub())

      val result = intercept[Exception](await(connector.getComplianceDataFromDES("123456789", startDate, endDate)))
      result.getMessage should include("invalid json")
    }

    "throw an exception when an upstream error is returned from penalties" in {
      wireMockServer.editStubMapping(upstreamErrorStub())

      val result = intercept[Exception](await(connector.getComplianceDataFromDES("123456789", startDate, endDate)))
      result.getMessage should include("Upstream Error")
    }
  }
}
