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

package utils

import models.appealInfo.AppealStatusEnum
import models.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPTypeEnum}

object LSPTypeHelper {

  def determineLSPType(penalty: LSPDetails): Option[LSPTypeEnum.Value] = {
        (penalty.penaltyCategory, penalty.appealInformation.flatMap(_.head.appealStatus)) match {
          case (Some(LSPPenaltyCategoryEnum.Threshold), _) => Some(LSPTypeEnum.Financial)
          case (Some(LSPPenaltyCategoryEnum.Charge), _) => Some(LSPTypeEnum.Financial)
          case (_, Some(AppealStatusEnum.Upheld)) if penalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive => Some(LSPTypeEnum.AppealedPoint)
          case (_, _) if penalty.FAPIndicator.contains("X") => if(penalty.penaltyStatus == LSPPenaltyStatusEnum.Active) Some(LSPTypeEnum.AddedFAP) else Some(LSPTypeEnum.RemovedFAP)
          case (_, _) if penalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive && penalty.expiryReason.isDefined => Some(LSPTypeEnum.RemovedPoint)
          case (_, _) => Some(LSPTypeEnum.Point)
          case _ => None
        }
      }
}