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

package viewmodels

import models.penalty.LatePaymentPenalty
import models.reason.PaymentPenaltyReasonEnum
import play.api.i18n.Messages
import utils.{ImplicitDateFormatter, ViewUtils}

import javax.inject.Inject

class CalculationPageHelper @Inject()() extends ViewUtils with ImplicitDateFormatter {

  def getCalculationRow(lpp: LatePaymentPenalty)(implicit messages: Messages): Option[Seq[String]] = {
    val chargeType = getChargeTypeBasedOnReason(lpp.reason)
    (lpp.financial.outstandingAmountDay15, lpp.financial.outstandingAmountDay31) match {
      case (Some(amountOnDay15), Some(amountOnDay31)) => {
        val firstPaymentDetail = messages("calculation.key.2.paymentDetail", chargeType, dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val firstCalculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15, firstPaymentDetail)
        val secondPaymentDetail = messages("calculation.key.2.paymentDetail", chargeType, dateTimeToString(lpp.period.dueDate.plusDays(30)))
        val secondCalculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay31, secondPaymentDetail)
        Some(Seq(firstCalculation, secondCalculation))
      }
      case (Some(amountOnDay15), None) => {
        val paymentDetail = messages("calculation.key.2.paymentDetail", chargeType, dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val calculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15, paymentDetail)
        Some(Seq(calculation))
      }
      case _ => {
        None
      }
    }
  }

  def getChargeTypeBasedOnReason(reason: PaymentPenaltyReasonEnum.Value)(implicit messages: Messages): String = {
    reason.toString match {
      case x if x.contains("VAT") => messages("calculation.parentCharge.VAT")
      case x if x.contains("ERROR_CORRECTION_NOTICE") => messages("calculation.parentCharge.ecn")
      case x if x.contains("CENTRAL_ASSESSMENT") => messages("calculation.parentCharge.centralAssessment")
      case x if x.contains("OFFICERS_ASSESSMENT") => messages("calculation.parentCharge.officersAssessment")
    }
  }
}