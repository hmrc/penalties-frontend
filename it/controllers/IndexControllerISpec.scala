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
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.Jsoup
import play.api.http.{HeaderNames, Status}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub.returnLSPDataStub
import testUtils.IntegrationSpecCommonBase
import utils.SessionKeys

import java.time.LocalDateTime
import models.communication.{Communication, CommunicationTypeEnum}
import models.payment.PaymentFinancial

class IndexControllerISpec extends IntegrationSpecCommonBase {
  val sampleDate1 = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleDate2 = LocalDateTime.of(2021, 2, 1, 1, 1, 1)
  val sampleDate3 = LocalDateTime.of(2021, 3, 1, 1, 1, 1)
  val sampleDate4 = LocalDateTime.of(2021, 4, 1, 1, 1, 1)
  val controller = injector.instanceOf[IndexController]
  val fakeAgentRequest = FakeRequest("GET", "/").withSession(SessionKeys.agentSessionVrn -> "123456789")
  val etmpPayloadWithAddedPoints: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0,Some(false),Some(Seq.empty), penaltyPointsThreshold = 4, penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = None,
        communications = Seq.empty,
        financial = None
      )
    ),Option(Seq.empty[LatePaymentPenalty])
  )

  val etmpPayloadWithRemovedPoints: ETMPPayload = ETMPPayload(
    pointsTotal = 1, lateSubmissions = 2, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, Some(false),Some(Seq.empty),penaltyPointsThreshold = 4,
    penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Removed,
        reason = Some("This is a great reason."),
        period = Some(PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      )
    ),Option(Seq.empty[LatePaymentPenalty])
  )

  val etmpPayloadWith2PointsandOneRemovedPoint: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 3, adjustmentPointsTotal = -1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0,Some(false),Some(Seq.empty), penaltyPointsThreshold = 4, penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "4",
        appealStatus = None,
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Active,
        None,
        period = Some(PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "12345678901",
        number = "3",
        appealStatus = None,
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Active,
        None,
        period = Some(PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "12345678902",
        number = "2",
        appealStatus = None,
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Active,
        None,
        period = Some(PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "12345678903",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Removed,
        reason = Some("This is a great reason."),
        period = Some(PenaltyPeriod(
          startDate = sampleDate1, endDate = sampleDate2, submission = Submission(
            sampleDate3,
            Some(sampleDate4),
            SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq.empty,
        financial = None
      )
    ),
    Option(Seq.empty[LatePaymentPenalty])
  )

  val latePaymentPenalty: Option[Seq[LatePaymentPenalty]] = Some(Seq(LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = "this is a reason",
    dateCreated = sampleDate1,
    status = PointStatusEnum.Paid,
    appealStatus = None,
    period = PaymentPeriod(
      sampleDate1,
      sampleDate1.plusMonths(1),
      sampleDate1.plusMonths(2).plusDays(7),
      PaymentStatusEnum.Paid
    ),
    communications = Seq(
      Communication(
        `type` = CommunicationTypeEnum.letter,
        dateSent = sampleDate1,
        documentId = "123456789"
      )
    ),
    financial = PaymentFinancial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = sampleDate1
    )
  )))

  val latePaymentPenaltyVATUnpaid: Option[Seq[LatePaymentPenalty]] = Some(Seq(LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = "this is a reason",
    dateCreated = sampleDate1,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      sampleDate1,
      sampleDate1.plusMonths(1),
      sampleDate1.plusMonths(1).plusDays(7),
      PaymentStatusEnum.Due
    ),
    communications = Seq(
      Communication(
        `type` = CommunicationTypeEnum.letter,
        dateSent = sampleDate1,
        documentId = "123456789"
      )
    ),
    financial = PaymentFinancial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = sampleDate1
    )
  )))

  val latePaymentPenaltyWithAppeal: Option[Seq[LatePaymentPenalty]] = Some(Seq(latePaymentPenalty.get.head.copy(appealStatus = Some(AppealStatusEnum.Under_Review))))

  val etmpPayloadWithLPP: ETMPPayload = etmpPayloadWithAddedPoints.copy(
    latePaymentPenalties = latePaymentPenalty
  )

  val etmpPayloadWithLPPVATUnpaid: ETMPPayload = etmpPayloadWithAddedPoints.copy(
    latePaymentPenalties = latePaymentPenaltyVATUnpaid
  )

  val etmpPayloadWithLPPAppeal: ETMPPayload = etmpPayloadWithLPP.copy(
    latePaymentPenalties = latePaymentPenaltyWithAppeal
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

    "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
      returnLSPDataStub(etmpPayloadWith2PointsandOneRemovedPoint)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "You have 2 penalty points. This is because:"
      parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "you have submitted 3 VAT Returns late"
      parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent you a letter explaining why"
      parsedBody.select("main section h3").get(0).text shouldBe "Penalty point 3"
      parsedBody.select("main section h3").get(1).text shouldBe "Penalty point 2"
      parsedBody.select("main section h3").get(2).text shouldBe "Penalty point 1"
      parsedBody.select("main section h3").get(3).text shouldBe "Penalty point"
      parsedBody.select("main section strong").get(3).text shouldBe "removed"
    }

    "return 200 (OK) and render the view when there are LPPs paid that are retrieved from the backend" in {
      returnLSPDataStub(etmpPayloadWithLPP)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT Period"
      summaryCardBody.select("dd").get(0).text shouldBe "1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(1).text shouldBe "Penalty reason"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT not paid within 15 days"
      parsedBody.select("#late-payment-penalties footer li").text() shouldBe "Appeal this penalty"
    }

    "return 200 (OK) and render the view when there are LPPs with VAT unpaid that are retrieved from the backend" in {
      returnLSPDataStub(etmpPayloadWithLPPVATUnpaid)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "due"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT Period"
      summaryCardBody.select("dd").get(0).text shouldBe "1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(1).text shouldBe "Penalty reason"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT not paid within 15 days"
      parsedBody.select("#late-payment-penalties footer li").text() shouldBe "Check if you can appeal"
    }

    "return 200 (OK) and render the view when there are appealed LPPs that are retrieved from the backend" in {
      returnLSPDataStub(etmpPayloadWithLPPAppeal)
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#late-payment-penalties section header h3").text shouldBe "£400 penalty"
      parsedBody.select("#late-payment-penalties section header strong").text shouldBe "paid"
      val summaryCardBody = parsedBody.select(" #late-payment-penalties .app-summary-card__body")
      summaryCardBody.select("dt").get(0).text shouldBe "VAT Period"
      summaryCardBody.select("dd").get(0).text shouldBe "1 January 2021 to 1 February 2021"
      summaryCardBody.select("dt").get(1).text shouldBe "Penalty reason"
      summaryCardBody.select("dd").get(1).text shouldBe "VAT not paid within 15 days"
      summaryCardBody.select("dt").get(2).text shouldBe "Appeal status"
      summaryCardBody.select("dd").get(2).text shouldBe "Under review by HMRC"
    }

    "agent view" must {
      "return 200 (OK) and render the view when there are added points that are retrieved from the backend" in {
        AuthStub.agentAuthorised()
        returnLSPDataStub(etmpPayloadWithAddedPoints)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 2 penalty points. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted a VAT Return late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we added 1 point and sent them a letter explaining why"
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
        AuthStub.agentAuthorised()
        returnLSPDataStub(etmpPayloadWithRemovedPoints)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 1 penalty point. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 2 VAT Returns late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent them a letter explaining why"
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

      "return 200 (OK) and render the view when removed points are below active points (active points are reindexed)" in {
        AuthStub.agentAuthorised()
        returnLSPDataStub(etmpPayloadWith2PointsandOneRemovedPoint)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#late-submission-penalties p.govuk-body").get(0).text shouldBe "Your client has 2 penalty points. This is because:"
        parsedBody.select("#late-submission-penalties ul li").get(0).text shouldBe "they have submitted 3 VAT Returns late"
        parsedBody.select("#late-submission-penalties ul li").get(1).text shouldBe "we removed 1 point and sent them a letter explaining why"
        parsedBody.select("main section h3").get(0).text shouldBe "Penalty point 3"
        parsedBody.select("main section h3").get(1).text shouldBe "Penalty point 2"
        parsedBody.select("main section h3").get(2).text shouldBe "Penalty point 1"
        parsedBody.select("main section h3").get(3).text shouldBe "Penalty point"
        parsedBody.select("main section strong").get(3).text shouldBe "removed"
      }
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-penalty" should {
    "redirect the user to the appeals service when the penalty is not a LPP" in {
      val request = buildClientForRequestToApp(uri = "/appeal-penalty?penaltyId=1234&isLPP=false&isObligation=false").get()
      await(request).status shouldBe Status.SEE_OTHER
      await(request).header(HeaderNames.LOCATION).get shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=false"
    }

    "redirect the user to the appeals service when the penalty is a LPP" in {
      val request = buildClientForRequestToApp(uri = "/appeal-penalty?penaltyId=1234&isLPP=true&isObligation=false").get()
      await(request).status shouldBe Status.SEE_OTHER
      await(request).header(HeaderNames.LOCATION).get shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal?penaltyId=1234&isLPP=true"
    }

    "redirect the user to the obligations appeals service when the penalty is not a LPP" in {
      val request = buildClientForRequestToApp(uri = "/appeal-penalty?penaltyId=1234&isLPP=false&isObligation=true").get()
      await(request).status shouldBe Status.SEE_OTHER
      await(request).header(HeaderNames.LOCATION).get shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234&isLPP=false"
    }

    "redirect the user to the obligations appeals service when the penalty is a LPP" in {
      val request = buildClientForRequestToApp(uri = "/appeal-penalty?penaltyId=1234&isLPP=true&isObligation=true").get()
      await(request).status shouldBe Status.SEE_OTHER
      await(request).header(HeaderNames.LOCATION).get shouldBe "http://localhost:9181/penalties-appeals/initialise-appeal-against-the-obligation?penaltyId=1234&isLPP=true"
    }
  }
}
