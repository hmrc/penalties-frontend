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

package connectors.httpParsers

import models.GetPenaltyDetails
import play.api.http.Status._
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys._

object PenaltiesConnectorParser {
  type GetPenaltyDetailsResponse = Either[ErrorResponse, GetPenaltyDetails]

  implicit object GetPenaltyDetailsResponseReads extends HttpReads[GetPenaltyDetailsResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetPenaltyDetailsResponse = {
      response.status match {
        case OK =>
          response.json.validate[GetPenaltyDetails](GetPenaltyDetails.format) match {
            case JsSuccess(model, _) =>
              logger.info("[GetPenaltyDetailsResponseReads][read]: Successful call to retrieve penalties details.")
              Right(model)
            case failure => {
              logger.debug(s"[GetPenaltyDetailsResponseReads][read]: Failed to parse to model - failures: $failure")
              logger.error("[GetPenaltyDetailsResponseReads][read]: Failed to parse to model")
              PagerDutyHelper.log("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND)
              Left(InvalidJson)
            }
          }
        case NO_CONTENT =>
          logger.info(s"[GetPenaltyDetailsResponseReads][read]: No content found for VRN provided, returning empty model")
          Right(GetPenaltyDetails(None, None, None))
        case BAD_REQUEST =>
          logger.error(s"[GetPenaltyDetailsResponseReads][read]: Bad request returned with reason: ${response.body}")
          PagerDutyHelper.log("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", RECEIVED_4XX_FROM_PENALTIES_BACKEND)
          Left(BadRequest)
        case status => logger.error(s"[GetPenaltyDetailsResponseReads][read]: Unexpected response, status $status returned with reason: ${response.body}")
          PagerDutyHelper.logStatusCode("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", status)(
            RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
    }
  }
}
