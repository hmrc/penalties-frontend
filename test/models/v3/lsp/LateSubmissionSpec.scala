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

package models.v3.lsp

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDate

class LateSubmissionSpec extends SpecBase {

  val lateSubmissionAsJson = Json.parse(
    """
      |{
      | "taxPeriodStartDate": "2069-10-30",
      | "taxPeriodEndDate": "2069-10-30",
      | "taxPeriodDueDate": "2069-10-30",
      | "returnReceiptDate": "2069-10-30"
      |}
      |""".stripMargin)

  val lateSubmissionAsModel = LateSubmission(
    taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
    taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
    taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
    returnReceiptDate = Some(LocalDate.parse("2069-10-30"))
  )

  "LateSubmission" should {
    "be readable from JSON" in {
      val result = Json.fromJson(lateSubmissionAsJson)(LateSubmission.format)
      result.isSuccess shouldBe true
      result.get shouldBe lateSubmissionAsModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(lateSubmissionAsModel)(LateSubmission.format)
      result shouldBe lateSubmissionAsJson
    }

  }
}
