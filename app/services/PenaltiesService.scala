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

package services

import connectors.PenaltiesConnector
import models.{ETMPPayload, User}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  def getETMPDataFromEnrolmentKey(enrolmentKey: String)(implicit user: User[_], hc: HeaderCarrier): Future[ETMPPayload] = connector.getPenaltiesData(enrolmentKey)

  def isAnyLSPUnpaid(penaltyPoints: Seq[PenaltyPoint]): Boolean = {
    penaltyPoints.exists(penalty => penalty.`type` == PenaltyTypeEnum.Financial &&
      penalty.status != PointStatusEnum.Paid && !penalty.appealStatus.contains(AppealStatusEnum.Accepted) && !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal))
  }

  def isAnyLSPUnpaidAndSubmissionIsDue(penaltyPoints: Seq[PenaltyPoint]): Boolean = {
    penaltyPoints.exists(penalty => penalty.status == PointStatusEnum.Due &&
      penalty.`type` == PenaltyTypeEnum.Financial &&
      penalty.period.isDefined && penalty.period.get.submission.submittedDate.isEmpty && !penalty.appealStatus.contains(AppealStatusEnum.Accepted) && !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal))
  }

  def isAnyVATUnpaid(penaltyPoints: Seq[PenaltyPoint]): Boolean = {
    penaltyPoints.exists(penalty =>
      penalty.status == PointStatusEnum.Due &&
        penalty.period.isDefined && penalty.period.get.submission.submittedDate.isEmpty &&
        !penalty.appealStatus.contains(AppealStatusEnum.Accepted) && !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal)
    )
  }

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

  def findOverdueVATFromPayload(payload: ETMPPayload): BigDecimal = {
    payload.vatOverview.map {
      allCharges => {
        allCharges.map(_.amount).sum
      }
    }
  }.getOrElse(0)

  def isOtherUnrelatedPenalties(payload: ETMPPayload): Boolean = {
    payload.otherPenalties.contains(true)
  }

  def findEstimatedLPPsFromPayload(payload: ETMPPayload): (BigDecimal, Boolean) = {
    payload.latePaymentPenalties.map {
      allLPPs => {
        val allAdditionalPoints = allLPPs.filter(_.`type` == PenaltyTypeEnum.Additional)
        val allFinancialPoints = allLPPs.filter(_.`type` == PenaltyTypeEnum.Financial)
        val estimatedLPPs = allAdditionalPoints.map(_.financial.outstandingAmountDue).sum
        val crystallisedLPPs = allFinancialPoints.map(_.financial.outstandingAmountDue).sum
        val isEstimatesIncluded = estimatedLPPs > BigDecimal(0)
        (crystallisedLPPs + estimatedLPPs, isEstimatesIncluded)
      }
    }
  }.getOrElse((0, false))

  def findTotalLSPFromPayload(payload: ETMPPayload): BigDecimal = {
    payload.penaltyPoints.map(_.financial.map(_.outstandingAmountDue)).collect { case Some(x) => x }.sum
  }

  def findEstimatedVATInterest(payload: ETMPPayload): (BigDecimal, Boolean) = {
    val estimatedVAT = findEstimatedVatInterestFromPayload(payload)
    val crystallisedVAT = findCrystalizedInterestFromPayload(payload)
    (crystallisedVAT + estimatedVAT, estimatedVAT > 0)
  }

  def findCrystalizedPenaltiesInterest(payload: ETMPPayload): BigDecimal = {
    val lspInterest: BigDecimal = payload.penaltyPoints.flatMap(
      penalty => {
        penalty.financial.map(_.crystalizedInterest.getOrElse(BigDecimal(0)))
      }
    ).sum

    val lppInterest: BigDecimal = payload.latePaymentPenalties.map {
      _.map { lpp =>
        lpp.financial.crystalizedInterest.getOrElse(BigDecimal(0))
      }.sum
    }.getOrElse(BigDecimal(0))
    lspInterest + lppInterest
  }

  def findEstimatedPenaltiesInterest(payload: ETMPPayload): BigDecimal = {
    val estimatedLspInterest: BigDecimal = payload.penaltyPoints.flatMap(
      _.financial.map(_.estimatedInterest.getOrElse(BigDecimal(0)))
    ).sum

    val estimatedLppInterest: BigDecimal = payload.latePaymentPenalties.map {
      _.map { lpp =>
        lpp.financial.estimatedInterest.getOrElse(BigDecimal(0))
      }.sum
    }.getOrElse(BigDecimal(0))
    estimatedLspInterest + estimatedLppInterest
  }

}
