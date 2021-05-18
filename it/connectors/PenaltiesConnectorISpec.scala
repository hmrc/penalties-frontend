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


import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.test.Helpers._
import stubs.PenaltiesStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {

  implicit private lazy val hc = HeaderCarrier()

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  "getPenaltiesData" should {
    "generate a ETMPPayload when valid JSON is returned from penalties" in {
      val result = connector.getPenaltiesData(vrn).futureValue
      result shouldBe sampleLspData
    }

    "throw an exception when invalid JSON is returned from penalties" in {
      wireMockServer.editStubMapping(invalidLspDataStub())

      val result = intercept[Exception](await(connector.getPenaltiesData(vrn)))
      result.getMessage should include("invalid json")
    }

    "throw an exception when an upstream error is returned from penalties" in {
      wireMockServer.editStubMapping(upstreamErrorStub())

      val result = intercept[Exception](await(connector.getPenaltiesData(vrn)))
      result.getMessage should include("Upstream Error")
    }
  }
}
