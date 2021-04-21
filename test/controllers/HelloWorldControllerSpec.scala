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
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.AuthTestModels
import views.html.HelloWorldPage

import scala.concurrent.Future

class HelloWorldControllerSpec extends SpecBase {
  val helloWorldPage: HelloWorldPage = injector.instanceOf[HelloWorldPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    val controller = new HelloWorldController(appConfig, stubMessagesControllerComponents(), authPredicate, helloWorldPage)
  }

  "GET /hello-world" should {
    "return 200" when {
      "user is authorised" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.helloWorld(fakeRequest)
        status(result) shouldBe Status.OK
      }

      "user is unauthorised" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result = controller.helloWorld(fakeRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "return HTML" in new Setup(AuthTestModels.successfulAuthResult) {
      val result = controller.helloWorld(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }
}
