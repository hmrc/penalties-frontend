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
import models.ETMPPayload
import models.v3.PenaltyDetails

import javax.inject.Inject

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  private def findEstimatedVatInterestFromPayload(payload: ETMPPayload): BigDecimal = {
    payload.vatOverview.map {
      estimatedVATInterest => {
        estimatedVATInterest.map(_.estimatedInterest.getOrElse(BigDecimal(0))).sum
      }
    }
  }.getOrElse(0)

  private def findCrystalizedInterestFromPayload(payload: ETMPPayload): BigDecimal = {
    payload.vatOverview.map {
      crystalizedInterest => {
        crystalizedInterest.map(_.crystalizedInterest.getOrElse(BigDecimal(0))).sum
      }
    }
  }.getOrElse(0)

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

//  def findEstimatedVATInterest(payload: ETMPPayload): (BigDecimal, Boolean) = {
//    val estimatedVAT = findEstimatedVatInterestFromPayload(payload)
//    val crystallisedVAT = findCrystalizedInterestFromPayload(payload)
//    (crystallisedVAT + estimatedVAT, estimatedVAT > 0)
//  }

  def findCrystalizedPenaltiesInterest(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPIPostedTotal).getOrElse(0)
  }

  def findEstimatedPenaltiesInterest(payload: PenaltyDetails): BigDecimal = {
    payload.totalisations.map(_.LPIEstimatedTotal).getOrElse(0)
  }
}
