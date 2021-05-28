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
import models.point.PenaltyPoint
import models.submission.SubmissionStatusEnum
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.ViewUtils

import javax.inject.Inject

class CompliancePageHelper @Inject()(p: views.html.components.p,
                                     bullets: views.html.components.bullets) extends ViewUtils {

  //scalastyle:off
  def getUnsubmittedReturns(etmpData: ETMPPayload): Seq[PenaltyPoint] = {
    // To be replaced by the Compliance Model
    etmpData.penaltyPoints.filter { x =>
      x.period.isDefined && x.period.get.submission.status == SubmissionStatusEnum.Overdue
    }
  }

  def getUnsubmittedReturnContentFromSequence(unsubmittedReturns: Seq[PenaltyPoint])(implicit messages: Messages): Html = {
    if (unsubmittedReturns.nonEmpty) {
      html(
        getUnsubmittedReturnBullets(unsubmittedReturns),
        p(content = html(stringAsHtml(messages("lsp.onThreshold.p1"))),
          classes = "govuk-body govuk-!-font-size-24"),
        p(content = html(stringAsHtml(messages("lsp.onThreshold.p2")))),
      )
    }
    html(stringAsHtml(""))
  }

  def getUnsubmittedReturnBullets(unsubmittedReturns: Seq[PenaltyPoint])(implicit messages: Messages): Html = {
    html(
      bullets(
        unsubmittedReturns.map(unsubmittedReturn => {
          html(stringAsHtml(unsubmittedReturn.period.get.startDate + " to " + unsubmittedReturn.period.get.endDate))
        })
      )
    )
  }
}

