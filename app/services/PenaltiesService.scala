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
import models.ETMPPayload
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  def getLspDataWithVrn(enrolmentKey: String)(implicit hc: HeaderCarrier): Future[ETMPPayload] = connector.getPenaltiesData(enrolmentKey)

  def isAnyLSPUnpaid(penaltyPoints: Seq[PenaltyPoint]): Boolean = {
    penaltyPoints.exists(penalty => penalty.`type` == PenaltyTypeEnum.Financial &&
      penalty.status != PointStatusEnum.Paid && !penalty.appealStatus.contains(AppealStatusEnum.Accepted) && !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal))
  }

  def isAnyLSPUnpaidAndSubmissionIsDue(penaltyPoints: Seq[PenaltyPoint]): Boolean = {
    penaltyPoints.exists(penalty => penalty.status == PointStatusEnum.Due &&
      penalty.`type` == PenaltyTypeEnum.Financial &&
      penalty.period.isDefined && penalty.period.get.submission.submittedDate.isEmpty && !penalty.appealStatus.contains(AppealStatusEnum.Accepted) && !penalty.appealStatus.contains(AppealStatusEnum.Accepted_By_Tribunal))
  }

  def findOverdueVATFromPayload(payload: ETMPPayload): BigDecimal = {
    payload.vatOverview.map {
      allCharges => {
        allCharges.map(_.amount).foldRight(BigDecimal(0))(_ + _)
      }
    }
  }.getOrElse(0)

  def isOtherUnrelatedPenalties(payload: ETMPPayload): Boolean = {
    payload.otherPenalties.contains(true)
  }
}
