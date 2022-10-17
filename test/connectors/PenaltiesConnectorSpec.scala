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

import base.{LogCapturing, SpecBase}
import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.httpParsers.PenaltiesConnectorParser.GetPenaltyDetailsResponse
import models.compliance.CompliancePayload
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnectorSpec extends SpecBase with FeatureSwitching with LogCapturing {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)

    val connector: PenaltiesConnector = new PenaltiesConnector(mockHttpClient, mockAppConfig)
    when(mockAppConfig.penaltiesUrl).thenReturn("/")
  }

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the body can be parsed" when {
      "UseAPI1811Model is enabled" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(), any(), any())
          (any(), any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModel)))

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(vrn)(vatTraderUser, HeaderCarrier()))
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "UseAPI1811Model is disabled" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(), any(), any())
          (any(), any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModelWithoutMetadata)))

        val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, HeaderCarrier()))
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }
    }

    "return a Left when" when {
      "an exception with status 4xx occurs upstream" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", BAD_REQUEST)))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails("123456789")(vatTraderUser, HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result.isLeft shouldBe true
          }
        }
      }

      "an exception with status 5xx occurs upstream is returned" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails("123456789")(vatTraderUser, HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result.isLeft shouldBe true
          }
        }
      }

      "an exception is returned" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.failed(new Exception("")))

        withCaptureOfLoggingFrom(logger){
          logs =>
            val result = await(connector.getPenaltyDetails("123456789")(vatTraderUser, HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result.isLeft shouldBe true
        }
      }
    }
  }

  "getObligationData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in new Setup {
      when(mockHttpClient.GET[CompliancePayload](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.successful(sampleCompliancePayload))

      val result: CompliancePayload = await(connector.getObligationData(vrn, LocalDate.of(2020, 1, 1),
        LocalDate.of(2020, 12, 1))(HeaderCarrier()))
      result shouldBe sampleCompliancePayload
    }

    "return an error when an error occurs upstream" in new Setup {
      when(mockHttpClient.GET[CompliancePayload](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", NOT_FOUND)))

      val result: Exception = intercept[Exception](await(connector.getObligationData(vrn, LocalDate.of(2020, 1, 1),
        LocalDate.of(2020, 12, 1))(HeaderCarrier())))
      result.getMessage shouldBe "Upstream error"
    }
  }
}
