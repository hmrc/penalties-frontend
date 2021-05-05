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

import models.ETMPPayload
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.ViewUtils

import javax.inject.Inject

class IndexPageHelper @Inject()(p: views.html.components.p,
                                strong: views.html.components.strong,
                                warningText: views.html.components.warningText) extends ViewUtils {

  def getContentBasedOnPointsFromModel(etmpData: ETMPPayload)(implicit messages: Messages): Html = {
    (etmpData.pointsTotal, etmpData.penaltyPointsThreshold) match {
      case (0, _) => {
        p(content = stringAsHtml(messages("lsp.pointSummary.noActivePoints")))
      }
      case (currentPoints, threshold) if currentPoints < threshold-1 => {
          html(
            renderPointsTotal(currentPoints),
            p(content = getPluralOrSingularBasedOnCurrentPenaltyPoints(currentPoints, etmpData.lateSubmissions)),
            p(content = stringAsHtml(
              messages("lsp.pointSummary.penaltyPoints.overview.anotherPoint")
            )),
            p(content = stringAsHtml(
              messages("lsp.pointSummary.penaltyPoints.overview.whatHappensWhenThresholdExceeded", threshold)
            ))
          )
      }
      case (currentPoints, threshold) if currentPoints == threshold-1 => {
        html(
          renderPointsTotal(currentPoints),
          warningText(stringAsHtml(messages("lsp.pointSummary.penaltyPoints.overview.warningText"))),
          p(getPluralOrSingularBasedOnCurrentPenaltyPoints(currentPoints, etmpData.lateSubmissions))
        )
      }

      //TODO: replace this with max scenarios - added this so we don't get match errors.
      case _ => {
        p(content = html(stringAsHtml("")))
      }
    }
  }

  def getPluralOrSingularBasedOnCurrentPenaltyPoints(currentPoints: Int, lateSubmissions: Int)(implicit messages: Messages): Html = {
    if(currentPoints == 1) {
      stringAsHtml(messages("lsp.pointSummary.penaltyPoints.overview.singular", currentPoints, lateSubmissions))
    } else {
      stringAsHtml(messages("lsp.pointSummary.penaltyPoints.overview.plural", currentPoints, lateSubmissions))
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
}
