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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.ComplianceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, SessionKeys}
import viewmodels.{CompliancePageHelper, TimelineHelper}
import views.html.ComplianceView

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ComplianceController @Inject()(view: ComplianceView,
                                     complianceService: ComplianceService,
                                     pageHelper: CompliancePageHelper,
                                    timelineHelper: TimelineHelper)(implicit ec: ExecutionContext,
                                                                    appConfig: AppConfig,
                                                                    authorise: AuthPredicate,
                                                                    errorHandler: ErrorHandler,
                                                                    controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    complianceService.getDESComplianceData(request.vrn).map {
      _.fold({
        logger.error("[ComplianceController][onPageLoad] - Received None from compliance service")
        errorHandler.showInternalServerError
      })(
        complianceData => {
          val latestLSPCreationDate = LocalDateTime.parse(request.session.get(SessionKeys.latestLSPCreationDate).get).toLocalDate
          val missingReturns = pageHelper.findMissingReturns(complianceData.compliancePayload, latestLSPCreationDate)
          val missingReturnsBulletContent = pageHelper.getUnsubmittedReturnContentFromSequence(missingReturns)
          val timelineContent = timelineHelper.getTimelineContent(complianceData, latestLSPCreationDate)
          Ok(view(
            missingReturns.nonEmpty,
            missingReturnsBulletContent,
            timelineContent
          ))
        }
      )
    }
  }
}
