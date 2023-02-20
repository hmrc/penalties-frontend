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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import models.User
import models.lpp.LPPPenaltyCategoryEnum.LPP2
import models.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PenaltiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.{CurrencyFormatter, EnrolmentKeys, PagerDutyHelper}
import viewmodels.CalculationPageHelper
import views.html.{CalculationLPP2View, CalculationLPPView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculationController @Inject()(viewLPP: CalculationLPPView,
                                      viewLPP2: CalculationLPP2View,
                                      penaltiesService: PenaltiesService,
                                      calculationPageHelper: CalculationPageHelper
                                     )(implicit ec: ExecutionContext,
                                       val appConfig: AppConfig,
                                       errorHandler: ErrorHandler,
                                       authorise: AuthPredicate,
                                       controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoad(principalChargeReference: String, penaltyCategory: String): Action[AnyContent] = authorise.async { implicit request =>
    val penaltyCategoryEnum = LPPPenaltyCategoryEnum.find(penaltyCategory).get
    getPenaltyDetails(principalChargeReference, penaltyCategoryEnum)
  }

  def getPenaltyDetails(principalChargeReference: String, penaltyCategory: LPPPenaltyCategoryEnum.Value)
                       (implicit request: User[_]): Future[Result] = {
    penaltiesService.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).map {
      _.fold(
        errors => {
          logger.error(s"[CalculationController][getPenaltyDetails] - Received status ${errors.status} and body ${errors.body}, rendering ISE.")
          errorHandler.showInternalServerError
        },
        payload => {
          val penalty: Option[LPPDetails] = payload.latePaymentPenalty.flatMap(_.details.find(penalty => {
            penalty.principalChargeReference == principalChargeReference && penalty.penaltyCategory == penaltyCategory
          }))
          if (penalty.isEmpty) {
            logger.error("[CalculationController][getPenaltyDetails] - Tried to render calculation page with new model but could not find penalty specified.")
            PagerDutyHelper.log("CalculationController: getPenaltyDetails", EMPTY_PENALTY_BODY)
            errorHandler.showInternalServerError
          } else {
            val startDateOfPeriod = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingFrom)
            val endDateOfPeriod = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingTo)
            val dueDateOfPenalty = penalty.get.penaltyChargeDueDate.map(calculationPageHelper.getDateAsDayMonthYear(_))
            val isPenaltyEstimate = penalty.get.penaltyStatus.equals(LPPPenaltyStatusEnum.Accruing)
            val amountReceived = if(isPenaltyEstimate) CurrencyFormatter.parseBigDecimalToFriendlyValue(0) else CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountPaid.getOrElse(0))
            val amountLeftToPay = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountOutstanding.get)
            val penaltyAmount = penalty.get.penaltyAmountOutstanding.get + penalty.get.penaltyAmountPaid.getOrElse(BigDecimal(0))
            val parsedPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penaltyAmount)
            val isTTPActive = calculationPageHelper.isTTPActive(payload)
            logger.debug(s"[CalculationController][getPenaltyDetails] - found penalty: ${penalty.get}")
            if (!penaltyCategory.equals(LPP2)) {
              val calculationRow = calculationPageHelper.getCalculationRowForLPP(penalty.get)
              calculationRow.fold({
                logger.error("[CalculationController][getPenaltyDetails] - " +
                  "Calculation row returned None - this could be because the user did not have a defined amount after 15 and/or 30 days of due date")
                PagerDutyHelper.log("CalculationController: getPenaltyDetails", INVALID_DATA_RETURNED_FOR_CALCULATION_ROW)
                errorHandler.showInternalServerError
              })(
                rowSeq => {
                  Ok(viewLPP(amountReceived, parsedPenaltyAmount, amountLeftToPay, rowSeq, isPenaltyEstimate, startDateOfPeriod, endDateOfPeriod,
                    dueDateOfPenalty, isTTPActive))
                })
            } else {
              val isEstimate = penalty.get.penaltyStatus.equals(LPPPenaltyStatusEnum.Accruing)
              Ok(viewLPP2(isEstimate, startDateOfPeriod, endDateOfPeriod, dueDateOfPenalty, parsedPenaltyAmount, amountReceived, amountLeftToPay, isTTPActive))
            }
          }
        }
      )
    }
  }
}
