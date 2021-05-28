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
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services.PenaltiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.CompliancePageHelper
import views.html.ComplianceView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ComplianceController @Inject()(view: ComplianceView,
                                     penaltiesService: PenaltiesService,
                                     pageHelper: CompliancePageHelper)(implicit ec: ExecutionContext,
                                                                       appConfig: AppConfig,
                                                                       authorise: AuthPredicate,
                                                                       controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    for {
      lSPData <- penaltiesService.getLspDataWithVrn(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn))
      missingReturns = pageHelper.getUnsubmittedReturns(lSPData)
      missingReturnsBulletContent = pageHelper.getUnsubmittedReturnContentFromSequence(missingReturns)
    } yield {
      Ok(view(
        missingReturns.nonEmpty,
        missingReturnsBulletContent
      ))
    }
  }

}
