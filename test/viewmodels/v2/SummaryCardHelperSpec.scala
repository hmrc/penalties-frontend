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

package viewmodels.v2

import assets.messages.IndexMessages._
import base.SpecBase
import models.User
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.v3.lpp.LPPDetailsMetadata
import models.v3.lpp.LPPPenaltyCategoryEnum.{LPP1, LPP2}
import models.v3.lpp.MainTransactionEnum.{CentralAssessmentFirstLPP, CentralAssessmentSecondLPP, ErrorCorrectionFirstLPP, ErrorCorrectionSecondLPP, OfficersAssessmentFirstLPP, OfficersAssessmentSecondLPP, VATReturnFirstLPP, VATReturnSecondLPP}
import models.v3.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter
import viewmodels.v2.{SummaryCardHelper => SummaryCardHelperv2}

import java.time.LocalDateTime

class SummaryCardHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: SummaryCardHelperv2 = injector.instanceOf[SummaryCardHelperv2]

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
    "12345678901234",
    isReturnSubmitted = true
  )

  def sampleLPPSummaryCardPenaltyPaid(chargeType: String): LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        penaltyType,
        Html("First penalty for late payment")
      ),
      helper.summaryListRow(
        overdueCharge,
        Html(periodValueLPP(chargeType, dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(chargeDue , Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(datePaid, Html(dateTimeToString(LocalDateTime.now)))
    ),
    Tag(content = Text("paid")),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeReference = "12345678901234",
    isVatPaid = true,
    amountDue = 1001.45,
    isPenaltyPaid = true,
    penaltyCategory = LPP1
  )

  def sampleLPPAdditionalSummaryCardPenaltyPaid(chargeType: String): LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        penaltyType,
        Html("Second penalty for late payment")
      ),
      helper.summaryListRow(
        overdueCharge,
        Html(periodValueLPP(chargeType, dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(chargeDue , Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(datePaid, Html(dateTimeToString(LocalDateTime.now)))
    ),
    Tag(content = Text("paid")),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeReference = "12345678901234",
    isPenaltyPaid = true,
    amountDue = 1001.45,
    isVatPaid = true,
    penaltyCategory = LPP2
  )

  val sampleLPPSummaryCardPenaltyUnpaidVAT: LatePaymentPenaltySummaryCard = sampleLPPSummaryCardPenaltyPaid("VAT").copy(isPenaltyPaid = false, isVatPaid = false,
    status = Tag(content = Text("£200 due"), classes = "penalty-due-tag"))

  "SummaryCardHelper" should {
    "findAndReindexPointIfIsActive" should {
      "reindex the point with the associated index + 1 when the point is in the indexed list of active points" in {
        val pointToPassIn: LSPDetails = samplePenaltyPointv2.copy(penaltyOrder = "2")
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (pointToPassIn, 0),
          (sampleFinancialPenaltyPointv2, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn.copy(penaltyOrder = "1")
        actualResult shouldBe expectedResult
      }

      "NOT reindex when the point is not in the indexed list" in {
        val pointToPassIn: LSPDetails = samplePenaltyPointv2.copy(penaltyOrder = "2")
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (sampleFinancialPenaltyPointv2, 0),
          (samplePenaltyPointv2, 1)
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
          totalPenaltyAmount = 200.0,
          multiplePenaltyPeriod = None
        )

        val pointToPassIn: LSPDetails = sampleFinancialPenaltyPointv2.copy(penaltyOrder = "5")
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
          isThresholdPoint = true,
          totalPenaltyAmount = 200.0
        )

        val pointToPassIn: LSPDetails = sampleFinancialPenaltyPointv2.copy(penaltyOrder = "1", penaltyCategory = LSPPenaltyCategoryEnum.Threshold)
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "show the appeal status when the point has been appealed - for under review" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Under_Appeal),
            appealLevel = Some(AppealLevelEnum.HMRC)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Under_Appeal
        result.appealLevel.get shouldBe AppealLevelEnum.HMRC
      }

      "show the appeal status when the point has been appealed - for under tribunal review" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Under_Appeal),
            appealLevel = Some(AppealLevelEnum.Tribunal)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealLevel.get shouldBe AppealLevelEnum.Tribunal
        result.appealStatus.get shouldBe AppealStatusEnum.Under_Appeal
      }

      "show the appeal status when the point has been appealed - for accepted" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Upheld),
            appealLevel = Some(AppealLevelEnum.HMRC)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Upheld
      }

      "show the appeal status when the point has been appealed - for accepted by tribunal" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Upheld),
            appealLevel = Some(AppealLevelEnum.Tribunal)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Upheld
        result.appealLevel.get shouldBe AppealLevelEnum.Tribunal
      }

      "show the appeal status when the point has been appealed - for rejected" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Rejected),
            appealLevel = Some(AppealLevelEnum.HMRC)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Rejected
        result.appealLevel.get shouldBe AppealLevelEnum.HMRC
      }
// TODO: Reinstated to be implemented
      "show the appeal status when the point has been appealed - for reinstated" ignore {
        val result = helper.financialSummaryCard(LSPDetailsAsModelNoFAP, quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe "Some Reinstated"
      }

      "show the appeal status when the point has been appealed - for tribunal rejected" in {
        val result = helper.financialSummaryCard(sampleFinancialPenaltyPointv2.copy(appealInformation = Some(Seq(
          AppealInformationType(
            appealStatus = Some(AppealStatusEnum.Rejected),
            appealLevel = Some(AppealLevelEnum.Tribunal)
          )
        ))), quarterlyThreshold)
        result.isAppealedPoint shouldBe true
        result.appealStatus.isDefined shouldBe true
        result.appealStatus.get shouldBe AppealStatusEnum.Rejected
        result.appealLevel.get shouldBe AppealLevelEnum.Tribunal
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
          Tag(content = Text("due"),
            classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = true,
          isFinancialPoint = true,
          totalPenaltyAmount = 200,
          multiplePenaltyPeriod = Some(Html(lspMultiplePenaltyPeriodMessage(dateTimeToString(sampleOldestDate.plusMonths(4).plusDays(23)))))
        )

        val multiplePenaltyPeriod: LSPDetails = sampleFinancialPenaltyPointWithMultiplePenaltyPeriodv2
        val actualResult = helper.financialSummaryCard(multiplePenaltyPeriod, quarterlyThreshold)
        actualResult shouldBe expectedResult
      }
    }

    "return SummaryCards" when {
      "given a Penalty point" when {
        "populateLateSubmissionPenaltyCard is called" in {
          val result = helper.populateLateSubmissionPenaltyCard(sampleReturnSubmittedPenaltyPointDatav2, quarterlyThreshold, quarterlyThreshold - 1)
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
            "12345678901234",
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
              "12345678901234",
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
              "12345678901234",
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
              "12345678901234",
              isReturnSubmitted = true,
              isAdjustedPoint = true
            ))


          val result = helper.populateLateSubmissionPenaltyCard(sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPointv2,
            quarterlyThreshold, quarterlyThreshold - 1)
          result.head.penaltyPoint shouldBe expectedResult.head.penaltyPoint
          result(1).penaltyPoint shouldBe expectedResult(1).penaltyPoint
          result(2).penaltyPoint shouldBe expectedResult(2).penaltyPoint
          result(3).penaltyPoint shouldBe expectedResult(3).penaltyPoint
        }
      }

      "given a Late Payment penalty" when {
        val sampleLPPSummaryCardPenaltyDue: LatePaymentPenaltySummaryCard = LatePaymentPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              penaltyType,
              Html("First penalty for late payment")
            ),
            helper.summaryListRow(
              overdueCharge,
              Html(periodValueLPP("VAT", dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(chargeDue , Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(datePaid, Html(dateTimeToString(LocalDateTime.now)))
          ),
          Tag(content = Text("£200 due"), classes = "penalty-due-tag"),
          penaltyChargeReference = Some("PEN1234567"),
          principalChargeReference = "123456789",
          amountDue = 400.0,
          isPenaltyPaid = false,
          isVatPaid = true,
          penaltyCategory = LPP1
        )

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Return 1st LPP (4703)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForVAT = Seq(sampleLatePaymentPenaltyPaidv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnFirstLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForVAT))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("VAT")))
          }
        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is VAT Return 2nd LPP (4704)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForVAT = Seq(sampleLatePaymentPenaltyAdditionalv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnSecondLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForVAT))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Central Assessment 1st LPP (4723)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForCentralAssessment = Seq(sampleLatePaymentPenaltyPaidv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentFirstLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Central Assessment of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Central Assessment 2nd LPP (4724)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForCentralAssessment = Seq(sampleLatePaymentPenaltyAdditionalv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentSecondLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Central Assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Error Correction Notice 1st LPP (4743)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForErrorCorrectionNotice = Seq(sampleLatePaymentPenaltyPaidv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ErrorCorrectionFirstLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForErrorCorrectionNotice))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Error Correction Notice of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Error Correction Notice 2nd LPP (4744)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForErrorCorrectionNotice = Seq(sampleLatePaymentPenaltyAdditionalv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ErrorCorrectionSecondLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForErrorCorrectionNotice))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Error Correction Notice of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Officer's Assessment 1st LPP (4741)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForOfficersAssessment = Seq(sampleLatePaymentPenaltyPaidv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(OfficersAssessmentFirstLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForOfficersAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Officer’s Assessment of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Officer's Assessment 2nd LPP (4742)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForOfficersAssessment = Seq(sampleLatePaymentPenaltyAdditionalv2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(OfficersAssessmentSecondLPP), outstandingAmount = Some(1)
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForOfficersAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Officer’s Assessment of VAT")))
          }
        }

        "return SummaryCards with VAT payment date in LPP " when {
            "populateLatePaymentPenalty for is called" in {
              val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLatePaymentPenaltyPaidv2.copy(
                LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = Some(VATReturnFirstLPP), outstandingAmount = Some(1)
                )
              ))))
              result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("VAT")))
            }
          }

          "set the isVatPaid boolean to false when the VAT is unpaid" in {
            val sampleLPPSummaryCardPenaltyUnpaidVAT: LatePaymentPenaltySummaryCard = sampleLPPSummaryCardPenaltyDue.copy(
              cardRows = Seq(
                helper.summaryListRow(
                  penaltyType,
                  Html("First penalty for late payment")
                ),
                helper.summaryListRow(
                  overdueCharge,
                  Html(periodValueLPP("VAT", dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
                ),
                helper.summaryListRow(chargeDue , Html(dateTimeToString(LocalDateTime.now))),
                helper.summaryListRow(datePaid, Html("Payment not yet received"))
              ),
              isPenaltyPaid = false,
              isVatPaid = false,
              status = Tag(content = Text("estimated")))

            val result = helper.populateLatePaymentPenaltyCard(Some(sampleLatePaymentPenaltyDataUnpaidVATv2))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyUnpaidVAT))
          }
        }
      }
    }

  "return Seq[SummaryListRow] when give a PenaltyPoint" when {
    "returnSubmittedCardBody is called" when {
      "given a PenaltyPoint and the threshold has not been met" in {
        val result = helper.returnSubmittedCardBody(samplePenaltyPointv2, thresholdMet = false)
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
        val result = helper.returnSubmittedCardBody(samplePenaltyPointv2, thresholdMet = true)
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
      val result = helper.returnNotSubmittedCardBody(sampleReturnNotSubmittedPenaltyPeriodv2.lateSubmissions.map(_.head).get)
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
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedUnderReviewv2), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an appealed point is provided - accepted" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedAcceptedv2), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - accepted by tax tribunal" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedAcceptedByTribunalv2), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedRejectedv2), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      //TODO implement Reinstated
      "an appealed point is provided - reinstated" ignore {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedAcceptedv2), None)
        result shouldBe Tag(
          content = Text(reinstatedTag)
        )
      }

      "an appealed point is provided - tribunal rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppealedTribunalRejectedv2), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an overdue penaltyPointSubmission is provided" in {
        val result = helper.tagStatus(Some(sampleOverduePenaltyPointv2), None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "an active penalty point is provided" in {
        val result = helper.tagStatus(Some(samplePenaltyPointv2.copy(chargeAmount = None)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "a penalty is submitted but the appeal is rejected - return the appropriate tag" in {
        val result = helper.tagStatus(Some(samplePenaltyPointv2.copy(penaltyStatus = LSPPenaltyStatusEnum.Active,
          chargeAmount = None,
          appealInformation = Some(Seq(
            AppealInformationType(
              appealStatus = Some(AppealStatusEnum.Rejected),
              appealLevel = None
            )
          )))), None)
        result shouldBe Tag(
          content = Text(rejectedTag)
        )
      }

      "a financial penalty has been added and the user has paid" in {
        val result = helper.tagStatus(Some(samplePenaltyPointv2.copy(penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyCategory = LSPPenaltyCategoryEnum.Charge)), None)
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus Accepted" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedAcceptedv2))
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      // TODO: implment for Reinstated
      "a financial penalty has been added and the user has paid - appealStatus Reinstated " ignore {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedAcceptedv2))
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(200)),
          classes = "penalty-due-tag"
        )
      }
      "a financial penalty has been added and the user has paid - appealStatus Rejected" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedRejectedLPPPaidv2))
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }

      "a financial penalty has been added and the user has not paid the penalty - appealStatus Rejected" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyAppealedRejectedv2))
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(200)),
          classes = "penalty-due-tag"
        )
      }

      "a financial penalty has been added and the user has estimated penalty" in {
        val result = helper.tagStatus(None,Some(sampleLatePaymentPenaltyEstimatedv2))
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
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedUnderReviewv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (under tribunal review) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedUnderTribunalReviewv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (accepted) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedAcceptedv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (accepted by tribunal) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedAcceptedByTribunalv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedRejectedv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }
// TODO: implment for Reinstated
    "when given an appealed point (reinstated) - set the relevant fields" ignore {
      val result = helper.pointSummaryCard(LSPDetailsAsModelNoFAP, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe "Some Reinstated"
    }

    "when given an appealed point (tribunal rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppealedTribunalRejectedv2, thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }
  }

  "lppSummaryCard" should {
    "when given a point where VAT has not been paid - set the correct field" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyUnpaidVATv2)
      result.isVatPaid shouldBe false
    }

    "when given an appealed point (under review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedUnderReviewv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (under tribunal review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedUnderTribunalReviewv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (accepted) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedAcceptedv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (accepted by tribunal) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedAcceptedTribunalv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedRejectedv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    //TODO: implement Reinstated
    "when given an appealed point (reinstated) - set the relevant fields" ignore {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyUnpaidVATv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus.get shouldBe "Reinstated"
    }

    "when given an appealed point (tribunal rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAppealedRejectedTribunalv2)
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an additional penalty - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLatePaymentPenaltyAdditionalv2)
      result.penaltyCategory.equals(LPP2) shouldBe true
      result.cardRows.exists(_.key.content == Text("Charge due")) shouldBe true
    }
  }

  "showDueOrPartiallyPaidDueTag" should {
    "when lsp financial data is passed in" should {

      "render a due tag - when there is financial data with an amount to be paid but no payments have been made" in {
         val penaltyAmountOutstanding = Some(BigDecimal(400.00))
        val penaltyAmountPaid = BigDecimal(0.00)

        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag - when there is no financial data for the penalty" in {
        val penaltyAmountPaid = BigDecimal(00.00)

        val result = helper.showDueOrPartiallyPaidDueTag(None, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made" in {
        val penaltyAmountOutstanding = Some(BigDecimal(146.12))
        val penaltyAmountPaid = BigDecimal(200.00)

        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(146.12)),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made (with whole tenths)" in {
        val penaltyAmountOutstanding = Some(BigDecimal(146.1))
        val penaltyAmountPaid = BigDecimal(200.00)

        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text("£146.10 due"),
          classes = "penalty-due-tag"
        )
      }
    }

    "when lpp financial data is passed in" should {

      "render a due tag - when there is financial data with an amount to be paid but no payments have been made" in {
        val penaltyAmountOutstanding = Some(BigDecimal(600))
        val penaltyAmountPaid = BigDecimal(0)
        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag - when there is no financial data for the penalty" in {
        val penaltyAmountPaid = BigDecimal(00.00)
        val result = helper.showDueOrPartiallyPaidDueTag(None, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made" in {
        val penaltyAmountOutstanding = Some(BigDecimal(383.94))
        val penaltyAmountPaid = BigDecimal(200.00)

        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(383.94)),
          classes = "penalty-due-tag"
        )
      }

      "render a due tag with the outstanding amount shown - when a partial payment has been made (with whole tenths)" in {
        val penaltyAmountOutstanding = Some(BigDecimal(383.90))
        val penaltyAmountPaid = BigDecimal(200.00)

        val result = helper.showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text("£383.90 due"),
          classes = "penalty-due-tag"
        )
      }
    }
  }
}
