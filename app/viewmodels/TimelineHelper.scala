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

import models.{FilingFrequencyEnum, User}
import models.compliance.{ComplianceData, CompliancePayload, ComplianceStatusEnum, ObligationDetail}
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.MessageRenderer.getMessage
import utils.{ImplicitDateFormatter, SessionKeys, ViewUtils}

import java.time.LocalDate
import javax.inject.Inject

class TimelineHelper @Inject()(timeline: views.html.components.timeline,
                               p: views.html.components.p) extends ImplicitDateFormatter with ViewUtils {

  def getTimelineContent(complianceData: ComplianceData, latestLSPCreationDate: LocalDate)(implicit messages: Messages, user: User[_]): Html = {
    val returnsAfterLSPCreationDate: Seq[ObligationDetail] = getReturnsAfterLSPCreationDate(complianceData.compliancePayload,
      latestLSPCreationDate)
    if (returnsAfterLSPCreationDate.nonEmpty) {
      val events: Seq[TimelineEvent] = returnsAfterLSPCreationDate.map { compReturn =>
        TimelineEvent(
          headerContent = messages("compliance.timeline.actionEvent.header", dateToString(compReturn.inboundCorrespondenceFromDate),
            dateToString(compReturn.inboundCorrespondenceToDate)),
          spanContent = messages("compliance.timeline.actionEvent.body", dateToString(compReturn.inboundCorrespondenceDueDate)),
          tagContent = if (compReturn.status == ComplianceStatusEnum.fulfilled)
            Some(messages(s"compliance.timeline.actionEvent.tag.submitted")) else None
        )
      }
      val expiryDate: LocalDate = getExpiryDateForPenalties(complianceData, latestLSPCreationDate)
      html(
        timeline(events),
        p(
          content = html(stringAsHtml(getMessage("compliance.point.expiry", dateToMonthYearString(expiryDate)))),
          classes = "govuk-body-l govuk-!-padding-top-3",
          id = Some("point-expiry-date")
        )
      )
    } else {
      html()
    }
  }

  def getReturnsAfterLSPCreationDate(complianceData: CompliancePayload, latestLSPCreationDate: LocalDate): Seq[ObligationDetail] = {
    complianceData.obligationDetails.filter(
      obligation => obligation.inboundCorrespondenceDueDate.isAfter(latestLSPCreationDate)
        || obligation.inboundCorrespondenceDueDate.isEqual(latestLSPCreationDate))
  }

    def getExpiryDateForPenalties(complianceData: ComplianceData, latestLSPCreationDate: LocalDate): LocalDate = {
    complianceData.filingFrequency match {
      case FilingFrequencyEnum.monthly if(complianceData.amountOfSubmissionsRequiredFor24MthsHistory.isDefined) => {
        latestLSPCreationDate.plusMonths(6 + complianceData.amountOfSubmissionsRequiredFor24MthsHistory.get)
      }
      case FilingFrequencyEnum.monthly => latestLSPCreationDate.plusMonths(6)
      case FilingFrequencyEnum.quarterly => latestLSPCreationDate.plusYears(1)
      case FilingFrequencyEnum.annually => latestLSPCreationDate.plusYears(2)
    }
  }
}
