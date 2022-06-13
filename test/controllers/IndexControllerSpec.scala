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
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import services.v2.{PenaltiesService => PenaltiesServicev2}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import viewmodels.{IndexPageHelper, SummaryCardHelper}
import viewmodels.v2.{IndexPageHelper => IndexPageHelperv2, SummaryCardHelper => SummaryCardHelperv2}
import views.html.IndexView
import views.html.v2.{IndexView => IndexViewv2}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  val page: IndexView = injector.instanceOf[IndexView]
  val indexPageHelper: IndexPageHelper = injector.instanceOf[IndexPageHelper]
  val cardHelper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]
  val mockPenaltiesService: PenaltiesService = mock(classOf[PenaltiesService])
  val page2: IndexViewv2 = injector.instanceOf[IndexViewv2]
  val indexPageHelper2: IndexPageHelperv2 = injector.instanceOf[IndexPageHelperv2]
  val cardHelper2: SummaryCardHelperv2 = injector.instanceOf[SummaryCardHelperv2]
  val mockPenaltiesService2: PenaltiesServicev2 = mock(classOf[PenaltiesServicev2])

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector, mockPenaltiesService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    when(mockPenaltiesService.getETMPDataFromEnrolmentKey(any())(any(), any())).thenReturn(Future.successful(sampleEmptyLspData))
    when(mockPenaltiesService2.getPenaltyDataFromEnrolmentKey(any())(any(), any())).thenReturn(Future.successful(samplePenaltyDetailsModel))
  }

  object Controller extends IndexController(
    page, page2,
    mockPenaltiesService, mockPenaltiesService2,
    cardHelper, cardHelper2,
    indexPageHelper, indexPageHelper2
  )(implicitly, implicitly, authPredicate, errorHandler, stubMessagesControllerComponents())

  "IndexController" should {

    "onPageLoad" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockPenaltiesService2.getLatestLSPCreationDate(any()))
            .thenReturn(None)
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe OK
          await(result).session.get(SessionKeys.latestLSPCreationDate).isEmpty shouldBe true
        }

        "return OK and correct view - adding the latest LSP creation date and threshold into the session in case of compliance view" in
          new Setup(AuthTestModels.successfulAuthResult) {
            when(mockPenaltiesService2.getLatestLSPCreationDate(any()))
              .thenReturn(Some(sampleDateV2))
            val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
            status(result) shouldBe OK
            await(result).session.get(SessionKeys.latestLSPCreationDate).isDefined shouldBe true
            await(result).session.get(SessionKeys.latestLSPCreationDate).get shouldBe sampleDateV2.toString
            await(result).session.get(SessionKeys.pointsThreshold).isDefined shouldBe true
            await(result).session.get(SessionKeys.pointsThreshold).get shouldBe "4"
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
      "the user wants to appeal a penalty" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789")(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=false&isAdditional=false")
      }

      "the user wants to appeal a penalty for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isLPP = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=true&isAdditional=false")
      }

      "the user wants to appeal a penalty for LPP - Additional" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isLPP = true, isAdditional = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=true&isAdditional=true")
      }

      "the user wants to appeal an obligation" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isObligation = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}" +
          s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId&isLPP=false&isAdditional=false")
      }

      "the user wants to appeal an obligation for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.redirectToAppeals("123456789", isLPP = true, isObligation = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(s"${appConfig.penaltiesAppealsBaseUrl}" +
          s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId&isLPP=true&isAdditional=false")
      }
    }

  }
}
