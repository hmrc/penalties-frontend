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
import models.lsp._
import models.lpp._
import models.{GetPenaltyDetails, Totalisations, User}
import play.api.i18n.Messages
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

  def findInterestOnAccount(totalisations: Option[Totalisations]): BigDecimal = {
    val accruingInterest: BigDecimal = totalisations.flatMap(_.totalAccountAccruingInterest).getOrElse(0)
    val postedInterest: BigDecimal = totalisations.flatMap(_.totalAccountPostedInterest).getOrElse(0)
    accruingInterest + postedInterest
  }

  //TODO remove
  def findEstimatedVATInterest(payload: GetPenaltyDetails): (BigDecimal, Boolean) = {
    (0, false)
  }

  //TODO remove
  def isOtherUnrelatedPenalties(payload: GetPenaltyDetails): Boolean = {
    false
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

  private def filterOutAppealedLPPs(lpps: Seq[LPPDetails]): Seq[LPPDetails] = {
    lpps
      .filterNot(details => details.appealInformation.exists(_.exists(_.appealStatus.contains(AppealStatusEnum.Upheld))))
  }

  def getRegimeThreshold(payload: GetPenaltyDetails): Int = {
    payload.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0)
  }

  //V2 content
  def findUnpaidVATCharges(totalisations: Option[Totalisations]): BigDecimal = {
    totalisations.flatMap(_.totalAccountOverdue).getOrElse(BigDecimal(0))
  }

  def findNumberOfLatePaymentPenalties(optLPPs: Option[LatePaymentPenalty]): Int = {
    //Find LPPs that have not been appealed successfully and are unpaid
    optLPPs.map {
      lpps => {
        val lppsThatHaveNotBeenAppealedSuccessfully = filterOutAppealedLPPs(lpps.details)
        lppsThatHaveNotBeenAppealedSuccessfully.count(details => {
            details.penaltyAmountOutstanding.isDefined &&
            details.penaltyAmountOutstanding.exists(_ > BigDecimal(0))
        })
      }
    }.getOrElse(0)
  }

  def findActiveLateSubmissionPenaltyPoints(lateSubmissionPenalties: Option[LateSubmissionPenalty]): Option[Int] = {
    lateSubmissionPenalties.map(_.summary.activePenaltyPoints)
  }

  def getRegimeThreshold(lateSubmissionPenalties: Option[LateSubmissionPenalty]): Option[Int] = {
    lateSubmissionPenalties.map(_.summary.regimeThreshold)
  }

  def getContentForLSP(amountOfLSPs: Int, regimeThreshold: Int)(implicit messages: Messages): Option[String] = {
    if(amountOfLSPs == 0 || regimeThreshold == 0) None
    else if(amountOfLSPs == 1) Some(messages("whatIsOwed.lsp.one"))
    else if(amountOfLSPs < regimeThreshold) Some(messages("whatIsOwed.lsp.multi", amountOfLSPs))
    //Now only amountOfLSPs >= regimeThreshold is possible
    else Some(messages("whatIsOwed.lsp.max"))
  }
}
