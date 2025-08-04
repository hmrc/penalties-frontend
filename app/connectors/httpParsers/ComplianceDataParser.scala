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

import models.compliance.CompliancePayload
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys._

object ComplianceDataParser {
  sealed trait GetCompliancePayloadFailure {
    val message: String
  }
  sealed trait GetCompliancePayloadSuccess {
    val model: CompliancePayload
  }

  case class CompliancePayloadSuccessResponse(model: CompliancePayload) extends GetCompliancePayloadSuccess
  case class CompliancePayloadFailureResponse(status: Int) extends GetCompliancePayloadFailure {
    override val message: String = s"Received status code: $status"
  }
  case object CompliancePayloadNoData extends GetCompliancePayloadFailure {
    override val message: String = "Received no data from call"
  }
  case object CompliancePayloadMalformed extends GetCompliancePayloadFailure {
    override val message: String = "Body received was malformed"
  }

  type CompliancePayloadResponse = Either[GetCompliancePayloadFailure, GetCompliancePayloadSuccess]

  implicit object CompliancePayloadReads extends HttpReads[CompliancePayloadResponse] {
    override def read(method: String, url: String, response: HttpResponse): CompliancePayloadResponse = {
      response.status match {
        case OK =>
          logger.debug(s"[CompliancePayloadReads][read] Json response: ${response.json}")
          response.json.validate[CompliancePayload](CompliancePayload.format) match {
            case JsSuccess(compliancePayload, _) =>
              Right(CompliancePayloadSuccessResponse(compliancePayload))
            case JsError(errors) =>
              PagerDutyHelper.log("CompliancePayloadReads", INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND)
              logger.error(s"[CompliancePayloadReads][read] Json validation errors: $errors")
              Left(CompliancePayloadMalformed)
          }
        case NOT_FOUND => {
          logger.info(s"[CompliancePayloadReads][read] - Received not found response from backend for API 1330 data." +
            s" No data associated with VRN. Body: ${response.body}")
          Left(CompliancePayloadNoData)
        }
        case BAD_REQUEST => {
          PagerDutyHelper.log("CompliancePayloadReads", RECEIVED_4XX_FROM_PENALTIES_BACKEND)
          logger.error(s"[CompliancePayloadReads][read] - Failed to parse to model with response body: ${response.body} (Status: $BAD_REQUEST)")
          Left(CompliancePayloadFailureResponse(BAD_REQUEST))
        }
        case INTERNAL_SERVER_ERROR =>
          PagerDutyHelper.log("CompliancePayloadReads", RECEIVED_5XX_FROM_PENALTIES_BACKEND)
          logger.error(s"[CompliancePayloadReads][read] Received ISE when calling backend for API 1330 data - with body: ${response.body}")
          Left(CompliancePayloadFailureResponse(INTERNAL_SERVER_ERROR))
        case _@status =>
          PagerDutyHelper.logStatusCode("CompliancePayloadReads", status)(RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
          logger.error(s"[CompliancePayloadReads][read] Received unexpected response when calling backend for API 1330 data," +
            s" status code: $status and body: ${response.body}")
          Left(CompliancePayloadFailureResponse(status))
      }
    }
  }
}

