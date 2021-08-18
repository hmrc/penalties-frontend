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

import assets.messages.ComplianceMessages._
import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import play.twirl.api.{Html, HtmlFormat}
import utils.ViewUtils
import viewmodels.TimelineEvent
import views.behaviours.ViewBehaviours
import views.html.ComplianceView
import views.html.components.{p, timeline}

class ComplianceViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val compliancePage: ComplianceView = injector.instanceOf[ComplianceView]
  val timeline = injector.instanceOf[timeline]
  val p = injector.instanceOf[p]
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
    val staticListItem = (item: Int) => s"#main-content li:nth-child($item)"

    val submitTheseMissingReturnsH2 = "#submit-these-missing-returns"

    val completeTheseActionsOnTimeH2 = "#complete-these-actions-on-time"

    val timelineEvent = (item: Int) => s"#main-content > div > div > ol > li:nth-child($item)"

    val pointExpiryContent = "#point-expiry-date"
  }

  "ComplianceView" should {

    "when a VAT trader is on the page" must {
      def applyView(isUnsubmittedReturns: Boolean, contentForMissingReturns: Html, timelineContent: Html): HtmlFormat.Appendable = {
        compliancePage.apply(isUnsubmittedReturns, contentForMissingReturns, timelineContent)(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithMissingReturns: Document = asDocument(applyView(isUnsubmittedReturns = true, html(stringAsHtml(sampleMissingReturns)), sampleTimelineHtml))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.pNthChild(2) -> p1,
        Selectors.pNthChild(3) -> p2,
        Selectors.staticListItem(1) -> li1,
        Selectors.staticListItem(2) -> li2,
        Selectors.submitTheseMissingReturnsH2 -> h2MissingReturns,
        Selectors.completeTheseActionsOnTimeH2 -> completeTheseActionsOnTime
      )

      behave like pageWithExpectedMessages(expectedContent)

      "contain the VAT period and compliance timeline when there is missing VAT returns" in {
        docWithMissingReturns.body().toString.contains("VAT Period 1 October 2021 to 31 December 2021") shouldBe true
      }

      "show a timeline with returns to be submitted and a point expiry date " in {
        docWithMissingReturns.select(Selectors.timelineEvent(1) + " > h2").text shouldBe "VAT Period 1 October 2021 to 31 December 2021"
        docWithMissingReturns.select(Selectors.timelineEvent(1) + " > span").text shouldBe "Submit VAT Return by February 2023"
        docWithMissingReturns.select(Selectors.timelineEvent(1) + " > div > p > strong").text shouldBe "Submitted on time"
        docWithMissingReturns.body().toString.contains("If you complete these actions we will remove your points in March 2023.") shouldBe true
      }

      "not display 'submit these missing returns' when the user has no missing returns" in {
        implicit val docWithNoMissingReturns: Document = asDocument(applyView(isUnsubmittedReturns = false, html(), html()))
        docWithNoMissingReturns.select(Selectors.submitTheseMissingReturnsH2).hasText shouldBe false
      }
    }

    "when a agent is on the page" must {
      def applyView(isUnsubmittedReturns: Boolean, contentForMissingReturns: Html, timelineContent: Html): HtmlFormat.Appendable = {
        compliancePage.apply(isUnsubmittedReturns, contentForMissingReturns, timelineContent)(implicitly, implicitly, implicitly, agentUser)
      }

      implicit val agentDocWithMissingReturns: Document = asDocument(applyView(isUnsubmittedReturns = true, html(stringAsHtml(sampleMissingReturns)), sampleAgentTimelineHtml))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.pNthChild(2) -> agentP1,
        Selectors.pNthChild(3) -> p2,
        Selectors.staticListItem(1) -> agentLi1,
        Selectors.staticListItem(2) -> agentLi2,
        Selectors.submitTheseMissingReturnsH2 -> h2MissingReturns,
        Selectors.completeTheseActionsOnTimeH2 -> completeTheseActionsOnTime
      )

      behave like pageWithExpectedMessages(expectedContent)

      "show a timeline with returns to be submitted and a point expiry date " in {
        agentDocWithMissingReturns.select(Selectors.timelineEvent(1) + " > h2").text shouldBe "VAT Period 1 October 2021 to 31 December 2021"
        agentDocWithMissingReturns.select(Selectors.timelineEvent(1) + " > span").text shouldBe "Submit VAT Return by February 2023"
        agentDocWithMissingReturns.select(Selectors.timelineEvent(1) + " > div > p > strong").text shouldBe "Submitted on time"
        agentDocWithMissingReturns.body().toString.contains("If these actions are completed we will remove your client’s points in March 2023.") shouldBe true
      }
    }
  }
}