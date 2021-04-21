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

package views

import assets.messages.IndexMessages._
import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import views.behaviours.ViewBehaviours
import views.html.IndexView

class IndexViewSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  "IndexView" when {

    val indexViewPage = injector.instanceOf[IndexView]

    "user is on page" must {

      val expectedContent = Seq(
        Selectors.h1 -> heading,
        Selectors.breadcrumbWithLink(1) -> breadcrumb1,
        Selectors.breadcrumbs(2) -> breadcrumb2,
        Selectors.tab -> tab1,
        Selectors.tabHeading -> subheading
      )

      implicit val doc: Document = asDocument(indexViewPage.apply())

      behave like pageWithExpectedMessages(expectedContent)

      "have correct route for breadcrumb link" in {
        doc.select(Selectors.breadcrumbWithLink(1)).attr("href") mustBe appConfig.vatOverviewUrl
      }
    }
  }
}
