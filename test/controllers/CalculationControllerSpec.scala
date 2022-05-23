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

package controllers

import base.SpecBase
import config.featureSwitches.{CallAPI1812ETMP, FeatureSwitching}
import models.ETMPPayload
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum}
import models.point.{PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import models.v3.lsp._
import models.v3.lpp.{LatePaymentPenalty => NewLatePaymentPenalty}
import models.v3.{GetPenaltyDetails, Totalisations}
import org.mockito.Matchers
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import services.v2.{PenaltiesService => PenaltiesServiceV2}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import viewmodels.CalculationPageHelper
import views.html.{CalculationAdditionalView, CalculationLPPView}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends SpecBase with FeatureSwitching {
  val calculationView: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationAdditionalView: CalculationAdditionalView = injector.instanceOf[CalculationAdditionalView]
  val mockPenaltiesService: PenaltiesService = mock(classOf[PenaltiesService])
  val mockPenaltiesServiceV2: PenaltiesServiceV2 = mock(classOf[PenaltiesServiceV2])
  val calculationPageHelper: CalculationPageHelper = injector.instanceOf[CalculationPageHelper]

  val etmpPayload: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0,
    penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 4,
    otherPenalties = None,
    vatOverview = None,
    penaltyPoints = Seq.empty,
    latePaymentPenalties = Some(Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
        dateCreated = sampleDate,
        status = PointStatusEnum.Due,
        appealStatus = None,
        period = PaymentPeriod(
          startDate = sampleDate, endDate = sampleDate, dueDate = sampleDate, paymentStatus = PaymentStatusEnum.Paid
        ),
        communications = Seq.empty,
        financial = Financial(
          amountDue = 300,
          outstandingAmountDue = 10.21,
          dueDate = sampleDate,
          outstandingAmountDay15 = Some(10),
          outstandingAmountDay31 = None,
          percentageOfOutstandingAmtCharged = Some(2),
          estimatedInterest = None,
          crystalizedInterest = None
        )
      )
    ))
  )

  val etmpPayloadNo15Or30DayAmount: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0,
    penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 4,
    otherPenalties = None,
    vatOverview = None,
    penaltyPoints = Seq.empty,
    latePaymentPenalties = Some(Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
        dateCreated = sampleDate,
        status = PointStatusEnum.Due,
        appealStatus = None,
        period = PaymentPeriod(
          startDate = sampleDate, endDate = sampleDate, dueDate = sampleDate, paymentStatus = PaymentStatusEnum.Paid
        ),
        communications = Seq.empty,
        financial = Financial(
          amountDue = 300,
          outstandingAmountDue = 10.21,
          dueDate = sampleDate,
          outstandingAmountDay15 = None,
          outstandingAmountDay31 = None,
          percentageOfOutstandingAmtCharged = Some(2),
          estimatedInterest = None,
          crystalizedInterest = None
        )
      )
    ))
  )

  val penaltyDetailsPayload: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = BigDecimal(200),
      penalisedPrincipalTotal = BigDecimal(2000),
      LPPPostedTotal = BigDecimal(165.25),
      LPPEstimatedTotal = BigDecimal(15.26),
      LPIPostedTotal = BigDecimal(1968.2),
      LPIEstimatedTotal = BigDecimal(7))),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some("FAP"),
          communicationsDate = LocalDate.parse("2069-10-30"),
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
              appealLevel =  Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(NewLatePaymentPenalty(
      details = Seq(LPPDetails(
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
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2069-10-30"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel =  Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  val penaltyDetailsPayloadNo15Or30DayAmount: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = BigDecimal(200),
      penalisedPrincipalTotal = BigDecimal(2000),
      LPPPostedTotal = BigDecimal(165.25),
      LPPEstimatedTotal = BigDecimal(15.26),
      LPIPostedTotal = BigDecimal(1968.2),
      LPIEstimatedTotal = BigDecimal(7))),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some("FAP"),
          communicationsDate = LocalDate.parse("2069-10-30"),
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
              appealLevel =  Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(NewLatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(1001.45),
        penaltyAmountOutstanding = Some(99.99),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = None,
        LPP1HRCalculationAmount = None,
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2069-10-30"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel =  Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], isFSEnabled: Boolean = false) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    if(isFSEnabled) enableFeatureSwitch(CallAPI1812ETMP) else disableFeatureSwitch(CallAPI1812ETMP)
    reset(mockPenaltiesService)
  }

  object Controller extends CalculationController(
    calculationView,
    calculationAdditionalView,
    mockPenaltiesService,
    mockPenaltiesServiceV2,
    calculationPageHelper
  )(implicitly, implicitly, errorHandler, authPredicate, stubMessagesControllerComponents())

  "onPageLoad" when {

    "the user is authorised" should {
      "show the page when the penalty ID specified matches the payload" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(etmpPayload))

        val result: Future[Result] = Controller.onPageLoad("123456789", isAdditional = false)(fakeRequest)
        status(result) shouldBe OK
      }

      "show an ISE when the calculation row can not be rendered - because the payload is invalid (missing both 15/30 day payment amounts)" in
        new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(etmpPayloadNo15Or30DayAmount))

        val result: Future[Result] = Controller.onPageLoad("123456789", isAdditional = false)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "show an ISE when the user specifies a penalty ID not in their data" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(sampleEmptyLspData))

        val result: Future[Result] = Controller.onPageLoad("1234", isAdditional = false)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "show the page when the penalty ID specified matches model API1812 payload" in new Setup(AuthTestModels.successfulAuthResult , isFSEnabled = true) {
        when(mockPenaltiesServiceV2.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(penaltyDetailsPayload))

        val result: Future[Result] = Controller.onPageLoad("12345678901234", isAdditional = false)(fakeRequest)
        status(result) shouldBe OK
      }

      "show an ISE when the calculation row with model API1812 can not be rendered - because the payload is invalid (missing both 15/30 day payment amounts)" in
        new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
          when(mockPenaltiesServiceV2.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(penaltyDetailsPayloadNo15Or30DayAmount))

          val result: Future[Result] = Controller.onPageLoad("12345678901234", isAdditional = false)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

      "show an ISE when the user specifies a penalty ID not found with model API1812 enabled" in
        new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
          when(mockPenaltiesServiceV2.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(penaltyDetailsPayload))

          val result: Future[Result] = Controller.onPageLoad("1234", isAdditional = false)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad("1234", isAdditional = false)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad("1234", isAdditional = false)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
