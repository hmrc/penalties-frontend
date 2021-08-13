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

package viewmodels

import models.penalty.{LatePaymentPenalty, PaymentStatusEnum}
import models.{ETMPPayload, User}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.PenaltiesService
import utils.MessageRenderer.getMessage
import utils.ViewUtils

import javax.inject.Inject

class IndexPageHelper @Inject()(p: views.html.components.p,
                                strong: views.html.components.strong,
                                bullets: views.html.components.bullets,
                                link: views.html.components.link,
                                warningText: views.html.components.warningText,
                                penaltiesService: PenaltiesService) extends ViewUtils {

  //scalastyle:off
  def getContentBasedOnPointsFromModel(etmpData: ETMPPayload)(implicit messages: Messages, user: User[_]): Html = {
    (etmpData.pointsTotal, etmpData.penaltyPointsThreshold, etmpData.adjustmentPointsTotal) match {
      case (0, _, _) => {
        p(content = stringAsHtml(messages("lsp.pointSummary.noActivePoints")))
      }
      case (currentPoints, threshold, _) if currentPoints >= threshold => {
        html(
          p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p1"))),
            classes = "govuk-body govuk-!-font-size-24"),
          p(content = html(stringAsHtml(getMessage("lsp.onThreshold.p2")))),
          bullets(Seq(
            stringAsHtml(getMessage("lsp.onThreshold.p2.b1")),
            stringAsHtml(getMessage("lsp.onThreshold.p2.b2"))
          )),
          p(link(link = controllers.routes.ComplianceController.onPageLoad().url, messages("lsp.onThreshold.link")))
        )
      }
      case (currentPoints, threshold, adjustedPoints) if adjustedPoints > 0 => {
        val base = Seq(
          p(content = getPluralOrSingular(currentPoints, currentPoints)("lsp.pointSummary.penaltyPoints.adjusted.singular", "lsp.pointSummary.penaltyPoints.adjusted.plural")),
          bullets(Seq(
            getPluralOrSingular(etmpData.lateSubmissions, etmpData.lateSubmissions)("lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.plural"),
            getPluralOrSingular(etmpData.adjustmentPointsTotal, etmpData.adjustmentPointsTotal)("lsp.pointSummary.penaltyPoints.adjusted.addedPoints.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.addedPoints.plural")
          )),
          p(content = stringAsHtml(
            messages("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold)
          )),
          getGuidanceLink
        )
        if (currentPoints == threshold - 1) {
          html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText")))): _*)
        } else {
          html(base: _*)
        }
      }

      case (currentPoints, threshold, adjustedPoints) if adjustedPoints < 0 => {
        val base = Seq(
          p(content = getPluralOrSingular(currentPoints, currentPoints)("lsp.pointSummary.penaltyPoints.adjusted.singular", "lsp.pointSummary.penaltyPoints.adjusted.plural")),
          bullets(Seq(
            getPluralOrSingular(etmpData.lateSubmissions, etmpData.lateSubmissions)("lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.vatReturnsLate.plural"),
            getPluralOrSingular(Math.abs(etmpData.adjustmentPointsTotal), Math.abs(etmpData.adjustmentPointsTotal))("lsp.pointSummary.penaltyPoints.adjusted.removedPoints.singular",
              "lsp.pointSummary.penaltyPoints.adjusted.removedPoints.plural")
          )),
          p(content = stringAsHtml(
            messages("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold)
          )),
          getGuidanceLink
        )
        if (currentPoints == threshold - 1) {
          html(base.+:(warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText")))): _*)
        } else {
          html(base: _*)
        }
      }

      case (currentPoints, threshold, _) if currentPoints < threshold - 1 => {
        html(
          renderPointsTotal(currentPoints),
          p(content = getPluralOrSingularContentForOverview(currentPoints, etmpData.lateSubmissions)),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.anotherPoint")
          )),
          p(content = stringAsHtml(
            getMessage("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold)
          )),
          getGuidanceLink
        )
      }
      case (currentPoints, threshold, _) if currentPoints == threshold - 1 => {
        html(
          renderPointsTotal(currentPoints),
          warningText(stringAsHtml(getMessage("lsp.pointSummary.penaltyPoints.overview.warningText"))),
          p(getPluralOrSingularContentForOverview(currentPoints, etmpData.lateSubmissions)),
          getGuidanceLink
        )
      }
      case _ => p(content = html(stringAsHtml("")))
    }
  }

  def getContentBasedOnLatePaymentPenaltiesFromModel(etmpData: ETMPPayload)(implicit messages: Messages, user: User[_]): Html = {
    if (etmpData.latePaymentPenalties.getOrElse(List.empty[LatePaymentPenalty]).isEmpty) {
      p(content = stringAsHtml(messages("lpp.penaltiesSummary.noPaymentPenalties")))
    } else {
      if (etmpData.latePaymentPenalties.isDefined && etmpData.latePaymentPenalties.get.exists(_.period.paymentStatus != PaymentStatusEnum.Paid)) {
        html(
          p(content = html(stringAsHtml(messages("lpp.penaltiesSummary.unpaid")))),
          p(link(link = "#", messages("lpp.penaltiesSummary.howLppCalculated.link", messages("site.opensInNewTab"))))
        )
      } else {
        p(content = html(stringAsHtml("")))
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

  def getPluralOrSingular(total: Int, arg: Int)(msgForSingular: String, msgForPlural: String)(implicit messages: Messages, user: User[_]): Html = {
    if (total == 1) {
      stringAsHtml(getMessage(msgForSingular, arg))
    } else {
      stringAsHtml(getMessage(msgForPlural, arg))
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

  def getWhatYouOweBreakdown(etmpData: ETMPPayload)(implicit messages: Messages): Option[HtmlFormat.Appendable] = {
    val amountOfLateVAT = penaltiesService.findOverdueVATFromPayload(etmpData)
    val lppAmount = penaltiesService.findEstimatedLPPsFromPayload(etmpData)
    val otherUnrelatedPenalties = penaltiesService.isOtherUnrelatedPenalties(etmpData)
    val totalAmountOfLSPs = penaltiesService.findTotalLSPFromPayload(etmpData)
    val estimatedVATInterest = penaltiesService.findEstimatedVATInterest(etmpData)
    val penaltiesCrystalizedInterest = penaltiesService.findCrystalizedPenaltiesInterest(etmpData)
    val penaltiesEstimatedInterest = penaltiesService.findEstimatedPenaltiesInterest(etmpData)
    val stringToConvertToBulletPoints = Seq(
      //TODO: fill this Seq with Option[String]'s with each bullet point - it will render only those which values exist
      returnMessageIfAmountMoreThanZero(amountOfLateVAT, "whatIsOwed.lateVAT"),
      returnEstimatedVATMessageIfMoreThanZero(estimatedVATInterest._1,estimatedVATInterest._2,"whatIsOwed.VATInterest"),
      returnEstimatedMessageIfHasEstimatedCharges(lppAmount._1, lppAmount._2, "whatIsOwed.lppAmount"),
      returnPenaltiesInterestMessages(penaltiesCrystalizedInterest, penaltiesEstimatedInterest),
      returnMessageIfAmountMoreThanZero(totalAmountOfLSPs, "whatIsOwed.amountOfLSPs"),
      returnMessageIfOtherUnrelatedPenalties(otherUnrelatedPenalties, "whatIsOwed.otherPenalties")
    ).collect{case Some(x) => x}
    if(stringToConvertToBulletPoints.isEmpty) {
      None
    } else {
      Some(bullets(
        stringToConvertToBulletPoints.map {
          stringAsHtml
        }
      ))
    }
  }

  private def returnMessageIfAmountMoreThanZero(amount: BigDecimal, msgKeyToApply: String)(implicit messages: Messages): Option[String] = {
    if(amount > 0) {
      val formattedAmount = if (amount.isWhole()) amount else "%,.2f".format(amount)
      Some(messages(msgKeyToApply, formattedAmount))
    } else None
  }

  private def returnEstimatedMessageIfHasEstimatedCharges(amount: BigDecimal, isEstimate: Boolean, msgKeyToApply: String)(implicit messages: Messages): Option[String] = {
    if(amount > 0) {
      val msgKey = if(isEstimate) s"$msgKeyToApply.estimated" else msgKeyToApply
      Some(messages(msgKey, amount))
    } else None
  }

  private def returnMessageIfOtherUnrelatedPenalties(isUnrelatedPenalties: Boolean, msgKey: String)(implicit messages: Messages): Option[String] = {
    if(isUnrelatedPenalties) {
      Some(messages(msgKey))
    } else None
  }

  private def returnEstimatedVATMessageIfMoreThanZero(vatInterest: BigDecimal, isEstimatedVAT: Boolean, msgKeyToApply: String)(implicit messages: Messages): Option[String]={
   if(vatInterest > 0) {
    (isEstimatedVAT,vatInterest.isWhole() ) match {
      case(true,true) => Some(messages(s"$msgKeyToApply.estimated", vatInterest))
      case(true,false) => Some(messages(s"$msgKeyToApply.estimated", "%,.2f".format(vatInterest)))
      case(false,false) => Some(messages(s"$msgKeyToApply", "%,.2f".format(vatInterest)))
      case(false,true) => Some(messages(s"$msgKeyToApply", vatInterest))
    }
   }
   else None
  }

  private def returnPenaltiesInterestMessages(crystalizedInterest: BigDecimal, estimatedInterest: BigDecimal)(implicit messages: Messages): Option[String] = {
    if (estimatedInterest > 0) {
      val totalInterest = crystalizedInterest + estimatedInterest
      Some(messages("whatIsOwed.allPenalties.estimatedInterest", totalInterest))
    } else if (estimatedInterest == 0 && crystalizedInterest > 0) {
      Some(messages("whatIsOwed.allPenalties.Interest", crystalizedInterest))
    } else None
  }
}
