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

package viewmodels

import base.SpecBase
import models.breathingSpace.BreathingSpace

import java.time.LocalDate

class BreathingSpaceHelperSpec extends SpecBase {

  "isUserInBreathingSpace" should {
    "return true" when {
      "the start date is before the current date and the end date is after the current date" in {
        val currentDate: LocalDate = LocalDate.of(2023, 1, 20)
        val breathingSpace: Option[Seq[BreathingSpace]] = Some(Seq(BreathingSpace(
          BSStartDate = LocalDate.of(2023, 1, 1), BSEndDate = LocalDate.of(2023, 1, 31)
        )))
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe true
      }

      "the start date is equal to the current date and the end date is after the current date" in {
        val currentDate: LocalDate = LocalDate.of(2023, 1, 1)
        val breathingSpace: Option[Seq[BreathingSpace]] = Some(Seq(BreathingSpace(
          BSStartDate = LocalDate.of(2023, 1, 1), BSEndDate = LocalDate.of(2023, 1, 31)
        )))
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe true
      }

      "the start date is before the current date and the end date is equal to the current date" in {
        val currentDate: LocalDate = LocalDate.of(2023, 1, 31)
        val breathingSpace: Option[Seq[BreathingSpace]] = Some(Seq(BreathingSpace(
          BSStartDate = LocalDate.of(2023, 1, 1), BSEndDate = LocalDate.of(2023, 1, 31)
        )))
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe true
      }
    }

    "return false" when {
      "the start date is after the current date" in {
        val currentDate: LocalDate = LocalDate.of(2022, 12, 31)
        val breathingSpace: Option[Seq[BreathingSpace]] = Some(Seq(BreathingSpace(
          BSStartDate = LocalDate.of(2023, 1, 1), BSEndDate = LocalDate.of(2023, 1, 31)
        )))
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe false
      }

      "the end date is before the current date" in {
        val currentDate: LocalDate = LocalDate.of(2023, 2, 1)
        val breathingSpace: Option[Seq[BreathingSpace]] = Some(Seq(BreathingSpace(
          BSStartDate = LocalDate.of(2023, 1, 1), BSEndDate = LocalDate.of(2023, 1, 31)
        )))
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe false
      }

      "there is no breathing space object" in {
        val currentDate: LocalDate = LocalDate.now
        val breathingSpace: Option[Seq[BreathingSpace]] = None
        val result: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(breathingSpace)(currentDate)
        result shouldBe false
      }
    }
  }
}
