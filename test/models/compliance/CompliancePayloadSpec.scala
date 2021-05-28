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

package models.compliance

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

class CompliancePayloadSpec extends AnyWordSpec with Matchers {

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)

  val compliancePayloadAsJson: JsValue = Json.parse(
    """
      {
      | "NoOfMissingReturns": 1,
      | "noOfSubmissionsReqForCompliance": 1,
      | "expiryDateOfAllPenaltyPoints": "2021-04-23T18:25:43.511",
      | "missingReturns": [
      | {
      |   "startDate": "2021-04-23T18:25:43.511",
      |   "endDate": "2021-04-30T18:25:43.511"
      |   }
      | ],
      | "returns" : [
      | {
      |   "startDate": "2021-04-23T18:25:43.511",
      |   "endDate": "2021-04-30T18:25:43.511",
      |   "dueDate": "2021-05-23T18:25:43.511",
      |   "status": "SUBMITTED"
      | }
      | ]
      |}
      | """.stripMargin
  )

  val compliancePayloadModel: CompliancePayload = CompliancePayload(
    NoOfMissingReturns = 1,
    noOfSubmissionsReqForCompliance = 1,
    expiryDateOfAllPenaltyPoints = sampleDate,
    missingReturns = Seq(
      MissingReturn(
        startDate = sampleDate,
        endDate = sampleDate.plusDays(7)
      )
    ),
    returns = Seq(
      Return(
        startDate = sampleDate,
        endDate = sampleDate.plusDays(7),
        dueDate = sampleDate.plusMonths(1),
        status = Some(ReturnStatusEnum.submitted)
      )
    )
  )

  "CompliancePayload" should {
    "be writable to JSON" in {
      val result = Json.toJson(compliancePayloadModel)
      result shouldBe compliancePayloadAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(compliancePayloadAsJson)(CompliancePayload.format)
      result.isSuccess shouldBe true
      result.get shouldBe compliancePayloadModel
    }
  }
}
