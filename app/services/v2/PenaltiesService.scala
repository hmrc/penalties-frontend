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

package services.v2

import connectors.PenaltiesConnector
import models.User
import models.v3.GetPenaltyDetails
import models.v3.lsp.LSPDetails
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  def getPenaltyDataFromEnrolmentKey(enrolmentKey: String)(implicit user: User[_], hc: HeaderCarrier): Future[GetPenaltyDetails] =
    connector.getPenaltyDetails(enrolmentKey)

  private def findEstimatedVatInterestFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    //TODO Add interest mapping once known
    0
  }
  private def findCrystalizedInterestFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    //TODO Add interest mapping once known
    0
  }

  def findOverdueVATFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.penalisedPrincipalTotal).getOrElse(0)
  }

  def findCrystallisedLPPsFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPPPostedTotal).getOrElse(0)
  }

  def findEstimatedLPPsFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPPEstimatedTotal).getOrElse(0)
  }

  def findTotalLSPFromPayload(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LSPTotalValue).getOrElse(0)
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
    payload.totalisations.map(_.LPIPostedTotal).getOrElse(0)
  }

  def findEstimatedPenaltiesInterest(payload: GetPenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPIEstimatedTotal).getOrElse(0)
  }
  //due point for lsp , has penalty period, not submitted, not appealed
  def isAnyLSPUnpaidAndSubmissionIsDue(penaltyPoints: Seq[LSPDetails]): Boolean = ???

  def isAnyLSPUnpaid(penaltyPoints: Seq[LSPDetails]): Boolean = ???

  def getLatestLSPCreationDate(payload: Seq[LSPDetails]): Option[LocalDate] = ???
}
