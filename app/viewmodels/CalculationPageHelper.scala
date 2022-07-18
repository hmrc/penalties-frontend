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

import models.lpp.LPPDetails
import play.api.i18n.Messages
import utils.{CurrencyFormatter, ImplicitDateFormatter, ViewUtils}

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject

class CalculationPageHelper @Inject()() extends ViewUtils with ImplicitDateFormatter {

  def getCalculationRowForLPP(lpp: LPPDetails)(implicit messages: Messages): Option[Seq[String]] = {
    (lpp.LPP1LRCalculationAmount, lpp.LPP1HRCalculationAmount) match {
      case (Some(amountOnDay15), Some(amountOnDay31)) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val amountOnDay31ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay31)
        val penaltyAmountOnDay15 = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15 * 0.02)
        val penaltyAmountOnDay31 = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay31 * 0.02)
        val firstCalculation = messages("calculation.key.2.text.30.days",
          s"${lpp.LPP1LRPercentage.get}", amountOnDay15ParsedAsString, messages("calculation.lpp1.15days"), penaltyAmountOnDay15)
        val secondCalculation = messages("calculation.key.2.text.30.days",
          s"${lpp.LPP1HRPercentage.get}", amountOnDay31ParsedAsString, messages("calculation.lpp1.30days"), penaltyAmountOnDay31)
        Some(Seq(firstCalculation, secondCalculation))
      case (Some(amountOnDay15), None) =>
        val amountOnDay15ParsedAsString = CurrencyFormatter.parseBigDecimalToFriendlyValue(amountOnDay15)
        val calculation = messages("calculation.key.2.text",
          s"${lpp.LPP1LRPercentage.get}", amountOnDay15ParsedAsString, messages("calculation.lpp1.15days"))
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
