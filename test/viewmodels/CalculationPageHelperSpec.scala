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

package viewmodels

import base.SpecBase
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum}
import models.point.{PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum

import java.time.LocalDateTime

class CalculationPageHelperSpec extends SpecBase {
  val calculationPageHelper: CalculationPageHelper = injector.instanceOf[CalculationPageHelper]
  
  "getCalculationRow" should {
    val sampleDate: LocalDateTime = LocalDateTime.of(2021, 2, 7, 18, 25, 43)
    val lppWith15DayAmount = LatePaymentPenalty(
      `type` = PenaltyTypeEnum.Financial,
      id = "1234",
      reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
      dateCreated = sampleDate,
      status = PointStatusEnum.Due,
      appealStatus = None,
      period = PaymentPeriod(
        startDate = sampleDate, endDate = sampleDate, dueDate = sampleDate, paymentStatus = PaymentStatusEnum.Due
      ),
      communications = Seq.empty,
      financial = Financial(
        amountDue = 100.12, outstandingAmountDue = 14, dueDate = sampleDate, outstandingAmountDay15 = Some(14), outstandingAmountDay31 = None, percentageOfOutstandingAmtCharged = Some(2), estimatedInterest = None, crystalizedInterest = None
      )
    )

    val lppWith30DayAmount = LatePaymentPenalty(
      `type` = PenaltyTypeEnum.Financial,
      id = "1234",
      reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
      dateCreated = sampleDate,
      status = PointStatusEnum.Due,
      appealStatus = None,
      period = PaymentPeriod(
        startDate = sampleDate, endDate = sampleDate, dueDate = sampleDate, paymentStatus = PaymentStatusEnum.Due
      ),
      communications = Seq.empty,
      financial = Financial(
        amountDue = 100.12, outstandingAmountDue = 12, dueDate = sampleDate, outstandingAmountDay15 = Some(14), outstandingAmountDay31 = Some(12), percentageOfOutstandingAmtCharged = Some(2), estimatedInterest = None, crystalizedInterest = None
      )
    )

    val lppWithNoAmounts = LatePaymentPenalty(
      `type` = PenaltyTypeEnum.Financial,
      id = "1234",
      reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
      dateCreated = sampleDate,
      status = PointStatusEnum.Due,
      appealStatus = None,
      period = PaymentPeriod(
        startDate = sampleDate, endDate = sampleDate, dueDate = sampleDate, paymentStatus = PaymentStatusEnum.Due
      ),
      communications = Seq.empty,
      financial = Financial(
        amountDue = 100.12, outstandingAmountDue = 12, dueDate = sampleDate, outstandingAmountDay15 = None, outstandingAmountDay31 = None, percentageOfOutstandingAmtCharged = Some(2), estimatedInterest = None, crystalizedInterest = None
      )
    )

    "return only one 'row' when the user has amount after 15 days" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWith15DayAmount)
      rows.isDefined shouldBe true
      rows.get.size shouldBe 1
      rows.get.head shouldBe "2% of £14.00 (VAT amount unpaid on 22 February 2021)"
    }
    
    "return 2 rows when the user has amount after 15 and 30 days" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWith30DayAmount)
      rows.isDefined shouldBe true
      rows.get.size shouldBe 2
      rows.get.head shouldBe "2% of £14.00 (VAT amount unpaid on 22 February 2021)"
      rows.get(1) shouldBe "2% of £12.00 (VAT amount unpaid on 9 March 2021)"
    }
    
    "return None when the user does not have either" in {
      val rows = calculationPageHelper.getCalculationRowForLPP(lppWithNoAmounts)
      rows.isDefined shouldBe false
    }
  }
}
