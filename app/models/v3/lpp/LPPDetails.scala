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

package models.v3.lpp

import models.v3.appealInfo.AppealInformationType
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class LPPDetails(
                       principalChargeReference: String,
                       penaltyCategory: LPPPenaltyCategoryEnum.Value,
                       penaltyChargeCreationDate: LocalDate,
                       penaltyStatus: LPPPenaltyStatusEnum.Value,
                       penaltyAmountPaid: Option[BigDecimal],
                       penaltyAmountOutstanding: Option[BigDecimal],
                       LPP1LRDays: Option[String],
                       LPP1HRDays: Option[String],
                       LPP2Days: Option[String],
                       LPP1LRCalculationAmount: Option[BigDecimal],
                       LPP1HRCalculationAmount: Option[BigDecimal],
                       LPP1LRPercentage: Option[BigDecimal],
                       LPP1HRPercentage: Option[BigDecimal],
                       LPP2Percentage: Option[BigDecimal],
                       communicationsDate: LocalDate,
                       penaltyChargeDueDate: LocalDate,
                       appealInformation: Option[Seq[AppealInformationType]],
                       principalChargeBillingFrom: LocalDate,
                       principalChargeBillingTo: LocalDate,
                       principalChargeDueDate: LocalDate
                     )

object LPPDetails {
  implicit val format: OFormat[LPPDetails] = Json.format[LPPDetails]
}
