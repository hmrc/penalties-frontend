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

import assets.messages.CalculationMessages._
import assets.messages.IndexMessages.{breadcrumb1, breadcrumb2, breadcrumb3}
import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.{CalculationLPP2View, CalculationLPPView}

class CalculationViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val calculationPage: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationLPP2Page: CalculationLPP2View = injector.instanceOf[CalculationLPP2View]

  object Selector extends BaseSelectors {

    val summaryListRowKey: Int => String = (item: Int) => s"#main-content dl > div:nth-child($item) > dt"

    val summaryListRowValue: Int => String = (item: Int) => s"#main-content dl > div:nth-child($item) > dd"

    val bulletNthChild: Int => String = (nThChild: Int) => s"#main-content > div > div > ul > li:nth-child($nThChild)"

    val govukBody: Int => String = (nthChild: Int) => s"#main-content .govuk-body:nth-of-type($nthChild)"

    val warning = "#main-content div .govuk-warning-text"

    val link = "#main-content a"

    val howPenaltyIsApplied = "#how-penalty-is-applied"

    val fifteenDayCalculation = "#15-day-calculation"

    val thirtyDayCalculation = "#30-day-calculation"

    val whenPenaltyIncreases = "#when-it-increases"

    val calculation = "#calculation"

    val betaFeedbackBannerText =  "body > div > div.govuk-phase-banner > p > span"

    val ttpInsetText = "#ttp-inset-text"
  }

  "CalculationView" should {

    "display the correct page" when {

      "it is an second penalty and the penalty is estimated" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            penaltyAmount = "50.50",
            amountReceived = "10.10",
            amountLeftToPay = "40.40",
            isTTPActive = false
          )(implicitly, implicitly, vatTraderUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.breadcrumbWithLink(1) -> breadcrumb1,
          Selector.breadcrumbWithLink(2) -> breadcrumb2,
          Selector.breadcrumbWithLink(3) -> breadcrumb3,
          Selector.title -> titleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreasesAccruing,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£50.50",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£10.10",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£40.40",
          Selector.h2 -> h2Estimates,
          Selector.govukBody(4) -> p2EstimatesLPP2,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)

        "have the correct breadcrumb links" in {
          doc.select(Selector.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.btaUrl
          doc.select(Selector.breadcrumbWithLink(2)).attr("href") shouldBe appConfig.vatOverviewUrl
          doc.select(Selector.breadcrumbWithLink(3)).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
        }
      }

      "it is an second penalty and the penalty is estimated (TTP active)" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            penaltyAmount = "50.50",
            amountReceived = "10.10",
            amountLeftToPay = "40.40",
            isTTPActive = true
          )(implicitly, implicitly, vatTraderUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.breadcrumbWithLink(1) -> breadcrumb1,
          Selector.breadcrumbWithLink(2) -> breadcrumb2,
          Selector.breadcrumbWithLink(3) -> breadcrumb3,
          Selector.title -> titleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreasesAccruing,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£50.50",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£10.10",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£40.40",
          Selector.ttpInsetText -> ttpActiveInsetText,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(4) -> p2EstimatesTTPActive,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)

        "have the correct breadcrumb links" in {
          doc.select(Selector.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.btaUrl
          doc.select(Selector.breadcrumbWithLink(2)).attr("href") shouldBe appConfig.vatOverviewUrl
          doc.select(Selector.breadcrumbWithLink(3)).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
        }
      }

      "it is a second penalty and the penalty is not estimated" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = false,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = Some("17 October 2022"),
            penaltyAmount = "50.50",
            amountReceived = "40.10",
            amountLeftToPay = "10.40",
            isTTPActive = false)(implicitly, implicitly, vatTraderUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> titleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreases,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> dueDate,
          Selector.summaryListRowValue(1) -> "17 October 2022",
          Selector.summaryListRowKey(2) -> th2LPP,
          Selector.summaryListRowValue(2) -> "£50.50",
          Selector.summaryListRowKey(3) -> th3LPP,
          Selector.summaryListRowValue(3) -> "£40.10",
          Selector.summaryListRowKey(4) -> th4LPP,
          Selector.summaryListRowValue(4) -> "£10.40",
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "it is a first penalty" must {
        def applyView(calculationRow: Seq[String]): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100.00",
            penaltyAmount = "400.00",
            amountLeftToPay = "300.00",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = false,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = Some("7 September 2022"),
            isTPPActive = false)(implicitly, implicitly, vatTraderUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £3,850.00 (the unpaid VAT 15 days after the due date)")))
        implicit val docWith2Calculations: Document = asDocument(applyView(Seq("2% of £3,850.00 (the unpaid VAT 15 days after the due date) = £77.00",
          "2% of £3,850.00 (the unpaid VAT 30 days after the due date) = £77.00")))


        val expectedContent = Seq(
          Selector.breadcrumbWithLink(1) -> breadcrumb1,
          Selector.breadcrumbWithLink(2) -> breadcrumb2,
          Selector.breadcrumbWithLink(3) -> breadcrumb3,
          Selector.title -> titleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
          Selector.fifteenDayCalculation -> onePartCalculation("2% of £3,850.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th2LPP,
          Selector.summaryListRowValue(1) -> "£400.00",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100.00",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300.00",
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)

        "have the correct breadcrumb links" in {
          docWithOnlyOneCalculation.select(Selector.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.btaUrl
          docWithOnlyOneCalculation.select(Selector.breadcrumbWithLink(2)).attr("href") shouldBe appConfig.vatOverviewUrl
          docWithOnlyOneCalculation.select(Selector.breadcrumbWithLink(3)).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
        }

        "when there is 2 calculations - show both" must {
          val expectedContent = Seq(
            Selector.title -> titleLPP,
            Selector.periodHiddenText -> periodHiddenText,
            Selector.periodWithText -> periodWithText,
            Selector.h1 -> headingLPP,
            Selector.howPenaltyIsApplied -> howPenaltyIsApplied30Days,
            Selector.thirtyDayCalculation -> twoPartCalculation,
            Selector.bulletNthChild(1) -> "2% of £3,850.00 (the unpaid VAT 15 days after the due date) = £77.00",
            Selector.bulletNthChild(2) -> "2% of £3,850.00 (the unpaid VAT 30 days after the due date) = £77.00",
            Selector.summaryListRowKey(1) -> dueDate,
            Selector.summaryListRowValue(1) -> "7 September 2022",
            Selector.summaryListRowKey(2) -> th2LPP,
            Selector.summaryListRowValue(2) -> "£400.00",
            Selector.summaryListRowKey(3) -> th3LPP,
            Selector.summaryListRowValue(3) -> "£100.00",
            Selector.summaryListRowKey(4) -> th4LPP,
            Selector.summaryListRowValue(4) -> "£300.00",
            Selector.link -> link
          )
          behave like pageWithExpectedMessages(expectedContent)(docWith2Calculations)
        }
      }

      "it is a first penalty and is estimated" must {
        def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100.00",
            penaltyAmount = "400.00",
            amountLeftToPay = "300.00",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            isTPPActive = false)(implicitly, implicitly, vatTraderUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £3,850.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

        val expectedContent = Seq(
          Selector.title -> titleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
          Selector.govukBody(1) -> estimateFooterNoteBillPayment,
          Selector.fifteenDayCalculation -> onePartCalculation("2% of £3,850.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£400.00",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100.00",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300.00",
          Selector.warning -> estimateFooterNoteWarningTrader,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(3) -> p2EstimatesLPP1,
          Selector.bulletNthChild(1) -> b1Estimates,
          Selector.bulletNthChild(2) -> b2Estimates,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
      }

      "it is a first penalty and is estimated (TPP active)" must {
        def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100.00",
            penaltyAmount = "400.00",
            amountLeftToPay = "300.00",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            isTPPActive = true)(implicitly, implicitly, vatTraderUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £3,850.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

        val expectedContent = Seq(
          Selector.title -> titleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
          Selector.govukBody(1) -> estimateFooterNoteBillPayment,
          Selector.fifteenDayCalculation -> onePartCalculation("2% of £3,850.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£400.00",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100.00",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300.00",
          Selector.ttpInsetText -> ttpActiveInsetText,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(3) -> p2EstimatesTTPActive,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
      }

      "it is a first penalty and with Penalty Amount and the user is an Agent" must {
        def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100",
            penaltyAmount = "400",
            amountLeftToPay = "300",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            isTPPActive = false)(implicitly, implicitly, agentUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.govukBody(1) -> estimateFooterNoteBillPayment,
          Selector.govukBody(2) -> onePartCalculation("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th1LPPEstimate,
          Selector.summaryListRowValue(1) -> "£400",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300",
          Selector.warning -> estimateFooterNoteWarningAgent,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(3) -> p2EstimatesLPP1,
          Selector.bulletNthChild(1) -> b1EstimatesAgent,
          Selector.bulletNthChild(2) -> b2Estimates,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
      }

      "it is a first penalty and the user is an Agent (estimate)" must {
        def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100",
            penaltyAmount = "400",
            amountLeftToPay = "300",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            isTPPActive = false)(implicitly, implicitly, agentUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
          Selector.govukBody(1) -> estimateFooterNoteBillPayment,
          Selector.fifteenDayCalculation -> onePartCalculation("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£400",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300",
          Selector.warning -> estimateFooterNoteWarningAgent,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(3) -> p2EstimatesLPP1,
          Selector.bulletNthChild(1) -> b1EstimatesAgent,
          Selector.bulletNthChild(2) -> b2Estimates,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
      }

      "it is a first penalty and the user is an Agent (estimate and TPP is active)" must {
        def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
          calculationPage.apply(
            amountReceived = "100",
            penaltyAmount = "400",
            amountLeftToPay = "300",
            calculationRowSeq = calculationRow,
            isPenaltyEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = None,
            isTPPActive = true)(implicitly, implicitly, agentUser)
        }

        implicit val docWithOnlyOneCalculation: Document =
          asDocument(applyView(Seq("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodHiddenText -> periodHiddenText,
          Selector.periodWithText -> periodWithText,
          Selector.h1 -> headingLPP,
          Selector.govukBody(1) -> estimateFooterNoteBillPayment,
          Selector.govukBody(2) -> onePartCalculation("2% of £10,000.00 (the unpaid VAT 15 days after the due date)"),
          Selector.summaryListRowKey(1) -> th1LPPEstimate,
          Selector.summaryListRowValue(1) -> "£400",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£100",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£300",
          Selector.ttpInsetText -> ttpActiveAgentInsetText,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(3) -> p2EstimatesAgentTTPActive,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
      }

      "it is a second penalty and the user is an Agent (no estimate)" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = false,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = Some("17 October 2022"),
            penaltyAmount = "50.50",
            amountReceived = "40.10",
            amountLeftToPay = "10.40",
            isTTPActive = false)(implicitly, implicitly, agentUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreases,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> dueDate,
          Selector.summaryListRowValue(1) -> "17 October 2022",
          Selector.summaryListRowKey(2) -> th2LPP,
          Selector.summaryListRowValue(2) -> "£50.50",
          Selector.summaryListRowKey(3) -> th3LPP,
          Selector.summaryListRowValue(3) -> "£40.10",
          Selector.summaryListRowKey(4) -> th4LPP,
          Selector.summaryListRowValue(4) -> "£10.40",
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "it is a second penalty and the user is an Agent (estimate)" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = Some("17 October 2022"),
            penaltyAmount = "50.50",
            amountReceived = "40.10",
            amountLeftToPay = "10.40",
            isTTPActive = false)(implicitly, implicitly, agentUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreasesAccruingAgent,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£50.50",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£40.10",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£10.40",
          Selector.h2 -> h2Estimates,
          Selector.govukBody(4) -> p2EstimatesLPP2Agent,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "it is a second penalty and the user is an Agent (estimate - TTP active)" must {
        def applyView(): HtmlFormat.Appendable = {
          calculationLPP2Page.apply(
            isEstimate = true,
            startDate = "1 April 2022",
            endDate = "30 June 2022",
            dueDate = Some("17 October 2022"),
            penaltyAmount = "50.50",
            amountReceived = "40.10",
            amountLeftToPay = "10.40",
            isTTPActive = true)(implicitly, implicitly, agentUser)
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> agentTitleLPP,
          Selector.periodWithText -> periodWithText,
          Selector.HeaderTextNotVisible -> periodHiddenText,
          Selector.h1 -> headingLPP,
          Selector.howPenaltyIsApplied -> howPenaltyIsAppliedLPP2,
          Selector.whenPenaltyIncreases -> whenPenaltyIncreasesAccruingAgent,
          Selector.calculation -> lpp2Calculation,
          Selector.summaryListRowKey(1) -> th2LPPAccruing,
          Selector.summaryListRowValue(1) -> "£50.50",
          Selector.summaryListRowKey(2) -> th3LPP,
          Selector.summaryListRowValue(2) -> "£40.10",
          Selector.summaryListRowKey(3) -> th4LPP,
          Selector.summaryListRowValue(3) -> "£10.40",
          Selector.ttpInsetText -> ttpActiveAgentInsetText,
          Selector.h2 -> h2Estimates,
          Selector.govukBody(4) -> p2EstimatesAgentTTPActive,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }

  "have a beta banner with the feedback correct content and a link with the 'backURL' queryParam" in {
    def applyView(): HtmlFormat.Appendable = calculationLPP2Page.apply(
      isEstimate = true,
      startDate = "1 April 2022",
      endDate = "30 June 2022",
      dueDate = None,
      penaltyAmount = "50.50",
      amountReceived = "10.10",
      amountLeftToPay = "40.40",
      isTTPActive = false
    )(implicitly, implicitly, vatTraderUser)
    val doc: Document = asDocument(applyView())

    doc.select(Selector.betaFeedbackBannerText).text() shouldBe "This is a new service - your feedback will help us to improve it."
    doc.select("#beta-feedback-link").attr("href").contains("http://localhost:9250/contact/beta-feedback?service=vat-penalties&backUrl=") shouldBe true
  }
}
