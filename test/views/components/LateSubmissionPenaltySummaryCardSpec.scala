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

package views.components

import base.{BaseSelectors, SpecBase}
import models.User
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lsp._
import org.jsoup.nodes.Document
import viewmodels.LateSubmissionPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLSP

import java.time.LocalDate

class LateSubmissionPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser

  val summaryCardHtml: summaryCardLSP = injector.instanceOf[summaryCardLSP]

  val summaryCardModelWithAppealedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Under_Appeal),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )))),
    quarterlyThreshold, 1).head

  val summaryCardModelUnappealable: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointAccepted: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Rejected),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAddedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None
    )
    ), quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointUnderTribunalReview: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Under_Appeal),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      ))
    )),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointAcceptedByTribunal: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      ))
    )),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointTribunalRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(lSPDetailsAsModelNoFAP.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Rejected),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      ))
    )),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAddedPointAtThreshold: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = None,
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None
    )
    ), quarterlyThreshold, 4).head

  val summaryCardModelWithRemovedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      FAPIndicator = None,
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Manual),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2020-01-01")),
          taxPeriodEndDate = Some(LocalDate.parse("2020-02-01")),
          taxPeriodDueDate = Some(LocalDate.parse("2020-03-07")),
          returnReceiptDate = Some(LocalDate.parse("2020-03-11")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None
    )
    ), quarterlyThreshold, 1).head

  val summaryCardModelWithRemovedPointFilingFrequencyChange: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None
    )), quarterlyThreshold, 1).head

  val summaryCardModelWithThresholdPenalty: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901238",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = None,
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = None,
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)

  val summaryCardModelNoReturnSubmitted: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901238",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = None,
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = None,
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = None,
          taxReturnStatus = TaxReturnStatusEnum.Open
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None
    ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Under_Appeal),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealAccepted: User[_] => LateSubmissionPenaltySummaryCard =
    (user: User[_]) => summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)(implicitly)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealRejected: User[_] => LateSubmissionPenaltySummaryCard =
    (user: User[_]) => summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Rejected),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealReinstated: User[_] => LateSubmissionPenaltySummaryCard =
    (user: User[_]) => summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)(implicitly)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Under_Appeal),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected: User[_] => LateSubmissionPenaltySummaryCard =
    (user: User[_]) => summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Rejected),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)(implicitly)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalAccepted: User[_] => LateSubmissionPenaltySummaryCard =
    (user: User[_]) => summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Charge,
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), quarterlyThreshold)(implicitly)

  val summaryCardModelWithFinancialLSP: Boolean => LateSubmissionPenaltySummaryCard = (isSubmitted: Boolean) => summaryCardHelper.financialSummaryCard(LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Charge,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = None,
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = None,
    communicationsDate = Some(LocalDate.parse("2069-10-30")),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
        returnReceiptDate = if(isSubmitted) Some(LocalDate.parse("2069-10-30")) else None,
        taxReturnStatus = if(isSubmitted) TaxReturnStatusEnum.Fulfilled else TaxReturnStatusEnum.Open
      )
    )),
    appealInformation = None,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  ), annualThreshold)(implicitly)

  val summaryCardModelWithMultiplePenaltyPeriodLSP: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = Some(ExpiryReasonEnum.Adjustment),
    communicationsDate = Some(LocalDate.parse("2069-10-30")),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(sampleOldestDate),
        taxPeriodEndDate = Some(sampleOldestDate.plusDays(15)),
        taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(7)),
        returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(12)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      ),
      LateSubmission(
        taxPeriodStartDate = Some(sampleOldestDate.plusDays(15)),
        taxPeriodEndDate = Some(sampleOldestDate.plusDays(31)),
        taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
        returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(24)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )),
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  ), annualThreshold)(implicitly)

  val summaryCardModelWithMultiplePenaltyPeriodLSPP: LateSubmissionPenaltySummaryCard = summaryCardHelper.pointSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = "01",
      penaltyCategory = LSPPenaltyCategoryEnum.Point,
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = Some("X"),
      penaltyCreationDate = LocalDate.of(2020, 1, 1),
      penaltyExpiryDate = LocalDate.of(2020, 2, 1),
      expiryReason = Some(ExpiryReasonEnum.Adjustment),
      communicationsDate = Some(LocalDate.of(2020, 6, 1)),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(sampleOldestDate),
          taxPeriodEndDate = Some(sampleOldestDate.plusDays(15)),
          taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(7)),
          returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(12)),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        ),
        LateSubmission(
          taxPeriodStartDate = Some(sampleOldestDate.plusDays(15)),
          taxPeriodEndDate = Some(sampleOldestDate.plusDays(31)),
          taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
          returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )),
      chargeAmount = Some(200),
      chargeOutstandingAmount = Some(200),
      chargeDueDate = Some(LocalDate.parse("2069-10-30"))
    ), true)(implicitly)


  "summaryCard" when {
    "given an added point and the threshold has not been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPoint))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h4").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in {
        doc.select("a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        doc.select("a").attr("href") shouldBe appConfig.adjustmentLink
      }

      "display the 'active' status for an added point" in {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "30 October 2069"
      }

      "display when the point is due to expire" in {
        doc.select("dt").get(1).text() shouldBe "Point due to expire"
        doc.select("dd").get(1).text() shouldBe "October 2069"
      }

      "display that the user can not appeal an added point" in {
        doc.select("footer").text() shouldBe "You cannot appeal this point"
      }
    }

    "given an added point and the threshold has been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPointAtThreshold))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h4").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in {
        doc.select("a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        doc.select("a").attr("href") shouldBe appConfig.adjustmentLink
      }

      "display the 'active' status for an added point" in {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "30 October 2069"
      }

      "NOT display when the point is due to expire" in {
        doc.select("dt").size() shouldBe 1
        doc.select("dd").size() shouldBe 1
      }

      "display that the user can not appeal an added point" in {
        doc.select("footer").text() shouldBe "You cannot appeal this point"
      }
    }

    "given a removed point(not FAP)" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPoint))
      "display that the point number as usual" in {
        doc.select("h4").text() shouldBe "Penalty point"
      }

      "display the VAT period the point was removed from" in {
        doc.select("dt").get(0).text() shouldBe "VAT period"
        doc.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the reason why the point was removed" in {
        doc.select("dt").get(1).text() shouldBe "Reason"
        //TODO: need to add content for removal reasons
        doc.select("dd").get(1).text() shouldBe "summaryCard.removalReason.MAN"
      }

      "not display any footer text" in {
        doc.select("footer li").hasText shouldBe false
      }
    }

    "given a removed point (FAP)" should {
      "display the reason why the point was removed (if reason was FAP)" in {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPointFilingFrequencyChange))
        doc.select("dt").get(1).text() shouldBe "Reason"
        doc.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
      }
    }

    "given a point - return not submitted" should {
      val docWithPoint: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelNoReturnSubmitted))

      "have an aria-label" in {
        docWithPoint.select(".app-summary-card__footer a").attr("aria-label") shouldBe "Check if you can appeal penalty point 1"
      }

    }

    "given a financial point" should {
      val docWithThresholdPenalty: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithThresholdPenalty))
      val docWithFinancialLSP: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(true)))
      val docWithFinancialPointAppealUnderReview: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress))
      val docWithFinancialPointAppealAccepted: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealAccepted(user)))
      val docWithFinancialPointAppealRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealRejected(user)))
      val docWithFinancialPointAppealUnderTribunalReview: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview))
      val docWithFinancialPointAppealTribunalRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected(user)))
      val docWithFinancialPointAppealTribunalAccepted: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalAccepted(user)))

      "shows the financial heading with point number when the point is below/at threshold for filing frequency" in {
        docWithThresholdPenalty.select(".app-summary-card__title").get(0).text shouldBe "Penalty point 1: £200 penalty"
      }

      "shows the financial heading WITHOUT point number when the point is above threshold for filing frequency and a rewording of the appeal text" in {
        docWithFinancialLSP.select(".app-summary-card__title").get(0).text shouldBe "£200 penalty"
        docWithFinancialLSP.select(".app-summary-card__footer a").get(0).text shouldBe "Appeal this penalty"
      }

      "shows the appeal information when the point is being appealed - i.e. under review" in {
        docWithFinancialPointAppealUnderReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderReview.select("dd").get(3).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for ACCEPTED" in {
        docWithFinancialPointAppealAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealAccepted.select("dd").get(3).text() shouldBe "Appeal accepted"
      }

      "have the appeal status for REJECTED" in {
        docWithFinancialPointAppealRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealRejected.select("dd").get(3).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in {
        docWithFinancialPointAppealUnderTribunalReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderTribunalReview.select("dd").get(3).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithFinancialPointAppealTribunalRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalRejected.select("dd").get(3).text() shouldBe "Appeal rejected by tax tribunal"
      }

      "have the appeal status for ACCEPTED BY TRIBUNAL" in {
        docWithFinancialPointAppealTribunalAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalAccepted.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal"
      }

      "display check if you can appeal link if the penalty is unappealable" in {
        docWithThresholdPenalty.select(".app-summary-card__footer a").text() shouldBe "Check if you can appeal"
        docWithThresholdPenalty.select("dt").eq(3).isEmpty shouldBe true
      }
    }

    "given an appealed point" should {
      val docWithAppealedPoint: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPoint))
      val docWithAppealedPointAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointAccepted))
      val docWithAppealedPointRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointRejected))
      val docWithAppealedPointUnderTribunalReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointUnderTribunalReview))
      val docWithAppealedPointAcceptedByTribunal: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointAcceptedByTribunal))
      val docWithAppealedPointUnderTribunalRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointTribunalRejected))


      "not show the appeal link" in {
        docWithAppealedPoint.select(".app-summary-card__footer a").isEmpty shouldBe true
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPoint.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPoint.select("dd").get(4).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in {
        docWithAppealedPointUnderTribunalReview.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalReview.select("dd").get(4).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for ACCEPTED - removing the point due to expire and point number" in {
        docWithAppealedPointAccepted.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAccepted.select("dd").get(3).text() shouldBe "Appeal accepted"
        docWithAppealedPointAccepted.select("h4").get(0).text() shouldBe "Penalty"
      }

      "have the appeal status for ACCEPTED_BY_TRIBUNAL - removing the point due to expire and point number" in {
        docWithAppealedPointAcceptedByTribunal.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAcceptedByTribunal.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAcceptedByTribunal.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal"
        docWithAppealedPointAcceptedByTribunal.select("h4").get(0).text() shouldBe "Penalty"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPointRejected.select("dt").text().contains("Point due to expire") shouldBe true
        docWithAppealedPointRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointRejected.select("dd").get(4).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithAppealedPointUnderTribunalRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalRejected.select("dd").get(4).text() shouldBe "Appeal rejected by tax tribunal"
      }
    }

    "given multiple penalty period in LSP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithMultiplePenaltyPeriodLSP))
      "show message VAT return submitted earlier in multiple penalty period" in {
        doc.select("p.govuk-body").text() shouldBe "The VAT Return due on 24 May 2021 was also submitted late. HMRC only applies 1 penalty for late submission in each month."
      }
    }

    "given multiple penalty period in LSPP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithMultiplePenaltyPeriodLSPP))
      "show message VAT return submitted earlier in multiple penalty period" in {
        doc.select("p.govuk-body").text() shouldBe "The VAT Return due on 24 May 2021 was also submitted late. HMRC only applies 1 penalty for late submission in each month."
      }
    }

    "given no multiple penalty period in LSP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(false)))
      val docSubmitted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(true)))
      "no message relating to multiple penalties in the same period should appear" in {
        doc.select("p.govuk-body").text().isEmpty shouldBe true
      }

      "set the correct aria-label for a lurking point with no return submitted" in {
        doc.select("a").attr("aria-label") shouldBe "Check if you can appeal for penalty on late VAT return due 30 October 2069"
      }

      "set the correct aria-label for a lurking point with a return submitted" in {
        docSubmitted.select("a").attr("aria-label") shouldBe "Appeal this penalty for late VAT return due on 30 October 2069"
      }
    }

    "given a non-appealed point and it is unappealable" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelUnappealable))
      "not show the appeal status row and have the check if you can appeal link" in {
        doc.select(".app-summary-card__footer a").text() shouldBe "Check if you can appeal"
        doc.select("dt").eq(4).isEmpty shouldBe true
      }
    }
  }

}
