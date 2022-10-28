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

import connectors.PenaltiesConnector
import connectors.httpParsers.PenaltiesConnectorParser.GetPenaltyDetailsResponse
import models.appealInfo.AppealStatusEnum
import models.lsp.{LSPDetails, TaxReturnStatusEnum}
import models.{GetPenaltyDetails, User}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  def getPenaltyDataFromEnrolmentKey(enrolmentKey: String)(implicit user: User[_], hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] =
    connector.getPenaltyDetails(enrolmentKey)

  def findOverdueVATFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.penalisedPrincipalTotal).getOrElse(0)
  }

  def findCrystallisedLPPsFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.LPPPostedTotal).getOrElse(0)
  }

  def findEstimatedLPPsFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.LPPEstimatedTotal).getOrElse(0)
  }

  def findTotalLSPFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.LSPTotalValue).getOrElse(0)
  }

  def findEstimatedVATInterest(payload: GetPenaltyDetails): (BigDecimal, Boolean) = {
    //TODO add functionality that implements finding estimated VAT interest
    (0, false)
  }

  def isOtherUnrelatedPenalties(payload: GetPenaltyDetails): Boolean = {
    //TODO add functionality that finds unrelated penalties
    false
  }

  def findCrystalizedPenaltiesInterest(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.LPIPostedTotal).getOrElse(0)
  }

  def findEstimatedPenaltiesInterest(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.flatMap(_.LPIEstimatedTotal).getOrElse(0)
  }

  def isAnyLSPUnpaidAndSubmissionIsDue(penaltyPoints: Seq[LSPDetails]): Boolean = {
    filterOutAppealedPenalties(penaltyPoints).exists(details => {
      details.chargeOutstandingAmount.exists(_ > BigDecimal(0)) && details.lateSubmissions.exists(_.exists(_.taxReturnStatus == TaxReturnStatusEnum.Open))
    })
  }

  def isAnyLSPUnpaid(penaltyPoints: Seq[LSPDetails]): Boolean = {
    filterOutAppealedPenalties(penaltyPoints).exists(_.chargeOutstandingAmount.exists(_ > BigDecimal(0)))
  }

  private def filterOutAppealedPenalties(penaltyPoints: Seq[LSPDetails]): Seq[LSPDetails] = {
    penaltyPoints
      .filterNot(details => details.appealInformation.exists(_.exists(_.appealStatus.contains(AppealStatusEnum.Upheld))))
  }

  def getRegimeThreshold(payload: GetPenaltyDetails): Int = {
    payload.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0)
  }
}
