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

import java.time.LocalDateTime

import javax.inject.Inject
import models.User
import models.compliance.Return
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.MessageRenderer.getMessage
import utils.{ImplicitDateFormatter, ViewUtils}

class TimelineHelper @Inject()(timeline: views.html.components.timeline,
                               p: views.html.components.p) extends ImplicitDateFormatter with ViewUtils {

  def getTimelineContent(complianceReturns: Seq[Return], pointExpiryDate: LocalDateTime)(implicit messages: Messages, user: User[_]): Html = {
    if (complianceReturns.nonEmpty) {
      val events: Seq[TimelineEvent] = complianceReturns.map { compReturn =>
        TimelineEvent(
          headerContent = messages("compliance.timeline.actionEvent.header", dateTimeToString(compReturn.startDate), dateTimeToString(compReturn.endDate)),
          spanContent = messages("compliance.timeline.actionEvent.body", dateTimeToString(compReturn.dueDate)),
          tagContent = if (compReturn.status.isDefined)
            Some(messages(s"compliance.timeline.actionEvent.tag.${compReturn.status.get.toString.toLowerCase}")) else None
        )
      }

      html(
        timeline(events),
        p(
          content = html(stringAsHtml(getMessage("compliance.point.expiry", dateTimeToMonthYearString(pointExpiryDate)))),
          classes = "govuk-body-l govuk-!-padding-top-3",
          id = Some("point-expiry-date")
        )
      )
    }else{
      html()
    }
  }
}
