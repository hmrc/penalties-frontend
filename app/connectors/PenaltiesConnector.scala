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
import config.featureSwitches.FeatureSwitching
import connectors.httpParsers.PenaltiesConnectorParser.{GetPenaltyDetailsResponse, GetPenaltyDetailsResponseReads}
import models.User
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   val appConfig: AppConfig)(implicit ec: ExecutionContext) extends FeatureSwitching {


  private val penaltiesBaseUrl: String = appConfig.penaltiesUrl
  private def getPenaltiesDataUrl(enrolmentKey: String)(implicit user: User[_]): String = {
    //TODO Remove Query Params
    val urlQueryParams = user.arn.fold(
      ""
    )(
      arn => s"?arn=$arn"
    )
    s"/etmp/penalties/$enrolmentKey$urlQueryParams"
  }

  def getPenaltyDetails(enrolmentKey: String)(implicit user: User[_], hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] = {
    httpClient.GET[GetPenaltyDetailsResponse](s"$penaltiesBaseUrl${getPenaltiesDataUrl(enrolmentKey)}")(GetPenaltyDetailsResponseReads, hc, ec)
  }
}
