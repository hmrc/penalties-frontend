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

import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import models.penalty.LatePaymentPenalty
import models.v3.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import views.html.{CalculationAdditionalView, CalculationLPPView}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PenaltiesService
import services.v2.{PenaltiesService => PenaltiesServiceV2}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.CalculationPageHelper
import models.point.PointStatusEnum
import config.featureSwitches.{CallAPI1812ETMP, FeatureSwitching, UseAPI1812Model}
import models.User

import scala.concurrent.{ExecutionContext, Future}

class CalculationController @Inject()(viewLPP: CalculationLPPView,
                                      viewAdd: CalculationAdditionalView,
                                      penaltiesService: PenaltiesService,
                                      penaltiesServiceV2: PenaltiesServiceV2,
                                      calculationPageHelper: CalculationPageHelper)(implicit ec: ExecutionContext,
                                                                                    val appConfig: AppConfig,
                                                                                    errorHandler: ErrorHandler,
                                                                                    authorise: AuthPredicate,
                                                                                    controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter with FeatureSwitching {

  def onPageLoad(penaltyId: String, isAdditional: Boolean): Action[AnyContent] = authorise.async { implicit request =>
    if (isEnabled(UseAPI1812Model)) {
      logger.debug(s"[CalculationController][onPageLoad] - Making call to API1812 endpoint")
      getPenaltyDetailsFromNewAPI(penaltyId, isAdditional)
    } else {
      logger.debug(s"[CalculationController][onPageLoad] - Making call to old endpoint")
      getOldPenaltyData(penaltyId, isAdditional)
    }
  }

  def getOldPenaltyData(penaltyId: String, isAdditional: Boolean)(implicit request: User[_]): Future[Result] = {
    penaltiesService.getETMPDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).map {
      payload => {
        val penalty: Option[LatePaymentPenalty] = payload.latePaymentPenalties.flatMap(_.find(_.id == penaltyId))
        if (penalty.isEmpty) {
          logger.error("[CalculationController][onPageLoad] - Tried to render calculation page but could not find penalty specified.")
          errorHandler.showInternalServerError
        } else {
          val startDateOfPeriod: String = calculationPageHelper.getDateTimeAsDayMonthYear(penalty.get.period.startDate)
          val endDateOfPeriod: String = calculationPageHelper.getDateTimeAsDayMonthYear(penalty.get.period.endDate)
          val amountReceived = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue - penalty.get.financial.outstandingAmountDue)
          val isPenaltyEstimate = penalty.get.status.equals(PointStatusEnum.Estimated)
          val amountLeftToPay = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.financial.outstandingAmountDue)
          val penaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue)
          logger.debug(s"[CalculationController][onPageLoad] - found penalty: ${penalty.get}")
          if (!isAdditional) {
            val penaltyEstimatedDate = penalty.get.period.dueDate.plusDays(30)
            val calculationRow = calculationPageHelper.getCalculationRowForLPP(penalty.get)
            calculationRow.fold({
              //TODO: log a PD
              logger.error("[CalculationController][onPageLoad] - " +
                "Calculation row returned None - this could be because the user did not have a defined amount after 15 and/or 30 days of due date")
              errorHandler.showInternalServerError
            })(
              rowSeq => {
                val isTwoCalculations: Boolean = rowSeq.size == 2
                val warningPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue * 2)
                val warningDate = calculationPageHelper.getDateTimeAsDayMonthYear(penaltyEstimatedDate)
                Ok(viewLPP(amountReceived, penaltyAmount,
                  amountLeftToPay, rowSeq,
                  isTwoCalculations, isPenaltyEstimate,
                  startDateOfPeriod, endDateOfPeriod,
                  warningPenaltyAmount, warningDate))
              })
          } else {
            val additionalPenaltyRate = "4"
            val daysSince31 = ChronoUnit.DAYS.between(penalty.get.period.dueDate.plusDays(31), LocalDateTime.now())
            val isEstimate = penalty.get.status.equals(PointStatusEnum.Estimated)
            Ok(viewAdd(daysSince31, isEstimate, additionalPenaltyRate, startDateOfPeriod, endDateOfPeriod, penaltyAmount, amountReceived, amountLeftToPay))
          }
        }
      }
    }
  }

  def getPenaltyDetailsFromNewAPI(penaltyId: String, isAdditional: Boolean)(implicit request: User[_]): Future[Result] = {
    penaltiesServiceV2.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).map {
      payload => {
        val penalty: Option[LPPDetails] = payload.latePaymentPenalty.flatMap(_.details.find(_.principalChargeReference == penaltyId))
        if (penalty.isEmpty) {
          logger.error("[CalculationController][onPageLoad] - Tried to render calculation page with new model but could not find penalty specified.")
          errorHandler.showInternalServerError
        } else {
          val startDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingFrom)
          val endDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingTo)
          val amountReceived = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountPaid.get)
          val isPenaltyEstimate = penalty.get.penaltyStatus.equals(LPPPenaltyStatusEnum.Accruing)
          val amountLeftToPay = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountOutstanding.get)
          val penaltyAmount = penalty.get.penaltyAmountOutstanding.get + penalty.get.penaltyAmountPaid.get
          val parsedPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penaltyAmount)
          logger.debug(s"[CalculationController][onPageLoad] - found penalty: ${penalty.get}")
          if (!isAdditional) {
            val penaltyEstimateDate = penalty.get.principalChargeDueDate.plusDays(30)
            val calculationRow = calculationPageHelper.getCalculationRowForLPPForNewAPI(penalty.get)
            calculationRow.fold({
              //TODO: log a PD
              logger.error("[CalculationController][onPageLoad] - " +
                "Calculation row returned None - this could be because the user did not have a defined amount after 15 and/or 30 days of due date")
              errorHandler.showInternalServerError
            })(
              rowSeq => {
                val isTwoCalculations: Boolean = rowSeq.size == 2
                val warningPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountOutstanding.get * 2)
                val warningDate = calculationPageHelper.getDateAsDayMonthYear(penaltyEstimateDate)
                Ok(viewLPP(amountReceived, parsedPenaltyAmount,
                  amountLeftToPay, rowSeq,
                  isTwoCalculations, isPenaltyEstimate,
                  startDateOfPeriod, endDateOfPeriod,
                  warningPenaltyAmount, warningDate))
              })
          } else {
            val additionalPenaltyRate = "4"
            val daysSince31 = ChronoUnit.DAYS.between(penalty.get.principalChargeDueDate.plusDays(31), LocalDate.now())
            val isEstimate = penalty.get.penaltyStatus.equals(LPPPenaltyStatusEnum.Accruing)
            Ok(viewAdd(daysSince31, isEstimate, additionalPenaltyRate, startDateOfPeriod, endDateOfPeriod, parsedPenaltyAmount, amountReceived, amountLeftToPay))
          }
        }
      }
    }
  }
}
