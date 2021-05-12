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
import models.penalty.PenaltyPeriod
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.nodes.Document
import play.twirl.api.Html
import play.twirl.api.HtmlFormat
import viewmodels.{SummaryCard, SummaryCardHelper}
import views.behaviours.ViewBehaviours
import views.html.IndexView
import views.html.components.p

import java.time.LocalDateTime

class IndexViewSpec extends SpecBase with ViewBehaviours {

  val pElement: p = injector.instanceOf[p]
  val helper = injector.instanceOf[SummaryCardHelper]
  val indexViewPage = injector.instanceOf[IndexView]

  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 0)
  val sampleDate2: LocalDateTime = LocalDateTime.of(2021, 2, 1, 1, 1, 0)

  val summaryCardToShowOnThePage: SummaryCard = summaryCardHelper.populateCard(
    Seq(
    PenaltyPoint(
      `type` = PenaltyTypeEnum.Point,
      id = penaltyId,
      number = "1",
      dateCreated = sampleDate1,
      dateExpired = Some(sampleDate2.plusYears(2)),
      status = PointStatusEnum.Active,
      reason = None,
      period = Some(PenaltyPeriod(
        startDate = sampleDate1,
        endDate = sampleDate2,
        submission = Submission(
          dueDate = sampleDate2.plusMonths(1).plusDays(7),
          submittedDate = None,
          status = SubmissionStatusEnum.Submitted
        )
      )),
      communications = Seq.empty,
      financial = None)
    ),
    quarterlyThreshold
  ).head

  val summaryCardRepresentingRemovedPoint: SummaryCard = summaryCardHelper.populateCard(
    Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = penaltyId,
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate2.plusYears(2)),
        status = PointStatusEnum.Removed,
        reason = Some("This is a great reason."),
        period = Some(PenaltyPeriod(
          startDate = sampleDate1,
          endDate = sampleDate2,
          submission = Submission(
            dueDate = sampleDate2.plusMonths(1).plusDays(7),
            submittedDate = None,
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None)
    ),
    quarterlyThreshold
  ).head

  val summaryCardRepresentingAddedPoint: SummaryCard = summaryCardHelper.populateCard(
    Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = penaltyId,
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate2.plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = None,
        communications = Seq.empty,
        financial = None)
    ),
    quarterlyThreshold
  ).head

  "IndexView" when {

    val indexViewPage = injector.instanceOf[IndexView]

    object Selectors extends BaseSelectors {
      val rowItem: Int => String = i => s"#late-submission-penalties > section > div > dl > div:nth-child($i) > dt"
    }

    "IndexView" when {

      val contentToDisplayOnPage: Html = pElement(content = Html("This is some content."), id = Some("sample-content"))

      def applyView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage, helper.populateCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold), "0")

      implicit val doc: Document = asDocument(applyView())

      "user is on page" must {

        val expectedContent = Seq(
          Selectors.h1 -> heading,
          Selectors.breadcrumbWithLink(1) -> breadcrumb1,
          Selectors.breadcrumbs(2) -> breadcrumb2,
          Selectors.tab -> tab1,
          Selectors.tabHeading -> subheading
        )

        behave like pageWithExpectedMessages(expectedContent)

        "have correct route for breadcrumb link" in {
          doc.select(Selectors.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.vatOverviewUrl
        }

        "have the specified content displayed on the page" in {
          doc.select("#sample-content").text() shouldBe "This is some content."
        }

        "display the removed point due to a change in submission filing" in {
          implicit val documentWithOneSummaryCardComponent = asDocument(indexViewPage.apply(contentToDisplayOnPage, Seq(summaryCardRepresentingRemovedPoint), "0"))
          val summaryCard = documentWithOneSummaryCardComponent.select(".app-summary-card")
          summaryCard.select("header h3").text shouldBe "Penalty point"
          summaryCard.select("strong").text shouldBe "removed"
          val summaryCardBody = summaryCard.select(".app-summary-card__body")
          summaryCardBody.select("dt").get(0).text shouldBe "VAT Period"
          summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 1 February 2021"
          summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
          summaryCardBody.select("dd").get(1).text() shouldBe "This is a great reason."
          summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
          //TODO: Change to external guidance when available
          summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
          summaryCard.select("footer li").hasText shouldBe false
        }

        "display the added point due to a change in submission filing" in {
          implicit val documentWithOneSummaryCardComponent = asDocument(indexViewPage.apply(contentToDisplayOnPage, Seq(summaryCardRepresentingAddedPoint), "0"))
          val summaryCard = documentWithOneSummaryCardComponent.select(".app-summary-card")
          summaryCard.select("header h3").text shouldBe "Penalty point 1: adjustment point"
          summaryCard.select("strong").text shouldBe "active"
          val summaryCardBody = summaryCard.select(".app-summary-card__body")
          summaryCardBody.select("dt").get(0).text shouldBe "Added on"
          summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
          summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
          summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
          summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
          //TODO: Change to external guidance when available
          summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
          summaryCard.select("footer li").text shouldBe "You cannot appeal this point"
        }

        "populate summary card when user has penalty points" in {

          doc.select(Selectors.summaryCardHeaderTitle).text shouldBe penaltyPointHeader
          doc.select(Selectors.summaryCardHeaderTag).text shouldBe activeTag
          doc.select(Selectors.rowItem(1)).text shouldBe period
          doc.select(Selectors.rowItem(2)).text shouldBe returnDue
          doc.select(Selectors.rowItem(3)).text shouldBe returnSubmitted
          doc.select(Selectors.rowItem(4)).text shouldBe pointExpiration
          doc.select(Selectors.summaryCardFooterLink).text shouldBe appealLinkText
          doc.select(Selectors.summaryCardFooterLink).attr("href") shouldBe redirectToAppealUrl
        }

        "populate summary card when user has a penalty point from un-submitted VAT return with due status" in {
          def applyView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage, helper.populateCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold), "0")

          implicit val doc: Document = asDocument(applyView())

          doc.select(Selectors.summaryCardHeaderTitle).text shouldBe penaltyPointHeader
          doc.select(Selectors.summaryCardHeaderTag).text shouldBe overdueTag
          doc.select(Selectors.rowItem(1)).text shouldBe period
          doc.select(Selectors.rowItem(2)).text shouldBe returnDue
          doc.select(Selectors.rowItem(3)).text shouldBe returnSubmitted
          doc.select(Selectors.summaryCardFooterLink).text shouldBe appealLinkText
          doc.select(Selectors.summaryCardFooterLink).attr("href") shouldBe redirectToAppealUrl
        }
      }

      "user has unpaid LSP's but has submitted a VAT return - show a call to action to pay with no preceding text" in {
        def applyView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage,
          helper.populateCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold),
          "£200.00",
          isUnpaidLSPExists = true,
          isAnyUnpaidLSPAndNotSubmittedReturn = false)
        implicit val doc: Document = asDocument(applyView())
        doc.select(".govuk-body-l").text().isEmpty shouldBe true
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe "Total penalty to pay: £200.00"
      }

      "user has unpaid LSP's and has NOT submitted a VAT return - show a call to action to pay WITH preceding text" in {
        def applyView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage,
          helper.populateCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold),
          "£200.00",
          isUnpaidLSPExists = true,
          isAnyUnpaidLSPAndNotSubmittedReturn = true)
        implicit val doc: Document = asDocument(applyView())
        doc.select(".govuk-body-l").text() shouldBe submitAndPayVATPenaltyText
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe "Total penalty to pay: £200.00"
      }

      "user has unpaid LSP's and therefore needs to pay their penalties - show a button for them to check and pay what they owe" in {
        def applyView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage,
          helper.populateCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold),
          "£200.00",
          isUnpaidLSPExists = true,
          isAnyUnpaidLSPAndNotSubmittedReturn = true)
        implicit val doc: Document = asDocument(applyView())
        doc.select("button.govuk-button").get(0).text() shouldBe payVATPenaltyText
      }
    }
  }
}
