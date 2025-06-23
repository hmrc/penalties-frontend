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

package connectors

import base.{LogCapturing, SpecBase}
import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.httpParsers.ComplianceDataParser._
import connectors.httpParsers.GetPenaltyDetailsParser.GetPenaltyDetailsResponse
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys
import models.{Id, IdType, Regime}
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

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(Regime.VATC, IdType.VRN, Id(vrn))(vatTraderUser, HeaderCarrier()))
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "UseAPI1811Model is disabled" in new Setup {
        when(mockHttpClient.GET[GetPenaltyDetailsResponse](any(), any(), any())
          (any(), any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModelWithoutMetadata)))

        val result = await(connector.getPenaltyDetails(Regime.VATC, IdType.VRN, Id(vrn))(vatTraderUser, HeaderCarrier()))
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
            val result = await(connector.getPenaltyDetails(Regime.VATC, IdType.VRN, Id("123456789"))(vatTraderUser, HeaderCarrier()))
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
            val result = await(connector.getPenaltyDetails(Regime.VATC, IdType.VRN, Id("123456789"))(vatTraderUser, HeaderCarrier()))
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
            val result = await(connector.getPenaltyDetails(Regime.VATC, IdType.VRN, Id("123456789"))(vatTraderUser, HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result.isLeft shouldBe true
        }
      }
    }
  }

  "getObligationData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in new Setup {
      when(mockHttpClient.GET[CompliancePayloadResponse](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.successful(Right(CompliancePayloadSuccessResponse(sampleCompliancePayload))))

      val result: CompliancePayloadResponse = await(connector.getObligationData(vrn, LocalDate.of(2020, 1, 1),
        LocalDate.of(2020, 12, 1))(HeaderCarrier()))
      result.isRight shouldBe true
      result.toOption.get.model shouldBe sampleCompliancePayload
    }

    "return a Left response" when {
      "the call returns a OK response however the body is not parsable as a model" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.successful(Left(CompliancePayloadMalformed)))
        val result: CompliancePayloadResponse =
          await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 1))(HeaderCarrier()))
        result.isLeft shouldBe true
      }

      "the call returns a Not Found status" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.successful(Left(CompliancePayloadNoData)))
        val result: CompliancePayloadResponse =
          await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 1))(HeaderCarrier()))
        result.isLeft shouldBe true
      }

      "the call returns a ISE" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.successful(Left(CompliancePayloadFailureResponse(INTERNAL_SERVER_ERROR))))
        val result: CompliancePayloadResponse =
          await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 1))(HeaderCarrier()))
        result.isLeft shouldBe true
      }

      "the call returns an unmatched response" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.successful(Left(CompliancePayloadFailureResponse(SERVICE_UNAVAILABLE))))
        val result: CompliancePayloadResponse =
          await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 1))(HeaderCarrier()))
        result.isLeft shouldBe true
      }

      "the call returns a UpstreamErrorResponse(4xx) exception" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("", BAD_REQUEST)))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: CompliancePayloadResponse =
              await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 1))(HeaderCarrier()))
            result.isLeft shouldBe true
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }

      "the call returns a UpstreamErrorResponse(5xx) exception" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("", INTERNAL_SERVER_ERROR)))
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: CompliancePayloadResponse =
              await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 1))(HeaderCarrier()))
            result.isLeft shouldBe true
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }

      "the call returns an exception" in new Setup {
        when(mockHttpClient.GET[CompliancePayloadResponse](any(),
          any(),
          any())
          (any(),
            any(),
            any()))
          .thenReturn(Future.failed(new Exception("failed")))
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: CompliancePayloadResponse =
              await(connector.getObligationData("123456789", LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 1))(HeaderCarrier()))
            result.isLeft shouldBe true
            logs.exists(_.getMessage.contains(PagerDutyKeys.UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }
    }
  }
}
