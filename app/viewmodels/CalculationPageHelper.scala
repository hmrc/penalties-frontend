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

package viewmodels

import models.penalty.LatePaymentPenalty
import models.v3.lpp.LPPDetails
import play.api.i18n.Messages
import utils.{CurrencyFormatter, ImplicitDateFormatter, ViewUtils}

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject

class CalculationPageHelper @Inject()() extends ViewUtils with ImplicitDateFormatter {

  def getCalculationRowForLPP(lpp: LatePaymentPenalty)(implicit messages: Messages): Option[Seq[String]] = {
    (lpp.financial.outstandingAmountDay15, lpp.financial.outstandingAmountDay31) match {
      case (Some(amountOnDay15), Some(amountOnDay31)) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val amountOnDay31ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay31)
        val firstPaymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val firstCalculation = messages("calculation.key.2.text",
          s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15ParsedAsString, firstPaymentDetail)
        val secondPaymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(30)))
        val secondCalculation = messages("calculation.key.2.text",
          s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay31ParsedAsString, secondPaymentDetail)
        Some(Seq(firstCalculation, secondCalculation))
      case (Some(amountOnDay15), None) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val paymentDetail = messages("calculation.key.2.paymentDetail", dateTimeToString(lpp.period.dueDate.plusDays(15)))
        val calculation = messages("calculation.key.2.text",
          s"${lpp.financial.percentageOfOutstandingAmtCharged.get}", amountOnDay15ParsedAsString, paymentDetail)
        Some(Seq(calculation))
      case _ =>
        None
    }
  }

  def getCalculationRowForLPPForNewAPI(lpp: LPPDetails)(implicit messages: Messages): Option[Seq[String]] = {
    (lpp.LPP1LRCalculationAmount, lpp.LPP1HRCalculationAmount) match {
      case (Some(amountOnDay15), Some(amountOnDay31)) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val amountOnDay31ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay31)
        val firstPaymentDetail = messages("calculation.key.2.paymentDetail", dateToString(lpp.penaltyChargeDueDate.plusDays(15)))
        val firstCalculation = messages("calculation.key.2.text",
          s"${lpp.LPP1LRPercentage.get}", amountOnDay15ParsedAsString, firstPaymentDetail)
        val secondPaymentDetail = messages("calculation.key.2.paymentDetail", dateToString(lpp.penaltyChargeDueDate.plusDays(30)))
        val secondCalculation = messages("calculation.key.2.text",
          s"${lpp.LPP1HRPercentage.get}", amountOnDay31ParsedAsString, secondPaymentDetail)
        Some(Seq(firstCalculation, secondCalculation))
      case (Some(amountOnDay15), None) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val paymentDetail = messages("calculation.key.2.paymentDetail", dateToString(lpp.penaltyChargeDueDate.plusDays(15)))
        val calculation = messages("calculation.key.2.text",
          s"${lpp.LPP1LRPercentage.get}", amountOnDay15ParsedAsString, paymentDetail)
        Some(Seq(calculation))
      case _ =>
        None
    }
  }

  def getDateTimeAsDayMonthYear(dateTime: LocalDateTime)(implicit messages: Messages): String = {
    dateTimeToString(dateTime)
  }

  def getDateAsDayMonthYear(date: LocalDate)(implicit messages: Messages): String = {
    dateToString(date)
  }
}
