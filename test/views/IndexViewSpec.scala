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

package views

import assets.messages.IndexMessages._
import base.{BaseSelectors, SpecBase}
import models.{GetPenaltyDetails, Totalisations}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.ViewUtils
import viewmodels.IndexPageHelper
import views.behaviours.ViewBehaviours
import views.html.IndexView

class IndexViewSpec extends SpecBase with ViewUtils with ViewBehaviours {
  val indexPage: IndexView = injector.instanceOf[IndexView]
  val helper: IndexPageHelper = injector.instanceOf[IndexPageHelper]


  object Selectors extends BaseSelectors {
    val betaFeedbackBannerText =  "body > div > div.govuk-phase-banner > p > span"

    val timeToPayParagraph: Int => String = (index: Int) => s"#time-to-pay > p:nth-child($index)"

    val whatYouOweButton = "#what-is-owed > a"
  }

  "Index View" should {

    val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = Some(
        Totalisations(
          LSPTotalValue = Some(100),
          penalisedPrincipalTotal = Some(223.45),
          LPPPostedTotal = Some(0),
          LPPEstimatedTotal = Some(0),
          totalAccountOverdue = Some(123.45),
          totalAccountPostedInterest = None,
          totalAccountAccruingInterest = None
        )
      ),
      lateSubmissionPenalty = None,
      latePaymentPenalty = None,
      breathingSpace = None
    )

    def applyView(isTTPActive: Boolean = false, isUserAgent: Boolean = false, userOwes: Boolean = false): HtmlFormat.Appendable = {
      indexPage.apply(html(), html(), Seq.empty, None, "",
        whatYouOweContent = if(!userOwes) None else helper.getWhatYouOweBreakdown(penaltyDetails))(implicitly, implicitly, if(isUserAgent) agentUser else vatTraderUser)
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

    "display button with correct text and link when user owes penalties" when {
      "user is trader" in {
        val docWithPenalties = asDocument(applyView(userOwes = true))
        docWithPenalties.select(Selectors.button).text() shouldBe whatYouOweButtonText
        docWithPenalties.select(Selectors.button).attr("href") shouldBe "http://localhost:9152/vat-through-software/what-you-owe"
      }

      "user is agent" in {
        val docWithPenalties = asDocument(applyView(userOwes = true, isUserAgent = true))
        docWithPenalties.select(Selectors.button).text() shouldBe whatYouOweButtonAgentText
        docWithPenalties.select(Selectors.button).attr("href") shouldBe "http://localhost:9152/vat-through-software/what-you-owe"
      }
    }

    "the footer should have the correct links" in {
      implicit val doc: Document = asDocument(applyView())
      val footerLinks = doc.select(".govuk-footer__link")
      footerLinks.get(0).text shouldBe "Cookies"
      footerLinks.get(1).text shouldBe "Accessibility statement"
      footerLinks.get(1).attr("href").contains("http://localhost:12346/accessibility-statement/penalties") shouldBe true
      footerLinks.get(2).text shouldBe "Privacy policy"
      footerLinks.get(3).text shouldBe "Terms and conditions"
      footerLinks.get(4).text shouldBe "Help using GOV.UK"
      footerLinks.get(5).text shouldBe "Contact"
    }
  }
}
