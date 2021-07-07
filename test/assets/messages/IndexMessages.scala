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

package assets.messages

object IndexMessages {

  val breadcrumb1 = "Your VAT Account"

  val breadcrumb2 = "VAT penalties and appeals"

  val heading = "VAT penalties and appeals"

  val tab1 = "Late submission penalties"

  val subheading = "Late submission penalties"

  val externalGuidanceLinkText = "Read the guidance about late submission penalties (opens in a new tab)"

  val singularOverviewText = "You have 1 penalty point for submitting a VAT Return late."

  val pluralOverviewText = "You have 2 penalty points for submitting 2 VAT Returns late."

  val penaltyPointsTotal = "Penalty points total:"

  val noActivePenaltyPoints = "No active late submission penalty points."

  def multiActivePenaltyPoints(amountOfPoints: Int, amountOfLateSubmissions: Int) = s"You have $amountOfPoints penalty points for submitting $amountOfLateSubmissions VAT Returns late."

  val whatHappensWhenNextSubmissionIsLate = "You’ll get another point if you submit late again. Points usually expire after 24 months but it can be longer if you keep submitting late."

  val quarterlyThresholdPlusOnePenaltyApplication = "If you reach 4 points you’ll have to pay a £200 penalty."

  val warningText = "Warning: You’ll get a £200 penalty if you submit another VAT Return late."

  val thresholdReached = "You have reached the financial penalty threshold."

  val lateReturnPenalty = "Until you bring your account up to date:"

  val lateReturnPenaltyBullet1 = "your penalty points will not expire"

  val lateReturnPenaltyBullet2 = "you’ll have to pay a £200 penalty every time you submit a VAT Return late"

  val bringAccountUpToDate = "Show me how to bring this account up to date"

  val penaltyPointHeader = "Penalty point 1"
  val activeTag = "active"
  val reinstatedTag = "reinstated"
  val cancelledTag = "cancelled"
  val rejectedTag = "rejected"
  val overdueTag = "due"
  val paidTag = "paid"

  val period = "VAT Period"
  val returnDue = "VAT Return due"
  val reason = "Reason"
  val returnSubmitted = "Return submitted"
  val pointExpiration = "Point due to expire"
  val notSubmitted = "Not yet submitted"

  val submitAndPayVATPenaltyText = "You need to submit your VAT Return and pay any VAT you owe immediately."
  val payVATPenaltyText = "Check and pay what you owe"

  val vatPeriodValue: (String, String) => String = (s1, s2) => s"$s1 to $s2"

  val appealLinkText = "Appeal penalty point 1"
  val checkAppeal = "Check if you can appeal"

}
