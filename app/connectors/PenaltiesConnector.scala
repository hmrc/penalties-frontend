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
import connectors.httpParsers.PenaltiesConnectorParser.{GetPenaltyDetailsResponse, GetPenaltyDetailsResponseReads}
import models.v3.GetPenaltyDetails
import models.{ETMPPayload, User}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val penaltiesBaseUrl: String = appConfig.penaltiesUrl
  private def getPenaltiesDataUrl(enrolmentKey: String, isUsingNewApi: Boolean)(implicit user: User[_]): String = {
    val urlQueryParams = user.arn.fold(s"?newApiModel=$isUsingNewApi")(arn => s"?arn=$arn&newApiModel=$isUsingNewApi")
    s"/etmp/penalties/$enrolmentKey$urlQueryParams"
  }

  def getPenaltiesData(enrolmentKey: String, isUsingNewApi: Boolean = false)(implicit user: User[_], hc: HeaderCarrier): Future[ETMPPayload] = {
    httpClient.GET[ETMPPayload](s"$penaltiesBaseUrl${getPenaltiesDataUrl(enrolmentKey, isUsingNewApi)}")
  }

  def getPenaltyDetails(enrolmentKey: String, isUsingNewApi: Boolean = true)(implicit user: User[_], hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] = {
    httpClient.GET[GetPenaltyDetailsResponse](s"$penaltiesBaseUrl${getPenaltiesDataUrl(enrolmentKey, isUsingNewApi)}")(GetPenaltyDetailsResponseReads, hc, ec)
  }
}
