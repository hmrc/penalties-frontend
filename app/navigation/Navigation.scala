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

package navigation

import config.AppConfig
import models.{CalculationPage, CompliancePage, IndexPage, Page, User}
import play.api.mvc.Call

import javax.inject.Inject

class Navigation @Inject()(appConfig: AppConfig) {
  lazy val reverseRoutes: Map[Page, User[_] => Call] = Map(
    CompliancePage -> (_ => controllers.routes.IndexController.onPageLoad),
    CalculationPage -> (_ => controllers.routes.IndexController.onPageLoad),
    IndexPage -> (_ => Call("GET", appConfig.vatOverviewUrlAgent))
  )

  def previousPage(page: Page)(implicit request: User[_]): Call = {
    reverseRoutes(page)(request)
  }
}
