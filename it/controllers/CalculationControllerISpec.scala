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
            dueDate = sampleDate1
          )
        )
      )
    )
  )

  "GET /calculation" should {
    "return 200 (OK)" when {
      "the user has specified a valid penalty ID (checking amount paid is correct)" in {
        returnLSPDataStub(etmpPayload)
        val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456789").get())
        request.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#main-content tr:nth-child(1) > th").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content tr:nth-child(1) > td").text() shouldBe "£400"
        parsedBody.select("#main-content tr:nth-child(3) > th").text() shouldBe "Amount received"
        parsedBody.select("#main-content tr:nth-child(3) > td").text() shouldBe "£277"
      }
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in {
      returnLSPDataStub(etmpPayload)
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=123456800").get())
      request.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/calculation?penaltyId=12345").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
