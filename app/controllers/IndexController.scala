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

import config.AppConfig
import controllers.predicates.AuthPredicate
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.mvc.Results.Ok
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(page: IndexView)(implicit ec: ExecutionContext,
                                                appConfig: AppConfig,
                                                authorise: AuthPredicate,
                                                controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    Future.successful(Ok(page()))
  }
}