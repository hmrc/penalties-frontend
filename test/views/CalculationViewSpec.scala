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
import views.html.CalculationView
import org.jsoup.nodes.Document

class CalculationViewSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val calculationPage: CalculationView = injector.instanceOf[CalculationView]

  object Selector extends BaseSelectors {
    val listRow = (item: Int) => s"#main-content tr:nth-child($item) th"

    val listValue = (item: Int) => s"#main-content tr:nth-child($item) td"

    val link = "#main-content a"
  }

  "CalculationView" should {

    def applyView(): HtmlFormat.Appendable = {
      calculationPage.apply()(implicitly, implicitly, implicitly, vatTraderUser)
    }

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selector.title -> title,
      Selector.h1 -> heading,
      Selector.listRow(1) -> th1,
//    Selector.listValue(1) -> "£0" //TODO: Implement with actual values
      Selector.listRow(2) -> th2,
//    Selector.listValue(2) -> "0% of £0 (VAT amount unpaid on 0)" //TODO: Implement with actual values
      Selector.listRow(3) -> th3,
//    Selector.listValue(3) -> "£0" //TODO: Implement with actual values
      Selector.listRow(4) -> th4,
//    Selector.listValue(4) -> "£0" //TODO: Implement with actual values
      Selector.link -> link
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
