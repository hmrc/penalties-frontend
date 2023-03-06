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

object IndexMessages {
  val title = "VAT penalties and appeals - Manage your VAT account - GOV.UK"

  val titleAgent = "VAT penalties and appeals - Your client’s VAT details - GOV.UK"

  val breadcrumb1 = "Business tax account"

  val breadcrumb2 = "Your VAT Account"

  val breadcrumb3 = "VAT penalties and appeals"

  val heading = "VAT penalties and appeals"

  val externalLSPGuidanceLinkText = "Read the guidance about late submission penalties (opens in a new tab)"

  val singularOverviewText = "You have 1 penalty point for submitting a VAT Return late."

  val singularAgentOverviewText = "Your client has 1 penalty point for submitting a VAT Return late."

  val pluralOverviewText = "You have 2 penalty points for submitting 2 VAT Returns late."

  val pluralAgentOverviewText = "Your client has 2 penalty points for submitting 2 VAT Returns late."

  val penaltyPointsTotal = "Penalty points total:"

  val noActivePenaltyPoints = "There are no active late submission penalty points."

  val noActivePaymentPenalty = "There are no late payment penalties."

  val unpaidVATText = "The earlier you pay your VAT, the lower your penalties and interest will be."

  val agentClientUnpaidVATText = "The earlier your client pays their VAT, the lower their penalties and interest will be."

  val howLppCalculatedLinkText = "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

  def multiActivePenaltyPoints(amountOfPoints: Int, amountOfLateSubmissions: Int) = s"You have $amountOfPoints penalty points for submitting $amountOfLateSubmissions VAT Returns late."

  def multiAgentActivePenaltyPoints(amountOfPoints: Int, amountOfLateSubmissions: Int) = s"Your client has $amountOfPoints penalty points for submitting $amountOfLateSubmissions VAT Returns late."

  val whatHappensWhenNextSubmissionIsLate = "You’ll get another point if you submit late again. Points usually expire after 24 months but it can be longer if you keep submitting late."

  val whatHappensWhenNextSubmissionIsLateForAgent = "They’ll get another point if they submit late again. Points usually expire after 24 months but it can be longer if your client keeps submitting late."

  val quarterlyThresholdPlusOnePenaltyApplication = "If you reach 4 points, you’ll have to pay a £200 penalty."

  val quarterlyThresholdPlusOnePenaltyApplicationForAgent = "If your client reaches 4 points, they’ll have to pay a £200 penalty."

  val warningText = "Warning: You’ll get a £200 penalty if you submit another VAT Return late."

  val warningTextAgent = "Warning: Your client will get a £200 penalty if they submit another VAT Return late."

  val thresholdReached = "You have reached the financial penalty threshold."

  val thresholdReachedAgent = "Your client has reached the financial penalty threshold."

  val lateReturnPenalty = "This means you have to pay a £200 penalty every time you submit a VAT Return late, until we remove your penalty points."

  val lateReturnPenaltyAgent = "This means they have to pay a £200 penalty every time they submit a VAT Return late, until we remove their points."

  val bringAccountUpToDate = "Actions to take to get your points removed by January 2022"

  val lspOnThresholdMessage = "Earliest date your points could be removed: January 2022"

  val bringAccountUpToDateAgent = "Actions your client must take to get their points removed by January 2022"

  val lspOnThresholdMessageAgent = "Earliest date your client’s points could be removed: January 2022"
  val traderCompliantContentP = "We will remove your penalty points in January 2022 because:"
  val traderCompliantBullet1 = "your VAT Return history is up to date"
  val traderCompliantBullet2 = "you have submitted on time for the last 12 months"

  val agentCompliantContentP = "We will remove your client’s penalty points in January 2022 because:"
  val agentCompliantBullet1 = "their VAT Return history is up to date"
  val agentCompliantBullet2 = "they have submitted on time for the last 12 months"

  val activeTag = "active"
  val cancelledTag = "cancelled"
  val overdueTag = "due"
  val overduePartiallyPaidTag: BigDecimal => String = amount => s"£$amount due"
  val paidTag = "paid"
  val estimate = "estimate"

  val period = "VAT period"
  val returnDue = "VAT Return due"
  val reason = "Reason"
  val returnSubmitted = "Return submitted"
  val pointExpiration = "Point due to expire"
  val notSubmitted = "Return not yet received"
  val penaltyType = "Penalty type"
  val overdueCharge = "Overdue charge"
  val chargeDue = "Charge due"
  val datePaid = "Date paid"

  val vatPeriodValue: (String, String) => String = (s1, s2) => s"$s1 to $s2"
  val periodValueLPPOnePeriod: (String, String, String) => String = (reason, s1, s2) => s"$reason for period $s1 to $s2"
  val periodValueLPPMultiplePeriods: (String, String, String) => String = (reason, s1, s2) => s"$reason for $s1 to $s2"

  val appealStatus = "Appeal status"
  def lspMultiplePenaltyPeriodMessage(dueDate : String) = s"The VAT Return due on $dueDate was also submitted late. <br> HMRC only applies 1 penalty for late submission in each month."

  val betaFeedbackContent = "This is a new service - your feedback will help us to improve it."

  val whatYouOweButtonText = "Check amounts and pay"
  val whatYouOweButtonAgentText = "Check amounts"
}
