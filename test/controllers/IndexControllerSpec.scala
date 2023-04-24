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

package controllers

import base.{LogCapturing, SpecBase}
import connectors.httpParsers.UnexpectedFailure
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import viewmodels.{IndexPageHelper, SummaryCardHelper}
import views.html.IndexView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with LogCapturing {

  val page: IndexView = injector.instanceOf[IndexView]
  val indexPageHelper: IndexPageHelper = injector.instanceOf[IndexPageHelper]
  val cardHelper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]
  val mockPenaltiesService: PenaltiesService = mock(classOf[PenaltiesService])

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockPenaltiesService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(any())(any(), any())).thenReturn(Future.successful(Right(samplePenaltyDetailsModel)))
  }

  object Controller extends IndexController(
    page,
    mockPenaltiesService,
    cardHelper,
    indexPageHelper
  )(implicitly, implicitly, authPredicate, errorHandler, stubMessagesControllerComponents())

  "IndexController" should {

    "onPageLoad" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe OK
        }

        "return OK and correct view - adding the POC Achievement date in case of compliance view" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
            status(result) shouldBe OK
            await(result).session.get(SessionKeys.pocAchievementDate).isDefined shouldBe true
            await(result).session.get(SessionKeys.pocAchievementDate).get shouldBe "2022-01-01"
          }

        "return an ISE when a left UnexpectedFailure is returned from the service call" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(any())(any(), any()))
            .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, ""))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "return an ISE when a left BadRequest is returned from the service call" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockPenaltiesService.getPenaltyDataFromEnrolmentKey(any())(any(), any()))
            .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_REQUEST, ""))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = Controller.onPageLoad(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = Controller.onPageLoad(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "redirectToAppeals" when {
      "the user wants to appeal a penalty for LSP" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789")(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=false&isAdditional=false")
      }

      "the user wants to appeal a penalty for LPP1" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isLPP = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=true&isAdditional=false")
      }

      "the user wants to appeal a penalty for LPP2" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isLPP = true, isAdditional = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=true&isAdditional=true")
      }

      "the user wants to appeal an obligation" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isObligation = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}" +
          s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId")
      }
    }
  }
}
