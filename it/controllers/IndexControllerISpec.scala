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
import stubs.AuthStub
import stubs.PenaltiesStub.returnLSPDataStub
import testUtils.IntegrationSpecCommonBase

import java.time.LocalDateTime

class IndexControllerISpec extends IntegrationSpecCommonBase {
  val sampleDate1 = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleDate2 = LocalDateTime.of(2021, 2, 1, 1, 1, 1)
  val sampleDate3 = LocalDateTime.of(2021, 3, 1, 1, 1, 1)
  val sampleDate4 = LocalDateTime.of(2021, 4, 1, 1, 1, 1)
  val etmpPayloadWithAddedPoints: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        ),
        communications = Seq.empty,
        financial = None
      )
    )
  )

  val etmpPayloadWithRemovedPoints: ETMPPayload = ETMPPayload(
    pointsTotal = 1, lateSubmissions = 2, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Removed,
        reason = Some("This is a great reason."),
        period = PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        ),
        communications = Seq.empty,
        financial = None
      )
    )
  )

  "GET /" should {
    "return 200 (OK) when the user is authorised" in {
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
    }

    "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
      returnLSPDataStub(etmpPayloadWithAddedPoints)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 2 penalty points. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted a VAT Return late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent you a letter explaining why"
      parsedBody.select("header h3").text shouldBe "Penalty point 1: adjustment point"
      parsedBody.select("main strong").text shouldBe "active"
      val summaryCardBody = parsedBody.select(".app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "Added on"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Point due to expire"
      summaryCardBody.select("dd").get(1).text() shouldBe "February 2023"
      summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
      //TODO: Change to external guidance when available
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
      parsedBody.select(".app-summary-card footer li").text shouldBe "You cannot appeal this point"
    }

    "return 200 (OK) and render the view when there are removed points that are retrieved from the backend" in {
      returnLSPDataStub(etmpPayloadWithRemovedPoints)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 1 penalty point. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 2 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("header h3").text shouldBe "Penalty point"
      parsedBody.select("main strong").text shouldBe "removed"
      val summaryCardBody = parsedBody.select(".app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT Period"
      summaryCardBody.select("dd").get(0).text() shouldBe "1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(1).text() shouldBe "Reason"
      summaryCardBody.select("dd").get(1).text() shouldBe "This is a great reason."
      summaryCardBody.select("p.govuk-body a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
      //TODO: Change to external guidance when available
      summaryCardBody.select("p.govuk-body a").attr("href") shouldBe "#"
      parsedBody.select(".app-summary-card footer li").text shouldBe ""
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
