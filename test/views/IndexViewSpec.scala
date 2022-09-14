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

package views

import assets.messages.IndexMessages._
import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.IndexView

class IndexViewSpec extends SpecBase with ViewUtils with ViewBehaviours {
  val indexPage: IndexView = injector.instanceOf[IndexView]


  object Selectors extends BaseSelectors {
    val betaFeedbackBannerText =  "body > div > div.govuk-phase-banner > p > span"

    val timeToPayParagraph: Int => String = (index: Int) => s"#time-to-pay > p:nth-child($index)"
  }

  "Index View" should {

    def applyView(isTTPActive: Boolean = false, isUserAgent: Boolean = false): HtmlFormat.Appendable = {
      indexPage.apply(html(), html(), Seq.empty, None, "", isTTPActive = isTTPActive)(implicitly, implicitly, implicitly, if(isUserAgent) agentUser else vatTraderUser)
    }

    implicit val doc: Document = asDocument(applyView())

    "display the content not provided by the index helper" must {

      val expectedContent = Seq(
        Selectors.breadcrumbWithLink(1) -> breadcrumb1,
        Selectors.breadcrumbWithLink(2) -> breadcrumb2,
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.betaFeedbackBannerText -> betaFeedbackContent
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a beta banner with the feedback correct content and a link with the 'backUrl' queryParam" in {
        doc.select(Selectors.betaFeedbackBannerText).text() shouldBe betaFeedbackContent
        doc.select("#beta-feedback-link").attr("href").contains("http://localhost:9250/contact/beta-feedback?service=vat-penalties&backUrl=") shouldBe true
      }
    }

    "display TTP (time to pay) content when a TTP is active (user is trader)" must {
      val expectedContent = Seq(
        Selectors.breadcrumbWithLink(1) -> breadcrumb1,
        Selectors.breadcrumbWithLink(2) -> breadcrumb2,
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.timeToPayParagraph(1) -> ttpText.head,
        Selectors.timeToPayParagraph(2) -> ttpText(1),
        Selectors.timeToPayParagraph(3) -> ttpText(2),
        Selectors.betaFeedbackBannerText -> betaFeedbackContent
      )

      behave like pageWithExpectedMessages(expectedContent)(asDocument(applyView(isTTPActive = true)))
    }

    "display TTP (time to pay) content when a TTP is active (user is agent)" must {
      val expectedContent = Seq(
        Selectors.title -> titleAgent,
        Selectors.h1 -> heading,
        Selectors.timeToPayParagraph(1) -> ttpAgentText.head,
        Selectors.timeToPayParagraph(2) -> ttpAgentText(1),
        Selectors.timeToPayParagraph(3) -> ttpAgentText(2),
        Selectors.betaFeedbackBannerText -> betaFeedbackContent
      )

      behave like pageWithExpectedMessages(expectedContent)(asDocument(applyView(isTTPActive = true, isUserAgent = true)))
    }
  }
}
