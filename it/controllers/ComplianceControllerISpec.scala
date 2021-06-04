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

package controllers

import models.ETMPPayload
import models.penalty.PenaltyPeriod
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.ComplianceStub._
import testUtils.IntegrationSpecCommonBase
import java.time.LocalDateTime

import models.compliance.{CompliancePayload, MissingReturn, Return, ReturnStatusEnum}

class ComplianceControllerISpec extends IntegrationSpecCommonBase {
  val sampleDate1 = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleDate2 = LocalDateTime.of(2021, 2, 1, 1, 1, 1)
  val sampleDate3 = LocalDateTime.of(2021, 2, 28, 1, 1, 1)
  val sampleDate4 = LocalDateTime.of(2021, 4, 1, 1, 1, 1)

  //TODO: Change this to new compliance payload with missing return

  val compliancePayloadWithMissingReturns: CompliancePayload = CompliancePayload(
    noOfMissingReturns = "1",
    noOfSubmissionsReqForCompliance = "1",
    expiryDateOfAllPenaltyPoints = sampleDate1.plusMonths(1).plusYears(2),
    missingReturns = Seq(
      MissingReturn(
        startDate = sampleDate2,
        endDate = sampleDate3
      )
    ),
    returns = Seq(
      Return(
        startDate = sampleDate2,
        endDate = sampleDate3,
        dueDate = sampleDate2.plusMonths(1).plusDays(7),
        status = Some(ReturnStatusEnum.Submitted)
      )
    )
  )

  val etmpPayloadWithOverdueSubmissionsPoints: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Due,
        reason = None,
        period = Some(
          PenaltyPeriod(
            startDate = sampleDate2,
            endDate = sampleDate3,
            submission = Submission(
              dueDate = sampleDate3.plusMonths(1).plusDays(7),
              submittedDate = None,
              status = SubmissionStatusEnum.Overdue
            )
          )
        ),
        communications = Seq.empty,
        financial = None
      )
    )
  )

  //TODO: Change this to new compliance payload with no missing return

  val compliancePayloadWithNoMissingReturns: CompliancePayload = CompliancePayload(
    noOfMissingReturns = "1",
    noOfSubmissionsReqForCompliance = "1",
    expiryDateOfAllPenaltyPoints = sampleDate1.plusMonths(1).plusYears(2),
    missingReturns = Seq.empty,
    returns = Seq(
      Return(
        startDate = sampleDate2,
        endDate = sampleDate3,
        dueDate = sampleDate2.plusMonths(1).plusDays(7),
        status = None
      )
    )
  )

  "GET /compliance" should {

    "return 200" when {
      "the service call succeeds to get previous and future compliance data" in {
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
      }

      "there is missing returns - show the 'missing returns' content" in {
        returnComplianceDataStub(compliancePayloadWithMissingReturns)
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#submit-these-missing-returns").text shouldBe "Submit these missing returns"
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT Period 1 February 2021 to 28 February 2021") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 8 March 2021") shouldBe true
        parsedBody.body().toString.contains("Submitted on time") shouldBe true
      }

      "there is no missing returns - do not show the 'missing returns' content" in {
        returnComplianceDataStub(compliancePayloadWithNoMissingReturns)
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#submit-these-missing-returns").text.isEmpty shouldBe true
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT Period 1 February 2021 to 28 February 2021") shouldBe false
        parsedBody.body().toString.contains("Submit VAT Return by 8 March 2021") shouldBe true
        parsedBody.body().toString.contains("Submitted on time") shouldBe false
      }
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/compliance").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
