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

import models.breathingSpace.BreathingSpace

import java.time.LocalDate

object BreathingSpaceHelper {
  def isUserInBreathingSpace(optBreathingSpaceDetails: Option[Seq[BreathingSpace]])(currentDate: LocalDate): Boolean = {
    optBreathingSpaceDetails.exists(_.exists(
      breathingSpaceDetails => {
        (breathingSpaceDetails.BSStartDate.isEqual(currentDate) || breathingSpaceDetails.BSStartDate.isBefore(currentDate)) &&
          (breathingSpaceDetails.BSEndDate.isEqual(currentDate) || breathingSpaceDetails.BSEndDate.isAfter(currentDate))
      }
    ))
  }
}
