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

package base

class BaseSelectors {

  val title = "title"

  val h1 = "h1"

  def breadcrumbs(index: Int): String = s"#main-content > div > div > div.govuk-breadcrumbs.govuk-breadcrumbs--collapse-on-mobile > ol > li:nth-child($index)"

  def breadcrumbWithLink(index: Int): String = s"${breadcrumbs(index)} > a"

  val summaryCard = s"#late-submission-penalties > section"

  val summaryCardHeaderTitle = s"$summaryCard > header > h3"

  val summaryCardHeaderTag = s"$summaryCard > header > div > ul > li > strong"

  val summaryCardBody = s"$summaryCard > div"

  val summaryCardFooterLink = s"$summaryCard > footer > a"

  val tab = "#main-content > div > div > div.govuk-tabs > ul > li > a"

  val tabHeading = "#late-submission-penalties > h2"

  val externalGuidance = "#guidance-link"

}
