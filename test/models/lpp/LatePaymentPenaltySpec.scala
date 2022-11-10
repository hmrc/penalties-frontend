/*
 * Copyright 2022 HM Revenue & Customs
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

package models.lpp

import base.SpecBase
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import play.api.libs.json.Json

import java.time.LocalDate

class LatePaymentPenaltySpec extends SpecBase {

  val latePaymentPenaltyAsJson = Json.parse(
    """
      |{
      | "details": [{
      |       "penaltyCategory": "LPP1",
      |       "penaltyStatus": "A",
      |       "penaltyAmountPaid": 1001.45,
      |       "penaltyAmountOutstanding": 99.99,
      |       "LPP1LRCalculationAmount": 99.99,
      |       "LPP1LRDays": "15",
      |       "LPP1LRPercentage": 2.00,
      |       "LPP1HRCalculationAmount": 99.99,
      |       "LPP1HRDays": "31",
      |       "LPP1HRPercentage": 2.00,
      |       "LPP2Days": "31",
      |       "LPP2Percentage": 4.00,
      |       "penaltyChargeCreationDate": "2069-10-30",
      |       "communicationsDate": "2069-10-30",
      |       "penaltyChargeDueDate": "2069-10-30",
      |       "principalChargeReference": "12345678901234",
      |       "appealInformation":
      |       [{
      |         "appealStatus": "99",
      |         "appealLevel": "01"
      |       }],
      |       "principalChargeBillingFrom": "2069-10-30",
      |       "principalChargeBillingTo": "2069-10-30",
      |       "principalChargeDueDate": "2069-10-30",
      |       "penaltyChargeReference": "PEN1234567",
      |       "principalChargeLatestClearing": "2069-10-30",
      |       "mainTransaction": "4700",
      |       "outstandingAmount": 99,
      |       "timeToPay":
      |       [{
      |         "TTPStartDate": "2069-10-30",
      |         "TTPEndDate": "2069-10-30"
      |       }]
      |   },
      |   {
      |       "penaltyCategory": "LPP1",
      |       "penaltyStatus": "A",
      |       "penaltyAmountPaid": 1001.45,
      |       "penaltyAmountOutstanding": 99.99,
      |       "LPP1LRCalculationAmount": 99.99,
      |       "LPP1LRDays": "15",
      |       "LPP1LRPercentage": 2.00,
      |       "LPP1HRCalculationAmount": 99.99,
      |       "LPP1HRDays": "31",
      |       "LPP1HRPercentage": 2.00,
      |       "LPP2Days": "31",
      |       "LPP2Percentage": 4.00,
      |       "penaltyChargeCreationDate": "2069-10-30",
      |       "communicationsDate": "2069-10-30",
      |       "penaltyChargeDueDate": "2069-10-30",
      |       "principalChargeReference": "12345678901235",
      |       "appealInformation":
      |       [{
      |         "appealStatus": "99",
      |         "appealLevel": "01"
      |       }],
      |       "principalChargeBillingFrom": "2069-10-30",
      |       "principalChargeBillingTo": "2069-10-30",
      |       "principalChargeDueDate": "2069-10-30",
      |       "penaltyChargeReference": "PEN1234567",
      |       "principalChargeLatestClearing": "2069-10-30",
      |       "mainTransaction": "4700",
      |       "outstandingAmount": 99
      |   }
      |   ]
      |}
      |""".stripMargin
  )

  val latePaymentPenaltyAsModel = LatePaymentPenalty(
    details = Seq(
      LPPDetails(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        principalChargeReference = "12345678901234",
        penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        appealInformation = Some(Seq(AppealInformationType(appealStatus = Some(AppealStatusEnum.Unappealable), appealLevel = Some(AppealLevelEnum.HMRC)))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        communicationsDate = Some(LocalDate.parse("2069-10-30")),
        penaltyAmountOutstanding = Some(99.99),
        penaltyAmountPaid = Some(1001.45),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1HRCalculationAmount = Some(99.99),
        LPP1LRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(BigDecimal(4.00).setScale(2)),
        LPP1LRPercentage = Some(BigDecimal(2.00).setScale(2)),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
        penaltyChargeReference = Some("PEN1234567"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99),
          timeToPay = Some(
            Seq(
              TimeToPay(
                TTPStartDate = LocalDate.parse("2069-10-30"),
                TTPEndDate = Some(LocalDate.parse("2069-10-30"))
              )
            )
          )
        )
      ),
      LPPDetails(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        principalChargeReference = "12345678901235",
        penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        appealInformation = Some(Seq(AppealInformationType(appealStatus = Some(AppealStatusEnum.Unappealable), appealLevel = Some(AppealLevelEnum.HMRC)))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        communicationsDate = Some(LocalDate.parse("2069-10-30")),
        penaltyAmountOutstanding = Some(99.99),
        penaltyAmountPaid = Some(1001.45),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1HRCalculationAmount = Some(99.99),
        LPP1LRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(BigDecimal(4.00).setScale(2)),
        LPP1LRPercentage = Some(BigDecimal(2.00).setScale(2)),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
        penaltyChargeReference = Some("PEN1234567"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99),
          timeToPay = None
        )
      )
    )
  )

  "LatePaymentPenalty" should {
    "be readable from JSON" in {
      val result = Json.fromJson(latePaymentPenaltyAsJson)(LatePaymentPenalty.format)
      result.isSuccess shouldBe true
      result.get shouldBe latePaymentPenaltyAsModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(latePaymentPenaltyAsModel)(LatePaymentPenalty.format)
      result shouldBe latePaymentPenaltyAsJson
    }
  }

}
