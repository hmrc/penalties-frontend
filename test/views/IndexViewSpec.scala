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
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.IndexView
import views.html.components.p

class IndexViewSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  val pElement: p = injector.instanceOf[p]

  "IndexView" when {

    val indexViewPage = injector.instanceOf[IndexView]

    "user is on page" must {

      val expectedContent = Seq(
        Selectors.h1 -> heading,
        Selectors.breadcrumbWithLink(1) -> breadcrumb1,
        Selectors.breadcrumbs(2) -> breadcrumb2,
        Selectors.tab -> tab1,
        Selectors.tabHeading -> subheading,
        Selectors.externalGuidance -> externalGuidanceLinkText
      )

      val contentToDisplayOnPage: Html = pElement(content = Html("This is some content."), id = Some("sample-content"))

      implicit val doc: Document = asDocument(indexViewPage.apply(contentToDisplayOnPage))

      behave like pageWithExpectedMessages(expectedContent)

      "have correct route for breadcrumb link" in {
        doc.select(Selectors.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.vatOverviewUrl
      }

      "have a link to external guidance which opens in a new tab" in {
        val element = doc.select(Selectors.externalGuidance)
        //TODO: change this when we have a GOV.UK guidance page
        element.attr("href") shouldBe "#"
        element.attr("target") shouldBe "_blank"
      }

      "have the specified content displayed on the page" in {
        doc.select("#sample-content").text() shouldBe "This is some content."
      }
    }
  }
}
