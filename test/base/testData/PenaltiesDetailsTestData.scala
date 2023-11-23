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

package base.testData

import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}

import java.time.LocalDate

trait PenaltiesDetailsTestData extends LSPDetailsTestData with LPPDetailsTestData {
  val sampleDate: LocalDate = LocalDate.of(2021, 1, 1)

  val samplePenaltyDetailsModel: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 200, PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(sampleLateSubmissionPoint))),
    latePaymentPenalty = Some(
      LatePaymentPenalty(
        Seq(
          sampleUnpaidLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20), timeToPay = None, vatOutstandingAmount = Some(BigDecimal(123.45))))
        )
      )
    ),
    breathingSpace = None
  )

  val samplePenaltyDetailsModelWithoutMetadata: GetPenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleUnpaidLPP1))))
}
