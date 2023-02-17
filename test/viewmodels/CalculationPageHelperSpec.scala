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

package viewmodels

import base.SpecBase
import config.featureSwitches.FeatureSwitching
import models.GetPenaltyDetails
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp._

import java.time.LocalDate

class CalculationPageHelperSpec extends SpecBase with FeatureSwitching {
  val calculationPageHelper: CalculationPageHelper = injector.instanceOf[CalculationPageHelper]

  "getCalculationRowForLPP" should {

    val lppWithNoAmounts = LPPDetails(
      principalChargeReference = "12345678901234",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing,
      penaltyAmountPaid = Some(1001.45),
      penaltyAmountOutstanding = Some(99.99),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("31"),
      LPP2Days = Some("31"),
      LPP1LRCalculationAmount = None,
      LPP1HRCalculationAmount = None,
      LPP2Percentage = Some(4.00),
      LPP1LRPercentage = Some(2.00),
      LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
      penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel =  Some(AppealLevelEnum.HMRC)
      ))),
      principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
      principalChargeBillingTo = LocalDate.parse("2069-10-30"),
      principalChargeDueDate = LocalDate.parse("2069-10-30"),
      penaltyChargeReference = Some("1234567890"),
      principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )

    val lppWith15and30DayAmount = LPPDetails(
      principalChargeReference = "12345678901234",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing,
      penaltyAmountPaid = Some(1001.45),
      penaltyAmountOutstanding = Some(99.99),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("31"),
      LPP2Days = Some("31"),
      LPP1LRCalculationAmount = Some(99.99),
      LPP1HRCalculationAmount = Some(99.99),
      LPP2Percentage = Some(4),
      LPP1LRPercentage = Some(2),
      LPP1HRPercentage = Some(2),
      penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel =  Some(AppealLevelEnum.HMRC)
      ))),
      principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
      principalChargeBillingTo = LocalDate.parse("2069-10-30"),
      principalChargeDueDate = LocalDate.parse("2069-10-30"),
      penaltyChargeReference = Some("1234567890"),
      principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )

    val lppWith15DayAmount = LPPDetails(
      principalChargeReference = "12345678901234",
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing,
      penaltyAmountPaid = Some(1001.45),
      penaltyAmountOutstanding = Some(99.99),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("31"),
      LPP2Days = Some("31"),
      LPP1LRCalculationAmount = Some(99.99),
      LPP1HRCalculationAmount = None,
      LPP2Percentage = None,
      LPP1LRPercentage = Some(2),
      LPP1HRPercentage = None,
      penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
      communicationsDate = Some(LocalDate.parse("2069-10-30")),
      penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel =  Some(AppealLevelEnum.HMRC)
      ))),
      principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
      principalChargeBillingTo = LocalDate.parse("2069-10-30"),
      principalChargeDueDate = LocalDate.parse("2069-10-30"),
      penaltyChargeReference = Some("1234567890"),
      principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )

    "return a single row when the user has an amount after 15 days" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWith15DayAmount)
      rows.isDefined shouldBe true
      rows.get.size shouldBe 1
      rows.get.head shouldBe "2% of £99.99 (the unpaid VAT 15 days after the due date)"
    }

    "return 2 rows when the user has an amount after 15 and 30 days" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWith15and30DayAmount)
      rows.isDefined shouldBe true
      rows.get.size shouldBe 2
      rows.get.head shouldBe "2% of £99.99 (the unpaid VAT 15 days after the due date) = £2.00"
      rows.get(1) shouldBe "2% of £99.99 (the unpaid VAT 30 days after the due date) = £2.00"
    }

    "return None when the user does not have either" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWithNoAmounts)
      rows.isDefined shouldBe false
    }
  }

  "isTTPActive" should {
    "return true" when {
      "a TTP is active and ends today" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 2)))
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }

      "a TTP is active and ends in the future" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 1)))
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }

      "a TTP has been applied in the past but also has a TTP active now" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 6, 20))
                      ),
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 6, 24),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 6, 25)))
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }
    }

    "return false" when {
      "no TTP field is present" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = None
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      "a TTP was active but has since expired" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 3)))
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      "a TTP is going to be active but start date is in the future" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 8, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 9, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None,
          breathingSpace = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 3)))
        val result = calculationPageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      setFeatureDate(None)
    }
  }
}
