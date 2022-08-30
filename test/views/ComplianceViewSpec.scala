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

import assets.messages.ComplianceMessages._
import assets.messages.IndexMessages.{breadcrumb1, breadcrumb2, breadcrumb3}
import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import utils.ViewUtils
import viewmodels.TimelineEvent
import views.behaviours.ViewBehaviours
import views.html.ComplianceView
import views.html.components.{p, timeline}

class ComplianceViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val compliancePage: ComplianceView = injector.instanceOf[ComplianceView]
  val timeline: timeline = injector.instanceOf[timeline]
  val p: p = injector.instanceOf[p]
  val sampleMissingReturns: String = "VAT Period 1 October 2021 to 31 December 2021"
  val sampleTimelineEvent: TimelineEvent = TimelineEvent(
    sampleMissingReturns,
    "Submit VAT Return by February 2023",
    Some("Submitted on time")
  )

  val sampleTimelineHtml: Html = html(
    timeline(
      Seq(sampleTimelineEvent)
    ),
    p(html(stringAsHtml("If you complete these actions we will remove your points in March 2023.")))
  )

  val sampleAgentTimelineHtml: Html = html(
    timeline(
      Seq(sampleTimelineEvent)
    ),
    p(html(stringAsHtml("If these actions are completed we will remove your client’s points in March 2023.")))
  )

  object Selectors extends BaseSelectors {
    val timelineEvent: Int => String = (item: Int) => s"#main-content > div > div > ol > li:nth-child($item)"

    val pointExpiryText = "#expiry-text"

    val pointExpiryDate = "#expiry-date"

    val missingDeadlineText = "#missing-deadline"

    val returnToVATLink = "#main-content > div > div > p > a"

    val betaFeedbackBannerText =  "body > div > div.govuk-phase-banner > p > span"
  }

  "ComplianceView" should {

    "when a VAT trader is on the page" must {
      def applyView(timelineContent: Html, periodOfComplianceAchievementDate: String): HtmlFormat.Appendable = {
        compliancePage.apply(timelineContent, periodOfComplianceAchievementDate)(fakeRequest, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithMissingReturns: Document =
        asDocument(applyView(html(stringAsHtml(sampleMissingReturns)), "1 January 2022"))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.breadcrumbWithLink(1) -> breadcrumb1,
        Selectors.breadcrumbWithLink(2) -> breadcrumb2,
        Selectors.breadcrumbWithLink(3) -> breadcrumb3,
        Selectors.pointExpiryText -> expiryContent,
        Selectors.pointExpiryDate -> "1 January 2022",
        Selectors.missingDeadlineText -> missingDeadlineContent,
        Selectors.returnToVATLink -> returnToVAT

      )

      behave like pageWithExpectedMessages(expectedContent)

      "contain the VAT period and compliance timeline when there is missing VAT returns" in {
        docWithMissingReturns.body().toString.contains("VAT Period 1 October 2021 to 31 December 2021") shouldBe true
      }

      "have the correct breadcrumb links" in {
        docWithMissingReturns.select(Selectors.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.btaUrl
        docWithMissingReturns.select(Selectors.breadcrumbWithLink(2)).attr("href") shouldBe appConfig.vatOverviewUrl
        docWithMissingReturns.select(Selectors.breadcrumbWithLink(3)).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
      }

      "have the correct 'Return to VAT penalties and appeals' link" in {
        docWithMissingReturns.select(Selectors.returnToVATLink).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
      }

    }

    "when a agent is on the page" must {
      def applyView(timelineContent: Html, periodOfComplianceAchievementDate: String): HtmlFormat.Appendable = {
        compliancePage.apply(timelineContent, periodOfComplianceAchievementDate)(implicitly, implicitly, implicitly, agentUser)
      }

      implicit val agentDocWithMissingReturns: Document =
        asDocument(applyView(html(stringAsHtml(sampleMissingReturns)), "1 January 2022"))

      val expectedContent = Seq(
        Selectors.title -> agentTitle,
        Selectors.h1 -> agentHeading,
        Selectors.pointExpiryText -> expiryContent,
        Selectors.pointExpiryDate -> "1 January 2022",
        Selectors.missingDeadlineText -> agentMissingDeadlineContent
      )

      behave like pageWithExpectedMessages(expectedContent)

    }

    "have a beta banner with the feedback correct content and a link with the 'backURL' queryParam" in {
      def applyView(): HtmlFormat.Appendable = compliancePage.apply(html(), "")(implicitly, implicitly, implicitly, vatTraderUser)
      val doc: Document = asDocument(applyView())

      doc.select(Selectors.betaFeedbackBannerText).text() shouldBe "This is a new service - your feedback will help us to improve it."
      doc.select("#beta-feedback-link").attr("href").contains("http://localhost:9250/contact/beta-feedback?service=vat-penalties&backUrl=") shouldBe true
    }

  }
}