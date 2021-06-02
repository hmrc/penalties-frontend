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

import javax.inject.Inject
import models.Return
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.{ImplicitDateFormatter, ViewUtils}

class TimelineHelper @Inject()(timeline: views.html.components.timeline) extends ImplicitDateFormatter with ViewUtils {

  def getTimelineContent(complianceReturns: Seq[Return])(implicit messages: Messages): Html = {

    val events: Seq[TimelineEvent] =  complianceReturns.map{ compReturn =>
        TimelineEvent(
          headerContent = messages("compliance.vat.actionEvent.header", dateTimeToString(compReturn.startDate), dateTimeToString(compReturn.endDate)),
          spanContent = messages("compliance.vat.actionEvent.body", dateTimeToString(compReturn.dueDate)),
          tagContent =  if(compReturn.status.isDefined) Some(messages(s"compliance.vat.actionEvent.tag.${compReturn.status.get}")) else None
        )
    }

    html(timeline(events))
  }

}
