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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import testUtils.IntegrationSpecCommonBase
import utils.SessionKeys

class CalculationControllerISpec extends IntegrationSpecCommonBase {

  val controller = injector.instanceOf[CalculationController]
  val fakeAgentRequest = FakeRequest("GET", "/calculation").withSession(SessionKeys.agentSessionVrn -> "123456789")

  "GET /" should {
    "return 200 (OK) when the user is authorised" in {
      val request = await(buildClientForRequestToApp(uri = "/calculation").get())
      request.status shouldBe Status.OK
    }

    "return 200 (OK) and render the view correctly" in { //TODO: implement without placeholders
      val request = await(buildClientForRequestToApp(uri = "/calculation").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content h1").text() shouldBe "Late payment penalty"
      parsedBody.select("#main-content tr").get(0).select("th").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content tr").get(0).select("td").text() shouldBe "£0" //TODO: placeholder value
      parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Calculation"
      parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "0% of £0 (PLACEHOLDER)" //TODO: placeholder value
      parsedBody.select("#main-content tr").get(2).select("th").text() shouldBe "Amount received"
      parsedBody.select("#main-content tr").get(2).select("td").text() shouldBe "£0" //TODO: placeholder value
      parsedBody.select("#main-content tr").get(3).select("th").text() shouldBe "Amount left to pay"
      parsedBody.select("#main-content tr").get(3).select("td").text() shouldBe "£0" //TODO: placeholder value
      parsedBody.select("#main-content a").text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/calculation").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
