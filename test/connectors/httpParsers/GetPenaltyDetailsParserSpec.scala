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

package connectors.httpParsers

import base.LogCapturing
import models.GetPenaltyDetails
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys

class GetPenaltyDetailsParserSpec extends AnyWordSpec with Matchers with LogCapturing {

  val mockGetPenaltyDetailsModel: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val mockOKHttpResponseWithValidBody: HttpResponse = HttpResponse.apply(status = Status.OK, json = Json.toJson(mockGetPenaltyDetailsModel), headers = Map.empty)

  val mockOKHttpResponseWithInvalidBody: HttpResponse =
    HttpResponse.apply(status = Status.OK, json = Json.parse(
      """
        |{
        | "lateSubmissionPenalty": {
        |   "summary": {
        |     "activePenaltyPoints": 1
        |     }
        |   }
        | }
        |""".stripMargin
    ), headers = Map.empty)

  val mockISEHttpResponse: HttpResponse = HttpResponse.apply(status = Status.INTERNAL_SERVER_ERROR, body = "Something went wrong.")
  val mockBadRequestHttpResponse: HttpResponse = HttpResponse.apply(status = Status.BAD_REQUEST, body = "Bad Request.")
  val mockNoContentHttpResponse: HttpResponse = HttpResponse.apply(status = Status.NO_CONTENT, body = "")

  val mockImATeapotHttpResponse: HttpResponse = HttpResponse.apply(status = Status.IM_A_TEAPOT, body = "I'm a teapot.")

  "GetPenaltyDetailsResponseReads" should {
    s"parse an OK (${Status.OK}) response" when {
      s"the body of the response is valid" in {
        val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockOKHttpResponseWithValidBody)
        result.isRight shouldBe true
        result.getOrElse(None) shouldBe mockGetPenaltyDetailsModel
      }

      s"the body is malformed - returning a $Left $InvalidJson" in {
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockOKHttpResponseWithInvalidBody)
            logs.exists(_.getMessage.contains(PagerDutyKeys.INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result.isLeft shouldBe true
          }
        }
      }
    }

    s"parse a BAD REQUEST (${Status.BAD_REQUEST}) response - logging a PagerDuty" in {
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockBadRequestHttpResponse)
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          result.isLeft shouldBe true
          result.left.getOrElse(UnexpectedFailure(IM_A_TEAPOT, "")) shouldBe BadRequest
        }
      }
    }

    s"parse a NO CONTENT (${Status.NO_CONTENT}) response" in {
      val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockNoContentHttpResponse)
      result.isRight shouldBe true
      result.getOrElse(UnexpectedFailure(IM_A_TEAPOT, "")) shouldBe mockGetPenaltyDetailsModel
    }

    s"parse an unknown error (e.g. IM A TEAPOT - ${Status.IM_A_TEAPOT}) - and log a PagerDuty" in {
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockImATeapotHttpResponse)
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          result.isLeft shouldBe true
          result.left.getOrElse(UnexpectedFailure(INTERNAL_SERVER_ERROR, "")) shouldBe UnexpectedFailure(Status.IM_A_TEAPOT, s"Unexpected response, status ${Status.IM_A_TEAPOT} returned")
        }
      }
    }

    s"parse an unknown error (e.g. ISE - ${Status.INTERNAL_SERVER_ERROR}) - and log a PagerDuty" in {
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result = GetPenaltyDetailsParser.GetPenaltyDetailsResponseReads.read("GET", "/", mockISEHttpResponse)
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          result.isLeft shouldBe true
          result.left.getOrElse(UnexpectedFailure(IM_A_TEAPOT, "")) shouldBe UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, s"Unexpected response, status ${Status.INTERNAL_SERVER_ERROR} returned")
        }
      }
    }
  }
}
