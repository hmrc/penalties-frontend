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

package models.lpp

import base.SpecBase
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import play.api.libs.json.Json

class LPPDetailsSpec extends SpecBase {

  val lppDetailsAsJson = Json.parse(
    """
      |{
      |       "penaltyCategory": "LPP1",
      |       "penaltyStatus": "A",
      |       "penaltyAmountPosted": 0,
      |       "penaltyAmountAccruing": 1001.45,
      |       "LPP1LRCalculationAmount": 99.99,
      |       "LPP1LRDays": "15",
      |       "LPP1LRPercentage": 2.00,
      |       "LPP1HRCalculationAmount": 99.99,
      |       "LPP1HRDays": "31",
      |       "LPP1HRPercentage": 2.00,
      |       "LPP2Days": "31",
      |       "LPP2Percentage": 4.00,
      |       "penaltyChargeCreationDate": "2021-06-07",
      |       "communicationsDate": "2021-06-07",
      |       "penaltyChargeDueDate": "2021-07-08",
      |       "principalChargeReference": "12345678901234",
      |       "appealInformation":
      |       [{
      |         "appealStatus": "99",
      |         "appealLevel": "01"
      |       }],
      |       "principalChargeBillingFrom": "2021-05-01",
      |       "principalChargeBillingTo": "2021-06-01",
      |       "principalChargeDueDate": "2021-06-07",
      |       "penaltyChargeReference": "PEN1234567",
      |       "principalChargeLatestClearing": "2021-08-07",
      |       "mainTransaction": "4700",
      |       "outstandingAmount": 99,
      |       "timeToPay":
      |       [{
      |         "TTPStartDate": "2021-06-01",
      |         "TTPEndDate": "2021-07-01"
      |       }]
      |   }
      |""".stripMargin)

  val lppDetailsAsModel = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountOutstanding = None,
    penaltyAmountPosted = 0,
    penaltyAmountAccruing = 1001.45,
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("31"),
    LPP2Days = Some("31"),
    LPP1LRCalculationAmount = Some(99.99),
    LPP1HRCalculationAmount = Some(99.99),
    LPP2Percentage = Some(4.00),
    LPP1LRPercentage = Some(2.00),
    LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
    penaltyChargeCreationDate = Some(penaltyChargeCreationDate),
    communicationsDate = Some(communicationDate),
    penaltyChargeDueDate = Some(penaltyDueDate),
    appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnum.Unappealable),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))),
    principalChargeBillingFrom = principleChargeBillingStartDate,
    principalChargeBillingTo = principleChargeBillingEndDate,
    principalChargeDueDate = principleChargeBillingDueDate,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99),
      timeToPay = Some(
        Seq(
          TimeToPay(
            TTPStartDate = Some(timeToPayPeriodStart),
            TTPEndDate = Some(timeToPayPeriodEnd)
          )
        )
      )
    )
  )

  "LPPDetails" should {
    "be readable from JSON" in {
      val result = Json.fromJson(lppDetailsAsJson)(LPPDetails.format)
      result.isSuccess shouldBe true
      result.get shouldBe lppDetailsAsModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(lppDetailsAsModel)(LPPDetails.format)
      result shouldBe lppDetailsAsJson
    }

    "be sortable" when {
      "by PenaltyCategory (LPP2 first)" in {
        val lppList = Seq(lppDetailsAsModel, lppDetailsAsModel.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2))
        val expectedResult = Seq(lppDetailsAsModel.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2), lppDetailsAsModel)
        val resultA = lppList.sorted
        val resultB = resultA.sorted

        resultA shouldBe expectedResult
        resultB shouldBe expectedResult
      }

      "by MainTransaction" in {
        val metadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.AAReturnChargeFirstLPP), outstandingAmount = None, timeToPay = None)
        val lppList = Seq(lppDetailsAsModel, lppDetailsAsModel.copy(LPPDetailsMetadata = metadata))
        val expectedResult = Seq(lppDetailsAsModel.copy(LPPDetailsMetadata = metadata), lppDetailsAsModel)
        val resultA = lppList.sorted
        val resultB = resultA.sorted

        resultA shouldBe expectedResult
        resultB shouldBe expectedResult
      }

      "by TaxPeriodEndDate (Newest First)" in {
        val lppList = Seq(lppDetailsAsModel, lppDetailsAsModel.copy(principalChargeBillingTo = principleChargeBillingEndDate.plusDays(1) ))
        val expectedResult = Seq(lppDetailsAsModel.copy(principalChargeBillingTo = principleChargeBillingEndDate.plusDays(1)), lppDetailsAsModel)
        val resultA = lppList.sorted
        val resultB = resultA.sorted

        resultA shouldBe expectedResult
        resultB shouldBe expectedResult
      }

      "by TaxPeriodStartDate (Newest First)" in {
        val lppList = Seq(lppDetailsAsModel, lppDetailsAsModel.copy(principalChargeBillingFrom = principleChargeBillingStartDate.plusDays(1)))
        val expectedResult = Seq(lppDetailsAsModel.copy(principalChargeBillingFrom = principleChargeBillingStartDate.plusDays(1)), lppDetailsAsModel)
        val resultA = lppList.sorted
        val resultB = resultA.sorted

        resultA shouldBe expectedResult
        resultB shouldBe expectedResult
      }
    }

    "not be sorted" in {
      val lppList = Seq(lppDetailsAsModel, lppDetailsAsModel.copy(penaltyAmountOutstanding = Some(0)))
      val result = lppList.sorted

      result shouldBe lppList
    }
  }
}
