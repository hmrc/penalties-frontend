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
import models.communication.{Communication, CommunicationTypeEnum}
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub.returnLSPDataStub
import testUtils.IntegrationSpecCommonBase

import java.time.LocalDateTime

class CalculationControllerISpec extends IntegrationSpecCommonBase {
  val controller = injector.instanceOf[CalculationController]
  val sampleDate1 = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

  val etmpPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
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
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
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
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Due,
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
          financial = Financial(
            amountDue = 400.00,
            outstandingAmountDue = 123.00,
            outstandingAmountDay15 = Some(123),
            outstandingAmountDay31 = Some(123),
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = sampleDate1
          )
        )
      )
    )
  )

  val etmpPayloadWithAdditionalPenalty = etmpPayload.copy(latePaymentPenalties = Some(
    Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Additional,
        id = "987654321",
        PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Paid,
        appealStatus = None,
        period = PaymentPeriod(
          sampleDate1,
          sampleDate1.plusMonths(1),
          sampleDate1.plusMonths(2).plusDays(31),
          PaymentStatusEnum.Paid
        ),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate1,
            documentId = "123456789"
          )
        ),
        financial = Financial(
          amountDue = 123.45,
          outstandingAmountDue = 0.00,
          dueDate = LocalDateTime.now().minusDays(40)
        )
      )
    )
  ))

  val etmpPayloadWithAdditionalDuePenalty = etmpPayloadWithAdditionalPenalty.copy(latePaymentPenalties = Some(
    Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Additional,
        id = "987654322",
        PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Due,
        appealStatus = None,
        period = PaymentPeriod(
          sampleDate1,
          sampleDate1.plusMonths(1),
          sampleDate1.plusMonths(2).plusDays(7),
          PaymentStatusEnum.Due
        ),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate1,
            documentId = "123456789"
          )
        ),
        financial = Financial(
          amountDue = 123.45,
          outstandingAmountDue = 0.00,
          dueDate = LocalDateTime.now().minusDays(40)
        )
      )
    )
  ))

  val etmpPayloadWithDecimals = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
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
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
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
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Due,
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
          financial = Financial(
            amountDue = 4.93,
            outstandingAmountDue = 2.03,
            outstandingAmountDay15 = Some(123.2),
            outstandingAmountDay31 = Some(123.2),
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = LocalDateTime.now().minusDays(29)
          )
        )
      )
    )
  )

  val etmpPayloadWithOnlyDay15Charge = etmpPayload.copy(
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
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
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Due,
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
          financial = Financial(
            amountDue = 400.00,
            outstandingAmountDue = 123.00,
            outstandingAmountDay15 = Some(123),
            outstandingAmountDay31 = None,
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = sampleDate1
          )
        )
      )
    )
  )

  val etmpPayloadWithDueDateMoreThan30days = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0, penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
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
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        reason = PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Due,
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
        financial = Financial(
          amountDue = 400.00,
          outstandingAmountDue = 123.00,
          outstandingAmountDay15 = Some(123),
          outstandingAmountDay31 = None,
          percentageOfOutstandingAmtCharged = Some(2),
          dueDate = LocalDateTime.now().minusDays(29)
        )
      )
      )
    )
  )

  "GET /calculation when it is not an additional penalty and  penalty is shown with estimate" should {
    "return 200 (OK)" when {
      "the user has specified a valid penalty ID" in {
        returnLSPDataStub(etmpPayload)
        val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456789&isAdditional=false").get())
        request.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content h1 span").text() shouldBe "1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content tr:nth-child(1) > th").text() shouldBe "Penalty amount (estimate)"
        parsedBody.select("#main-content tr:nth-child(1) > td").text() shouldBe "£400.00"
        parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Calculation"
        parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "2% of £123.00 (VAT amount unpaid on 23 March 2021) + 2% of £123.00 (VAT amount unpaid on 7 April 2021)"
        parsedBody.select("#main-content tr:nth-child(3) > th").text() shouldBe "Amount received"
        parsedBody.select("#main-content tr:nth-child(3) > td").text() shouldBe "£277.00"
        parsedBody.select("#main-content tr").get(3).select("th").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content tr").get(3).select("td").text() shouldBe "£123.00"
        parsedBody.select("#main-content div .govuk-warning-text").text() shouldBe "! This penalty will rise to £800.00 (a further 2% of the unpaid VAT) if you do not make a VAT payment by 31 January 2021."
        parsedBody.select("#main-content .govuk-body").get(0).text() shouldBe "Paying part of your VAT bill will reduce further penalties."
        parsedBody.select("#main-content .govuk-body").get(1).text() shouldBe "Penalties and interest will show as estimates if HMRC has not been given enough information to calculate the final amounts."
        parsedBody.select("#main-content h2").text() shouldBe "Estimates"
        parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
        parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
      }

      "the user has specified a valid penalty ID (parses decimals correctly)" in {
        returnLSPDataStub(etmpPayloadWithDecimals)
        val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456789&isAdditional=false").get())
        request.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content h1 span").text() shouldBe "1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content tr:nth-child(1) > th").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content tr:nth-child(1) > td").text() shouldBe "£4.93"
        parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Calculation"
        parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "2% of £123.20 (VAT amount unpaid on 23 March 2021) + 2% of £123.20 (VAT amount unpaid on 7 April 2021)"
        parsedBody.select("#main-content tr:nth-child(3) > th").text() shouldBe "Amount received"
        parsedBody.select("#main-content tr:nth-child(3) > td").text() shouldBe "£2.90"
        parsedBody.select("#main-content tr").get(3).select("th").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content tr").get(3).select("td").text() shouldBe "£2.03"
        parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
        parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
      }
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID (only one interest charge)" in {
      returnLSPDataStub(etmpPayloadWithOnlyDay15Charge)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456789&isAdditional=false").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Calculation"
      parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "2% of £123.00 (VAT amount unpaid on 23 March 2021)"
    }

    "return 200 (OK) and render the view correctly with Penalty Amount)" in {
      returnLSPDataStub(etmpPayloadWithDueDateMoreThan30days)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456789&isAdditional=false").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content tr").get(0).select("th").text() shouldBe  "Penalty amount"
      parsedBody.select("#main-content tr").get(0).select("td").text() shouldBe "£400.00"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      returnLSPDataStub(etmpPayload)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456800&isAdditional=false").get())
      request.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=12345&isAdditional=false").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /calculation when it is an additional penalty" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in { //TODO: implement without placeholders
      returnLSPDataStub(etmpPayloadWithAdditionalPenalty)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=987654321&isAdditional=true").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content h1").text() shouldBe "Additional penalty"
      parsedBody.select("#main-content p").get(0).text() shouldBe
        "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
      parsedBody.select("#main-content tr").get(0).select("th").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content tr").get(0).select("td").text() shouldBe "£0" //TODO: placeholder value
      parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Number of days since day 31"
      parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "9 days"
      parsedBody.select("#main-content tr").get(2).select("th").text() shouldBe "Additional penalty rate"
      parsedBody.select("#main-content tr").get(2).select("td").text() shouldBe "4%"
      parsedBody.select("#main-content tr").get(3).select("th").text() shouldBe "Calculation"
      parsedBody.select("#main-content tr").get(3).select("td").text() shouldBe "VAT amount unpaid x 4% x number of days since day 31 ÷ 365"
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due" in { //TODO: implement without placeholders
      returnLSPDataStub(etmpPayloadWithAdditionalDuePenalty)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=987654322&isAdditional=true").get())
      request.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(request.body)
      parsedBody.select("#main-content h1").text() shouldBe "Additional penalty"
      parsedBody.select("#main-content p").get(0).text() shouldBe
        "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
      parsedBody.select("#main-content tr").get(0).select("th").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content tr").get(0).select("td").text() shouldBe "£0" //TODO: placeholder value
      parsedBody.select("#main-content tr").get(1).select("th").text() shouldBe "Number of days since day 31"
      parsedBody.select("#main-content tr").get(1).select("td").text() shouldBe "9 days"
      parsedBody.select("#main-content tr").get(2).select("th").text() shouldBe "Additional penalty rate"
      parsedBody.select("#main-content tr").get(2).select("td").text() shouldBe "4%"
      parsedBody.select("#main-content tr").get(3).select("th").text() shouldBe "Calculation"
      parsedBody.select("#main-content tr").get(3).select("td").text() shouldBe "VAT amount unpaid x 4% x number of days since day 31 ÷ 365"
      parsedBody.select("#main-content p").get(1).text() shouldBe
        "Penalties and interest will show as estimates if HMRC does not have enough information to calculate the final amounts."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      returnLSPDataStub(etmpPayload)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456800&isAdditional=true").get())
      request.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=12345&isAdditional=true").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
