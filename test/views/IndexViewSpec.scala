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
import models.User
import models.communication.{Communication, CommunicationTypeEnum}
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.nodes.Document
import play.twirl.api.{Html, HtmlFormat}
import utils.SessionKeys
import viewmodels.{LatePaymentPenaltySummaryCard, LateSubmissionPenaltySummaryCard, SummaryCardHelper}
import views.behaviours.ViewBehaviours
import views.html.IndexView
import views.html.components.p

import java.time.LocalDateTime

class IndexViewSpec extends SpecBase with ViewBehaviours {

  val pElement: p = injector.instanceOf[p]
  val helper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]
  val indexViewPage: IndexView = injector.instanceOf[IndexView]

  implicit val user: User[_] = vatTraderUser

  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 0)
  val sampleDate2: LocalDateTime = LocalDateTime.of(2021, 2, 1, 1, 1, 0)

  val summaryLSPCardToShowOnThePage: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
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
    quarterlyThreshold,
    1
  ).head

  val summaryLSPCardRepresentingRemovedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
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
    quarterlyThreshold,
    1
  ).head

  val summaryLSPCardRepresentingAddedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
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
    quarterlyThreshold,
    1
  ).head

  val summaryLPPCardToShowOnThePage: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(LatePaymentPenalty(
      `type` = PenaltyTypeEnum.Financial,
      id = penaltyId,
      reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
      dateCreated = LocalDateTime.now,
      status = PointStatusEnum.Active,
      appealStatus = None,
      period = PaymentPeriod(
        startDate = LocalDateTime.now,
        endDate = LocalDateTime.now,
        dueDate = LocalDateTime.now,
        PaymentStatusEnum.Due
      ),
      communications = Seq(Communication(
        `type` = CommunicationTypeEnum.letter,
        dateSent = LocalDateTime.now,
        documentId = "123456789"
      )),
      financial = Financial(
        amountDue = 0.00,
        outstandingAmountDue = 0.00,
        dueDate = LocalDateTime.now
      )
    )))
  ).get.head

  "IndexView" when {

    val indexViewPage = injector.instanceOf[IndexView]

    val agentRequest = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")

    object Selectors extends BaseSelectors {
      def rowItem(summaryCard: String, i: Int) = s"$summaryCard > div > dl > div:nth-child($i) > dt"

      val serviceNameLink = ".govuk-header__content a.govuk-header__link"
    }

    "IndexView" when {

      val contentToDisplayOnPage: Html = pElement(content = Html("This is some content."), id = Some("sample-content"))
      val contentLPPToDisplayOnPage: Html = pElement(content = Html("This is some LPP content."), id = Some("sample-lpp-content"))

      def applyVATTraderView(lppData: Seq[LatePaymentPenalty] = Seq.empty): HtmlFormat.Appendable =
        indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage,
        helper.populateLateSubmissionPenaltyCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold, 1),
        helper.populateLatePaymentPenaltyCard(Some(lppData)), "0")(fakeRequest, implicitly, implicitly, vatTraderUser)

      implicit val vatTraderDoc: Document = asDocument(applyVATTraderView(sampleLatePaymentPenaltyData))
      implicit val vatTraderDocLPPAdditionalPenalty: Document = asDocument(applyVATTraderView(Seq(sampleLatePaymentPenaltyAdditional)))
      implicit val vatTraderDocWithLPPVATUnpaid: Document = asDocument(applyVATTraderView(sampleLatePaymentPenaltyDataUnpaidVAT))
      implicit val vatTraderDocWithLPPVATPaymentDate: Document = asDocument(applyVATTraderView(sampleLatePaymentPenaltyDataVATPaymentDate))

      def applyAgentView(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage,
        helper.populateLateSubmissionPenaltyCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold, 1)
        , helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyData)), "0")(agentRequest, implicitly, implicitly, agentUser)

      implicit val agentDoc: Document = asDocument(applyAgentView())

      "agent is on page" must {
        val expectedAgentContent = Seq(
          Selectors.serviceNameLink -> agentHeading,
          Selectors.h1 -> heading,
          Selectors.tab(1) -> tab1,
          Selectors.tab(2) -> tab2,
          Selectors.tabHeading -> subheading,
          Selectors.tabHeadingLPP -> subheadingLPP
        )

        behave like pageWithExpectedMessages(expectedAgentContent)(agentDoc)

        "not have breadcrumb links for 'Your VAT account'" in {
          agentDoc.select(Selectors.breadcrumbs(1)).isEmpty shouldBe true
          agentDoc.select(Selectors.breadcrumbWithLink(2)).isEmpty shouldBe true
        }

        "show the content and headings when the client has outstanding payments" in {
          val sampleContent = pElement(content = Html("sample content"))
          def applyView(): HtmlFormat.Appendable = {
            indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage,
              helper.populateLateSubmissionPenaltyCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold,
              1), helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyData)), "0", whatYouOweContent = Some(sampleContent))(agentRequest, implicitly, implicitly, agentUser)
          }
          implicit val doc: Document = asDocument(applyView())
          doc.select("#what-is-owed > h2").text shouldBe "Overview"
          doc.select("#what-is-owed > p").first.text shouldBe "Your client owes:"
          doc.select("#main-content h2:nth-child(3)").text shouldBe "Penalty and appeal details"
          doc.select("#what-is-owed > a").text shouldBe "Check amounts"
          doc.select("#main-content .govuk-details__summary-text").text shouldBe "Payment help"
        }
      }

      "user is on page" must {

        val expectedContent = Seq(
          Selectors.h1 -> heading,
          Selectors.breadcrumbWithLink(1) -> breadcrumb1,
          Selectors.breadcrumbs(2) -> breadcrumb2,
          Selectors.tab(1) -> tab1,
          Selectors.tab(2) -> tab2,
          Selectors.tabHeading -> subheading,
          Selectors.tabHeadingLPP -> subheadingLPP
        )

        behave like pageWithExpectedMessages(expectedContent)(vatTraderDoc)

        "have correct route for breadcrumb link" in {
          vatTraderDoc.select(Selectors.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.vatOverviewUrl
        }

        "have the specified content displayed on the page" in {
          vatTraderDoc.select("#sample-content").text() shouldBe "This is some content."
        }

        "display the removed point due to a change in submission filing" in {
          implicit val documentWithOneSummaryCardComponent: Document = {
            asDocument(indexViewPage.apply(contentToDisplayOnPage,
              contentLPPToDisplayOnPage, Seq(summaryLSPCardRepresentingRemovedPoint), None, "0")(fakeRequest, implicitly, implicitly, vatTraderUser))
          }
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
          implicit val documentWithOneSummaryCardComponent: Document = {
            asDocument(indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage, Seq(summaryLSPCardRepresentingAddedPoint), None, "0")(fakeRequest,
              implicitly, implicitly, vatTraderUser))
          }
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

        "populate summary card when user has LSPs" in {

          vatTraderDoc.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLSPCard)).text shouldBe lspHeader
          vatTraderDoc.select(Selectors.summaryCardHeaderTag(Selectors.summaryLSPCard)).text shouldBe activeTag
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLSPCard, 1)).text shouldBe period
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLSPCard, 2)).text shouldBe returnDue
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLSPCard, 3)).text shouldBe returnSubmitted
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLSPCard, 4)).text shouldBe pointExpiration
          vatTraderDoc.select(Selectors.summaryCardFooterLink(Selectors.summaryLSPCard)).text shouldBe appealPointLinkText
          vatTraderDoc.select(Selectors.summaryCardFooterLink(Selectors.summaryLSPCard)).attr("href") shouldBe redirectToAppealUrlForLSP
        }

        "populate summary card when user has LPPs" in {
          vatTraderDoc.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLPPCard)).text shouldBe lppHeader
          vatTraderDoc.select(Selectors.viewCalculation).text shouldBe viewCalculationLink
          vatTraderDoc.select(Selectors.viewCalculation).attr("href") shouldBe "/penalties/calculation?penaltyId=123456789&isAdditional=false"
          vatTraderDoc.select(Selectors.summaryCardHeaderTag(Selectors.summaryLPPCard)).text shouldBe paidTag
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLPPCard, 1)).text shouldBe period
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLPPCard, 2)).text shouldBe paymentDue
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLPPCard, 3)).text shouldBe vatPaymentDate
          vatTraderDoc.select(Selectors.rowItem(Selectors.summaryLPPCard, 4)).text shouldBe penaltyReason
          vatTraderDoc.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).text shouldBe appealPointText
          vatTraderDoc.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).attr("href") shouldBe redirectToAppealUrlForLPP
        }

        "populate summary card when user has LPPs with VAT unpaid" in {
          vatTraderDocWithLPPVATUnpaid.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLPPCard)).text shouldBe lppHeader
          vatTraderDocWithLPPVATUnpaid.select(Selectors.viewCalculation).text shouldBe viewCalculationLink
          vatTraderDocWithLPPVATUnpaid.select(Selectors.summaryCardHeaderTag(Selectors.summaryLPPCard)).text shouldBe
            overduePartiallyPaidTag(200)
          vatTraderDocWithLPPVATUnpaid.select(Selectors.rowItem(Selectors.summaryLPPCard, 1)).text shouldBe period
          vatTraderDocWithLPPVATUnpaid.select(Selectors.rowItem(Selectors.summaryLPPCard, 2)).text shouldBe paymentDue
          vatTraderDocWithLPPVATUnpaid.select(Selectors.rowItem(Selectors.summaryLPPCard, 3)).text shouldBe vatPaymentDate
          vatTraderDocWithLPPVATUnpaid.select(Selectors.rowItem(Selectors.summaryLPPCard, 4)).text shouldBe penaltyReason
          vatTraderDocWithLPPVATUnpaid.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).text shouldBe
            checkAppeal
          vatTraderDocWithLPPVATUnpaid.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).attr("href") shouldBe
            redirectToAppealObligationUrlForLPP
        }

        "populate summary card when user has LPPs with VAT payment date" in {
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLPPCard)).text shouldBe lppHeader
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.viewCalculation).text shouldBe viewCalculationLink
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.summaryCardHeaderTag(Selectors.summaryLPPCard)).text shouldBe
            overduePartiallyPaidTag(200)
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.rowItem(Selectors.summaryLPPCard, 1)).text shouldBe period
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.rowItem(Selectors.summaryLPPCard, 2)).text shouldBe paymentDue
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.rowItem(Selectors.summaryLPPCard, 3)).text shouldBe vatPaymentDate
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.rowItem(Selectors.summaryLPPCard, 4)).text shouldBe penaltyReason
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).text shouldBe
            checkAppeal
          vatTraderDocWithLPPVATPaymentDate.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).attr("href") shouldBe
            redirectToAppealObligationUrlForLPP
        }

        "populate summary card when user has LPPs with additional penalties" in {
          vatTraderDocLPPAdditionalPenalty.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLPPCard)).text shouldBe additionalPenaltyHeader
          vatTraderDocLPPAdditionalPenalty.select(Selectors.summaryCardHeaderTag(Selectors.summaryLPPCard)).text shouldBe paidTag
          vatTraderDocLPPAdditionalPenalty.select(Selectors.rowItem(Selectors.summaryLPPCard, 1)).text shouldBe period
          vatTraderDocLPPAdditionalPenalty.select(Selectors.rowItem(Selectors.summaryLPPCard, 2)).text shouldBe penaltyReason
          vatTraderDocLPPAdditionalPenalty.select(Selectors.rowItem(Selectors.summaryLPPCard, 3)).text shouldBe chargedDailyFrom
          vatTraderDocLPPAdditionalPenalty.select(Selectors.summaryCardFooterLink(Selectors.summaryLPPCard)).text shouldBe appealPointText
          vatTraderDocLPPAdditionalPenalty.select(Selectors.summaryCardFooterLink(
            Selectors.summaryLPPCard)).attr("href") shouldBe redirectToAppealObligationUrlForLPPAdditional
          vatTraderDocLPPAdditionalPenalty.select(Selectors.viewCalculation).text shouldBe viewCalculationLink
          vatTraderDocLPPAdditionalPenalty.select(
            Selectors.viewCalculation).attr("href") shouldBe "/penalties/calculation?penaltyId=123456789&isAdditional=true"
        }

        "populate summary card when user has LPPs and has appealed them" in {
          def applyVATTraderViewWithLPPAppeal(): HtmlFormat.Appendable = indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage,
            helper.populateLateSubmissionPenaltyCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold, 1),
            helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyAppealedData)), "0")(fakeRequest, implicitly, implicitly, vatTraderUser)

          implicit val vatTraderDocWithLPPAppeal: Document = asDocument(applyVATTraderViewWithLPPAppeal())

          vatTraderDocWithLPPAppeal.select(Selectors.rowItem(Selectors.summaryLPPCard, 5)).text shouldBe appealStatus
        }

        "populate summary card when user has a penalty point from un-submitted VAT return with due status" in {
          def applyView(): HtmlFormat.Appendable = {
            indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage,
              helper.populateLateSubmissionPenaltyCard(sampleReturnNotSubmittedPenaltyPointData, quarterlyThreshold,
                1), helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyData)), "0")(fakeRequest, implicitly, implicitly, vatTraderUser)
          }

          implicit val doc: Document = asDocument(applyView())

          doc.select(Selectors.summaryCardHeaderTitle(Selectors.summaryLSPCard)).text shouldBe lspHeader
          doc.select(Selectors.summaryCardHeaderTag(Selectors.summaryLSPCard)).text shouldBe overdueTag
          doc.select(Selectors.rowItem(Selectors.summaryLSPCard, 1)).text shouldBe period
          doc.select(Selectors.rowItem(Selectors.summaryLSPCard, 2)).text shouldBe returnDue
          doc.select(Selectors.rowItem(Selectors.summaryLSPCard, 3)).text shouldBe returnSubmitted
          doc.select(Selectors.summaryCardFooterLink(Selectors.summaryLSPCard)).text shouldBe checkAppeal
          doc.select(Selectors.summaryCardFooterLink(Selectors.summaryLSPCard)).attr("href") shouldBe redirectToAppealObligationUrlForLSP
        }

        "show the content and headings when the user has outstanding payments" in {
          val sampleContent = pElement(content = Html("sample content"))
          def applyView(): HtmlFormat.Appendable = {
            indexViewPage.apply(contentToDisplayOnPage, contentLPPToDisplayOnPage, helper.populateLateSubmissionPenaltyCard(Seq(samplePenaltyPoint),
              quarterlyThreshold, 1), helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyDataUnpaidVAT)),
              "0", whatYouOweContent = Some(sampleContent))(fakeRequest, implicitly, implicitly, vatTraderUser)
          }
          implicit val doc: Document = asDocument(applyView())
          doc.select("#what-is-owed > h2").text shouldBe "Overview"
          doc.select("#what-is-owed > p").first.text shouldBe "You owe:"
          doc.select("#main-content h2:nth-child(4)").text shouldBe "Penalty and appeal details"
          doc.select("#what-is-owed > a").text shouldBe "Check amounts and pay"
          doc.select("#what-is-owed > h3").text shouldBe "If you cannot pay today"
        }
      }
    }
  }
}
