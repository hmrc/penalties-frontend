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

import models.lsp.LateSubmission

object PenaltyPeriodHelper {

  def sortByPenaltyStartDate(p1: LateSubmission, p2: LateSubmission): Int = {
    if (p1.taxPeriodStartDate.isDefined && p2.taxPeriodStartDate.isDefined) {
      p1.taxPeriodStartDate.get.compareTo(p2.taxPeriodStartDate.get)
    }
    else {
      0
    }
  }

  def sortedPenaltyPeriod(penaltyPeriod: Seq[LateSubmission]): Seq[LateSubmission] = {
    if (penaltyPeriod.nonEmpty)
      penaltyPeriod.sortWith(sortByPenaltyStartDate(_, _) < 0)
    else
      Seq.empty
  }
}
