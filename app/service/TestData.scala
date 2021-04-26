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

package service

import java.time.LocalDateTime

import models.{PenaltyCommunication, PenaltyDetails, PenaltyModel, PenaltyPeriod, PeriodSubmission}

case class TestData(){

  val periodSubmission: PeriodSubmission =
    PeriodSubmission(
      LocalDateTime.now(),
      LocalDateTime.now().minusDays(3),
      "Submitted".toUpperCase
    )

  val com1 =
    PenaltyCommunication(
      "letter",
      LocalDateTime.now().minusDays(10),
      "id-125"
    )

  val penaltyCommunications = Seq(
    com1
  )

  val penaltyPeriod: PenaltyPeriod =
    PenaltyPeriod(
      LocalDateTime.now().minusDays(2),
      LocalDateTime.now(),
      periodSubmission
    )

  val penaltyModel1: PenaltyModel =
    PenaltyModel(
      "point",
      "ID-1234",
      1,
      LocalDateTime.now(),
      LocalDateTime.now().plusDays(2),
      "Active",
      penaltyPeriod,
      penaltyCommunications
    )
  val penaltyModel2: PenaltyModel =
    PenaltyModel(
      "financial",
      "ID-1236",
      2,
      LocalDateTime.now(),
      LocalDateTime.now().plusDays(2),
      "In review",
      penaltyPeriod,
      penaltyCommunications
    )

  val cardDetails: PenaltyDetails =
    PenaltyDetails(
      1,
      1,
      1,
      200,
      400.00,
      4,
      Seq(penaltyModel2, penaltyModel1)
    )

}
