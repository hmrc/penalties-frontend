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

package models

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json}

case class PenaltyDetails(
                           pointsTotal: Int,
                           lateSubmissions: Int,
                           adjustmentPointsTotal: Int,
                           fixedPenaltyAmount: BigDecimal,
                           penaltyAmountsTotal: BigDecimal,
                           penaltyPointsThreshold: Int,
                           penalties: Seq[PenaltyModel]
                         )

object PenaltyDetails{
  implicit val fmt: Format[PenaltyDetails] = Json.format[PenaltyDetails]
}

case class PenaltyModel(
                         penaltyType: String,
                         id: String,
                         number: Int,
                         dateCreated: LocalDateTime,
                         dateExpired: LocalDateTime,
                         status: String,
                         penaltyPeriod: PenaltyPeriod,
                         communications: Seq[PenaltyCommunication]
                       )

object PenaltyModel{
  implicit val fmt: Format[PenaltyModel] = Json.format[PenaltyModel]
}


case class PenaltyPeriod(
                          startDate: LocalDateTime,
                          endDate: LocalDateTime,
                          submission: PeriodSubmission
                        )

object PenaltyPeriod{
  implicit val fmt: Format[PenaltyPeriod] = Json.format[PenaltyPeriod]
}


case class PenaltyCommunication(
                                 communicationType: String,
                                 dateSent: LocalDateTime,
                                 documentId: String
                               )

object PenaltyCommunication{
  implicit val fmt: Format[PenaltyCommunication] = Json.format[PenaltyCommunication]
}

case class PeriodSubmission(
                             dueDate: LocalDateTime,
                             submissionDate: LocalDateTime,
                             status: String
                           )
object PeriodSubmission{
  implicit val fmt: Format[PeriodSubmission] = Json.format[PeriodSubmission]
}
