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

package viewmodels.v2

import models.User
import models.v3.GetPenaltyDetails
import models.v3.appealInfo.AppealStatusEnum.Upheld
import models.v3.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import models.v3.lsp.{LSPPenaltyStatusEnum, TaxReturnStatusEnum}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.v2.PenaltiesService
import utils.MessageRenderer.getMessage
import utils.{CurrencyFormatter, ViewUtils}

import javax.inject.Inject

class IndexPageHelper @Inject()(p: views.html.components.p,
                                strong: views.html.components.strong,
                                bullets: views.html.components.bullets,
                                link: views.html.components.link,
                                warningText: views.html.components.warningText,
                                penaltiesService: PenaltiesService) extends ViewUtils with CurrencyFormatter {

  //scalastyle:off
  def getContentBasedOnPointsFromModel(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages, user: User[_]): Html = {
    val fixedPenaltyAmount: String = parseBigDecimalNoPaddedZeroToFriendlyValue(penaltyDetails.lateSubmissionPenalty.map(_.summary.penaltyChargeAmount).getOrElse(0))
    val activePoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.summary.activePenaltyPoints).getOrElse(0)
    val regimeThreshold: Int = penaltyDetails.lateSubmissionPenalty.map(_.summary.regimeThreshold).getOrElse(0)
    val removedPoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.summary.inactivePenaltyPoints).getOrElse(0)
    val addedPoints: Int = penaltyDetails.lateSubmissionPenalty.map(_.details.count(point => point.FAPIndicator.contains("X") && point.penaltyStatus.equals(LSPPenaltyStatusEnum.Active))).getOrElse(0)
    val amountOfLateSubmissions: Int = penaltyDetails.lateSubmissionPenalty.map(_.details.count(_.lateSubmissions.flatMap(_.headOption.map(_.taxReturnStatus.equals(TaxReturnStatusEnum.Open))).isDefined)).getOrElse(0)
    (activePoints, regimeThreshold, addedPoints, removedPoints) match {
      case (0, _, _, _) =>
        p(content = stringAsHtml(messages("lsp.pointSummary.noActivePoints")))
      case (currentPoints, threshold, _, _) if currentPoints >= threshold =>
        html(
          p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p1"))),
            classes = "govuk-body govuk-!-font-size-24"),
          p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p2")))),
          bullets(Seq(
            stringAsHtml(getMessage("lsp.onThreshold.p2.b1")),
            stringAsHtml(getMessage("lsp.onThreshold.p2.b2", fixedPenaltyAmount))
          )),
          p(link(link = controllers.routes.ComplianceController.onPageLoad.url, messages("lsp.onThreshold.link")))
        )
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
          html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount)))): _*)
        } else {
          html(base: _*)
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
          html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount)))): _*)
        } else {
          html(base: _*)
        }

      case (currentPoints, threshold, _, _) if currentPoints < threshold - 1 =>
        html(
          renderPointsTotal(currentPoints),
          p(content = getPluralOrSingularContentForOverview(currentPoints, amountOfLateSubmissions)),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.anotherPoint")
          )),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold, fixedPenaltyAmount)
          )),
          getGuidanceLink
        )
      case (currentPoints, threshold, _, _) if currentPoints == threshold - 1 =>
        html(
          renderPointsTotal(currentPoints),
          warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText", fixedPenaltyAmount))),
          p(getPluralOrSingularContentForOverview(currentPoints, amountOfLateSubmissions)),
          getGuidanceLink
        )
      case _ => p(content = html(stringAsHtml("")))
    }
  }

  def getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages, user: User[_]): Html = {
    if (penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(List.empty[LPPDetails]).isEmpty) {
      p(content = stringAsHtml(messages("lpp.penaltiesSummary.noPaymentPenalties")))
    } else {
      val isAnyLPPNotPaid: Boolean = penaltyDetails.latePaymentPenalty.exists(_.details.exists(
        penalty => {
          penalty.appealInformation.map(_.exists(_.appealStatus.contains(Upheld))).isEmpty &&
            penalty.penaltyStatus == LPPPenaltyStatusEnum.Accruing
        }))
      if (isAnyLPPNotPaid) {
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
    val penaltiesCrystalizedInterest = penaltiesService.findCrystalizedPenaltiesInterest(penaltyDetails)
    val penaltiesEstimatedInterest = penaltiesService.findEstimatedPenaltiesInterest(penaltyDetails)
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
      returnEstimatedMessageIfInterestMoreThanZero(penaltiesCrystalizedInterest + penaltiesEstimatedInterest, penaltiesEstimatedInterest > BigDecimal(0), "whatIsOwed.allPenalties.interest"),
      singularOrPluralAmountOfLSPs,
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

  private def returnMessageIfOtherUnrelatedPenalties(isUnrelatedPenalties: Boolean, msgKey: String)(implicit messages: Messages): Option[String] = {
    //TODO implement other unrelated penalties functionality
    None
  }

  private def returnEstimatedMessageIfInterestMoreThanZero(interestAmount: BigDecimal, isEstimatedAmount: Boolean, msgKeyToApply: String)(implicit messages: Messages): Option[String] = {
    if (interestAmount > 0) {
      (isEstimatedAmount, interestAmount.isWhole()) match {
        case (true, true) => Some(messages(s"$msgKeyToApply.estimated", interestAmount))
        case (true, false) => Some(messages(s"$msgKeyToApply.estimated", "%,.2f".format(interestAmount)))
        case (false, false) => Some(messages(s"$msgKeyToApply", "%,.2f".format(interestAmount)))
        case (false, true) => Some(messages(s"$msgKeyToApply", interestAmount))
      }
    }
    else None
  }
}
