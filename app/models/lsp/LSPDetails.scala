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

package models.lsp

import java.time.LocalDate

import models.appealInfo.AppealInformationType
import play.api.libs.json.{Json, OFormat}

case class LSPDetails(
                       penaltyNumber: String,
                       penaltyOrder: Option[String],
                       penaltyCategory: Option[LSPPenaltyCategoryEnum.Value],
                       penaltyStatus: LSPPenaltyStatusEnum.Value,
                       FAPIndicator: Option[String],
                       penaltyCreationDate: LocalDate,
                       penaltyExpiryDate: LocalDate,
                       expiryReason: Option[ExpiryReasonEnum.Value],
                       communicationsDate: Option[LocalDate],
                       lateSubmissions: Option[Seq[LateSubmission]],
                       appealInformation: Option[Seq[AppealInformationType]],
                       chargeAmount: Option[BigDecimal],
                       chargeOutstandingAmount: Option[BigDecimal],
                       chargeDueDate: Option[LocalDate],
                       lspTypeEnum: Option[LSPTypeEnum.Value] = None
                     )

object LSPDetails {
  implicit val format: OFormat[LSPDetails] = Json.format[LSPDetails]
}
