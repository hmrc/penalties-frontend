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

import config.AppConfig
import controllers.predicates.AuthPredicate
import featureSwitches.{FeatureSwitching, UseAPI1812Model}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.PenaltiesService
import services.v2.{PenaltiesService => PenaltiesServicev2}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger.logger
import utils.SessionKeys._
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.{IndexPageHelper, SummaryCardHelper}
import viewmodels.v2.{IndexPageHelper => IndexPageHelperv2, SummaryCardHelper => SummaryCardHelperv2}
import views.html.IndexView
import views.html.v2.{IndexView => IndexViewv2}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(view: IndexView,
                                view2: IndexViewv2,
                                penaltiesService: PenaltiesService,
                                penaltiesServicev2: PenaltiesServicev2,
                                cardHelper: SummaryCardHelper,
                                cardHelper2: SummaryCardHelperv2,
                                pageHelper: IndexPageHelper,
                                pageHelperv2: IndexPageHelperv2)(implicit ec: ExecutionContext,
                                                             appConfig: AppConfig,
                                                             authorise: AuthPredicate,
                                                             controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter with FeatureSwitching {

  def onPageLoad: Action[AnyContent] = authorise.async { implicit request =>
    if(!isEnabled(UseAPI1812Model))
    for {
      etmpData <- penaltiesService.getETMPDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn))
      contentToDisplayAboveCards = pageHelper.getContentBasedOnPointsFromModel(etmpData)
      contentLPPToDisplayAboveCards = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(etmpData)
      whatYouOweBreakdown = pageHelper.getWhatYouOweBreakdown(etmpData)
      lspSummaryCards = cardHelper.populateLateSubmissionPenaltyCard(etmpData.penaltyPoints, etmpData.penaltyPointsThreshold, etmpData.pointsTotal)
      lppSummaryCards = cardHelper.populateLatePaymentPenaltyCard(etmpData.latePaymentPenalties)
      isAnyUnpaidLSPAndNotSubmittedReturn = penaltiesService.isAnyLSPUnpaidAndSubmissionIsDue(etmpData.penaltyPoints)
      isAnyUnpaidLSP = penaltiesService.isAnyLSPUnpaid(etmpData.penaltyPoints)
      latestLSPCreation = penaltiesService.getLatestLSPCreationDate(etmpData)
    } yield {
      lazy val result = Ok(view(contentToDisplayAboveCards,
        contentLPPToDisplayAboveCards,
        lspSummaryCards,
        lppSummaryCards,
        currencyFormatAsNonHTMLString(etmpData.penaltyAmountsTotal),
        isAnyUnpaidLSP,
        isAnyUnpaidLSPAndNotSubmittedReturn,
        whatYouOweBreakdown))
      if(latestLSPCreation.isDefined) {
        result
          .removingFromSession(allKeysExcludingAgentVRN: _*)
          .addingToSession(latestLSPCreationDate -> latestLSPCreation.get.toString,
                           pointsThreshold -> etmpData.penaltyPointsThreshold.toString)
      } else {
        result
          .removingFromSession(allKeysExcludingAgentVRN: _*)
      }
    } else {
      for {
        penaltyData <- penaltiesServicev2.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn))
        contentToDisplayAboveCards = pageHelperv2.getContentBasedOnPointsFromModel(penaltyData)
        contentLPPToDisplayAboveCards = pageHelperv2.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyData)
        whatYouOweBreakdown = pageHelperv2.getWhatYouOweBreakdown(penaltyData)
        lspSummaryCards = cardHelper2.populateLateSubmissionPenaltyCard(penaltyData.lateSubmissionPenalty.get.details,
          penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0),
          penaltyData.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0))
        lppSummaryCards = cardHelper2.populateLatePaymentPenaltyCard(penaltyData.latePaymentPenalty.map(_.details))
        isAnyUnpaidLSPAndNotSubmittedReturn = penaltiesServicev2.isAnyLSPUnpaidAndSubmissionIsDue(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
        isAnyUnpaidLSP = penaltiesServicev2.isAnyLSPUnpaid(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
        latestLSPCreation = penaltiesServicev2.getLatestLSPCreationDate(penaltyData.lateSubmissionPenalty.map(_.details).getOrElse(Seq.empty))
      } yield {
        lazy val result = Ok(view2(contentToDisplayAboveCards,
          contentLPPToDisplayAboveCards,
          lspSummaryCards,
          lppSummaryCards,
          currencyFormatAsNonHTMLString(penaltyData.totalisations.map(_.LSPTotalValue).getOrElse(0)),
          isAnyUnpaidLSP,
          isAnyUnpaidLSPAndNotSubmittedReturn,
          whatYouOweBreakdown))
        if(latestLSPCreation.isDefined) {
          result
            .removingFromSession(allKeysExcludingAgentVRN: _*)
            .addingToSession(latestLSPCreationDate -> latestLSPCreation.get.toString,
              pointsThreshold -> penaltyData.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0).toString)
        } else {
          result
            .removingFromSession(allKeysExcludingAgentVRN: _*)
        }
      }
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
