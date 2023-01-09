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

package base

class BaseSelectors {

  val title = "title"

  val h1 = "#main-content h1"

  val h2 = "#main-content h2"

  def breadcrumbs(index: Int): String = s"body > div > div.govuk-breadcrumbs.govuk-breadcrumbs--collapse-on-mobile > ol > li:nth-child($index)"

  def breadcrumbWithLink(index: Int): String = s"${breadcrumbs(index)} > a"

  val summaryLSPCard = s"#late-submission-penalties > section"

  val summaryLPPCard = s"#late-payment-penalties > section"

  def summaryCardHeaderTag(summaryCard: String): String = s"$summaryCard > header > div > strong"

  def summaryCardBody(summaryCard: String): String = s"$summaryCard > div"

  def summaryCardFooterLink(summaryCard: String): String = s"$summaryCard > footer a"

  def tab(index: Int): String = s"#main-content > div > div > div.govuk-tabs > ul > li:nth-child($index)> a"

  val tabHeading = "#late-submission-penalties > h2"

  val tabHeadingLPP = "#late-payment-penalties > h2"

  val externalGuidance = "#guidance-link"

  val pNthChild: Int => String = (nThChild: Int) => s"#main-content p:nth-child($nThChild)"

  val viewCalculation = "#late-payment-penalties > section > header > div > ul > li > a"

  val periodSpan = "#main-content > div > div > span"

  val periodWithText = "#main-content > div > div > header > p"

  val periodHiddenText = "#main-content > div > div > header > p > span"

  val HeaderTextNotVisible = "#main-content > div > div > header > p > .govuk-visually-hidden"

  val button = "#main-content .govuk-button"
}
