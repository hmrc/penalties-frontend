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

import java.time.LocalDateTime
import javax.inject.Inject

class CalculationPageHelper @Inject()() extends ViewUtils with ImplicitDateFormatter {

  def getCalculationRowForLPP(lpp: LatePaymentPenalty)(implicit messages: Messages): Option[Seq[String]] = {
    (lpp.financial.outstandingAmountDay15, lpp.financial.outstandingAmountDay31) match {
      case (Some(amountOnDay15), Some(amountOnDay31)) => {
        val amountOnDay15ParsedAsString = parseBigDecimalToFriendlyValue(amountOnDay15)
        val amountOnDay31ParsedAsString = parseBigDecimalToFriendlyValue(amountOnDay31)
        val firstPaymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val firstCalculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15ParsedAsString, firstPaymentDetail)
        val secondPaymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(30)))
        val secondCalculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay31ParsedAsString, secondPaymentDetail)
        Some(Seq(firstCalculation, secondCalculation))
      }
      case (Some(amountOnDay15), None) => {
        val amountOnDay15ParsedAsString = parseBigDecimalToFriendlyValue(amountOnDay15)
        val paymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val calculation = messages("calculation.key.2.text", s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15ParsedAsString, paymentDetail)
        Some(Seq(calculation))
      }
      case _ => {
        None
      }
    }
  }

  def getDateAsDayMonthYear(dateTime: LocalDateTime): String = {
    dateTimeToString(dateTime)
  }

  def parseBigDecimalToFriendlyValue(amount: BigDecimal): String = {
      "%,.2f".format(amount)
  }
}
