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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.ComplianceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys.NO_DATA_RETURNED_FROM_COMPLIANCE
import utils.{CurrencyFormatter, ImplicitDateFormatter, PagerDutyHelper, SessionKeys}
import viewmodels.TimelineHelper
import views.html.ComplianceView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ComplianceController @Inject()(view: ComplianceView,
                                     complianceService: ComplianceService,
                                     timelineHelper: TimelineHelper
                                    )(implicit ec: ExecutionContext,
                                      appConfig: AppConfig,
                                      authorise: AuthPredicate,
                                      errorHandler: ErrorHandler,
                                      controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter with ImplicitDateFormatter {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    complianceService.getDESComplianceData(request.vrn).map {
      _.fold({
        logger.error("[ComplianceController][onPageLoad] - Received None from compliance service")
        PagerDutyHelper.log("ComplianceController: onPageLoad", NO_DATA_RETURNED_FROM_COMPLIANCE)
        errorHandler.showInternalServerError
      })(
        complianceData => {
          val pocAchievementDate: LocalDate = LocalDate.parse(request.session.get(SessionKeys.pocAchievementDate).get)
          val parsedPOCAchievementDate: String = dateToMonthYearString(pocAchievementDate)
          val timelineContent = timelineHelper.getTimelineContent(complianceData)
          val regimeThreshold =  request.session.get(SessionKeys.regimeThreshold).get
          Ok(view(
            timelineContent,
            parsedPOCAchievementDate,
            regimeThreshold
          ))
        }
      )
    }
  }
}