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

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import models.penalty.LatePaymentPenalty
import views.html.{CalculationAdditionalView, CalculationLPPView}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PenaltiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.CalculationPageHelper
import models.point.PointStatusEnum

import scala.concurrent.ExecutionContext

class CalculationController @Inject()(viewLPP: CalculationLPPView,
                                      viewAdd: CalculationAdditionalView,
                                      penaltiesService: PenaltiesService,
                                      calculationPageHelper: CalculationPageHelper)(implicit ec: ExecutionContext,
                                                                                    appConfig: AppConfig,
                                                                                    errorHandler: ErrorHandler,
                                                                                    authorise: AuthPredicate,
                                                                                    controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoad(penaltyId: String, isAdditional: Boolean): Action[AnyContent] = authorise.async { implicit request =>
    penaltiesService.getETMPDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).map {
      payload => {
        val penalty: Option[LatePaymentPenalty] = payload.latePaymentPenalties.flatMap(_.find(_.id == penaltyId))
        if(penalty.isEmpty) {
          logger.error("[CalculationController][onPageLoad] - Tried to render calculation page but could not find penalty specified.")
          errorHandler.showInternalServerError
        } else {
          val startDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.period.startDate)
          val endDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.period.endDate)
          val amountReceived = calculationPageHelper
            .parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue - penalty.get.financial.outstandingAmountDue)
          val isPenaltyEstimate = penalty.get.status.equals(PointStatusEnum.Estimated)
          val amountLeftToPay = calculationPageHelper.parseBigDecimalToFriendlyValue(penalty.get.financial.outstandingAmountDue)
          val penaltyAmount = calculationPageHelper.parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue)
          logger.debug(s"[CalculationController][onPageLoad] - found penalty: ${penalty.get}")
          if(!isAdditional) {
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
                val warningPenaltyAmount = calculationPageHelper.parseBigDecimalToFriendlyValue(penalty.get.financial.amountDue * 2)
                val warningDate = calculationPageHelper.getDateAsDayMonthYear(penaltyEstimatedDate)
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
            Ok(viewAdd(daysSince31, isEstimate, additionalPenaltyRate, startDateOfPeriod, endDateOfPeriod, penaltyAmount, amountReceived,amountLeftToPay))
          }
        }
      }
    }
  }
}
