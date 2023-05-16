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

package models.lsp

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class TaxReturnStatusEnumSpec extends SpecBase {
  "TaxReturnStatusEnum" should {
    "be writable to JSON for value Open" in {
      val result = Json.toJson(TaxReturnStatusEnum.Open)
      result shouldBe JsString("Open")
    }

    "be writable to JSON for value Fulfilled" in {
      val result = Json.toJson(TaxReturnStatusEnum.Fulfilled)
      result shouldBe JsString("Fulfilled")
    }

    "be writable to JSON for value Reversed" in {
      val result = Json.toJson(TaxReturnStatusEnum.Reversed)
      result shouldBe JsString("Reversed")
    }

    "be writable to JSON for AddedFAP" in {
      val result = Json.toJson(TaxReturnStatusEnum.AddedFAP)(TaxReturnStatusEnum.format)
      result shouldBe JsString(" ")
    }

    "be readable from JSON for value Open" in {
      val result = Json.fromJson(JsString("Open"))(TaxReturnStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe TaxReturnStatusEnum.Open
    }

    "be readable from JSON for value Fulfilled" in {
      val result = Json.fromJson(JsString("Fulfilled"))(TaxReturnStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe TaxReturnStatusEnum.Fulfilled
    }

    "be readable from JSON for value Reversed" in {
      val result = Json.fromJson(JsString("Reversed"))(TaxReturnStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe TaxReturnStatusEnum.Reversed
    }

    "be readable from JSON for an empty string (FAP) to AddedFAP" in {
      val result = Json.fromJson(JsString(" "))(TaxReturnStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe TaxReturnStatusEnum.AddedFAP
    }

    "return a JsError when the enum is not readable" in {
      val result = Json.fromJson(JsString("error"))(TaxReturnStatusEnum.format)
      result.isError shouldBe true
    }
  }
}
