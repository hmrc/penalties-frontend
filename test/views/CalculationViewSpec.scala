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

import base.{BaseSelectors, SpecBase}
import assets.messages.CalculationMessages._
import play.twirl.api.HtmlFormat
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.{CalculationLPPView, CalculationAdditionalView}
import org.jsoup.nodes.Document

class CalculationViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val calculationPage: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationAdditionalPage: CalculationAdditionalView = injector.instanceOf[CalculationAdditionalView]

  object Selector extends BaseSelectors {
    
    //TODO: rename back to listRow and listValue when both calculation pages are updated
    val listAdditionalKey: Int => String = (item: Int) => s"#main-content dl > div:nth-child($item) > dt"

    val listAdditionalValue: Int => String = (item: Int) => s"#main-content dl > div:nth-child($item) > dd"

    val listRow: Int => String = (item: Int) => s"#main-content tr:nth-child($item) th"

    val listValue: Int => String = (item: Int) => s"#main-content tr:nth-child($item) td"
    
    val bulletNthChild: Int => String = (nThChild: Int) => s"#main-content > div > div > ul > li:nth-child($nThChild)"

    val govukBody: Int => String = (nthChild: Int) => s"#main-content .govuk-body:nth-of-type($nthChild)"

    val warning = "#main-content div .govuk-warning-text"

    val link = "#main-content a"
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
        Selector.title -> titleAdditional,
        Selector.periodSpan -> period,
        Selector.h1 -> headingAdditional,
        Selector.govukBody(1) -> p1Additional,
        Selector.listAdditionalKey(1) -> th1Additional,
        Selector.listAdditionalValue(1) -> "£50.50",
        Selector.listAdditionalKey(2) -> th2Additional,
        Selector.listAdditionalValue(2) -> "7 days",
        Selector.listAdditionalKey(3) -> th3Additional,
        Selector.listAdditionalValue(3) -> "4%",
        Selector.listAdditionalKey(4) -> th4Additional,
        Selector.listAdditionalValue(4) -> "VAT amount unpaid x 4% x number of days since day 31 ÷ 365",
        Selector.listAdditionalKey(5) -> th3LPP,
        Selector.listAdditionalValue(5) -> "£10.10",
        Selector.listAdditionalKey(6) -> th4LPP,
        Selector.listAdditionalValue(6) -> "£40.40",
        Selector.govukBody(2) -> p2Additional,
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)
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
        Selector.title -> titleAdditional,
        Selector.periodSpan -> period,
        Selector.h1 -> headingAdditional,
        Selector.govukBody(1) -> p1Additional,
        Selector.listAdditionalKey(1) -> th1LPP,
        Selector.listAdditionalValue(1) -> "£50.50",
        Selector.listAdditionalKey(2) -> th2Additional,
        Selector.listAdditionalValue(2) -> "7 days",
        Selector.listAdditionalKey(3) -> th3Additional,
        Selector.listAdditionalValue(3) -> "4%",
        Selector.listAdditionalKey(4) -> th4Additional,
        Selector.listAdditionalValue(4) -> "VAT amount unpaid x 4% x number of days since day 31 ÷ 365",
        Selector.listAdditionalKey(5) -> th3LPP,
        Selector.listAdditionalValue(5) -> "£40.10",
        Selector.listAdditionalKey(6) -> th4LPP,
        Selector.listAdditionalValue(6) -> "£10.40",
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "if it is not an additional penalty" must {
      def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
        calculationPage.apply(
          amountReceived = "100",
          penaltyAmount = "400",
          amountLeftToPay = "300",
          calculationRowSeq = calculationRow,
          isCalculationRowMultipleAmounts = isMultipleAmounts,
          isPenaltyEstimate = false,
          "1 October 2022",
          "31 December 2022",
          warningPenaltyAmount="",
          warningDate="")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithOnlyOneCalculation: Document =
        asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)"), isMultipleAmounts = false))
      implicit val docWith2Calculations: Document = asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        "2% of £10,000.00 (Central assessment amount unpaid on 6 June 2025"), isMultipleAmounts = true))


      val expectedContent = Seq(
        Selector.title -> titleLPP,
        Selector.periodSpan -> period,
        Selector.h1 -> headingLPP,
        Selector.listRow(1) -> th1LPP,
        Selector.listValue(1) -> "£400",
        Selector.listRow(2) -> th2LPP,
        Selector.listValue(2) -> "2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        Selector.listRow(3) -> th3LPP,
        Selector.listValue(3) -> "£100",
        Selector.listRow(4) -> th4LPP,
        Selector.listValue(4) -> "£300",
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)

      "when there is 2 calculations - show both" must {
        val expectedContent = Seq(
          Selector.title -> titleLPP,
          Selector.periodSpan -> period,
          Selector.h1 -> headingLPP,
          Selector.listRow(1) -> th1LPP,
          Selector.listValue(1) -> "£400",
          Selector.listRow(2) -> th2LPP,
          Selector.listValue(2) ->
            "2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025) + 2% of £10,000.00 (Central assessment amount unpaid on 6 June 2025",
          Selector.listRow(3) -> th3LPP,
          Selector.listValue(3) -> "£100",
          Selector.listRow(4) -> th4LPP,
          Selector.listValue(4) -> "£300",
          Selector.link -> link
        )
        behave like pageWithExpectedMessages(expectedContent)(docWith2Calculations)
      }
    }

    "it is not an additional penalty and with Penalty Amount " must {
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
          warningPenaltyAmount = "800",
          warningDate = "15 January 2023")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithOnlyOneCalculation: Document =
        asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)"), isMultipleAmounts = false))
      implicit val docWith2Calculations: Document = asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        "2% of £10,000.00 (Central assessment amount unpaid on 6 June 2025"), isMultipleAmounts = true))


      val expectedContent = Seq(
        Selector.title -> titleLPP,
        Selector.periodSpan -> period,
        Selector.h1 -> headingLPP,
        Selector.listRow(1) -> th1LPPEstimate,
        Selector.listValue(1) -> "£400",
        Selector.listRow(2) -> th2LPP,
        Selector.listValue(2) -> "2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        Selector.listRow(3) -> th3LPP,
        Selector.listValue(3) -> "£100",
        Selector.listRow(4) -> th4LPP,
        Selector.listValue(4) -> "£300",
        Selector.warning -> estimateFooterNoteWarning,
        Selector.govukBody(1) -> estimateFooterNoteBillPayment,
        Selector.govukBody(2) -> estimateFooterNoteText,
        Selector.h2 -> h2Additional,
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)
    }
    }
}
