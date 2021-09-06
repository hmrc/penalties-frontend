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

package controllers

import base.SpecBase
import models.ETMPPayload
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum}
import models.point.{PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import org.mockito.Matchers
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import viewmodels.CalculationPageHelper
import views.html.{CalculationLPPView, CalculationAdditionalView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends SpecBase {
  val calculationView: CalculationLPPView = injector.instanceOf[CalculationLPPView]
  val calculationAdditionalView: CalculationAdditionalView = injector.instanceOf[CalculationAdditionalView]
  val mockPenaltiesService: PenaltiesService = mock(classOf[PenaltiesService])
  val calculationPageHelper: CalculationPageHelper = injector.instanceOf[CalculationPageHelper]

  val etmpPayload = ETMPPayload(
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

  val etmpPayloadNo15Or30DayAmount = ETMPPayload(
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

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
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

  "onPageLoad" should {

    "the user is authorised" when {
      "show the page when the penalty ID specified matches the payload" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(etmpPayload))

        val result = Controller.onPageLoad("123456789", false)(fakeRequest)
        status(result) shouldBe OK
      }

      "show an ISE when the calculation row can not be rendered - because the payload is invalid (missing both 15/30 day payment amounts)" in
        new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(etmpPayloadNo15Or30DayAmount))

        val result = Controller.onPageLoad("123456789", false)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "show an ISE when the user specifies a penalty ID not in their data" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockPenaltiesService.getETMPDataFromEnrolmentKey(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(sampleLspData))

        val result = Controller.onPageLoad("1234", false)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad("1234", false)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad("1234", false)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
