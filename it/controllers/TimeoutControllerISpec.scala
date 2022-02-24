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

import config.AppConfig
import org.jsoup.Jsoup
import org.mockito.Mockito.mock
import play.api.http.Status
import play.api.test.Helpers._
import testUtils.IntegrationSpecCommonBase

class TimeoutControllerISpec extends IntegrationSpecCommonBase {
  val controller: TimeoutController = injector.instanceOf[TimeoutController]
  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  "GET /timeout" should {
    "return 200 (OK) and render the view correctly" in {
      val request = await(buildClientForRequestToApp(uri = "/timeout").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content h1").first().ownText() shouldBe "For your security, we signed you out"
      parsedBody.select("#main-content .govuk-button").first.text() shouldBe "Sign in"
      parsedBody.select("#main-content .govuk-button").first().attr("href") shouldBe appConfig.signInUrl
    }
  }
}
