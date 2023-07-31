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

import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.errors.InternalServerErrorCustom
import assets.messages.ISECustomMessages._

class InternalServerErrorCustomSpec extends SpecBase with ViewBehaviours with ViewUtils {
  val iseCustomPage: InternalServerErrorCustom = injector.instanceOf[InternalServerErrorCustom]

  object Selector extends BaseSelectors {
    val link = "#main-content p:nth-child(3) a"
  }

  "InternalServerErrorCustom" should {

    "display the correct page" when {

      "the user is a trader" must {
        def applyView() = {
          iseCustomPage.apply()(
            implicitly, implicitly, implicitly, vatTraderUser
          )
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> title,
          Selector.h1 -> heading,
          Selector.pNthChild(2) -> p,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "the user is a agent" must {
        def applyView() = {
          iseCustomPage.apply()(
            implicitly, implicitly, implicitly, agentUser
          )
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> title,
          Selector.h1 -> heading,
          Selector.pNthChild(2) -> p,
          Selector.link -> agentLink
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }
}
