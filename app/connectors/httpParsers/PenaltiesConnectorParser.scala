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
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._
import play.api.libs.json.JsSuccess
import utils.Logger.logger

object PenaltiesConnectorParser {
  type GetPenaltyDetailsResponse = Either[ErrorResponse, GetPenaltyDetails]

  implicit object GetPenaltyDetailsResponseReads extends HttpReads[GetPenaltyDetailsResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetPenaltyDetailsResponse = {
      response.status match {
        case OK =>
          response.json.validate[GetPenaltyDetails](GetPenaltyDetails.format) match {
            case JsSuccess(model, _) => Right(model)
            case _ => Left(InvalidJson)
          }
        case NO_CONTENT =>
          logger.debug(s"[GetPenaltyDetailsResponseReads][read]: No content found for VRN provided, returning empty model")
          Right(GetPenaltyDetails(None, None, None))
        case BAD_REQUEST =>
          logger.debug(s"[GetPenaltyDetailsResponseReads][read]: Bad request returned with reason: ${response.body}")
          Left(BadRequest)
        case status => logger.warn(s"[GetPenaltyDetailsResponseReads][read]: Unexpected response, status $status returned with reason: ${response.body}")
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
    }
  }
}
