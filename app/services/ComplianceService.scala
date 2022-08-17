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

package services

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.ComplianceConnector
import models.User
import models.compliance.CompliancePayload
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.SessionKeys

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ComplianceService @Inject()(connector: ComplianceConnector)(implicit val appConfig: AppConfig) extends FeatureSwitching {

  def getDESComplianceData(vrn: String)(implicit hc: HeaderCarrier, user: User[_],
                                        ec: ExecutionContext, pocAchievementDate: Option[LocalDate] = None): Future[Option[CompliancePayload]] = {
    val pocAchievementDateFromSession: Option[LocalDate] = user.session.get(SessionKeys.pocAchievementDate).map(LocalDate.parse(_))
    pocAchievementDate.orElse(pocAchievementDateFromSession) match {
      case Some(pocAchievementDate) => {
        val fromDate = pocAchievementDate.minusYears(2)
        connector.getComplianceDataFromDES(vrn, fromDate, pocAchievementDate).map {
          complianceData => {
            logger.debug(s"[ComplianceService][getDESComplianceData] - Compliance Data  $complianceData")
            Some(complianceData)
          }
        }
      }
      case _ => {
        logger.error(s"[ComplianceService][getDESComplianceData] - POC Achievement date was not present in session")
        Future.successful(None)
      }
    }
  }
}
