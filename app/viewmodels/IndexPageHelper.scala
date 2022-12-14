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

package viewmodels

import config.featureSwitches.FeatureSwitching
import config.{AppConfig, ErrorHandler}
import models.appealInfo.AppealStatusEnum.Upheld
import models.compliance.{CompliancePayload, ComplianceStatusEnum}
import models.lpp.LPPDetails
import models.lsp.{ExpiryReasonEnum, LSPDetails, LSPPenaltyStatusEnum, TaxReturnStatusEnum}
import models.{GetPenaltyDetails, User}
import play.api.i18n.Messages
import play.api.mvc.Result
import play.twirl.api.{Html, HtmlFormat}
import services.{ComplianceService, PenaltiesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.MessageRenderer.getMessage
import utils.{CurrencyFormatter, ImplicitDateFormatter, ViewUtils}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexPageHelper @Inject()(p: views.html.components.p,
                                strong: views.html.components.strong,
                                bullets: views.html.components.bullets,
                                link: views.html.components.link,
                                warningText: views.html.components.warningText,
                                penaltiesService: PenaltiesService,
                                complianceService: ComplianceService,
                                errorHandler: ErrorHandler)(implicit val appConfig: AppConfig) extends ViewUtils with CurrencyFormatter with ImplicitDateFormatter with FeatureSwitching {

  //scalastyle:off
  def getContentBasedOnPointsFromModel(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages, user: User[_],
                                                                          hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, Html]] = {
    val fixedPenaltyAmount: String = parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyDetails.lateSubmissionPenalty.map(_.summary.penaltyChargeAmount).getOrElse(0))
    val activePoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0)
    val regimeThreshold: Int = penaltiesService.getRegimeThreshold(penaltyDetails)
    val removedPoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.summary.inactivePenaltyPoints).getOrElse(0)
    val addedPoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.details.count(point => point.FAPIndicator.contains("X") && point.penaltyStatus.equals(LSPPenaltyStatusEnum.Active))).getOrElse(0)
    val amountOfLateSubmissions: Int = penaltyDetails.lateSubmissionPenalty.map(_.details.count(_.lateSubmissions.flatMap(_.headOption.map(_.taxReturnStatus.equals(TaxReturnStatusEnum.Open))).isDefined)).getOrElse(0)
    (activePoints, regimeThreshold, addedPoints, removedPoints) match {
      case (0, _, _, _) =>
        Future(Right(p(content = stringAsHtml(messages("lsp.pointSummary.noActivePoints")))))
      case (currentPoints, threshold, _, _) if currentPoints >= threshold =>
        lazy val optPOCAchievementDate: Option[LocalDate] = penaltyDetails.lateSubmissionPenalty.map(_.summary.PoCAchievementDate)
        lazy val parsedPOCAchievementDate: String = dateToMonthYearString(optPOCAchievementDate.get)
        callObligationAPI(user.vrn)(implicitly, implicitly, implicitly, optPOCAchievementDate).map {
          _.fold(
            Left(_),
            data => {
              val numberOfOpenObligations = data.obligationDetails.filter(_.status.equals(ComplianceStatusEnum.open))
              if (numberOfOpenObligations.isEmpty) {
                Right(html(
                  p(content = html(stringAsHtml(getMessage("lsp.onThreshold.compliant.p1", parsedPOCAchievementDate)))),
                  bullets(Seq(
                    stringAsHtml(getMessage("lsp.onThreshold.compliant.p2")),
                    stringAsHtml(getMessage("lsp.onThreshold.compliant.p3", getLSPCompliantMonths(threshold)))
                  ))
                ))
              } else {
                Right(html(
                  p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p1"))),
                    classes = "govuk-body govuk-!-font-size-24"),
                  p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p2", fixedPenaltyAmount)))),
                  Html("<p class=" + "govuk-body>" + s"${getMessage("lsp.onThreshold.p3")} <strong>$parsedPOCAchievementDate</strong></p>"),
                  p(link(link = controllers.routes.ComplianceController.onPageLoad.url, getMessage("lsp.onThreshold.link", parsedPOCAchievementDate)))
                ))
              }
            }
          )
        }
      case (currentPoints, threshold, addedPoints, _) if addedPoints > 0 =>
        val base = Seq(
          p(content = getPluralOrSingular(currentPoints)("lsp.pointSummary.penaltyPoints.adjusted.singular", "lsp.pointSummary.penaltyPoints.adjusted.plural")),
          bullets(Seq(
            getPluralOrSingular(amountOfLateSubmissions)("lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.plural"),
            getPluralOrSingular(addedPoints)("lsp.pointSummary.penaltyPoints.adjusted.addedPoints.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.addedPoints.plural")
          )),
          p(content = stringAsHtml(
            messages("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold, fixedPenaltyAmount)
          )),
          getGuidanceLink
        )
        if (currentPoints == threshold - 1) {
          Future(Right(html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount)))): _*)))
        } else {
          Future(Right(html(base: _*)))
        }

      case (currentPoints, threshold, _, removedPoints) if removedPoints > 0 =>
        val base = Seq(
          p(content = getPluralOrSingular(currentPoints)("lsp.pointSummary.penaltyPoints.adjusted.singular", "lsp.pointSummary.penaltyPoints.adjusted.plural")),
          bullets(Seq(
            getPluralOrSingular(amountOfLateSubmissions)("lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.plural"),
            getPluralOrSingular(removedPoints)("lsp.pointSummary.penaltyPoints.adjusted.removedPoints.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.removedPoints.plural")
          )),
          p(content = stringAsHtml(
            messages("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold, fixedPenaltyAmount)
          )),
          getGuidanceLink
        )
        if (currentPoints == threshold - 1) {
          Future(Right(html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount)))): _*)))
        } else {
          Future(Right(html(base: _*)))
        }

      case (currentPoints, threshold, _, _) if currentPoints < threshold - 1 =>
        Future(Right(html(
          renderPointsTotal(currentPoints),
          p(content = getPluralOrSingularContentForOverview(currentPoints, amountOfLateSubmissions)),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.anotherPoint")
          )),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold, fixedPenaltyAmount)
          )),
          getGuidanceLink
        )))
      case (currentPoints, threshold, _, _) if currentPoints == threshold - 1 =>
        Future(Right(html(
          renderPointsTotal(currentPoints),
          warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount))),
          p(getPluralOrSingularContentForOverview(currentPoints, amountOfLateSubmissions)),
          getGuidanceLink
        )))
      case _ => Future(Right(p(content = html(stringAsHtml("")))))
    }
  }

  def getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages, user: User[_]): Html = {
    if (penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(List.empty[LPPDetails]).isEmpty) {
      p(content = stringAsHtml(messages("lpp.penaltiesSummary.noPaymentPenalties")))
    } else {
      val isAnyLPPNotPaidAndNotAppealed: Boolean = penaltyDetails.latePaymentPenalty.exists(_.details.exists(
        penalty => {
          !penalty.appealInformation.exists(_.exists(_.appealStatus.contains(Upheld))) &&
            penalty.principalChargeLatestClearing.isEmpty
        }))
      if (isAnyLPPNotPaidAndNotAppealed) {
        html(
          p(content = html(stringAsHtml(getMessage("lpp.penaltiesSummary.unpaid")))),
          p(link(link = "#", messages("lpp.penaltiesSummary.howLppCalculated.link", messages("site.opensInNewTab"))))
        )
      } else {
        p(link(link = "#", messages("lpp.penaltiesSummary.howLppCalculated.link", messages("site.opensInNewTab"))))
      }
    }
  }

  def getPluralOrSingularContentForOverview(currentPoints: Int, lateSubmissions: Int)(implicit messages: Messages, user: User[_]): Html = {
    if (currentPoints == 1) {
      stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.singular", currentPoints))
    } else {
      stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.plural", currentPoints, lateSubmissions))
    }
  }

  def getPluralOrSingular(total: Int)(msgForSingular: String, msgForPlural: String)(implicit messages: Messages, user: User[_]): Html = {
    if (total == 1) {
      stringAsHtml(getMessage(msgForSingular, total))
    } else {
      stringAsHtml(getMessage(msgForPlural, total))
    }
  }

  def renderPointsTotal(currentPoints: Int)(implicit messages: Messages): Html = {
    p(classes = "govuk-body govuk-!-font-size-27", content = {
      html(
        stringAsHtml(messages("lsp.pointSummary.penaltyPoints.totalSummary", currentPoints)),
        strong(stringAsHtml(s"$currentPoints"))
      )
    })
  }

  def getGuidanceLink(implicit messages: Messages): HtmlFormat.Appendable = p(
    content = link(
      //TODO: change this to external guidance link
      link = "#",
      messageKey = messages("index.guidance.link"),
      id = Some("guidance-link"),
      isExternal = true),
    classes = "govuk-body")

  private def callObligationAPI(vrn: String)(implicit ec: ExecutionContext, hc: HeaderCarrier, user: User[_], pocAchievementDate: Option[LocalDate]): Future[Either[Result, CompliancePayload]] = {
    complianceService.getDESComplianceData(vrn)(implicitly, implicitly, implicitly, pocAchievementDate).map {
      _.fold[Either[Result, CompliancePayload]](
        {
          logger.debug(s"[IndexPageHelper][callObligationAPI] - Received error when calling the Obligation API")
          Left(errorHandler.showInternalServerError)
        })(
        Right(_)
      )
    }
  }

  //TODO: remove V2 suffix when new WYO content added
  def getWhatYouOweBreakdownV2(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages): Option[HtmlFormat.Appendable] = {
    val unpaidVATCharges = penaltiesService.findUnpaidVATCharges(penaltyDetails.totalisations)
    val interestOnAccount = penaltiesService.findInterestOnAccount(penaltyDetails.totalisations)
    val latePaymentPenalties = penaltiesService.findNumberOfLatePaymentPenalties(penaltyDetails.latePaymentPenalty)
    val lateSubmissionPenalties = penaltiesService.findActiveLateSubmissionPenaltyPoints(penaltyDetails.lateSubmissionPenalty).getOrElse(0)
    val regimeThreshold = penaltiesService.getRegimeThreshold(penaltyDetails.lateSubmissionPenalty).getOrElse(0)
    val lateSubmissionPenaltyOptContent = penaltiesService.getContentForLSP(lateSubmissionPenalties, regimeThreshold)
    val whatYouOweContent: Seq[String] = Seq(
      if(unpaidVATCharges > BigDecimal(0)) Some(messages("whatIsOwed.unpaidVATCharges")) else None,
      if(interestOnAccount > BigDecimal(0)) Some(messages("whatIsOwed.unpaidInterest")) else None,
      if(latePaymentPenalties == 1) Some(messages("whatIsOwed.lpp")) else if(latePaymentPenalties > 1) Some(messages("whatIsOwed.lpp.multi")) else None,
      lateSubmissionPenaltyOptContent
    ).collect { case Some(x) => x }
    if(whatYouOweContent.nonEmpty) Some(bullets(whatYouOweContent.map(stringAsHtml))) else None
  }

  //TODO: remove when new WYO content added
  def getWhatYouOweBreakdown(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages): Option[HtmlFormat.Appendable] = {
    val amountOfLateVAT = penaltiesService.findOverdueVATFromPayload(penaltyDetails)
    val crystallisedLPPAmount = penaltiesService.findCrystallisedLPPsFromPayload(penaltyDetails)
    val estimatedLPPAmount = penaltiesService.findEstimatedLPPsFromPayload(penaltyDetails)
    val otherUnrelatedPenalties = penaltiesService.isOtherUnrelatedPenalties(penaltyDetails)
    val totalAmountOfLSPs = penaltiesService.findTotalLSPFromPayload(penaltyDetails)
    val totalNumberOfLSPs = penaltyDetails.lateSubmissionPenalty.map(_.details.filter(
      penalty => penalty.penaltyStatus.equals(LSPPenaltyStatusEnum.Active) &&
        penalty.chargeOutstandingAmount.exists(_ > BigDecimal(0))
    )).map(_.size).getOrElse(0)
    val estimatedVATInterest = penaltiesService.findEstimatedVATInterest(penaltyDetails)
    val singularOrPluralAmountOfLSPs = if (totalNumberOfLSPs > 1) {
      returnEstimatedMessageIfInterestMoreThanZero(totalAmountOfLSPs, isEstimatedAmount = false, "whatIsOwed.amountOfLSPs.plural")
    } else {
      returnEstimatedMessageIfInterestMoreThanZero(totalAmountOfLSPs, isEstimatedAmount = false, "whatIsOwed.amountOfLSPs.singular")
    }
    val stringToConvertToBulletPoints = Seq(
      returnEstimatedMessageIfInterestMoreThanZero(amountOfLateVAT, isEstimatedAmount = false, "whatIsOwed.lateVAT"),
      //TODO implement functionality for VAT interest
      returnEstimatedMessageIfInterestMoreThanZero(estimatedVATInterest._1, estimatedVATInterest._2, "whatIsOwed.VATInterest"),
      returnEstimatedMessageIfInterestMoreThanZero(crystallisedLPPAmount, isEstimatedAmount = false, "whatIsOwed.lppAmount"),
      returnEstimatedMessageIfInterestMoreThanZero(estimatedLPPAmount, isEstimatedAmount = true, "whatIsOwed.lppAmount"),
      singularOrPluralAmountOfLSPs,
      //TODO implement Other penalties mapping
      returnMessageIfOtherUnrelatedPenalties(false, "whatIsOwed.otherPenalties")
    ).collect { case Some(x) => x }
    if (stringToConvertToBulletPoints.isEmpty || (stringToConvertToBulletPoints.size == 1 && otherUnrelatedPenalties)) {
      None
    }
    else {
      Some(bullets(
        stringToConvertToBulletPoints.map {
          stringAsHtml
        }
      ))
    }
  }

  def isTTPActive(penaltyDetails: GetPenaltyDetails): Boolean = {
    penaltyDetails.latePaymentPenalty.exists {
      _.details.headOption.exists { //Current understanding is that TTP values are replicated across every LPP
        _.LPPDetailsMetadata.timeToPay.exists {
          _.exists(ttp => ttp.TTPEndDate.isDefined && (ttp.TTPEndDate.get.isEqual(getFeatureDate) || ttp.TTPEndDate.get.isAfter(getFeatureDate) && //Find any TTP end date that exists and ends today or in the future
            (ttp.TTPStartDate.isEqual(getFeatureDate) || ttp.TTPStartDate.isBefore(getFeatureDate))))  //Find any TTP start date that exists and starts today or earlier
        }
      }
    }
  }

  private def returnMessageIfOtherUnrelatedPenalties(isUnrelatedPenalties: Boolean, msgKey: String)(implicit messages: Messages): Option[String] = {
    //TODO implement other unrelated penalties functionality
    None
  }

  private def returnEstimatedMessageIfInterestMoreThanZero(interestAmount: BigDecimal, isEstimatedAmount: Boolean, msgKeyToApply: String)(implicit messages: Messages): Option[String] = {
    if (interestAmount > 0) {
      (isEstimatedAmount, interestAmount.isWhole) match {
        case (true, true) => Some(messages(s"$msgKeyToApply.estimated", interestAmount))
        case (true, false) => Some(messages(s"$msgKeyToApply.estimated", "%,.2f".format(interestAmount)))
        case (false, false) => Some(messages(s"$msgKeyToApply", "%,.2f".format(interestAmount)))
        case (false, true) => Some(messages(s"$msgKeyToApply", interestAmount))
      }
    }
    else None
  }

  private def getLSPCompliantMonths(pointsThreshold: Int): Int = {
    pointsThreshold match {
      case 5 => 6 //Monthly filer
      case 4 => 12 //Quarterly filer
      case 2 => 24 //Annually filer
    }
  }

  def filteredExpiredPoints(points: Seq[LSPDetails]): Seq[LSPDetails] = {
    val expiredReasonsToFilterOut: Seq[ExpiryReasonEnum.Value] = Seq(
      ExpiryReasonEnum.Reversal,
      ExpiryReasonEnum.NaturalExpiration,
      ExpiryReasonEnum.SubmissionOnTime,
      ExpiryReasonEnum.Compliance
    )
    points.filterNot(point => point.expiryReason.exists(expiredReasonsToFilterOut.contains(_)))
  }

  def sortPointsInDescendingOrder(points: Seq[LSPDetails]): Seq[LSPDetails] = {
    points.sortWith((thisElement, nextElement) => thisElement.penaltyOrder.toInt > nextElement.penaltyOrder.toInt)
  }
}
