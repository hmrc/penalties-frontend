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

import base.SpecBase
import config.AppConfig
import config.featureSwitches.{FeatureSwitching, UseAPI1811Model}
import connectors.httpParsers.PenaltiesConnectorParser.GetPenaltyDetailsResponse
import models.ETMPPayload
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnectorSpec extends SpecBase with FeatureSwitching {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup(isFSEnabled: Boolean = false) {
    reset(mockHttpClient)
    reset(mockAppConfig)

    val connector: PenaltiesConnector = new PenaltiesConnector(mockHttpClient, mockAppConfig)
    when(mockAppConfig.penaltiesUrl).thenReturn("/")
    if (isFSEnabled) enableFeatureSwitch(UseAPI1811Model) else disableFeatureSwitch(UseAPI1811Model)
  }

  "getPenaltiesData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in new Setup {
      when(mockHttpClient.GET[ETMPPayload](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.successful(sampleEmptyLspData))

      val result: ETMPPayload = await(connector.getPenaltiesData(vrn)(vatTraderUser, HeaderCarrier()))
      result shouldBe sampleEmptyLspData
    }

    "return an error when an error occurs upstream" in new Setup {
    when(mockHttpClient.GET[ETMPPayload](any(),
      any(),
      any())
      (any(),
        any(),
        any()))
      .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

    val result: Exception = intercept[Exception](await(connector.getPenaltiesData("123456789")(vatTraderUser, HeaderCarrier())))
    result.getMessage shouldBe "Upstream error"
  }
  }

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the body can be parsed" when {
      "UseAPI1811Model is enabled" in new Setup(true) {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(), any(), any())
          (any(), any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModel)))

        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, HeaderCarrier()))
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "UseAPI1811Model is disabled" in new Setup() {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(), any(), any())
          (any(), any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModelWithoutMetadata)))

        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, HeaderCarrier()))
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }
    }

    "return an error when an error occurs upstream" in new Setup{
      when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result: Exception = intercept[Exception](await(connector.getPenaltyDetails("123456789")(vatTraderUser, HeaderCarrier())))
      result.getMessage shouldBe "Upstream error"
    }
  }

}
