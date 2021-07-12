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
import services.PenaltiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger.logger
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.{IndexPageHelper, SummaryCardHelper}
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(view: IndexView,
                                penaltiesService: PenaltiesService,
                                cardHelper: SummaryCardHelper,
                                pageHelper: IndexPageHelper)(implicit ec: ExecutionContext,
                                                             appConfig: AppConfig,
                                                             authorise: AuthPredicate,
                                                             controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    for {
      lSPData <- penaltiesService.getLspDataWithVrn(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn))
      contentToDisplayAboveCards = pageHelper.getContentBasedOnPointsFromModel(lSPData)
      contentLPPToDisplayAboveCards = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(lSPData)
      summaryCards = cardHelper.populateCard(lSPData.penaltyPoints, lSPData.penaltyPointsThreshold, lSPData.pointsTotal)
      isAnyUnpaidLSPAndNotSubmittedReturn = penaltiesService.isAnyLSPUnpaidAndSubmissionIsDue(lSPData.penaltyPoints)
      isAnyUnpaidLSP = penaltiesService.isAnyLSPUnpaid(lSPData.penaltyPoints)
    } yield {
      Ok(view(contentToDisplayAboveCards,
        contentLPPToDisplayAboveCards,
        summaryCards,
        currencyFormatAsNonHTMLString(lSPData.penaltyAmountsTotal),
        isAnyUnpaidLSP,
        isAnyUnpaidLSPAndNotSubmittedReturn))
    }
  }

  def redirectToAppeals(penaltyId: String): Action[AnyContent] = authorise.async { implicit request =>
    logger.debug(s"[IndexController][redirectToAppeals] redirect to appeals frontend with id $penaltyId")
    Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId"))
  }
}
