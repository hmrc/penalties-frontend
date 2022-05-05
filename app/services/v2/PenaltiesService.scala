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

import models.v3.PenaltyDetails

import javax.inject.Inject

class PenaltiesService @Inject()() {

  private def findEstimatedVatInterestFromPayload(payload: PenaltyDetails): BigDecimal = {
    //TODO Add interest mapping once known
    0
  }
  private def findCrystalizedInterestFromPayload(payload: PenaltyDetails): BigDecimal = {
    //TODO Add interest mapping once known
    0
  }

  def findOverdueVATFromPayload(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.penalisedPrincipalTotal).getOrElse(0)
  }

  def findCrystallisedLPPsFromPayload(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPPPostedTotal).getOrElse(0)
  }

  def findEstimatedLPPsFromPayload(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPPEstimatedTotal).getOrElse(0)
  }

  def findTotalLSPFromPayload(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LSPTotalValue).getOrElse(0)
  }

  def findEstimatedVATInterest(payload: PenaltyDetails): (BigDecimal, Boolean) = {
    //TODO add functionality that implements finding estimated VAT interest
    (0, false)
  }

  def isOtherUnrelatedPenalties(payload: PenaltyDetails): Boolean = {
    //TODO add functionality that finds unrelated penalties
    false
  }

  def findCrystalizedPenaltiesInterest(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPIPostedTotal).getOrElse(0)
  }

  def findEstimatedPenaltiesInterest(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPIEstimatedTotal).getOrElse(0)
  }
}
