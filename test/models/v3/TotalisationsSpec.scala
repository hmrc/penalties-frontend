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

package models.v3

import base.SpecBase
import play.api.libs.json.Json

class TotalisationsSpec extends SpecBase {

  val totalisationJson = Json.parse(
    """
      |{
      |   "LSPTotalValue": 200,
      |   "penalisedPrincipalTotal": 2000,
      |   "LPPPostedTotal": 165.25,
      |   "LPPEstimatedTotal": 15.26,
      |   "LPIPostedTotal": 1968.2,
      |   "LPIEstimatedTotal": 7
      |}
      |""".stripMargin)

  val totalisationModel = Totalisations(
    LSPTotalValue = 200,
    penalisedPrincipalTotal = 2000,
    LPPPostedTotal = 165.25,
    LPPEstimatedTotal = 15.26,
    LPIPostedTotal = 1968.2,
    LPIEstimatedTotal = 7
  )

  "Totalisation" should {
    "be readable from JSON" in {
      val result = Json.fromJson(totalisationJson)(Totalisations.format)
      result.isSuccess shouldBe true
      result.get shouldBe totalisationModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(totalisationModel)(Totalisations.format)
      result shouldBe totalisationJson
    }
  }
}
