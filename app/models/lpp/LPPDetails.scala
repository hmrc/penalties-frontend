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

package models.lpp

import models.appealInfo.AppealInformationType
import play.api.libs.json._
import utils.JsonUtils

import java.time.LocalDate

case class LPPDetails(
                       principalChargeReference: String,
                       penaltyCategory: LPPPenaltyCategoryEnum.Value,
                       penaltyChargeCreationDate: Option[LocalDate],
                       penaltyStatus: LPPPenaltyStatusEnum.Value,
                       penaltyAmountPaid: Option[BigDecimal],
                       penaltyAmountPosted: BigDecimal,
                       penaltyAmountAccruing: BigDecimal,
                       penaltyAmountOutstanding: Option[BigDecimal],
                       LPP1LRDays: Option[String],
                       LPP1HRDays: Option[String],
                       LPP2Days: Option[String],
                       LPP1LRCalculationAmount: Option[BigDecimal],
                       LPP1HRCalculationAmount: Option[BigDecimal],
                       LPP1LRPercentage: Option[BigDecimal],
                       LPP1HRPercentage: Option[BigDecimal],
                       LPP2Percentage: Option[BigDecimal],
                       communicationsDate: Option[LocalDate],
                       penaltyChargeDueDate: Option[LocalDate],
                       appealInformation: Option[Seq[AppealInformationType]],
                       principalChargeBillingFrom: LocalDate,
                       principalChargeBillingTo: LocalDate,
                       principalChargeDueDate: LocalDate,
                       penaltyChargeReference: Option[String],
                       principalChargeLatestClearing: Option[LocalDate],
                       vatOutstandingAmount: Option[BigDecimal],
                       LPPDetailsMetadata: LPPDetailsMetadata
                     ) extends Ordered[LPPDetails] {
  override def compare(that: LPPDetails): Int = {
    (this.principalChargeBillingFrom, that.principalChargeBillingFrom,
      this.principalChargeBillingTo, that.principalChargeBillingTo,
      this.LPPDetailsMetadata.mainTransaction, that.LPPDetailsMetadata.mainTransaction,
      this.penaltyCategory, that.penaltyCategory)
    match {
      //Compare tax period start dates
      case (startDateA, startDateB, _, _, _, _, _, _) if startDateA.isBefore(startDateB) => 1
      case (startDateA, startDateB, _, _, _, _, _, _) if startDateA.isAfter(startDateB) => -1

      //Compare tax period end dates
      case (_, _, endDateA, endDateB, _, _, _, _) if endDateA.isBefore(endDateB) => 1
      case (_, _, endDateA, endDateB, _, _, _, _) if endDateA.isAfter(endDateB) => -1

      //Compare mainTransactions
      case (_, _, _, _, Some(mainTransA), Some(mainTransB), _, _) if mainTransA < mainTransB => 1
      case (_, _, _, _, Some(mainTransA), Some(mainTransB), _, _) if mainTransA > mainTransB => -1

      //Compare penaltyCategory
      case (_, _, _, _, _, _, categoryA, categoryB) if categoryA < categoryB => 1
      case (_, _, _, _, _, _, categoryA, categoryB) if categoryA > categoryB => -1

      //No difference found between this and that (will use ETMP order)
      case _ => 0
    }
  }
}

object LPPDetails extends JsonUtils {
  implicit val format: Format[LPPDetails] = new Format[LPPDetails] {
    override def reads(json: JsValue): JsResult[LPPDetails] = {
      for {
        principalChargeReference <- (json \ "principalChargeReference").validate[String]
        penaltyCategory <- (json \ "penaltyCategory").validate[LPPPenaltyCategoryEnum.Value]
        penaltyChargeCreationDate <- (json \ "penaltyChargeCreationDate").validateOpt[LocalDate]
        penaltyStatus <- (json \ "penaltyStatus").validate[LPPPenaltyStatusEnum.Value]
        penaltyAmountPaid <- (json \ "penaltyAmountPaid").validateOpt[BigDecimal]
        penaltyAmountPosted <- (json \ "penaltyAmountPosted").validate[BigDecimal]
        penaltyAmountAccruing <- (json \ "penaltyAmountAccruing").validate[BigDecimal]
        penaltyAmountOutstanding <- (json \ "penaltyAmountOutstanding").validateOpt[BigDecimal]
        lPP1LRDays <- (json \ "LPP1LRDays").validateOpt[String]
        lPP1HRDays <- (json \ "LPP1HRDays").validateOpt[String]
        lPP2Days <- (json \ "LPP2Days").validateOpt[String]
        lPP1LRCalculationAmount <- (json \ "LPP1LRCalculationAmount").validateOpt[BigDecimal]
        lPP1HRCalculationAmount <- (json \ "LPP1HRCalculationAmount").validateOpt[BigDecimal]
        lPP1LRPercentage <- (json \ "LPP1LRPercentage").validateOpt[BigDecimal]
        lPP1HRPercentage <- (json \ "LPP1HRPercentage").validateOpt[BigDecimal]
        lPP2Percentage <- (json \ "LPP2Percentage").validateOpt[BigDecimal]
        communicationsDate <- (json \ "communicationsDate").validateOpt[LocalDate]
        penaltyChargeDueDate <- (json \ "penaltyChargeDueDate").validateOpt[LocalDate]
        appealInformation <- (json \ "appealInformation").validateOpt[Seq[AppealInformationType]]
        principalChargeBillingFrom <- (json \ "principalChargeBillingFrom").validate[LocalDate]
        principalChargeBillingTo <- (json \ "principalChargeBillingTo").validate[LocalDate]
        principalChargeDueDate <- (json \ "principalChargeDueDate").validate[LocalDate]
        penaltyChargeReference <- (json \ "penaltyChargeReference").validateOpt[String]
        principalChargeLatestClearing <- (json \ "principalChargeLatestClearing").validateOpt[LocalDate]
        vatOutstandingAmount <-  (json \ "vatOutstandingAmount").validateOpt[BigDecimal]
        metadata <- Json.fromJson(json)(LPPDetailsMetadata.format)
      }
      yield {
        LPPDetails(principalChargeReference, penaltyCategory, penaltyChargeCreationDate, penaltyStatus, penaltyAmountPaid,
          penaltyAmountPosted, penaltyAmountAccruing, penaltyAmountOutstanding, lPP1LRDays, lPP1HRDays, lPP2Days, lPP1LRCalculationAmount,
          lPP1HRCalculationAmount, lPP1LRPercentage, lPP1HRPercentage, lPP2Percentage, communicationsDate, penaltyChargeDueDate, appealInformation,
          principalChargeBillingFrom, principalChargeBillingTo, principalChargeDueDate, penaltyChargeReference,
          principalChargeLatestClearing, vatOutstandingAmount, metadata)
      }
    }

    override def writes(o: LPPDetails): JsValue = {
      jsonObjNoNulls(
        "principalChargeReference" -> o.principalChargeReference,
        "penaltyCategory" -> o.penaltyCategory,
        "penaltyChargeCreationDate" -> o.penaltyChargeCreationDate,
        "penaltyStatus" -> o.penaltyStatus,
        "penaltyAmountPaid" -> o.penaltyAmountPaid,
        "penaltyAmountPosted" -> o.penaltyAmountPosted,
        "penaltyAmountAccruing" -> o.penaltyAmountAccruing,
        "penaltyAmountOutstanding" -> o.penaltyAmountOutstanding,
        "LPP1LRDays" -> o.LPP1LRDays,
        "LPP1HRDays" -> o.LPP1HRDays,
        "LPP2Days" -> o.LPP2Days,
        "LPP1LRCalculationAmount" -> o.LPP1LRCalculationAmount,
        "LPP1HRCalculationAmount" -> o.LPP1HRCalculationAmount,
        "LPP1LRPercentage" -> o.LPP1LRPercentage,
        "LPP1HRPercentage" -> o.LPP1HRPercentage,
        "LPP2Percentage" -> o.LPP2Percentage,
        "communicationsDate" -> o.communicationsDate,
        "penaltyChargeDueDate" -> o.penaltyChargeDueDate,
        "appealInformation" -> o.appealInformation,
        "principalChargeBillingFrom" -> o.principalChargeBillingFrom,
        "principalChargeBillingTo" -> o.principalChargeBillingTo,
        "principalChargeDueDate" -> o.principalChargeDueDate,
        "penaltyChargeReference" -> o.penaltyChargeReference,
        "principalChargeLatestClearing" -> o.principalChargeLatestClearing,
        "vatOutstandingAmount" -> o.vatOutstandingAmount,
      ).deepMerge(Json.toJsObject(o.LPPDetailsMetadata)(LPPDetailsMetadata.format))
    }
  }
}

case class LPPDetailsMetadata(
                               mainTransaction: Option[MainTransactionEnum.Value],
                               outstandingAmount: Option[BigDecimal],
                               timeToPay: Option[Seq[TimeToPay]]
                             )

object LPPDetailsMetadata {
  implicit val format: OFormat[LPPDetailsMetadata] = Json.format[LPPDetailsMetadata]
}

case class TimeToPay(
                      TTPStartDate: Option[LocalDate],
                      TTPEndDate: Option[LocalDate]
                    )

object TimeToPay {
  implicit val format: OFormat[TimeToPay] = Json.format[TimeToPay]
}
