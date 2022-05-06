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

import assets.messages.IndexMessages._
import base.SpecBase
import models.User
import models.point.{AppealStatusEnum, PenaltyPoint, PointStatusEnum}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter
import java.time.LocalDateTime

import models.financial.Financial

class SummaryCardHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  implicit val user: User[_] = vatTraderUser

  val sampleLSPSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
    ),
    Tag(content = Text("active")),
    "1",
    "123456789",
    isReturnSubmitted = true
  )

  val sampleLPPSummaryCardPenaltyPaid: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
    ),
      helper.summaryListRow(paymentDue , Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(vatPaymentDate, Html("Payment not yet received")),
      helper.summaryListRow(penaltyReason, Html("VAT not paid within 15 days"))
  ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = true,
    400.00,
    isVatPaid = true
  )

  val sampleLPPSummaryCardVATPaymentDate: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(paymentDue , Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(vatPaymentDate, Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(penaltyReason, Html("VAT not paid within 15 days"))
    ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = false,
    0.0,
    isVatPaid = true
  )

  val sampleLPPAdditionalSummaryCardPenaltyPaid: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(penaltyReason, Html("VAT more than 30 days late")),
      helper.summaryListRow(chargedDailyFrom, Html(dateTimeToString(LocalDateTime.now.plusDays(31))))
    ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = true,
    123.45,
    isVatPaid = true,
    isAdditionalPenalty = true
  )

  val sampleLPPAdditionalSummaryCardReasonCentralAssessment: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(penaltyReason, Html("Central Assessment more than 30 days late")),
      helper.summaryListRow(chargedDailyFrom, Html(dateTimeToString(LocalDateTime.now.plusDays(31))))
    ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = true,
    123.45,
    isVatPaid = true,
    isAdditionalPenalty = true
  )

  val sampleLPPAdditionalSummaryCardReasonErrorCorrectionNotice: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(penaltyReason, Html("Error Correction Notice more than 30 days late")),
      helper.summaryListRow(chargedDailyFrom, Html(dateTimeToString(LocalDateTime.now.plusDays(31))))
    ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = true,
    123.45,
    isVatPaid = true,
    isAdditionalPenalty = true
  )

  val sampleLPPAdditionalSummaryCardReasonOfficersAssessment: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(penaltyReason, Html("Officer’s Assessment more than 30 days late")),
      helper.summaryListRow(chargedDailyFrom, Html(dateTimeToString(LocalDateTime.now.plusDays(31))))
    ),
    Tag(content = Text("paid")),
    "123456789",
    isPenaltyPaid = true,
    123.45,
    isVatPaid = true,
    isAdditionalPenalty = true
  )
  val sampleLPPSummaryCardPenaltyUnpaidVAT: LatePaymentPenaltySummaryCard = sampleLPPSummaryCardPenaltyPaid.copy(isPenaltyPaid = false, isVatPaid = false,
    status = Tag(content = Text("£200 due"), classes = "penalty-due-tag"))

  "SummaryCard helper" should {
    "findAndReindexPointIfIsActive" should {
      "reindex the point with the associated index + 1 when the point is in the indexed list of active points" in {
        val pointToPassIn: PenaltyPoint = samplePenaltyPoint.copy(number = "2")
        val indexedPoints: Seq[(PenaltyPoint, Int)] = Seq(
          (pointToPassIn, 0),
          (sampleFinancialPenaltyPoint, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn.copy(number = "1")
        actualResult shouldBe expectedResult
      }

      "NOT reindex when the point is not in the indexed list" in {
        val pointToPassIn: PenaltyPoint = samplePenaltyPoint.copy(number = "2")
        val indexedPoints: Seq[(PenaltyPoint, Int)] = Seq(
          (sampleFinancialPenaltyPoint, 0),
          (samplePenaltyPoint, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn
        actualResult shouldBe expectedResult
      }
    }

    "getPenaltyNumberBasedOnThreshold" should {
      "when given a penalty number greater than the threshold - return an empty string" in {
        val penaltyNumber = "5"
        val result = helper.getPenaltyNumberBasedOnThreshold(penaltyNumber, quarterlyThreshold)
        result shouldBe ""
      }

      "when given a penalty number at the threshold - return the penalty number" in {
        val penaltyNumber = "4"
        val result = helper.getPenaltyNumberBasedOnThreshold(penaltyNumber, quarterlyThreshold)
        result shouldBe penaltyNumber
      }

      "when given a penalty number below the threshold - return the penalty number" in {
        val penaltyNumber = "3"
        val result = helper.getPenaltyNumberBasedOnThreshold(penaltyNumber, quarterlyThreshold)
        result shouldBe penaltyNumber
      }
    }

    "financialSummaryCard" should {
      "have an empty string when the penalty number exceeds the threshold" in {
        val sampleSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "",
          "123456789",
          isReturnSubmitted = false,
          isFinancialPoint = true,
          amountDue = 200.0,
          multiplePenaltyPeriod = None
        )

        val pointToPassIn: PenaltyPoint = sampleFinancialPenaltyPoint.copy(number = "5")
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "have the penalty number when it DOES NOT exceeds the threshold" in {
        val sampleSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "1",
          "123456789",
          isReturnSubmitted = false,
          isFinancialPoint = true,
          amountDue = 200.0
        )

        val pointToPassIn: PenaltyPoint = sampleFinancialPenaltyPoint.copy(number = "1")
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "show the appeal status when the point has been appealed - for under review" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Under_Review)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Under_Review
      }

      "show the appeal status when the point has been appealed - for under tribunal review" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus =
          Some(AppealStatusEnum.Under_Tribunal_Review)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Under_Tribunal_Review
      }

      "show the appeal status when the point has been appealed - for accepted" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus =
          Some(AppealStatusEnum.Accepted)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Accepted
      }

      "show the appeal status when the point has been appealed - for accepted by tribunal" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus =
          Some(AppealStatusEnum.Accepted_By_Tribunal)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Accepted_By_Tribunal
      }

      "show the appeal status when the point has been appealed - for rejected" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Rejected)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Rejected
      }

      "show the appeal status when the point has been appealed - for reinstated" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Reinstated)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Reinstated
      }

      "show the appeal status when the point has been appealed - for tribunal rejected" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected)), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Tribunal_Rejected
      }

      "show message for multiple penalty period " in {

        val expectedResult: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(sampleOldestDate), dateTimeToString(sampleOldestDate.plusDays(15))))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(sampleOldestDate.plusMonths(4).plusDays(7)))),
            helper.summaryListRow(returnSubmitted, Html(dateTimeToString(sampleOldestDate.plusMonths(4).plusDays(12))))
          ),
          Tag(content = Text("active")),
          "1",
          "123456789",
          isReturnSubmitted = true,
          isFinancialPoint = true,
          amountDue = 200.0,
          multiplePenaltyPeriod = Some(Html(lspMultiplePenaltyPeriodMessage(dateTimeToString(sampleOldestDate.plusMonths(4).plusDays(23)))))
        )

        val multiplePenaltyPeriod: PenaltyPoint = sampleFinancialPenaltyPointWithMultiplePenaltyPeriod
        val actualResult = helper.financialSummaryCard(multiplePenaltyPeriod, quarterlyThreshold)
        actualResult shouldBe expectedResult
      }
    }

    "return SummaryCards" when {
      "given a Penalty point" when {
        "populateLateSubmissionPenaltyCard is called" in {
          val result = helper.populateLateSubmissionPenaltyCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold, quarterlyThreshold - 1)
          result shouldBe Seq(sampleLSPSummaryCardReturnSubmitted)
        }

        "user has removed points below active points - active points should be reindexed so that the points are logically numbered correctly" in {
          val expectedResult: Seq[LateSubmissionPenaltySummaryCard] = Seq(LateSubmissionPenaltySummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
              ),
              helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
            ),
            Tag(content = Text("active")),
            "3",
            "123456789",
            isReturnSubmitted = true
          ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
                ),
                helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
                helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
                helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
              ),
              Tag(content = Text("active")),
              "2",
              "123456789",
              isReturnSubmitted = true
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
                ),
                helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
                helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
                helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
              ),
              Tag(content = Text("active")),
              "1",
              "123456789",
              isReturnSubmitted = true
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
                ),
                helper.summaryListRow(reason, Html("reason"))
              ),
              Tag(content = Text("removed")),
              "",
              "123456789",
              isReturnSubmitted = true,
              isAdjustedPoint = true
            ))


          val result = helper.populateLateSubmissionPenaltyCard(sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPoint,
            quarterlyThreshold, quarterlyThreshold - 1)
          result shouldBe expectedResult
        }
      }

      "given a Late Payment penalty" when {
        "return SummaryCards when given Late Payment penalty with penalty Reason VAT_NOT_PAID_WITHIN_15_DAYS" when {
          "populateLatePaymentPenaltyCard is called" in {
            val result = helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyData))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid))
          }

          "return SummaryCards when given Late Payment penalty reason VAT_NOT_PAID_WITHIN_30_DAYS" when {
            "populateLatePaymentPenaltyCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonVATNotPaidWithin30Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("VAT not paid within 30 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty 'Additional' show reason VAT_NOT_PAID_AFTER_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyAdditionalReason))
              result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid))
            }
          }

          "return SummaryCards when given Late Payment penalty show reason CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonCentralAssessmentNotPaidWithin15Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Central Assessment not paid within 15 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty show reason CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonCentralAssessmentNotPaidWithin30Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Central Assessment not paid within 30 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty 'Additional' show reason CENTRAL_ASSESSMENT_NOT_PAID_AFTER_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyAdditionalReasonCentralAssessment)))
              result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardReasonCentralAssessment))
            }
          }

          "return SummaryCards when given Late Payment penalty show reason ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_15_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonErrorCorrectionNoticeNotPaidWithin15Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Error Correction Notice not paid within 15 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty show reason ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonErrorCorrectionNoticeNotPaidWithin30Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Error Correction Notice not paid within 30 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty 'Additional' show reason ERROR_CORRECTION_NOTICE_NOT_PAID_AFTER_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyAdditionalReasonErrorCorrectionNotice)))
              result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardReasonErrorCorrectionNotice))
            }
          }

          "return SummaryCards when given Late Payment penalty show reason OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonOfficersAssessmentNotPaidWithin15Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Officer’s Assessment not paid within 15 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty show reason OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyReasonOfficersAssessmentNotPaidWithin30Days)))
              result.get.map(_.cardRows.exists(_.value.content == HtmlContent("Officer’s Assessment not paid within 30 days"))) shouldBe List(true)
            }
          }

          "return SummaryCards when given Late Payment penalty 'Additional' show reason OFFICERS_ASSESSMENT_NOT_PAID_AFTER_30_DAYS" when {
            "populateLatePaymentPenaltyAdditionalCard is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyAdditionalReasonOfficersAssessment)))
              result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardReasonOfficersAssessment))
            }
          }

          "return SummaryCards with VAT payment date in LPP " when {
            "populateLatePaymentPenalty for  is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyVATPaymentDueDate)))
              result shouldBe Some(Seq(sampleLPPSummaryCardVATPaymentDate))
            }
          }

          "set the isVatPaid boolean to false when the VAT is unpaid" in {
            val result = helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyDataUnpaidVAT))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyUnpaidVAT))
          }
        }
      }
    }
  }

  "return Seq[SummaryListRow] when give a PenaltyPoint" when {
    "returnSubmittedCardBody is called" when {
      "given a PenaltyPoint and the threshold has not been met" in {
        val result = helper.returnSubmittedCardBody(samplePenaltyPoint, thresholdMet = false)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
          ),
          helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
          helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
          helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
        )
      }

      "given a PenaltyPoint and the threshold has been met" in {
        val result = helper.returnSubmittedCardBody(samplePenaltyPoint, thresholdMet = true)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
          ),
          helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
          helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now)))
        )
      }
    }

    "returnNotSubmittedCardBody is called" in {
      val result = helper.returnNotSubmittedCardBody(sampleReturnNotSubmittedPenaltyPeriod)
      result shouldBe Seq(
        helper.summaryListRow(
          period,
          Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
        ),
        helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
        helper.summaryListRow(returnSubmitted, Html(notSubmitted))
      )
    }
  }

  "return a SummaryListRow" when {
    "summaryListRow is called" in {
      val result = helper.summaryListRow("", Html(""))
      result shouldBe SummaryListRow(
        key = Key(
          content = Text(""),
          classes = "govuk-summary-list__key"
        ),
        value = Value(
          content = HtmlContent(""),
          classes = "govuk-summary-list__value"
        ),
        classes = "govuk-summary-list__row"
      )
    }
  }

  "return a Tag" when {

    "renderedTag is called" in {
      val result = helper.renderTag("test", "")
      result shouldBe Tag(
        content = Text("test")
      )
    }

    "tagStatus is called" when {
      "an appealed point is provided - under review" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedUnderReview), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an appealed point is provided - accepted" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedAccepted), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - accepted by tax tribunal" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedAcceptedByTribunal), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedRejected), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an appealed point is provided - reinstated" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedReinstated), None)
        result shouldBe Tag(
          content = Text(reinstatedTag)
        )
      }

      "an appealed point is provided - tribunal rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedTribunalRejected), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an overdue penaltyPointSubmission is provided" in {
        val result = helper.tagStatus(Some(sampleOverduePenaltyPoint), None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "an active penalty point is provided" in {
        val result = helper.tagStatus(Some(samplePenaltyPoint), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "a penalty is submitted but the appeal is rejected - return the appropriate tag" in {
        val result = helper.tagStatus(Some(samplePenaltyPoint.copy(status = PointStatusEnum.Rejected)), None)
        result shouldBe Tag(
          content = Text(rejectedTag)
        )
      }

      "a financial penalty has been added and the user has paid" in {
        val result = helper.tagStatus(Some(samplePenaltyPoint.copy(status = PointStatusEnum.Paid)), None)
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus Accepted" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedAccepted))
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus AcceptedByTribunal" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedAcceptedTribunal))
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus Reinstated " in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedReinstated))
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(200)),
          classes = "penalty-due-tag"
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus Rejected" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedRejectedLPPPaid))
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }

      "a financial penalty has been added and the user has not paid the penalty - appealStatus Rejected" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedRejected))
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(200)),
          classes = "penalty-due-tag"
        )
      }

      "a financial penalty has been added and the user has estimated penalty" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyEstimated))
        result shouldBe Tag(
          content = Text(estimated)
        )
      }
    }
  }

  "pointsThresholdMet" should {
    "return true" when {
      "active points is above threshold" in {
        val annuallyResult = helper.pointsThresholdMet(annualThreshold, annualThreshold + 1)
        val quarterlyResult = helper.pointsThresholdMet(quarterlyThreshold, quarterlyThreshold + 1)
        val monthlyResult = helper.pointsThresholdMet(monthlyThreshold, monthlyThreshold + 1)

        annuallyResult shouldBe true
        quarterlyResult shouldBe true
        monthlyResult shouldBe true
      }

      "active points is at threshold" in {
        val annuallyResult = helper.pointsThresholdMet(annualThreshold, annualThreshold)
        val quarterlyResult = helper.pointsThresholdMet(quarterlyThreshold, quarterlyThreshold)
        val monthlyResult = helper.pointsThresholdMet(monthlyThreshold, monthlyThreshold)

        annuallyResult shouldBe true
        quarterlyResult shouldBe true
        monthlyResult shouldBe true
      }
    }

    "return false" when {
      "active points is below threshold" in {
        val annuallyResult = helper.pointsThresholdMet(annualThreshold, annualThreshold - 1)
        val quarterlyResult = helper.pointsThresholdMet(quarterlyThreshold, quarterlyThreshold - 1)
        val monthlyResult = helper.pointsThresholdMet(monthlyThreshold, monthlyThreshold - 1)

        annuallyResult shouldBe false
        quarterlyResult shouldBe false
        monthlyResult shouldBe false
      }
    }
  }

  "pointSummaryCard" should {
    "when given an appealed point (under review) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedUnderReview, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Under_Review
    }

    "when given an appealed point (under tribunal review) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedUnderTribunalReview, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Under_Tribunal_Review
    }

    "when given an appealed point (accepted) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedAccepted, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Accepted
    }

    "when given an appealed point (accepted by tribunal) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedAcceptedByTribunal, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Accepted_By_Tribunal
    }

    "when given an appealed point (rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedRejected, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Rejected
    }

    "when given an appealed point (reinstated) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedReinstated, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Reinstated
    }
    "when given an appealed point (tribunal rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedTribunalRejected, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Tribunal_Rejected
    }
  }

  "lppSummaryCard" should {
    "when given a point where VAT has not been paid - set the correct field" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyUnpaidVAT)
      result.isVatPaid shouldBe false
    }

    "when given an appealed point (under review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedUnderReview)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Under_Review
    }

    "when given an appealed point (under tribunal review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedUnderTribunalReview)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Under_Tribunal_Review
    }

    "when given an appealed point (accepted) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedAccepted)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Accepted
    }

    "when given an appealed point (accepted by tribunal) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedAcceptedTribunal)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Accepted_By_Tribunal
    }

    "when given an appealed point (rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedRejected)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Rejected
    }

    "when given an appealed point (reinstated) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedReinstated)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Reinstated
    }

    "when given an appealed point (tribunal rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedRejectedTribunal)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe AppealStatusEnum.Tribunal_Rejected
    }

    "when given an additional penalty - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAdditional)
      result.isAdditionalPenalty shouldBe true
      result.cardRows.exists(_.key.content == Text("Charged daily from")) shouldBe true
    }
  }

  "showDueOrPartiallyPaidDueTag" should {
    "when lsp financial data is passed in" should {

      "render a due tag - when there is financial data with an amount to be paid but no payments have been made" in {
        val lspFinancialDataNoPaymentsMade = Some(Financial(400, 400, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lspFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag - when there is no financial data for the penalty" in {
        val result = helper.showDueOrPartiallyPaidDueTag(None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made" in {
        val lspFinancialDataNoPaymentsMade = Some(Financial(400, 146.12, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lspFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(146.12)),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made (with whole tenths)" in {
        val lspFinancialDataNoPaymentsMade = Some(Financial(400, 146.1, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lspFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text("£146.10 due"),
          classes = "penalty-due-tag"
        )
      }
    }

    "when lpp financial data is passed in" should {

      "render a due tag - when there is financial data with an amount to be paid but no payments have been made" in {
        val lppFinancialDataNoPaymentsMade = Some(Financial(600, 600, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lppFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag - when there is no financial data for the penalty" in {
        val result = helper.showDueOrPartiallyPaidDueTag(None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made" in {
        val lspFinancialDataNoPaymentsMade = Some(Financial(600, 383.94, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lspFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(383.94)),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made (with whole tenths)" in {
        val lspFinancialDataNoPaymentsMade = Some(Financial(600, 383.9, LocalDateTime.now))

        val result = helper.showDueOrPartiallyPaidDueTag(lspFinancialDataNoPaymentsMade)
        result shouldBe Tag(
          content = Text("£383.90 due"),
          classes = "penalty-due-tag"
        )
      }
    }
  }
}
