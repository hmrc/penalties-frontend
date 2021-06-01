/*
 * Copyright 2021 HM Revenue & Customs
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
import javax.inject.Inject
import models.compliance.CompliancePayload
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class ComplianceConnector @Inject()(httpClient: HttpClient,
                                   appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val penaltiesBaseUrl: String = appConfig.penaltiesUrl
  private def getComplianceDataUrl(enrolmentKey: String): String = s"/compliance/compliance-data?enrolmentKey=$enrolmentKey"

  def getComplianceData(enrolmentKey: String)(implicit hc: HeaderCarrier): Future[CompliancePayload] = {
    httpClient.GET[CompliancePayload](s"$penaltiesBaseUrl${getComplianceDataUrl(enrolmentKey)}")
  }
}