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
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import services.PenaltiesService
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.AuthTestModels
import views.html.IndexView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class IndexControllerSpec extends SpecBase with MockitoSugar {

  val page: IndexView = injector.instanceOf[IndexView]
  val mockPenaltiesService:PenaltiesService = mock[PenaltiesService]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    when(mockPenaltiesService.getLspDataWithVrn(any())(any())).thenReturn(Future.successful(sampleLspData))
  }

  object Controller extends IndexController(
    page,
    mockPenaltiesService
  )(implicitly, implicitly, authPredicate, stubMessagesControllerComponents())

  "IndexController" should {

    "onPageLoad" when {

      "the user is authorised" when {

        "return OK" in new Setup(AuthTestModels.successfulAuthResult) {

          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)

          status(result) shouldBe OK
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
  }
}
