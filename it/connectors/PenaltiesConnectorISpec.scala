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
import config.featureSwitches.UseAPI1811Model
import connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import models.User
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.PenaltiesStub
import stubs.PenaltiesStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  val vatTraderUser: User[_] = User("1234", active = true, None)(FakeRequest())

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  class Setup(isFSEnabled: Boolean) {
    PenaltiesStub.getPenaltyDetailsStub(isFSEnabled)
    if (isFSEnabled) enableFeatureSwitch(UseAPI1811Model) else disableFeatureSwitch(UseAPI1811Model)
  }

  "getPenaltiesData" should {
    "generate a ETMPPayload when valid JSON is returned from penalties" in {
      val result = await(connector.getPenaltiesData(vrn)(vatTraderUser, implicitly))
      result shouldBe sampleLspData
    }

    "generate a ETMPPayload when valid JSON is returned from penalties with multiple penalty period" in {
      wireMockServer.editStubMapping(lspWithMultiplePenaltyPeriodDataStub())
      val result = await(connector.getPenaltiesData(vrn)(vatTraderUser, implicitly))
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
    "generate a valid PenaltyDetails model when valid JSON is returned" when {
      "UseAPI1811Model feature switch is enabled" in new Setup(isFSEnabled = true) {
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result shouldBe Right(samplePenaltyDetails)
      }

      "UseAPI1811Model feature switch is disabled" in new Setup(isFSEnabled = false) {
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result shouldBe Right(samplePenaltyDetailsNoMetaData)
      }
    }

    s"return $BAD_REQUEST (Bad Request) when invalid JSON is returned" when {
      "UseAPI1811Model feature switch is enabled" in new Setup(isFSEnabled = true) {
        wireMockServer.editStubMapping(invalidPenaltyDetailsStub(true))
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result.isLeft shouldBe true
        result shouldBe Left(InvalidJson)
      }
      "UseAPI1811Model feature switch is disabled" in new Setup(isFSEnabled = false) {
        wireMockServer.editStubMapping(invalidPenaltyDetailsStub())
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result.isLeft shouldBe true
        result shouldBe Left(InvalidJson)
      }
    }

    "throw an exception when an upstream error is returned from penalties" when {
      "UseAPI1811Model feature switch is enabled" in new Setup(isFSEnabled = true) {
        wireMockServer.editStubMapping(penaltyDetailsUpstreamErrorStub(true))
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result.isLeft shouldBe true
        result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
      }
      "UseAPI1811Model feature switch is disabled" in new Setup(isFSEnabled = false) {
        wireMockServer.editStubMapping(penaltyDetailsUpstreamErrorStub())
        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, implicitly))
        result.isLeft shouldBe true
        result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
      }
    }
  }
}
