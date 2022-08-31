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

package base

import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}

import java.time.LocalDate

trait TestData {

  val sampleDate: LocalDate = LocalDate.of(2021, 1, 1)

  val samplePenaltyPointV2: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = None,
    penaltyCreationDate = sampleDate,
    penaltyExpiryDate = sampleDate,
    expiryReason = None,
    communicationsDate = sampleDate,
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(sampleDate),
        taxPeriodEndDate = Some(sampleDate),
        taxPeriodDueDate = Some(sampleDate),
        returnReceiptDate = Some(sampleDate),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = None,
    chargeAmount = None,
    chargeOutstandingAmount = None,
    chargeDueDate = None
  )

  val sampleFinancialPenaltyV2: LSPDetails = samplePenaltyPointV2.copy(
    penaltyCategory = LSPPenaltyCategoryEnum.Charge,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(sampleDate)
  )

  val lSPDetailsAsModelNoFAP: LSPDetails = samplePenaltyPointV2.copy(
    FAPIndicator = None
  )

  val sampleFinancialPenaltyWithMultiplePeriods: LSPDetails = sampleFinancialPenaltyV2.copy(
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(sampleDate),
        taxPeriodEndDate = Some(sampleDate.plusDays(15)),
        taxPeriodDueDate = Some(sampleDate.plusMonths(4).plusDays(7)),
        returnReceiptDate = Some(sampleDate.plusMonths(4).plusDays(12)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      ),
      LateSubmission(
        taxPeriodStartDate = Some(sampleDate.plusDays(16)),
        taxPeriodEndDate = Some(sampleDate.plusDays(31)),
        taxPeriodDueDate = Some(sampleDate.plusMonths(4).plusDays(23)),
        returnReceiptDate = Some(sampleDate.plusMonths(4).plusDays(25)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    ))
  )

  val sampleReturnSubmittedPenaltyPointV2: LSPDetails = samplePenaltyPointV2.copy(chargeAmount = None,
    chargeOutstandingAmount = None,
    FAPIndicator = None,
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(sampleDate),
        taxPeriodEndDate = Some(sampleDate),
        taxPeriodDueDate = Some(sampleDate),
        returnReceiptDate = Some(sampleDate),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    ))
  )

  val sampleRemovedPenaltyPointV2: LSPDetails = samplePenaltyPointV2.copy(
    penaltyStatus = LSPPenaltyStatusEnum.Inactive,
    expiryReason = Some("FAP")
  )

  val sampleLatePaymentPenalty: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountOutstanding = Some(99.99),
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("31"),
    LPP2Days = Some("31"),
    LPP1LRCalculationAmount = Some(99.99),
    LPP1HRCalculationAmount = Some(99.99),
    LPP2Percentage = Some(4.00),
    LPP1LRPercentage = Some(2.00),
    LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
    penaltyChargeCreationDate = sampleDate,
    communicationsDate = sampleDate,
    penaltyChargeDueDate = sampleDate,
    appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnum.Rejected),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))),
    principalChargeBillingFrom = sampleDate,
    principalChargeBillingTo = sampleDate,
    principalChargeDueDate = sampleDate,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    ),
    timeToPay = None
  )

  val sampleLatePaymentPenaltyPaid: LPPDetails = sampleLatePaymentPenalty.copy(
    principalChargeLatestClearing = Some(sampleDate),
    penaltyAmountOutstanding = Some(0),
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    appealInformation = None
  )

  val sampleLatePaymentPenaltyAdditionalV2: LPPDetails = sampleLatePaymentPenalty.copy(
    penaltyCategory = LPPPenaltyCategoryEnum.LPP2
  )

  val samplePenaltyPointNotSubmitted: LSPDetails = samplePenaltyPointV2.copy(
    lateSubmissions = Some(
      Seq(
        LateSubmission(
          taxPeriodStartDate = Some(sampleDate),
          taxPeriodEndDate = Some(sampleDate),
          taxPeriodDueDate = Some(sampleDate),
          returnReceiptDate = None,
          taxReturnStatus = TaxReturnStatusEnum.Open
        )
      )
    )
  )

  val samplePenaltyPointAppeal: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LSPDetails = (appealStatus, appealLevel) => samplePenaltyPointV2.copy(
    penaltyStatus = if(appealStatus.equals(AppealStatusEnum.Upheld)) LSPPenaltyStatusEnum.Inactive else LSPPenaltyStatusEnum.Active,
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    )
  )

  val sampleLatePaymentPenaltyUnpaidPenaltyAppeal: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LPPDetails = (appealStatus, appealLevel) => sampleLatePaymentPenalty.copy(
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    ),
    penaltyAmountPaid = Some(10),
    penaltyAmountOutstanding = Some(200),
    principalChargeLatestClearing = Some(sampleDate),
    penaltyStatus = LPPPenaltyStatusEnum.Posted
  )

  val sampleLatePaymentPenaltyPaidPenaltyAppeal: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LPPDetails = (appealStatus, appealLevel) => sampleLatePaymentPenalty.copy(
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    ),
    principalChargeLatestClearing = Some(sampleDate),
    penaltyAmountOutstanding = Some(0),
    penaltyStatus = LPPPenaltyStatusEnum.Posted
  )

  val samplePenaltyDetailsModel: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      LPIPostedTotal = Some(1968.2),
      LPIEstimatedTotal = Some(7)
    )),
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 200, PoCAchievementDate = LocalDate.of(2022, 1, 1)
      ),
      details = Seq(samplePenaltyPointV2))),
    latePaymentPenalty = Some(
      LatePaymentPenalty(
        Seq(
          sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20)))
        )
      )
    )
  )

  val samplePenaltyDetailsModelWithoutMetadata: GetPenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLatePaymentPenalty))))
}
