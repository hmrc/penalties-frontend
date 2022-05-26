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
import models.FilingFrequencyEnum._
import models.compliance.{ComplianceData, ComplianceStatusEnum}
import models.{FilingFrequencyEnum, User}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.SessionKeys

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ComplianceService @Inject()(connector: ComplianceConnector)(implicit val appConfig: AppConfig) extends FeatureSwitching {

  def getDESComplianceData(vrn: String)(implicit hc: HeaderCarrier, user: User[_],
                                        ec: ExecutionContext): Future[Option[ComplianceData]] = {
    (user.session.get(SessionKeys.latestLSPCreationDate), user.session.get(SessionKeys.pointsThreshold)) match {
      case (Some(lspCreationDateAsString), Some(pointsThresholdAsString)) => {
        val lspCreationDate: LocalDate = {
          Try(LocalDate.parse(lspCreationDateAsString))
            .getOrElse(LocalDateTime.parse(lspCreationDateAsString).toLocalDate)
        }
        val fromDate = lspCreationDate.minusYears(2)
        val filingFrequency: FilingFrequencyEnum.Value = getFilingFrequency(pointsThresholdAsString)
        val toDate: LocalDate = getToDateFromFilingFrequency(lspCreationDate, filingFrequency)
        connector.getComplianceDataFromDES(vrn, fromDate, toDate).map {
          complianceData => {
            val amountOfFulfilledSubmissionsInPast2Years: Int = complianceData.obligationDetails.count(obligation =>
              obligation.status == ComplianceStatusEnum.fulfilled && obligation.inboundCorrespondenceToDate.isBefore(LocalDate.now()))
            logger.debug(s"[ComplianceService][getDESComplianceData] - Amount of fulfilled submissions in " +
              s"the past 2 years: $amountOfFulfilledSubmissionsInPast2Years")
            val submissionsNeededForMonthlyFiler: Int = (amountOfFulfilledSubmissionsInPast2Years + 6) - 24
            val amountOfSubmissionsNeededFor24MonthHistory = {
              if (submissionsNeededForMonthlyFiler >= 0) None
              else Some(Math.abs(submissionsNeededForMonthlyFiler))
            }
            logger.debug(s"[ComplianceService][getDESComplianceData] - Amount of submissions needed " +
              s"for 2 year filing history: $amountOfSubmissionsNeededFor24MonthHistory")
            Some(ComplianceData(complianceData, amountOfSubmissionsNeededFor24MonthHistory, filingFrequency))
          }
        }
      }
      case _ => {
        logger.error(s"[ComplianceService][getDESComplianceData] - Some/all session keys were not present: latest LSP creation date defined: ${
          user.session.get(SessionKeys.latestLSPCreationDate).isDefined
        } - points threshold defined: ${
          user.session.get(SessionKeys.pointsThreshold).isDefined
        }")
        Future.successful(None)
      }
    }
  }

  private def getToDateFromFilingFrequency(lspCreationDate: LocalDate, filingFrequency: FilingFrequencyEnum.Value): LocalDate = {
    (filingFrequency match {
      case FilingFrequencyEnum.annually => lspCreationDate.plusYears(2)
      case FilingFrequencyEnum.quarterly => lspCreationDate.plusYears(1)
      case FilingFrequencyEnum.monthly => lspCreationDate.plusMonths(6)
    })
  }

  private def getFilingFrequency(pointsThreshold: String): FilingFrequencyEnum.Value = {
    pointsThreshold match {
      case "5" => monthly
      case "4" => quarterly
      case "2" => annually
    }
  }
}
