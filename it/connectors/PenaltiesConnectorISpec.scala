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


import connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import models.User
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.PenaltiesStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  val vatTraderUser: User[_] = User("1234", active = true, None)(FakeRequest())

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  "getPenaltiesData" should {
    "generate a ETMPPayload when valid JSON is returned from penalties" in {
      val result = connector.getPenaltiesData(vrn)(vatTraderUser, implicitly).futureValue
      result shouldBe sampleLspData
    }

    "generate a ETMPPayload when valid JSON is returned from penalties with multiple penalty period" in {
      wireMockServer.editStubMapping(lspWithMultiplePenaltyPeriodDataStub())
      val result = connector.getPenaltiesData(vrn)(vatTraderUser, implicitly).futureValue
      result shouldBe sampleLspDataWithMultiplePenaltyPeriod
    }

    "throw an exception when invalid JSON is returned from penalties" in {
      wireMockServer.editStubMapping(invalidLspDataStub())

      val result = intercept[Exception](await(connector.getPenaltiesData(vrn)(vatTraderUser, implicitly)))
      result.getMessage should include("invalid json")
    }

    "throw an exception when an upstream error is returned from penalties" in {
      wireMockServer.editStubMapping(upstreamErrorStub())

      val result = intercept[Exception](await(connector.getPenaltiesData(vrn)(vatTraderUser, implicitly)))
      result.getMessage should include("Upstream Error")
    }
  }

  "getPenaltyDetails" should {
    "generate a valid PenaltyDetails model when valid JSON is returned" in {
      val result = connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly).futureValue
      result shouldBe Right(samplePenaltyDetails)
    }

    s"return $BAD_REQUEST (Bad Request) when invalid JSON is returned" in {
      wireMockServer.editStubMapping(invalidPenaltyDetailsStub())
      val result = connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly).futureValue
      result.isLeft shouldBe true
      result shouldBe Left(InvalidJson)
    }

    "throw an exception when an upstream error is returned from penalties" in {
      wireMockServer.editStubMapping(penaltyDetailsUpstreamErrorStub())
      val result = connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly).futureValue
      result.isLeft shouldBe true
      result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
    }
  }
}
