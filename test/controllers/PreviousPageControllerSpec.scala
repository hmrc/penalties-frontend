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

import base.SpecBase
import models.IndexPage
import navigation.Navigation
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.Call
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.Future

class PreviousPageControllerSpec extends SpecBase {
  val mockNavigator = mock(classOf[Navigation])
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller = new PreviousPageController(mockNavigator)(mcc, authPredicate)
  }

  "previousPage" should {
    "redirect to the previous page relative to the given page" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockNavigator.previousPage(Matchers.any())(Matchers.any()))
        .thenReturn(Call("", "/url"))
      val result = controller.previousPage(IndexPage.toString)(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }
  }
}
