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
import models.User
import models.v3.lpp.LPPPenaltyCategoryEnum.LPP2
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.v2.{PenaltiesService => PenaltiesServiceV2}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, EnrolmentKeys}
import viewmodels.CalculationPageHelper
import views.html.{CalculationAdditionalView, CalculationLPPView}

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculationController @Inject()(viewLPP: CalculationLPPView,
                                      viewAdd: CalculationAdditionalView,
                                      penaltiesServiceV2: PenaltiesServiceV2,
                                      calculationPageHelper: CalculationPageHelper)(implicit ec: ExecutionContext,
                                                                                    val appConfig: AppConfig,
                                                                                    errorHandler: ErrorHandler,
                                                                                    authorise: AuthPredicate,
                                                                                    controllerComponents: MessagesControllerComponents)
  extends FrontendController(controllerComponents) with I18nSupport with CurrencyFormatter {

  def onPageLoadForNewAPI(principalChargeReference: String, penaltyCategory: String): Action[AnyContent] = authorise.async { implicit request =>
    logger.debug(s"[CalculationController][onPageLoadForNewAPI] - Making call to new endpoint")
    val penaltyCategoryEnum = LPPPenaltyCategoryEnum.find(penaltyCategory).get
    getPenaltyDetailsFromNewAPI(principalChargeReference, penaltyCategoryEnum)
  }

  def getPenaltyDetailsFromNewAPI(principalChargeReference: String, penaltyCategory: LPPPenaltyCategoryEnum.Value)
                                 (implicit request: User[_]): Future[Result] = {
    penaltiesServiceV2.getPenaltyDataFromEnrolmentKey(EnrolmentKeys.constructMTDVATEnrolmentKey(request.vrn)).map {
      _.fold(
        errors => {
          logger.error(s"[OtherReasonController][getPenaltyDetailsFromNewAPI] - Received status ${errors.status} and body ${errors.body}, rendering ISE.")
          errorHandler.showInternalServerError
        },
        payload => {
          val penalty: Option[LPPDetails] = payload.latePaymentPenalty.flatMap(_.details.find(penalty => {
            penalty.principalChargeReference == principalChargeReference && penalty.penaltyCategory == penaltyCategory
          }))
          if (penalty.isEmpty) {
            logger.error("[CalculationController][getPenaltyDetailsFromNewAPI] - Tried to render calculation page with new model but could not find penalty specified.")
            errorHandler.showInternalServerError
          } else {
            val startDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingFrom)
            val endDateOfPeriod: String = calculationPageHelper.getDateAsDayMonthYear(penalty.get.principalChargeBillingTo)
            val amountReceived = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountPaid.get)
            val isPenaltyEstimate = penalty.get.penaltyStatus.equals(LPPPenaltyStatusEnum.Accruing)
            val amountLeftToPay = CurrencyFormatter.parseBigDecimalToFriendlyValue(penalty.get.penaltyAmountOutstanding.get)
            val penaltyAmount = penalty.get.penaltyAmountOutstanding.get + penalty.get.penaltyAmountPaid.get
            val parsedPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(penaltyAmount)
            logger.debug(s"[CalculationController][getPenaltyDetailsFromNewAPI] - found penalty: ${penalty.get}")
            if (!penaltyCategory.equals(LPP2)) {
              val penaltyEstimateDate = penalty.get.principalChargeDueDate.plusDays(30)
              val calculationRow = calculationPageHelper.getCalculationRowForLPPForNewAPI(penalty.get)
              calculationRow.fold({
                //TODO: log a PD
                logger.error("[CalculationController][getPenaltyDetailsFromNewAPI] - " +
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
      )
    }
  }
}
