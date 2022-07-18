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

package assets.messages

object CalculationMessages {
  val titleLPP = "Late payment penalty - Manage your VAT account - GOV.UK"

  val period = "1 October 2022 to 31 December 2022"

  val periodHiddenText = "The period dates are"

  val periodWithText = "The period dates are 1 October 2022 to 31 December 2022"

  val headingLPP = "Late payment penalty"

  val p1Additional = "The additional penalty is charged from 31 days after the payment due date, until the total is paid."

  val th1LPPEstimate = "Penalty amount (estimate)"

  val th2LPP = "Penalty amount"

  val th2LPPAccruing = "Penalty amount (estimate)"

  val th2Additional = "Number of days since day 31"

  val th3LPP = "Amount received"

  val th3Additional = "Additional penalty rate"

  val th4LPP = "Amount left to pay"

  val th4LPP1 = "Left to pay"

  val th4Additional = "Calculation"

  val h2Additional = "Estimates"

  val p2AdditionalLPP2 = "Penalties and interest will show as estimates if HMRC does not have enough information to calculate the final amounts."

  val p2AdditionalLPP1 = "Penalties and interest will show as estimates until:"

  val p2EstimatedLPP1 = "The calculation we use is: 2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)"

  val b1Additional = "you pay the VAT bill, or"

  val b1AdditionalAgent = "your client pays the VAT bill, or"

  val b2Additional = "30 days have passed since the VAT due date"

  val p3Additional = "This could be because:"

  val bullet1Additional = "we have not received your VAT payment"

  val bullet2Additional = "you have an unpaid penalty on your account"

  val estimateFooterNoteBillPayment = "This penalty applies if VAT has not been paid for 15 days."

  val estimateFooterNoteWarning = "! This penalty will rise to £800.00 (a further 2% of the unpaid VAT) if you do not make a VAT payment by 15 January 2023."

  val estimateFooterNoteWarningTrader = "! This penalty will increase by a further 2% if VAT remains unpaid 30 days after the due date and you have not set up a payment plan."

  val estimateFooterNoteWarningAgent = "! This penalty will increase by a further 2% if VAT remains unpaid 30 days after the due date and your client has not set up a payment plan."

  val estimateFooterNoteText = "Penalties and interest will show as estimates if HMRC has not been given enough information to calculate the final amounts."

  val link = "Return to VAT penalties and appeals"

  val howPenaltyIsApplied15Days = "This penalty applies if VAT has not been paid for 15 days."

  val howPenaltyIsApplied30Days = "This penalty applies if VAT has not been paid for 30 days."

  val twoPartCalculation = s"It is made up of 2 parts:"

  val onePartCalculation = (calculation: String) => s"The calculation we use is: $calculation"

  val dueDate = "Due date"

  val linkEstimatedTrader = "Return to what you owe"

  val linkEstimatedAgent = "Return to what your client owes"
}
