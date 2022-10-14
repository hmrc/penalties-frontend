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

import config.featureSwitches.FeatureSwitching
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.PenaltiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys._
import utils.{CurrencyFormatter, EnrolmentKeys, ImplicitDateFormatter}
import viewmodels.{IndexPageHelper, SummaryCardHelper}
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(view: IndexView,
                                penaltiesService: PenaltiesService,
                                cardHelper: SummaryCardHelper,
                                pageHelper: IndexPageHelper
                               )(implicit ec: ExecutionContext,
                                 val appConfig: AppConfig,
                                 authorise: AuthPredicate,
                                 errorHandler: ErrorHandler,
                                 controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter with FeatureSwitching with ImplicitDateFormatter{

  //scalastyle:off
  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    penaltiesService.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).flatMap {
      _.fold(
        errors => {
          logger.error(s"[IndexController][onPageLoad] - Received error with status ${errors.status} and body ${errors.body} rendering ISE.")
          Future(errorHandler.showInternalServerError)
        }, penaltyData => {
          pageHelper.getContentBasedOnPointsFromModel(penaltyData).map {
            _.fold(
              identity,
              contentToDisplayAboveCards => {
                val optPOCAchievementDate: Option[String] = penaltyData.lateSubmissionPenalty.map(_.summary.PoCAchievementDate.toString)
                val optRegimeThreshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold.toString)
                val contentLPPToDisplayAboveCards = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyData)
                val whatYouOweBreakdown = pageHelper.getWhatYouOweBreakdown(penaltyData)
                val filteredPenalties = pageHelper.filteredExpiredPoints(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                val lspSummaryCards = cardHelper.populateLateSubmissionPenaltyCard(filteredPenalties,
                  penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0),
                  penaltyData.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0))
                val lppSummaryCards = cardHelper.populateLatePaymentPenaltyCard(penaltyData.latePaymentPenalty.map(_.details))
                val isAnyUnpaidLSPAndNotSubmittedReturn = penaltiesService.isAnyLSPUnpaidAndSubmissionIsDue(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                val isAnyUnpaidLSP = penaltiesService.isAnyLSPUnpaid(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                val isTTPActive = pageHelper.isTTPActive(penaltyData)
                lazy val result = Ok(view(contentToDisplayAboveCards,
                  contentLPPToDisplayAboveCards,
                  lspSummaryCards,
                  lppSummaryCards,
                  currencyFormatAsNonHTMLString(penaltyData.totalisations.flatMap(_.LSPTotalValue).getOrElse(0)),
                  isAnyUnpaidLSP,
                  isAnyUnpaidLSPAndNotSubmittedReturn,
                  isTTPActive,
                  whatYouOweBreakdown))
                (optPOCAchievementDate.isDefined, optRegimeThreshold.isDefined) match {
                  case (true, true) =>
                    result
                      .removingFromSession(allKeysExcludingAgentVRN: _*)
                      .addingToSession(
                        pocAchievementDate -> optPOCAchievementDate.get,
                        regimeThreshold -> optRegimeThreshold.get
                      )
                  case (true, false) =>
                    result
                      .removingFromSession(allKeysExcludingAgentVRN: _*)
                      .addingToSession(
                        pocAchievementDate -> optPOCAchievementDate.get
                      )
                  case(false, true) =>
                    result
                      .removingFromSession(allKeysExcludingAgentVRN: _*)
                      .addingToSession(
                        regimeThreshold -> optRegimeThreshold.get
                      )
                  case (false, false) =>
                    result
                      .removingFromSession(allKeysExcludingAgentVRN: _*)
                }
              }
            )
          }
        }
      )
    }
  }

  def redirectToAppeals(penaltyId: String, isLPP: Boolean = false, isObligation: Boolean = false,
                        isAdditional: Boolean = false): Action[AnyContent] = authorise.async {
    logger.debug(s"[IndexController][redirectToAppeals] redirect to appeals frontend with id $penaltyId and is late payment penalty: $isLPP " +
      s"and is obligation appeal: $isObligation and is additional: $isAdditional")
    if (isObligation) {
      Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}" +
        s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId&isLPP=$isLPP&isAdditional=$isAdditional"))
    } else {
      Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=$isLPP&isAdditional=$isAdditional"))
    }
  }
}
