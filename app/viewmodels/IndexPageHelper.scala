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

import models.penalty.LatePaymentPenalty
import models.{ETMPPayload, User}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.MessageRenderer.getMessage
import utils.ViewUtils

import javax.inject.Inject

class IndexPageHelper @Inject()(p: views.html.components.p,
                                strong: views.html.components.strong,
                                bullets: views.html.components.bullets,
                                link: views.html.components.link,
                                warningText: views.html.components.warningText) extends ViewUtils {

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
      if (etmpData.latePaymentPenalties.get.map(_.financial.outstandingAmountDue).sum > 0) {
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
}
