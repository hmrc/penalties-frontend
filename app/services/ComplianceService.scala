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

package services

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.PenaltiesConnector
import models.User
import models.compliance.CompliancePayload
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys.POC_ACHIEVEMENT_DATE_NOT_FOUND
import utils.{PagerDutyHelper, SessionKeys}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ComplianceService @Inject() (connector: PenaltiesConnector)(implicit val appConfig: AppConfig) extends FeatureSwitching {

  def getDESComplianceData(vrn: String)(implicit
      hc: HeaderCarrier,
      user: User[_],
      ec: ExecutionContext,
      pocAchievementDate: Option[LocalDate] = None): Future[Option[CompliancePayload]] = {
    val pocAchievementDateFromSession: Option[LocalDate] = user.session.get(SessionKeys.pocAchievementDate).map(LocalDate.parse(_))
    pocAchievementDate.orElse(pocAchievementDateFromSession) match {
      case Some(pocAchievementDate) =>
        val validatedDate = replaceDefaultYearForCurrentYearWhenOngoingObligations(pocAchievementDate)
        val fromDate      = validatedDate.minusYears(2)
        connector.getObligationData(vrn, fromDate, validatedDate).map {
          _.fold(
            failure => {
              logger.error(s"[ComplianceService][getDESComplianceData] - Connector failure: ${failure.message}")
              logger.error(
                "[ComplianceService][getDESComplianceData] - Failed to retrieve obligation data, returning None back to controller (renders ISE)")
              None
            },
            obligationData => {
              logger.debug(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data,  obligation data = $obligationData")
              logger.info(s"[ComplianceService][getDESComplianceData] - Successful call to get obligation data.")
              Some(obligationData.model)
            }
          )
        }
      case _ =>
        logger.error(s"[ComplianceService][getDESComplianceData] - POC Achievement date was not present in session")
        PagerDutyHelper.log("ComplianceService: getDESComplianceData", POC_ACHIEVEMENT_DATE_NOT_FOUND)
        Future.successful(None)
    }
  }

  // HIP replaces the 'None' date value with '9999' which breaks the call to obligations
  private def replaceDefaultYearForCurrentYearWhenOngoingObligations(date: LocalDate): LocalDate =
    if (date.getYear > 9000) LocalDate.now() else date
}
