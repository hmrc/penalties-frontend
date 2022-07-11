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

package models.lpp

import base.SpecBase
import models.lpp.LPPPenaltyCategoryEnum.{LPP1, LPP2}
import play.api.libs.json.{JsString, Json}

class LPPPenaltyCategoryEnumSpec extends SpecBase {

  "LPPPenaltyCategoryEnum" should {
    "be readable from JSON for LPP1" in {
      val result = Json.fromJson(JsString("LPP1"))(LPPPenaltyCategoryEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe LPPPenaltyCategoryEnum.LPP1
    }

    "be readable from JSON for LPP2" in {
      val result = Json.fromJson(JsString("LPP2"))(LPPPenaltyCategoryEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe LPPPenaltyCategoryEnum.LPP2
    }

    "return JsError when the value is not recognised" in {
      val result = Json.fromJson(JsString("error"))(LPPPenaltyCategoryEnum.format)
      result.isError shouldBe true
    }

    "be writable to JSON for LPP1" in {
      val result = Json.toJson(LPPPenaltyCategoryEnum.LPP1)(LPPPenaltyCategoryEnum.format)
      result shouldBe JsString("LPP1")
    }

    "be writable to JSON for LPP2" in {
      val result = Json.toJson(LPPPenaltyCategoryEnum.LPP2)(LPPPenaltyCategoryEnum.format)
      result shouldBe JsString("LPP2")
    }
  }

  "find" should {
    s"return $Some when the name of the enum can be found for LPP1" in {
      val result = LPPPenaltyCategoryEnum.find("LPP1")
      result.isDefined shouldBe true
      result.get shouldBe LPP1
    }

    s"return $Some when the name of the enum can be found for LPP2" in {
      val result = LPPPenaltyCategoryEnum.find("LPP2")
      result.isDefined shouldBe true
      result.get shouldBe LPP2
    }

    s"return $None when the name specified does not match an enum" in {
      val result = LPPPenaltyCategoryEnum.find("hmmmmm")
      result.isEmpty shouldBe true
    }
  }
}
