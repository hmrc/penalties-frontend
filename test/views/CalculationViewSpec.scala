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

import base.{BaseSelectors, SpecBase}
import assets.messages.CalculationMessages._
import assets.messages.IndexMessages.{breadcrumb1, breadcrumb2, breadcrumb3}
import play.twirl.api.HtmlFormat
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.{CalculationAdditionalView, CalculationLPPView}
import org.jsoup.nodes.Document

class CalculationViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val calculationPage: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationAdditionalPage: CalculationAdditionalView = injector.instanceOf[CalculationAdditionalView]

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
  }

  "CalculationView" should {

    "if it is an additional penalty and the penalty is estimated" must {
      def applyView(): HtmlFormat.Appendable = {
        calculationAdditionalPage.apply(
          daysSince31 = 7,
          isEstimate = true,
          additionalPenaltyRate = "4",
          startDate = "1 October 2022",
          endDate = "31 December 2022",
          penaltyAmount = "50.50",
          amountReceived = "10.10",
          amountLeftToPay = "40.40"
        )(implicitly, implicitly, implicitly, vatTraderUser)
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
        Selector.govukBody(1) -> p1Additional,
        Selector.summaryListRowKey(1) -> th2LPPAccruing,
        Selector.summaryListRowValue(1) -> "??50.50",
        Selector.summaryListRowKey(2) -> th2Additional,
        Selector.summaryListRowValue(2) -> "7 days",
        Selector.summaryListRowKey(3) -> th3Additional,
        Selector.summaryListRowValue(3) -> "4%",
        Selector.summaryListRowKey(4) -> th4Additional,
        Selector.summaryListRowValue(4) -> "VAT amount unpaid ?? 4% ?? number of days since day 31 ?? 365",
        Selector.summaryListRowKey(5) -> th3LPP,
        Selector.summaryListRowValue(5) -> "??10.10",
        Selector.summaryListRowKey(6) -> th4LPP,
        Selector.summaryListRowValue(6) -> "??40.40",
        Selector.govukBody(2) -> p2AdditionalLPP2,
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have the correct breadcrumb links" in {
        doc.select(Selector.breadcrumbWithLink(1)).attr("href") shouldBe appConfig.btaUrl
        doc.select(Selector.breadcrumbWithLink(2)).attr("href") shouldBe appConfig.vatOverviewUrl
        doc.select(Selector.breadcrumbWithLink(3)).attr("href") shouldBe controllers.routes.IndexController.onPageLoad.url
      }
    }

    "if it is an additional penalty and the penalty is not estimated" must {
      def applyView(): HtmlFormat.Appendable = {
        calculationAdditionalPage.apply(
          daysSince31 = 7,
          isEstimate = false,
          additionalPenaltyRate = "4",
          startDate = "1 October 2022",
          endDate = "31 December 2022",
          penaltyAmount = "50.50",
          amountReceived = "40.10",
          amountLeftToPay = "10.40")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selector.title -> titleLPP,
        Selector.periodWithText -> periodWithText,
        Selector.HeaderTextNotVisible -> periodHiddenText,
        Selector.h1 -> headingLPP,
        Selector.govukBody(1) -> p1Additional,
        Selector.summaryListRowKey(1) -> th2LPP,
        Selector.summaryListRowValue(1) -> "??50.50",
        Selector.summaryListRowKey(2) -> th2Additional,
        Selector.summaryListRowValue(2) -> "7 days",
        Selector.summaryListRowKey(3) -> th3Additional,
        Selector.summaryListRowValue(3) -> "4%",
        Selector.summaryListRowKey(4) -> th4Additional,
        Selector.summaryListRowValue(4) -> "VAT amount unpaid ?? 4% ?? number of days since day 31 ?? 365",
        Selector.summaryListRowKey(5) -> th3LPP,
        Selector.summaryListRowValue(5) -> "??40.10",
        Selector.summaryListRowKey(6) -> th4LPP,
        Selector.summaryListRowValue(6) -> "??10.40",
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "if it is not an additional penalty" must {
      def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
        calculationPage.apply(
          amountReceived = "100.00",
          penaltyAmount = "400.00",
          amountLeftToPay = "300.00",
          calculationRowSeq = calculationRow,
          isCalculationRowMultipleAmounts = isMultipleAmounts,
          isPenaltyEstimate = false,
          "1 October 2022",
          "31 December 2022",
          "7 February 2022",
          warningPenaltyAmount = "",
          warningDate = "")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithOnlyOneCalculation: Document =
        asDocument(applyView(Seq("2% of ??3,850.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))
      implicit val docWith2Calculations: Document = asDocument(applyView(Seq("2% of ??3,850.00 (the unpaid VAT 15 days after the due date) = ??77.00",
        "2% of ??3,850.00 (the unpaid VAT 30 days after the due date) = ??77.00"), isMultipleAmounts = true))


      val expectedContent = Seq(
        Selector.breadcrumbWithLink(1) -> breadcrumb1,
        Selector.breadcrumbWithLink(2) -> breadcrumb2,
        Selector.breadcrumbWithLink(3) -> breadcrumb3,
        Selector.title -> titleLPP,
        Selector.periodHiddenText -> periodHiddenText,
        Selector.periodWithText -> periodWithText,
        Selector.h1 -> headingLPP,
        Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
        Selector.fifteenDayCalculation -> onePartCalculation("2% of ??3,850.00 (the unpaid VAT 15 days after the due date)"),
        Selector.summaryListRowKey(1) -> th2LPP,
        Selector.summaryListRowValue(1) -> "??400.00",
        Selector.summaryListRowKey(2) -> th3LPP,
        Selector.summaryListRowValue(2) -> "??100.00",
        Selector.summaryListRowKey(3) -> th4LPP1,
        Selector.summaryListRowValue(3) -> "??300.00",
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
          Selector.bulletNthChild(1) -> "2% of ??3,850.00 (the unpaid VAT 15 days after the due date) = ??77.00",
          Selector.bulletNthChild(2) -> "2% of ??3,850.00 (the unpaid VAT 30 days after the due date) = ??77.00",
          Selector.summaryListRowKey(1) -> dueDate,
          Selector.summaryListRowValue(1) -> "7 February 2022",
          Selector.summaryListRowKey(2) -> th2LPP,
          Selector.summaryListRowValue(2) -> "??400.00",
          Selector.summaryListRowKey(3) -> th3LPP,
          Selector.summaryListRowValue(3) -> "??100.00",
          Selector.summaryListRowKey(4) -> th4LPP1,
          Selector.summaryListRowValue(4) -> "??300.00",
          Selector.link -> link
        )
        behave like pageWithExpectedMessages(expectedContent)(docWith2Calculations)
      }
    }

    "it is not an additional penalty and is estimated" must {
      def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
        calculationPage.apply(
          amountReceived = "100.00",
          penaltyAmount = "400.00",
          amountLeftToPay = "300.00",
          calculationRowSeq = calculationRow,
          isCalculationRowMultipleAmounts = isMultipleAmounts,
          isPenaltyEstimate = true,
          "1 October 2022",
          "31 December 2022",
          "7 February 2022",
          warningPenaltyAmount = "800.00",
          warningDate = "15 January 2023")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithOnlyOneCalculation: Document =
        asDocument(applyView(Seq("2% of ??3,850.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

      val expectedContent = Seq(
        Selector.title -> titleLPP,
        Selector.periodHiddenText -> periodHiddenText,
        Selector.periodWithText -> periodWithText,
        Selector.h1 -> headingLPP,
        Selector.howPenaltyIsApplied -> howPenaltyIsApplied15Days,
        Selector.govukBody(1) -> estimateFooterNoteBillPayment,
        Selector.fifteenDayCalculation -> onePartCalculation("2% of ??3,850.00 (the unpaid VAT 15 days after the due date)"),
        Selector.summaryListRowKey(1) -> th2LPPAccruing,
        Selector.summaryListRowValue(1) -> "??400.00",
        Selector.summaryListRowKey(2) -> th3LPP,
        Selector.summaryListRowValue(2) -> "??100.00",
        Selector.summaryListRowKey(3) -> th4LPP1,
        Selector.summaryListRowValue(3) -> "??300.00",
        Selector.warning -> estimateFooterNoteWarningTrader,
        Selector.h2 -> h2Additional,
        Selector.govukBody(3) -> p2AdditionalLPP1,
        Selector.bulletNthChild(1) -> b1Additional,
        Selector.bulletNthChild(2) -> b2Additional,
        Selector.link -> linkEstimatedTrader
      )

      behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
    }

    "it is not an additional penalty and with Penalty Amount and the user is an Agent" must {
      def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
        calculationPage.apply(
          amountReceived = "100",
          penaltyAmount = "400",
          amountLeftToPay = "300",
          calculationRowSeq = calculationRow,
          isCalculationRowMultipleAmounts = isMultipleAmounts,
          isPenaltyEstimate = true,
          "1 October 2022",
          "31 December 2022",
          "7 February 2022",
          warningPenaltyAmount = "800",
          warningDate = "15 January 2023")(implicitly, implicitly, implicitly, agentUser)
      }

      implicit val docWithOnlyOneCalculation: Document =
        asDocument(applyView(Seq("2% of ??10,000.00 (the unpaid VAT 15 days after the due date)"), isMultipleAmounts = false))

      val expectedContent = Seq(
        Selector.title -> titleLPP,
        Selector.periodHiddenText -> periodHiddenText,
        Selector.periodWithText -> periodWithText,
        Selector.h1 -> headingLPP,
        Selector.govukBody(1) -> estimateFooterNoteBillPayment,
        Selector.govukBody(2) -> onePartCalculation("2% of ??10,000.00 (the unpaid VAT 15 days after the due date)"),
        Selector.summaryListRowKey(1) -> th1LPPEstimate,
        Selector.summaryListRowValue(1) -> "??400",
        Selector.summaryListRowKey(2) -> th3LPP,
        Selector.summaryListRowValue(2) -> "??100",
        Selector.summaryListRowKey(3) -> th4LPP1,
        Selector.summaryListRowValue(3) -> "??300",
        Selector.warning -> estimateFooterNoteWarningAgent,
        Selector.h2 -> h2Additional,
        Selector.govukBody(3) -> p2AdditionalLPP1,
        Selector.bulletNthChild(1) -> b1AdditionalAgent,
        Selector.bulletNthChild(2) -> b2Additional,
        Selector.link -> linkEstimatedAgent
      )

      behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
    }
  }
}
