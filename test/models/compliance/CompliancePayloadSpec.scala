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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CompliancePayloadSpec extends AnyWordSpec with Matchers {

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)

  val compliancePayloadModel: CompliancePayload = CompliancePayload(
    regime = "",
    VRN = "",
    NoOfMissingReturns = 1,
    noOfSubmissionsReqForCompliance = 1,
    expiryDateOfAllPenaltyPoints = LocalDateTime.now,
    missingReturns = Seq(
      MissingReturn(
        startDate = LocalDateTime.now,
        endDate = LocalDateTime.now.plus(7, ChronoUnit.DAYS)
      )
    ),
    returns = Seq(
      Return(
        startDate = LocalDateTime.now,
        endDate = LocalDateTime.now.plus(7, ChronoUnit.DAYS),
        dueDate = LocalDateTime.now.plus(1, ChronoUnit.MONTHS),
        status = Some(ReturnStatusEnum.submitted)
      )
    )
  )
}
