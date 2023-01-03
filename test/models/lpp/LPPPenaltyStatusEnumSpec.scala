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

package models.lpp

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class LPPPenaltyStatusEnumSpec extends SpecBase {

  "LPPPenaltyStatusEnum" should {
    "be readable from JSON for A - Accruing" in {
      val result = Json.fromJson(JsString("A"))(LPPPenaltyStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe LPPPenaltyStatusEnum.Accruing
    }

    "be readable from JSON for P - Posted" in {
      val result = Json.fromJson(JsString("P"))(LPPPenaltyStatusEnum.format)
      result.isSuccess shouldBe true
      result.get shouldBe LPPPenaltyStatusEnum.Posted
    }

    "return a JsError when the value is not recognised" in {
      val result = Json.fromJson(JsString("error"))(LPPPenaltyStatusEnum.format)
      result.isError shouldBe true
    }

    "be writable to JSON for A - Accruing" in {
      val result = Json.toJson(LPPPenaltyStatusEnum.Accruing)(LPPPenaltyStatusEnum.format)
      result shouldBe JsString("A")
    }

    "be writable to JSON for P - Posted" in {
      val result = Json.toJson(LPPPenaltyStatusEnum.Posted)(LPPPenaltyStatusEnum.format)
      result shouldBe JsString("P")
    }
  }

}
