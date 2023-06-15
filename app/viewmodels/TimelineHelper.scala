/*
 * Copyright 2023 HM Revenue & Customs
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

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail}
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.{ImplicitDateFormatter, ViewUtils}

import javax.inject.Inject

class TimelineHelper @Inject()(timeline: views.html.components.timeline)
                              (implicit val appConfig: AppConfig) extends ImplicitDateFormatter with ViewUtils with FeatureSwitching {

  def getTimelineContent(complianceData: CompliancePayload)(implicit messages: Messages): Html = {
    val unfulfilledReturnsAfterLSPCreationDate: Seq[ObligationDetail] = complianceData.obligationDetails.filter(_.status.equals(ComplianceStatusEnum.Open))
    if (unfulfilledReturnsAfterLSPCreationDate.nonEmpty) {
      val events: Seq[TimelineEvent] = unfulfilledReturnsAfterLSPCreationDate.map { compReturn =>
        val isReturnLate = compReturn.inboundCorrespondenceDueDate.isBefore(getFeatureDate)
        TimelineEvent(
          headerContent = messages("compliance.timeline.actionEvent.header", dateToString(compReturn.inboundCorrespondenceFromDate),
            dateToString(compReturn.inboundCorrespondenceToDate)),
          spanContent =
            if(isReturnLate) messages("compliance.timeline.actionEvent.body.late", dateToString(compReturn.inboundCorrespondenceDueDate))
            else messages("compliance.timeline.actionEvent.body", dateToString(compReturn.inboundCorrespondenceDueDate)),
          tagContent = if (isReturnLate) Some(messages(s"compliance.timeline.actionEvent.tag.late")) else None
        )
      }
      timeline(events)
    } else {
      html()
    }
  }
}
