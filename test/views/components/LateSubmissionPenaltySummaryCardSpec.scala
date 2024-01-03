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
import config.featureSwitches.{FeatureSwitching, ShowFindOutHowToAppealJourney}
import models.User
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lsp._
import org.jsoup.nodes.Document
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import viewmodels.LateSubmissionPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLSP
import java.time.LocalDate

class LateSubmissionPenaltySummaryCardSpec extends SpecBase with ViewBehaviours with FeatureSwitching with BeforeAndAfterAll with BeforeAndAfterEach {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser

  class Setup(isShowAppealAgainstObligationChangesEnabled: Boolean = false) {
    if(isShowAppealAgainstObligationChangesEnabled) {
      enableFeatureSwitch(ShowFindOutHowToAppealJourney)
    } else {
      disableFeatureSwitch(ShowFindOutHowToAppealJourney)
    }
  }

  val summaryCardHtml: summaryCardLSP = injector.instanceOf[summaryCardLSP]

  def summaryCardModelWithAppealedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Under_Appeal),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.Financial))),
    monthlyThreshold, 1).head

  def summaryCardModelUnappealable: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.Financial))),
    quarterlyThreshold, 1).head

  def summaryCardModelWithAppealedPointAccepted: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      expiryReason = Some(ExpiryReasonEnum.Appeal),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
      penaltyOrder = None,
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.AppealedPoint))),
    quarterlyThreshold, 0).head

  def summaryCardModelWithAppealedPointRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Rejected),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )), lspTypeEnum = Some(LSPTypeEnum.Financial))),
    quarterlyThreshold, 1).head

  def summaryCardModelWithAddedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = None,
      lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
    )
    ), quarterlyThreshold, 1).head

  def summaryCardModelWithAppealedPointUnderTribunalReview: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Under_Appeal),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.Financial)
    )),
    quarterlyThreshold, 1).head

  def summaryCardModelWithAppealedPointAcceptedByTribunal: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      expiryReason = Some(ExpiryReasonEnum.Appeal),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
      penaltyOrder = None,
      FAPIndicator = None,
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.Point)
    )),
    quarterlyThreshold, 0).head

  def summaryCardModelWithAppealedPointTribunalRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(sampleLateSubmissionPenaltyCharge.copy(
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Rejected),
          appealLevel = Some(AppealLevelEnum.Tribunal)
        )
      )), lspTypeEnum = Some(LSPTypeEnum.Financial)
    )),
    quarterlyThreshold, 1).head

  def summaryCardModelWithAddedPointAtThreshold: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None,
      lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
    )
    ), quarterlyThreshold, 4).head

  def summaryCardModelWithRemovedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None,
      lspTypeEnum = Some(LSPTypeEnum.RemovedPoint)
    )
    ), quarterlyThreshold, 1).head

  def summaryCardModelWithRemovedPointFilingFrequencyChange: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = None,
      lspTypeEnum = Some(LSPTypeEnum.RemovedFAP)
    )), quarterlyThreshold, 1).head

  def summaryCardModelWithThresholdPenalty: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901238",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.Point)
    ), quarterlyThreshold)

  def summaryCardModelNoReturnSubmitted: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901238",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Open)
        )
      )),
      appealInformation = None,
      chargeAmount = None,
      chargeOutstandingAmount = None,
      chargeDueDate = None,
      lspTypeEnum = Some(LSPTypeEnum.Point)
    ), quarterlyThreshold)

  def summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
    ), quarterlyThreshold)

  def summaryCardModelForFinancialPenaltyWithAndAppealRejected: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = None,
      expiryReason = None,
      penaltyCreationDate = LocalDate.parse("2069-10-30"),
      penaltyExpiryDate = LocalDate.parse("2069-10-30"),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.Financial)
    ), quarterlyThreshold)

  def summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
    ), quarterlyThreshold)

  def summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected: LateSubmissionPenaltySummaryCard =
    summaryCardHelper.financialSummaryCard(LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
      penaltyStatus = LSPPenaltyStatusEnum.Active,
      FAPIndicator = None,
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.Point)
    ), quarterlyThreshold)(implicitly)

  def summaryCardModelWithFinancialLSP: Boolean => LateSubmissionPenaltySummaryCard = (isSubmitted: Boolean) => summaryCardHelper.financialSummaryCard(LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = Some("01"),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
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
        returnReceiptDate = if (isSubmitted) Some(LocalDate.parse("2069-10-30")) else None,
        taxReturnStatus = if (isSubmitted) Some(TaxReturnStatusEnum.Fulfilled) else Some(TaxReturnStatusEnum.Open)
      )
    )),
    appealInformation = None,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30")),
    lspTypeEnum = Some(LSPTypeEnum.Financial)
  ), annualThreshold)(implicitly)

  def summaryCardModelWithMultiplePenaltyPeriodLSP: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = Some("01"),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
      ),
      LateSubmission(
        taxPeriodStartDate = Some(sampleOldestDate.plusDays(15)),
        taxPeriodEndDate = Some(sampleOldestDate.plusDays(31)),
        taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
        returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(24)),
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
    chargeDueDate = Some(LocalDate.parse("2069-10-30")),
    lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
  ), annualThreshold)(implicitly)

  def summaryCardModelWithMultiplePenaltyPeriodLSPP: LateSubmissionPenaltySummaryCard = summaryCardHelper.pointSummaryCard(
    LSPDetails(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
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
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
        ),
        LateSubmission(
          taxPeriodStartDate = Some(sampleOldestDate.plusDays(15)),
          taxPeriodEndDate = Some(sampleOldestDate.plusDays(31)),
          taxPeriodDueDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
          returnReceiptDate = Some(sampleOldestDate.plusMonths(4).plusDays(23)),
          taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
      chargeDueDate = Some(LocalDate.parse("2069-10-30")),
      lspTypeEnum = Some(LSPTypeEnum.AddedFAP)
    ), true)(implicitly)

  override def afterEach(): Unit = {
    super.afterEach()
    sys.props -= ShowFindOutHowToAppealJourney.name
  }

  "summaryCard" when {
    "given an added point and the threshold has not been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPoint))
      "display that the point has been added i.e. Penalty point X: adjustment point" in new Setup() {
        doc.select("h4").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in new Setup() {
        doc.select("a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        doc.select("a").attr("href") shouldBe appConfig.adjustmentLink
      }

      "display the 'active' status for an added point" in new Setup() {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in new Setup() {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "30 October 2069"
      }

      "display when the point is due to expire" in new Setup() {
        doc.select("dt").get(1).text() shouldBe "Point due to expire"
        doc.select("dd").get(1).text() shouldBe "October 2069"
      }

      "display that the user can not appeal an added point" in new Setup() {
        doc.select("footer").text() shouldBe "You cannot appeal this point"
      }
    }

    "given an added point and the threshold has been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPointAtThreshold))
      "display that the point has been added i.e. Penalty point X: adjustment point" in new Setup() {
        doc.select("h4").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in new Setup() {
        doc.select("a").text() shouldBe "Read the guidance about adjustment points (opens in a new tab)"
        doc.select("a").attr("href") shouldBe appConfig.adjustmentLink
      }

      "display the 'active' status for an added point" in new Setup() {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in new Setup() {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "30 October 2069"
      }

      "NOT display when the point is due to expire" in new Setup() {
        doc.select("dt").size() shouldBe 1
        doc.select("dd").size() shouldBe 1
      }

      "display that the user can not appeal an added point" in new Setup() {
        doc.select("footer").text() shouldBe "You cannot appeal this point"
      }
    }

    "given a removed point(not FAP)" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPoint))
      "display that the point number as usual" in new Setup() {
        doc.select("h4").text() shouldBe "Penalty point"
      }

      "display the VAT period the point was removed from" in new Setup() {
        doc.select("dt").get(0).text() shouldBe "VAT period"
        doc.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the reason why the point was removed" in new Setup() {
        doc.select("dt").get(1).text() shouldBe "Reason"
        doc.select("dd").get(1).text() shouldBe "Removed by HMRC"
      }

      "not display any footer text" in new Setup() {
        doc.select("footer li").hasText shouldBe false
      }
    }

    "given a removed point (FAP)" should {
      "display the reason why the point was removed (if reason was FAP)" in new Setup() {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPointFilingFrequencyChange))
        doc.select("dt").get(1).text() shouldBe "Reason"
        doc.select("dd").get(1).text() shouldBe "Change to VAT return deadlines"
      }
    }

    "given a point - return not submitted" should {

      "have a hidden span for check if you can appeal" in new Setup() {
        val docWithPoint: Document = asDocument(summaryCardHtml.apply(summaryCardModelNoReturnSubmitted))
        docWithPoint.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Check if you can appeal"
        docWithPoint.select(".app-summary-card__footer span").text() shouldBe "penalty point 1"
      }

      "have a hidden span for find out how to appeal (ShowAppealAgainstObligationChanges feature enabled)" in new Setup(
        isShowAppealAgainstObligationChangesEnabled = true
      ) {
        val docWithPointFSEnabled: Document = asDocument(summaryCardHtml.apply(summaryCardModelNoReturnSubmitted))
        docWithPointFSEnabled.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Find out how to appeal"
        docWithPointFSEnabled.select(".app-summary-card__footer span").text() shouldBe "penalty point 1"
      }

    }

    "given a financial point" should {
      def docWithThresholdPenalty: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithThresholdPenalty))
      val docWithFinancialLSP: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(true)))
      val docWithFinancialPointAppealUnderReview: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress))
      val docWithFinancialPointAppealRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelForFinancialPenaltyWithAndAppealRejected))
      val docWithFinancialPointAppealUnderTribunalReview: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview))
      val docWithFinancialPointAppealTribunalRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected))

      "shows the financial heading with point number when the point is below/at threshold for filing frequency" in new Setup() {
        docWithThresholdPenalty.select(".app-summary-card__title").get(0).text() shouldBe "Penalty point 1: £200 penalty"
      }

      "shows the financial heading WITHOUT point number when the point is above threshold for filing frequency and a rewording of the appeal text" in new Setup() {
        docWithFinancialLSP.select(".app-summary-card__title").get(0).ownText() shouldBe "£200 penalty"
        docWithFinancialLSP.select(".app-summary-card__title span").get(0).ownText() shouldBe "for late submission of VAT due on 30 October 2069"
        docWithFinancialLSP.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Appeal this penalty"
        docWithFinancialLSP.select(".app-summary-card__footer span").get(0).text() shouldBe "for late VAT return due on 30 October 2069"
      }

      "shows the appeal information when the point is being appealed - i.e. under review" in new Setup() {
        docWithFinancialPointAppealUnderReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderReview.select("dd").get(3).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for REJECTED" in new Setup() {
        docWithFinancialPointAppealRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealRejected.select("dd").get(3).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in new Setup() {
        docWithFinancialPointAppealUnderTribunalReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderTribunalReview.select("dd").get(3).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for TRIBUNAL REJECTED" in new Setup() {
        docWithFinancialPointAppealTribunalRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalRejected.select("dd").get(3).text() shouldBe "Appeal rejected by tax tribunal"
      }

      "display check if you can appeal link if the penalty is unappealable" in new Setup() {
        docWithThresholdPenalty.select(".app-summary-card__footer a").text() shouldBe "Check if you can appeal"
        docWithThresholdPenalty.select("dt").eq(3).isEmpty shouldBe true
      }

      "display find out how to appeal link if the penalty is unappealable (ShowAppealAgainstObligationChanges feature enabled)" in new Setup(
        isShowAppealAgainstObligationChangesEnabled = true
      ) {
        docWithThresholdPenalty.select(".app-summary-card__footer a").text() shouldBe "Find out how to appeal"
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


      "not show the appeal link" in new Setup() {
        docWithAppealedPoint.select(".app-summary-card__footer a").isEmpty shouldBe true
      }

      "have the appeal status for UNDER_REVIEW" in new Setup() {
        docWithAppealedPoint.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPoint.select("dd").get(3).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in new Setup() {
        docWithAppealedPointUnderTribunalReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalReview.select("dd").get(3).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for ACCEPTED - removing the point due to expire and point number" in new Setup() {
        docWithAppealedPointAccepted.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAccepted.select("dd").get(3).text() shouldBe "Appeal accepted"
        docWithAppealedPointAccepted.select("h4").get(0).text() shouldBe "Penalty"
      }

      "have the appeal status for ACCEPTED_BY_TRIBUNAL - removing the point due to expire and point number" in new Setup() {
        docWithAppealedPointAcceptedByTribunal.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAcceptedByTribunal.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAcceptedByTribunal.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal"
        docWithAppealedPointAcceptedByTribunal.select("h4").get(0).text() shouldBe "Penalty"
      }

      "have the appeal status for REJECTED" in new Setup() {
        docWithAppealedPointRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointRejected.select("dd").get(3).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for TRIBUNAL REJECTED" in new Setup() {
        docWithAppealedPointUnderTribunalRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalRejected.select("dd").get(3).text() shouldBe "Appeal rejected by tax tribunal"
      }
    }

    "given multiple penalty period in LSP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithMultiplePenaltyPeriodLSP))
      "show message VAT return submitted earlier in multiple penalty period" in new Setup() {
        doc.select("p.govuk-body").text() shouldBe "The VAT Return due on 24 May 2021 was also submitted late. HMRC only applies 1 penalty for late submission in each month."
      }
    }

    "given multiple penalty period in LSPP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithMultiplePenaltyPeriodLSPP))
      "show message VAT return submitted earlier in multiple penalty period" in new Setup() {
        doc.select("p.govuk-body").text() shouldBe "The VAT Return due on 24 May 2021 was also submitted late. HMRC only applies 1 penalty for late submission in each month."
      }
    }

    "given no multiple penalty period in LSP" should {
      implicit def doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(false)))
      val docSubmitted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(true)))
      "no message relating to multiple penalties in the same period should appear" in new Setup() {
        doc.select("p.govuk-body").text().isEmpty shouldBe true
      }

      "set the correct hidden span for a lurking point with no return submitted" in new Setup() {
        doc.select("a").get(0).ownText() shouldBe "Check if you can appeal"
        doc.select("a span").get(0).text() shouldBe "for penalty on late VAT return due 30 October 2069"
      }

      "set the correct hidden span for a lurking point with no return submitted (ShowAppealAgainstObligationChanges feature enabled)" in new Setup(
        isShowAppealAgainstObligationChangesEnabled = true
      ) {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialLSP(false)))
        doc.select("a").get(0).ownText() shouldBe "Find out how to appeal"
        doc.select("a span").get(0).text() shouldBe "for penalty on late VAT return due 30 October 2069"
      }

      "set the correct hidden span for a lurking point with a return submitted" in new Setup() {
        docSubmitted.select("a").get(0).ownText() shouldBe "Appeal this penalty"
        docSubmitted.select("a span").get(0).text() shouldBe "for late VAT return due on 30 October 2069"
      }
    }

    "given a non-appealed point and it is unappealable" should {
      implicit def doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelUnappealable))
      "not show the appeal status row and have the check if you can appeal link" in new Setup() {
        doc.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Check if you can appeal"
        doc.select(".app-summary-card__footer a span").text() shouldBe "for late VAT return due on 12 March 2021"
        doc.select("dt").eq(4).isEmpty shouldBe true
      }

      "not show the appeal status row and have the find out how to appeal link (ShowAppealAgainstObligationChanges feature enabled)" in new Setup(
        isShowAppealAgainstObligationChangesEnabled = true
      ) {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelUnappealable))
        doc.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Find out how to appeal"
        doc.select(".app-summary-card__footer a span").text() shouldBe "for late VAT return due on 12 March 2021"
        doc.select("dt").eq(4).isEmpty shouldBe true
      }
    }

    "given a non-appealed point and it is unappealable, with an empty appeal level" should {
      implicit def doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelUnappealable.copy(appealLevel = None)))
      "not show the appeal status row and have the check if you can appeal link" in new Setup() {
        doc.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Check if you can appeal"
        doc.select(".app-summary-card__footer a span").text() shouldBe "for late VAT return due on 12 March 2021"
        doc.select("dt").eq(4).isEmpty shouldBe true
      }

      "not show the appeal status row and have the find out how to appeal link (ShowAppealAgainstObligationChanges feature enabled)" in new Setup(
        isShowAppealAgainstObligationChangesEnabled = true
      ) {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelUnappealable.copy(appealLevel = None)))
        doc.select(".app-summary-card__footer a").get(0).ownText() shouldBe "Find out how to appeal"
        doc.select(".app-summary-card__footer a span").text() shouldBe "for late VAT return due on 12 March 2021"
        doc.select("dt").eq(4).isEmpty shouldBe true
      }
    }
  }

}
