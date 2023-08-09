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
import play.api.libs.json.Json

import java.time.LocalDate

class LSPSummarySpec extends SpecBase {

  val LSPSummaryAsModel = LSPSummary(
    activePenaltyPoints = 10,
    inactivePenaltyPoints = 12,
    regimeThreshold = 10,
    penaltyChargeAmount = 684.25,
    PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
  )

  val LSPSummaryAsJson = Json.parse(
    """
      |{
      |     "activePenaltyPoints": 10,
      |     "inactivePenaltyPoints": 12,
      |     "regimeThreshold": 10,
      |     "penaltyChargeAmount": 684.25,
      |     "PoCAchievementDate": "2022-01-01"
      |}
      |""".stripMargin)

  "LSPSummary" should {
    "be writeable to JSON" in {
      val result = Json.toJson(LSPSummaryAsModel)(LSPSummary.format)
      result shouldBe LSPSummaryAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(LSPSummaryAsJson)(LSPSummary.format)
      result.isSuccess shouldBe true
      result.get shouldBe LSPSummaryAsModel
    }
  }
}
