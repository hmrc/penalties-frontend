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
import play.api.http.Status.OK
import play.api.test.Helpers.{defaultAwaitTimeout, status, stubMessagesControllerComponents}
import testUtils.AuthMocks
import views.html.TimeoutView

import scala.concurrent.ExecutionContext

class TimeoutControllerSpec extends SpecBase with AuthMocks {

  val page: TimeoutView = injector.instanceOf[TimeoutView]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  object Controller extends TimeoutController(
    page
  )(implicitly, implicitly, stubMessagesControllerComponents())

  "onPageLoad" should {
    "return OK" in {
      val result = Controller.onPageLoad()(fakeRequest)
      status(result) shouldBe OK
    }
  }
}
