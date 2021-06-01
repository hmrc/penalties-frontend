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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.twirl.api.Html

class ViewUtilsSpec extends AnyWordSpec with Matchers {
  object Viewtils extends ViewUtils

  "stringAsHtml" should {
    "convert a string to HTML" in {
      val content: String = "This is some content."
      Viewtils.stringAsHtml(content).body shouldBe "This is some content."
    }
  }

  "html" should {
    "take a sequence of HTML content and format as one HTML element" in {
      val result = Viewtils.html(Html("1"), Html("2"), Html("3"), Html("4"))
      result.body shouldBe "1234"
    }
  }
}