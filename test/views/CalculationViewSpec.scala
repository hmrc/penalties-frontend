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
    val listRow = (item: Int) => s"#main-content tr:nth-child($item) th"

    val listValue = (item: Int) => s"#main-content tr:nth-child($item) td"

    val bulletNthChild = (nThChild: Int) => s"#main-content > div > div > ul > li:nth-child($nThChild)"

    val govukBody = (nthChild: Int) => s"#main-content .govuk-body:nth-of-type($nthChild)"

    val link = "#main-content a"
  }

  "CalculationView" should {

    "it is an additional penalty" must {
      def applyView(): HtmlFormat.Appendable = {
        calculationAdditionalPage.apply(amountPaid = "100")(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selector.title -> titleAdd,
        Selector.h1 -> headingAdd,
        Selector.govukBody(1) -> p1Add,
        Selector.listRow(1) -> th1Add,
        //    Selector.listValue(1) -> "£0" //TODO: Implement with actual values
        Selector.listRow(2) -> th2Add,
        //    Selector.listValue(2) -> "0% of £0 (VAT amount unpaid on 0)" //TODO: Implement with actual values
        Selector.listRow(3) -> th3Add,
        //    Selector.listValue(3) -> "£0" //TODO: Implement with actual values
        Selector.listRow(4) -> th4Add,
        //    Selector.listValue(4) -> "£0" //TODO: Implement with actual values
        Selector.govukBody(2) -> p2Add,
        Selector.govukBody(3) -> p3Add,
        Selector.bulletNthChild(1) -> bullet1Add,
        Selector.bulletNthChild(2) -> bullet2Add,
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "it is not an additional penalty" must {
      def applyView(calculationRow: Seq[String], isMultipleAmounts: Boolean): HtmlFormat.Appendable = {
        calculationPage.apply(
          amountPaid = "100",
          penaltyAmount = "400",
          amountLeftToPay = "50",
          calculationRowSeq = calculationRow,
          isCalculationRowMultipleAmounts = isMultipleAmounts)(implicitly, implicitly, implicitly, vatTraderUser)
      }

      implicit val docWithOnlyOneCalculation: Document = asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)"), isMultipleAmounts = false))
      implicit val docWith2Calculations: Document = asDocument(applyView(Seq("2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        "2% of £10,000.00 (Central assessment amount unpaid on 6 June 2025"), isMultipleAmounts = true))

      val expectedContent = Seq(
        Selector.title -> title,
        Selector.h1 -> heading,
        Selector.listRow(1) -> th1,
        Selector.listValue(1) -> "£400",
        Selector.listRow(2) -> th2,
        Selector.listValue(2) -> "2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025)",
        Selector.listRow(3) -> th3,
        Selector.listValue(3) -> "£100",
        Selector.listRow(4) -> th4,
        Selector.listValue(4) -> "£50",
        Selector.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)(docWithOnlyOneCalculation)

      "when there is 2 calculations - show both" must {
        val expectedContent = Seq(
          Selector.title -> title,
          Selector.h1 -> heading,
          Selector.listRow(1) -> th1,
          Selector.listValue(1) -> "£400",
          Selector.listRow(2) -> th2,
          Selector.listValue(2) -> "2% of £10,000.00 (Central assessment amount unpaid on 22 May 2025) + 2% of £10,000.00 (Central assessment amount unpaid on 6 June 2025",
          Selector.listRow(3) -> th3,
          Selector.listValue(3) -> "£100",
          Selector.listRow(4) -> th4,
          //    Selector.listValue(4) -> "£0" //TODO: Implement with actual values
          Selector.link -> link
        )
        behave like pageWithExpectedMessages(expectedContent)(docWith2Calculations)
      }
    }
  }
}
