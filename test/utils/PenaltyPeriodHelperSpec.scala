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

package utils

import base.SpecBase
import models.lsp.{LateSubmission, TaxReturnStatusEnum}

import java.time.LocalDate

class PenaltyPeriodHelperSpec extends SpecBase {

  val penaltyPeriods = Seq(LateSubmission(
    taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
    taxPeriodEndDate = Some(LocalDate.parse("2069-10-15")),
    taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
    returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
    ),
    LateSubmission(
      taxPeriodStartDate = Some(LocalDate.parse("2069-12-15")),
      taxPeriodEndDate = Some(LocalDate.parse("2069-12-15")),
      taxPeriodDueDate = Some(LocalDate.parse("2069-12-15")),
      returnReceiptDate = Some(LocalDate.parse("2069-12-15")),
      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
    )
  )

  "PenaltyPeriod Helper" should {
    "sortedPenaltyPeriod " should {
      "return sorted Penalty Period with oldest startDate " in {
        val sortedPenaltyPeriod = PenaltyPeriodHelper.sortedPenaltyPeriod(penaltyPeriods)
        sortedPenaltyPeriod.head.taxPeriodStartDate.get shouldBe LocalDate.of(2069, 10, 30)
        sortedPenaltyPeriod.head.taxPeriodEndDate.get shouldBe LocalDate.of(2069, 10, 15)
      }
    }
  }
}
