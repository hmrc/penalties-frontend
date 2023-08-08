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

package models

import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.breathingSpace.BreathingSpace
import models.lpp._
import models.lsp._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate


class GetPenaltyDetailsSpec extends AnyWordSpec with Matchers {

  val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = Some(10432.21),
      totalAccountPostedInterest = Some(4.32),
      totalAccountAccruingInterest = Some(1.23)
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some(ExpiryReasonEnum.Adjustment),
          communicationsDate = Some(LocalDate.parse("2069-10-30")),
          lateSubmissions = Some(Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
              returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
              taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
            )
          )),
          appealInformation = Some(Seq(
            AppealInformationType(
              appealStatus = Some(AppealStatusEnum.Unappealable),
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
      details = Seq(LPPDetails(
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
        penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
        communicationsDate = Some(LocalDate.parse("2069-10-30")),
        penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("PEN1234567"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99),
          timeToPay = None
        )
      ))
    )),
    breathingSpace = Some(Seq(BreathingSpace(
      BSStartDate = LocalDate.of(2023, 1, 1),
      BSEndDate = LocalDate.of(2023, 12, 31)
    )))
  )
  val penaltyDetailsAsJson: JsValue = Json.parse(
    """
    {
     "totalisations": {
       "LSPTotalValue": 200,
       "penalisedPrincipalTotal": 2000,
       "LPPPostedTotal": 165.25,
       "LPPEstimatedTotal": 15.26,
       "totalAccountOverdue": 10432.21,
       "totalAccountPostedInterest": 4.32,
       "totalAccountAccruingInterest": 1.23
     },
     "lateSubmissionPenalty": {
       "summary": {
         "activePenaltyPoints": 10,
         "inactivePenaltyPoints": 12,
         "regimeThreshold": 10,
         "penaltyChargeAmount": 684.25,
         "PoCAchievementDate": "2022-01-01"
       },
       "details": [
         {
           "penaltyNumber": "12345678901234",
           "penaltyOrder": "01",
           "penaltyCategory": "P",
           "penaltyStatus": "ACTIVE",
           "FAPIndicator": "X",
           "penaltyCreationDate": "2069-10-30",
           "penaltyExpiryDate": "2069-10-30",
           "expiryReason": "FAP",
           "communicationsDate": "2069-10-30",
           "lateSubmissions": [
             {
               "taxPeriodStartDate": "2069-10-30",
               "taxPeriodEndDate": "2069-10-30",
               "taxPeriodDueDate": "2069-10-30",
               "returnReceiptDate": "2069-10-30",
               "taxReturnStatus": "Fulfilled"
             }
           ],
           "appealInformation": [
             {
               "appealStatus": "99",
               "appealLevel": "01"
             }
           ],
           "chargeDueDate": "2069-10-30",
           "chargeOutstandingAmount": 200,
           "chargeAmount": 200
       }]
     },
     "latePaymentPenalty": {
         "details": [{
           "penaltyCategory": "LPP1",
           "penaltyStatus": "A",
           "penaltyAmountPosted": 0,
           "penaltyAmountAccruing": 1001.45,
           "LPP1LRCalculationAmount": 99.99,
           "LPP1LRDays": "15",
           "LPP1LRPercentage": 2.00,
           "LPP1HRCalculationAmount": 99.99,
           "LPP1HRDays": "31",
           "LPP1HRPercentage": 2.00,
           "LPP2Days": "31",
           "LPP2Percentage": 4.00,
           "penaltyChargeCreationDate": "2069-10-30",
           "communicationsDate": "2069-10-30",
           "penaltyChargeDueDate": "2069-10-30",
           "principalChargeReference": "12345678901234",
           "appealInformation":
           [{
             "appealStatus": "99",
             "appealLevel": "01"
           }],
           "principalChargeBillingFrom": "2069-10-30",
           "principalChargeBillingTo": "2069-10-30",
           "principalChargeDueDate": "2069-10-30",
           "penaltyChargeReference": "PEN1234567",
           "principalChargeLatestClearing": "2069-10-30",
           "mainTransaction": "4700",
           "outstandingAmount": 99
       }]
     },
     "breathingSpace": [{
       "BSStartDate": "2023-01-01",
       "BSEndDate": "2023-12-31"
     }]
    }
  """.stripMargin)

  "PenaltyDetailsSpec" should {

    "be writeable to JSON" in {
      val result: JsValue = Json.toJson(penaltyDetails)(GetPenaltyDetails.format)
      result shouldBe penaltyDetailsAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(penaltyDetailsAsJson)(GetPenaltyDetails.format)
      result.isSuccess shouldBe true
      result.get shouldBe penaltyDetails
    }
  }
}
