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

  def breadcrumbs(index: Int) = s"#main-content > div > div > div.govuk-breadcrumbs.govuk-breadcrumbs--collapse-on-mobile > ol > li:nth-child($index)"

  def breadcrumbWithLink(index: Int) = s"${breadcrumbs(index)} > a"

  val tab = "#main-content > div > div > div.govuk-tabs > ul > li > a"

  val tabHeading = "#late-submission-penalties > h2"
}
