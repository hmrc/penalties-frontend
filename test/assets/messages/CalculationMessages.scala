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

package assets.messages

object CalculationMessages {
  val titleLPP = "Late payment penalty - Manage your VAT account - GOV.UK"

  val agentTitleLPP = "Late payment penalty - Your client’s VAT details - GOV.UK"

  val periodHiddenText = "The period dates are"

  val periodWithText = "The period dates are 1 April 2022 to 30 June 2022"

  val headingLPP = "Late payment penalty"

  val th1LPPEstimate = "Penalty amount (estimate)"

  val th2LPP = "Penalty amount"

  val th2LPPAccruing = "Penalty amount (estimate)"

  val th3LPP = "Amount received"

  val th4LPP = "Left to pay"

  val th4Additional = "Calculation"

  val h2Estimates = "Estimates"

  val p2EstimatesLPP2 = "Penalties and interest will show as estimates until you pay the charge they relate to."

  val p2EstimatesLPP2Agent = "Penalties and interest will show as estimates until your client pays the charge they relate to."

  val p2EstimatesLPP1 = "Penalties will show as estimates until:"

  val b1Estimates = "you pay the VAT bill, or"

  val b1EstimatesAgent = "your client pays the VAT bill, or"

  val b2Estimates = "30 days have passed since the VAT due date"

  val estimateFooterNoteBillPayment = "This penalty applies if VAT has not been paid for 15 days."

  val estimateFooterNoteWarningTrader = "! Warning: The penalty will increase by a further 2% if VAT remains unpaid 30 days after the due date and you have not set up a payment plan."

  val estimateFooterNoteWarningAgent = "! Warning: The penalty will increase by a further 2% if VAT remains unpaid 30 days after the due date and your client has not set up a payment plan."

  val link = "Return to VAT penalties and appeals"

  val howPenaltyIsAppliedLPP2 = "This penalty applies from day 31, if any VAT remains unpaid."

  val howPenaltyIsApplied15Days = "This penalty applies if VAT has not been paid for 15 days."

  val howPenaltyIsApplied30Days = "This penalty applies if VAT has not been paid for 30 days."

  val whenPenaltyIncreases = "The total increases daily based on the amount of unpaid VAT for the period."

  val whenPenaltyIncreasesAccruing = "The total increases daily until you pay your VAT or set up a payment plan."

  val whenPenaltyIncreasesAccruingAgent = "The total increases daily until your client pays their VAT or sets up a payment plan."

  val lpp2Calculation = "The calculation we use for each day is: (Penalty rate of 4% × unpaid VAT) ÷ days in a year"

  val twoPartCalculation = s"It is made up of 2 parts:"

  val onePartCalculation = (calculation: String) => s"The calculation we use is: $calculation"

  val dueDate = "Due date"
}
