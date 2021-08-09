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

package models

import models.communication.{Communication, CommunicationTypeEnum}
import models.financial.{AmountTypeEnum, Financial, OverviewElement}
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.submission.{Submission, SubmissionStatusEnum}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import models.payment.PaymentFinancial
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}

class ETMPPayloadSpec extends AnyWordSpec with Matchers {

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43).plus(511, ChronoUnit.MILLIS)

  val etmpPayloadAsJson: JsValue = Json.parse(
    """
        {
      |	"pointsTotal": 1,
      |	"lateSubmissions": 1,
      |	"adjustmentPointsTotal": 1,
      |	"fixedPenaltyAmount": 200,
      |	"penaltyAmountsTotal": 400.00,
      |	"penaltyPointsThreshold": 4,
      |	"penaltyPoints": [
      |		{
      |			"type": "financial",
      |     "id": "123456790",
      |			"number": "2",
      |     "appealStatus": "UNDER_REVIEW",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "secureMessage",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			],
      |     "financial": {
      |        "amountDue": 400.00,
      |        "dueDate": "2021-04-23T18:25:43.511"
      |     }
      |		},
      |		{
      |			"type": "point",
      |     "id": "123456789",
      |			"number": "1",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |     "reason": "reason",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "letter",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			]
      |		}
      |	]
      |}
      |""".stripMargin)

  val etmpPayloadWithVATOverviewAsJson: JsValue = Json.parse(
    """
        {
      |	"pointsTotal": 1,
      |	"lateSubmissions": 1,
      |	"adjustmentPointsTotal": 1,
      |	"fixedPenaltyAmount": 200,
      |	"penaltyAmountsTotal": 400.00,
      | "otherPenalties": false,
      | "vatOverview": [
      |   {
      |     "type": "VAT",
      |     "amount": 100.00,
      |     "estimatedInterest": 10.00,
      |     "crystalizedInterest": 10.00
      |   },
      |   {
      |     "type": "CENTRAL_ASSESSMENT",
      |     "amount": 100.00,
      |     "estimatedInterest": 10.00,
      |     "crystalizedInterest": 10.00
      |   }
      | ],
      |	"penaltyPointsThreshold": 4,
      |	"penaltyPoints": [
      |		{
      |			"type": "financial",
      |			"number": "2",
      |     "appealStatus": "UNDER_REVIEW",
      |     "id": "123456790",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "secureMessage",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			],
      |     "financial": {
      |        "amountDue": 400.00,
      |        "dueDate": "2021-04-23T18:25:43.511"
      |     }
      |		},
      |		{
      |			"type": "point",
      |			"number": "1",
      |     "id": "123456789",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |     "reason": "reason",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "letter",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			]
      |		}
      |	]
      |}
      |""".stripMargin)

  val etmpPayloadAsJsonWithLPP: JsValue = Json.parse(
    """
        {
      |	"pointsTotal": 1,
      |	"lateSubmissions": 1,
      |	"adjustmentPointsTotal": 1,
      |	"fixedPenaltyAmount": 200,
      |	"penaltyAmountsTotal": 400.00,
      |	"penaltyPointsThreshold": 4,
      |	"penaltyPoints": [
      |		{
      |			"type": "financial",
      |     "id": "123456790",
      |			"number": "2",
      |     "appealStatus": "UNDER_REVIEW",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "secureMessage",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			],
      |     "financial": {
      |        "amountDue": 400.00,
      |        "dueDate": "2021-04-23T18:25:43.511"
      |     }
      |		},
      |		{
      |			"type": "point",
      |     "id": "123456789",
      |			"number": "1",
      |			"dateCreated": "2021-04-23T18:25:43.511",
      |			"dateExpired": "2021-04-23T18:25:43.511",
      |			"status": "ACTIVE",
      |     "reason": "reason",
      |			"period": {
      |				"startDate": "2021-04-23T18:25:43.511",
      |				"endDate": "2021-04-23T18:25:43.511",
      |				"submission": {
      |					"dueDate": "2021-04-23T18:25:43.511",
      |					"submittedDate": "2021-04-23T18:25:43.511",
      |					"status": "SUBMITTED"
      |				}
      |			},
      |			"communications": [
      |				{
      |					"type": "letter",
      |					"dateSent": "2021-04-23T18:25:43.511",
      |					"documentId": "1234567890"
      |				}
      |			]
      |		}
      |	],
      | "latePaymentPenalties": [
      |   {
      |       "type": "additional",
      |       "reason": "VAT_OVERDUE_BY_31_DAYS",
      |       "id": "1234567892",
      |       "dateCreated": "2021-04-23T18:25:43.511",
      |       "status": "ACTIVE",
      |       "period": {
      |         "startDate": "2021-04-23T18:25:43.511",
      |         "endDate": "2021-04-23T18:25:43.511",
      |         "dueDate": "2021-04-23T18:25:43.511",
      |	        "paymentStatus": "PAID"
      |       },
      |       "communications": [
      |         {
      |          "type": "letter",
      |          "dateSent": "2021-04-23T18:25:43.511",
      |          "documentId": "1234567890"
      |         }
      |       ],
      |       "financial": {
      |         "amountDue": 123.45,
      |         "outstandingAmountDue": 2.00,
      |         "dueDate": "2021-04-23T18:25:43.511"
      |       }
      |     },
      |     {
      |       "type": "financial",
      |       "reason": "VAT_NOT_PAID_ON_TIME",
      |       "id": "1234567891",
      |       "dateCreated": "2021-04-23T18:25:43.511",
      |       "status": "ACTIVE",
      |       "period": {
      |         "startDate": "2021-04-23T18:25:43.511",
      |         "endDate": "2021-04-23T18:25:43.511",
      |         "dueDate": "2021-04-23T18:25:43.511",
      |	        "paymentStatus": "PAID"
      |       },
      |       "communications": [
      |       {
      |          "type": "letter",
      |          "dateSent": "2021-04-23T18:25:43.511",
      |          "documentId": "1234567890"
      |        }
      |       ],
      |       "financial": {
      |         "amountDue": 400.00,
      |         "outstandingAmountDue": 2.00,
      |         "dueDate": "2021-04-23T18:25:43.511"
      |       }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val etmpPayloadModel: ETMPPayload = ETMPPayload(
    pointsTotal = 1,
    lateSubmissions = 1,
    adjustmentPointsTotal = 1,
    fixedPenaltyAmount = 200.00,
    penaltyAmountsTotal = 400.00,
    penaltyPointsThreshold = 4,
    penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        number = "2",
        id = "123456790",
        appealStatus = Some(AppealStatusEnum.Under_Review),
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Active,
        reason = None,
        period = Some(PenaltyPeriod(
          startDate = sampleDate,
          endDate = sampleDate,
          submission = Submission(
            dueDate = sampleDate,
            submittedDate = Some(sampleDate),
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.secureMessage,
            dateSent = sampleDate,
            documentId = "1234567890"
          )
        ),
        financial = Some(Financial(
          amountDue = 400.00,
          dueDate = sampleDate
        ))
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        number = "1",
        id = "123456789",
        appealStatus = None,
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Active,
        reason = Some("reason"),
        period = Some(PenaltyPeriod(
          startDate = sampleDate,
          endDate = sampleDate,
          submission = Submission(
            dueDate = sampleDate,
            submittedDate = Some(sampleDate),
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate,
            documentId = "1234567890")
        ),
        financial = None
      )
    )
  )

  val etmpPayloadModelWithVATOverview: ETMPPayload = ETMPPayload(
    pointsTotal = 1,
    lateSubmissions = 1,
    adjustmentPointsTotal = 1,
    fixedPenaltyAmount = 200.00,
    penaltyAmountsTotal = 400.00,
    otherPenalties = Some(false),
    vatOverview = Some(
      Seq(
        OverviewElement(
          `type` = AmountTypeEnum.VAT,
          amount = 100.00,
          estimatedInterest = Some(10.00),
          crystalizedInterest = Some(10.00)
        ),
        OverviewElement(
          `type` = AmountTypeEnum.Central_Assessment,
          amount = 100.00,
          estimatedInterest = Some(10.00),
          crystalizedInterest = Some(10.00)
        )
      )
    ),
    penaltyPointsThreshold = 4,
    penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        number = "2",
        id = "123456790",
        appealStatus = Some(AppealStatusEnum.Under_Review),
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Active,
        reason = None,
        period = Some(PenaltyPeriod(
          startDate = sampleDate,
          endDate = sampleDate,
          submission = Submission(
            dueDate = sampleDate,
            submittedDate = Some(sampleDate),
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.secureMessage,
            dateSent = sampleDate,
            documentId = "1234567890"
          )
        ),
        financial = Some(Financial(
          amountDue = 400.00,
          dueDate = sampleDate
        ))
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        number = "1",
        id = "123456789",
        appealStatus = None,
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Active,
        reason = Some("reason"),
        period = Some(PenaltyPeriod(
          startDate = sampleDate,
          endDate = sampleDate,
          submission = Submission(
            dueDate = sampleDate,
            submittedDate = Some(sampleDate),
            status = SubmissionStatusEnum.Submitted
          )
        )),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate,
            documentId = "1234567890")
        ),
        financial = None
      )
    )
  )

  val etmpPayloadModelWithLPPs: ETMPPayload = etmpPayloadModel.copy(
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "1234567892",
          reason = "VAT_OVERDUE_BY_31_DAYS",
          dateCreated = sampleDate,
          status = PointStatusEnum.Active,
          appealStatus = None,
          period = PaymentPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            dueDate = sampleDate,
            paymentStatus = PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate,
              documentId = "1234567890"
            )
          ),
          financial = PaymentFinancial(
            amountDue = 123.45,
            outstandingAmountDue = 2.00,
            dueDate = sampleDate
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "1234567891",
          reason = "VAT_NOT_PAID_ON_TIME",
          dateCreated = sampleDate,
          status = PointStatusEnum.Active,
          appealStatus = None,
          period = PaymentPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            dueDate = sampleDate,
            paymentStatus = PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate,
              documentId = "1234567890"
            )
          ),
          financial = PaymentFinancial(
            amountDue = 400.00,
            outstandingAmountDue = 2.00,
            dueDate = sampleDate
          )
        )
      )
    )
  )

  "ETMPPayload" should {
    "be writable to JSON" in {
      val result = Json.toJson(etmpPayloadModel)
      result shouldBe etmpPayloadAsJson
    }

    "be writable to JSON when there is a LPP" in {
      val result = Json.toJson(etmpPayloadModelWithLPPs)
      result shouldBe etmpPayloadAsJsonWithLPP
    }

    "be writable to JSON when there is a VAT overview field present" in {
      val result = Json.toJson(etmpPayloadModelWithVATOverview)
      result shouldBe etmpPayloadWithVATOverviewAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(etmpPayloadAsJson)(ETMPPayload.format)
      result.isSuccess shouldBe true
      result.get shouldBe etmpPayloadModel
    }

    "be readable from JSON when there is a LPP" in {
      val result = Json.fromJson(etmpPayloadAsJsonWithLPP)(ETMPPayload.format)
      result.isSuccess shouldBe true
      result.get shouldBe etmpPayloadModelWithLPPs
    }
    "be readable from JSON when there is a VAT overview field present" in {
      val result = Json.fromJson(etmpPayloadWithVATOverviewAsJson)(ETMPPayload.format)
      result.isSuccess shouldBe true
      result.get shouldBe etmpPayloadModelWithVATOverview
    }
  }
}
