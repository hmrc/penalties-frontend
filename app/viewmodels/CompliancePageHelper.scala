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

import models.User
import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail}
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.{ImplicitDateFormatter, ViewUtils}

import java.time.LocalDate
import javax.inject.Inject

class CompliancePageHelper @Inject()(bullets: views.html.components.bullets) extends ViewUtils with ImplicitDateFormatter {

  def getUnsubmittedReturnContentFromSequence(missingReturns: Seq[ObligationDetail])(implicit messages: Messages): Html = {
    if(missingReturns.nonEmpty) {
      html(
        bullets(
          missingReturns.map(unsubmittedReturn => {
            html(stringAsHtml(
              messages("compliance.vat.missingReturn",
                dateToString(unsubmittedReturn.inboundCorrespondenceFromDate),
                dateToString(unsubmittedReturn.inboundCorrespondenceToDate))))
          }),
          classes = "govuk-list govuk-list--bullet govuk-body-l")
      )
    } else {
      html()
    }
  }

  def findMissingReturns(compliancePayload: CompliancePayload, latestLSPCreationDate: LocalDate): Seq[ObligationDetail] = {
    compliancePayload.obligationDetails.filter(obligation =>
      obligation.status == ComplianceStatusEnum.open && obligation.inboundCorrespondenceDueDate.isBefore(latestLSPCreationDate)
    )
  }
}

