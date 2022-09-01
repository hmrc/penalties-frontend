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

package navigation

import base.SpecBase
import config.AppConfig
import models.{CalculationPage, CompliancePage, IndexPage, Page, User}
import org.mockito.Mockito._
import play.api.mvc.Call

class NavigationSpec extends SpecBase {
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup {
    reset(mockAppConfig)
    val navigation = new Navigation(mockAppConfig)
    when(mockAppConfig.vatOverviewUrlAgent).thenReturn("/agent/url")
  }

  "previousPage" should {
    implicit val user: User[_] = User("12345678")(fakeRequest)
    def previousPageTest(page: Page, expectedCall: Call): Unit = {
      s"route back to the correct page for $page" in new Setup {
        val result = this.navigation.previousPage(page)
        result shouldBe expectedCall
      }
    }

    previousPageTest(IndexPage, Call("GET", "/agent/url"))
    previousPageTest(CompliancePage, controllers.routes.IndexController.onPageLoad)
    previousPageTest(CalculationPage, controllers.routes.IndexController.onPageLoad)
  }
}
