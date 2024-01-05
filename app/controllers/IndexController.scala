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
import viewmodels.{BreathingSpaceHelper, IndexPageHelper, SummaryCardHelper}
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
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter with FeatureSwitching with ImplicitDateFormatter {

  //scalastyle:off
  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    penaltiesService.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).flatMap {
      _.fold(
        errors => {
          logger.error(s"[IndexController][onPageLoad] - Received error with status ${errors.status} and body ${errors.body} rendering ISE.")
          Future(errorHandler.showInternalServerError(Some(request)))
        }, penaltyData => {
          val isUserInBreathingSpace: Boolean = BreathingSpaceHelper.isUserInBreathingSpace(penaltyData.breathingSpace)(getFeatureDate)
          pageHelper.getContentBasedOnPointsFromModel(penaltyData, isUserInBreathingSpace).map {
            _.fold(
              identity,
              contentToDisplayAboveCards => {
                val optPOCAchievementDate: Option[String] = {
                  val pocAchievementDateExists = penaltyData.lateSubmissionPenalty.map(_.summary.PoCAchievementDate.isDefined)
                  if (pocAchievementDateExists.isDefined && pocAchievementDateExists.get) penaltyData.lateSubmissionPenalty.map(_.summary.PoCAchievementDate.get.toString) else None
                }
                val optRegimeThreshold = penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold.toString)
                val contentLPPToDisplayAboveCards = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyData, isUserInBreathingSpace)
                val whatYouOweBreakdown = pageHelper.getWhatYouOweBreakdown(penaltyData)
                val filteredPenalties = pageHelper.filteredExpiredPoints(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                val orderedPenalties = pageHelper.sortPointsInDescendingOrder(filteredPenalties)
                val lspWithPenaltyTypes = pageHelper.setLSPType(orderedPenalties)
                val lspSummaryCards = cardHelper.populateLateSubmissionPenaltyCard(lspWithPenaltyTypes,
                  penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0),
                  penaltyData.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0))
                val lppSummaryCards = cardHelper.populateLatePaymentPenaltyCard(penaltyData.latePaymentPenalty.map(_.details).map(_.sorted))
                val isAnyUnpaidLSPAndNotSubmittedReturn = penaltiesService.isAnyLSPUnpaidAndSubmissionIsDue(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                val isAnyUnpaidLSP = penaltiesService.isAnyLSPUnpaid(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
                lazy val result = Ok(view(contentToDisplayAboveCards,
                  contentLPPToDisplayAboveCards,
                  lspSummaryCards,
                  lppSummaryCards,
                  currencyFormatAsNonHTMLString(penaltyData.totalisations.flatMap(_.LSPTotalValue).getOrElse(0)),
                  isAnyUnpaidLSP,
                  isAnyUnpaidLSPAndNotSubmittedReturn,
                  isUserInBreathingSpace,
                  whatYouOweBreakdown))
                result.removingFromSession(allKeysExcludingAgentVRN: _*).addingToSession(
                    if (optPOCAchievementDate.isDefined) pocAchievementDate -> optPOCAchievementDate.get else "" -> "",
                    if (optPOCAchievementDate.isDefined) regimeThreshold -> optRegimeThreshold.get else "" -> ""
                )
              }
            )
          }
        }
      )
    }
  }

  def redirectToAppeals(penaltyId: String, isLPP: Boolean = false, isFindOutHowToAppeal: Boolean = false,
                        isAdditional: Boolean = false): Action[AnyContent] = authorise.async {
    logger.debug(s"[IndexController][redirectToAppeals] - Redirect to appeals frontend with id $penaltyId and is late payment penalty: $isLPP " +
      s"and cannot be appealed: $isFindOutHowToAppeal and is additional: $isAdditional")
    if (isFindOutHowToAppeal) {
      Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}" +
        s"/initialise-appeal-against-the-obligation?penaltyId=$penaltyId"))
    } else {
      Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=$penaltyId&isLPP=$isLPP&findOutHowToAppeal=$isFindOutHowToAppeal&isAdditional=$isAdditional"))
    }
  }


  def redirectToFindOutHowToAppeal(principalChargeReference: String, vatAmountInPence: Int, vatPeriodStartDate: String, vatPeriodEndDate:String,
                        isCa: Boolean = false): Action[AnyContent] = authorise.async {
    logger.debug(s"[IndexController][redirectToFindOutHowToAppeal] - Redirect to appeals frontend with principleChargeReference: $principalChargeReference " +
      s"and has vatPeriodStartDate: $vatPeriodStartDate and has vatPeriodEndDate: $vatPeriodEndDate and has vatAmountInPence: $vatAmountInPence and is Ca: $isCa")
    Future(Redirect(s"${appConfig.penaltiesAppealsBaseUrl}/initialise-appeal-find-out-how-to-appeal?principalChargeReference=$principalChargeReference&vatAmountInPence=$vatAmountInPence&vatPeriodStartDate=$vatPeriodStartDate&vatPeriodEndDate=$vatPeriodEndDate&isCa=$isCa"))
  }
}
