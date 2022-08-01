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
import config.featureSwitches.FeatureSwitching
import models.{GetPenaltyDetails, Totalisations}
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp._
import models.lsp._
import org.mockito.Matchers
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import viewmodels.CalculationPageHelper
import views.html.{CalculationLPP2View, CalculationLPPView}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends SpecBase with FeatureSwitching {
  val calculationView: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationAdditionalView: CalculationLPP2View = injector.instanceOf[CalculationLPP2View]
  val mockPenaltiesService: PenaltiesService = mock(classOf[PenaltiesService])
  val calculationPageHelper: CalculationPageHelper = injector.instanceOf[CalculationPageHelper]

  val emptyPenaltyDetailsPayload: GetPenaltyDetails = GetPenaltyDetails(None, None, None)

  val penaltyDetailsPayload: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(BigDecimal(200)),
      penalisedPrincipalTotal = Some(BigDecimal(2000)),
      LPPPostedTotal = Some(BigDecimal(165.25)),
      LPPEstimatedTotal = Some(BigDecimal(15.26)),
      LPIPostedTotal = Some(BigDecimal(1968.2)),
      LPIEstimatedTotal = Some(BigDecimal(7))
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
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
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
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
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  val penaltyDetailsPayloadNo15Or30DayAmount: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(BigDecimal(200)),
      penalisedPrincipalTotal = Some(BigDecimal(2000)),
      LPPPostedTotal = Some(BigDecimal(165.25)),
      LPPEstimatedTotal = Some(BigDecimal(15.26)),
      LPIPostedTotal = Some(BigDecimal(1968.2)),
      LPIEstimatedTotal = Some(BigDecimal(7))
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
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
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
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
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], isFSEnabled: Boolean = false) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    reset(mockPenaltiesService)
  }

  object Controller extends CalculationController(
    calculationView,
    calculationAdditionalView,
    mockPenaltiesService,
    calculationPageHelper
  )(implicitly, implicitly, errorHandler, authPredicate, stubMessagesControllerComponents())

  "onPageLoad" when {
    "the user is authorised" should {
      "show the page when the penalty ID specified matches model API1812 payload" in new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
        when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Right(penaltyDetailsPayload)))

        val result: Future[Result] = Controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
        status(result) shouldBe OK
      }
      "show an ISE when the penalty ID specified returns an empty payload" in new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
        when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Right(emptyPenaltyDetailsPayload)))

        val result: Future[Result] = Controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "show an ISE when the calculation row with model API1812 can not be rendered - because the payload is invalid (missing both 15/30 day payment amounts)" in
        new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
          when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(Right(penaltyDetailsPayloadNo15Or30DayAmount)))

          val result: Future[Result] = Controller.onPageLoad("12345678901234", "LPP1")(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

      "show an ISE when the user specifies a penalty ID not found with model API1812 enabled" in
        new Setup(AuthTestModels.successfulAuthResult, isFSEnabled = true) {
          when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(Right(penaltyDetailsPayload)))

          val result: Future[Result] = Controller.onPageLoad("1234", "LPP1")(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
    }

    "the user is unauthorised" should {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad("1234", "LPP1")(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad("1234", "LPP1")(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
