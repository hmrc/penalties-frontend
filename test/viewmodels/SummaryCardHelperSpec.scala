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

import assets.messages.IndexMessages._
import base.SpecBase
import models.User
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp.LPPPenaltyCategoryEnum._
import models.lpp.MainTransactionEnum._
import models.lpp.{LPPDetailsMetadata, LPPPenaltyStatusEnum}
import models.lsp._
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

import java.time.LocalDateTime

class SummaryCardHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  implicit val user: User[_] = vatTraderUser

  val sampleLSPSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
      ),
      helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
      helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate))),
      helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(sampleDate)))
    ),
    Tag(content = Text("active")),
    "1",
    "12345678901234",
    isReturnSubmitted = true,
    dueDate = Some(dateToString(sampleDate))
  )

  def sampleLPPSummaryCardPenaltyPaid(chargeType: String): LatePaymentPenaltySummaryCard = {
    val periodText = getPeriodText(chargeType)
    LatePaymentPenaltySummaryCard(
      Seq(
        helper.summaryListRow(
          penaltyType,
          Html("First penalty for late payment")
        ),
        helper.summaryListRow(
          overdueCharge,
          Html(periodText(chargeType, dateToString(sampleDate), dateToString(sampleDate)))
        ),
        helper.summaryListRow(chargeDue, Html(dateToString(sampleDate))),
        helper.summaryListRow(datePaid, Html(dateToString(sampleDate)))
      ),
      Tag(content = Text("paid")),
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeReference = "12345678901234",
      isVatPaid = true,
      amountDue = 1001.45,
      isPenaltyPaid = true,
      penaltyCategory = LPP1,
      dueDate = "1 January 2021",
      taxPeriodStartDate = sampleDate.toString,
      taxPeriodEndDate = sampleDate.toString
    )
  }

  def sampleLPPAdditionalSummaryCardPenaltyPaid(chargeType: String): LatePaymentPenaltySummaryCard = {
    val periodText = getPeriodText(chargeType)
    LatePaymentPenaltySummaryCard(
      Seq(
        helper.summaryListRow(
          penaltyType,
          Html("Second penalty for late payment")
        ),
        helper.summaryListRow(
          overdueCharge,
          Html(periodText(chargeType, dateToString(sampleDate), dateToString(sampleDate)))
        ),
        helper.summaryListRow(chargeDue, Html(dateToString(sampleDate))),
        helper.summaryListRow(datePaid, Html(dateToString(sampleDate)))
      ),
      Tag(content = Text("paid")),
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeReference = "12345678901234",
      isPenaltyPaid = true,
      amountDue = 1001.45,
      isVatPaid = true,
      penaltyCategory = LPP2,
      dueDate = "1 January 2021",
      taxPeriodStartDate = sampleDate.toString,
      taxPeriodEndDate = sampleDate.toString
    )
  }

  private def getPeriodText(chargeType: String): (String, String, String) => String = {
    chargeType match {
      case "VAT" | "Central assessment of VAT" => periodValueLPPOnePeriod
      case _ => periodValueLPPMultiplePeriods
    }
  }

  val sampleLPPSummaryCardPenaltyUnpaidVAT: LatePaymentPenaltySummaryCard = sampleLPPSummaryCardPenaltyPaid("VAT").copy(isPenaltyPaid = false, isVatPaid = false,
    status = Tag(content = Text("£200 due"), classes = "penalty-due-tag"))

  "SummaryCardHelper" should {
    "findAndReindexPointIfIsActive" should {
      "reindex the point with the associated index + 1 when the point is in the indexed list of active points" in {
        val pointToPassIn: LSPDetails = samplePenaltyPoint.copy(penaltyOrder = "2")
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (pointToPassIn, 0),
          (sampleFinancialPenalty, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn.copy(penaltyOrder = "1")
        actualResult shouldBe expectedResult
      }

      "NOT reindex when the point is not in the indexed list" in {
        val pointToPassIn: LSPDetails = samplePenaltyPoint.copy(penaltyOrder = "2")
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (sampleFinancialPenalty, 0),
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
              Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate)))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "",
          "12345678901234",
          isReturnSubmitted = true,
          isFinancialPoint = true,
          totalPenaltyAmount = 200,
          multiplePenaltyPeriod = None,
          dueDate = Some(dateToString(sampleDate))
        )

        val pointToPassIn: LSPDetails = sampleFinancialPenalty.copy(penaltyOrder = "5")
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "have the penalty number when it DOES NOT exceeds the threshold" in {
        val sampleSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate)))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = true,
          isThresholdPoint = true,
          totalPenaltyAmount = 200,
          dueDate = Some(dateToString(sampleDate))
        )

        val pointToPassIn: LSPDetails = sampleFinancialPenalty.copy(penaltyOrder = "1", penaltyCategory = LSPPenaltyCategoryEnum.Threshold)
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "show the appeal status when the point has been appealed - for under review" in {
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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

      "show the appeal status when the point has been appealed - for tribunal rejected" in {
        val result = helper.financialSummaryCard(sampleFinancialPenalty.copy(appealInformation = Some(Seq(
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

      "show message for multiple penalty period" in {
        val expectedResult: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(sampleOldestDate), dateToString(sampleOldestDate.plusDays(15))))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(sampleOldestDate.plusMonths(4).plusDays(7)))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(sampleOldestDate.plusMonths(4).plusDays(12))))
          ),
          Tag(content = Text("due"),
            classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = true,
          isFinancialPoint = true,
          totalPenaltyAmount = 200,
          multiplePenaltyPeriod = Some(Html(lspMultiplePenaltyPeriodMessage(dateToString(sampleOldestDate.plusMonths(4).plusDays(23))))),
          dueDate = Some(dateToString(sampleOldestDate.plusMonths(4).plusDays(7)))
        )

        val actualResult = helper.financialSummaryCard(sampleFinancialPenaltyWithMultiplePeriods, quarterlyThreshold)
        actualResult shouldBe expectedResult
      }

      "display return not yet received for penalty with unsubmitted return" in {
        val sampleSummaryCardReturnNotSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = false,
          isThresholdPoint = true,
          totalPenaltyAmount = 200,
          dueDate = Some(dateToString(sampleDate))
        )

        val pointToPassIn: LSPDetails = sampleFinancialPenalty.copy(penaltyOrder = "1", penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(sampleDate),
                taxPeriodEndDate = Some(sampleDate),
                taxPeriodDueDate = Some(sampleDate),
                returnReceiptDate = None,
                taxReturnStatus = TaxReturnStatusEnum.Open
              )
            )
          ))
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnNotSubmitted
        actualResult shouldBe expectedResult
      }
    }

    "return SummaryCards" when {
      "given a Penalty point" when {
        "populateLateSubmissionPenaltyCard is called" in {
          val result = helper.populateLateSubmissionPenaltyCard(Seq(sampleReturnSubmittedPenaltyPoint), quarterlyThreshold, quarterlyThreshold - 1)
          result shouldBe Seq(sampleLSPSummaryCardReturnSubmitted)
        }

        "user has removed points below active points - active points should be reindexed so that the points are logically numbered correctly" in {
          val sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPointv2: Seq[LSPDetails] = Seq(
            samplePenaltyPoint.copy(penaltyOrder = "4"),
            samplePenaltyPoint.copy(penaltyOrder = "3"),
            samplePenaltyPoint.copy(penaltyOrder = "2"),
            sampleRemovedPenaltyPoint
          )
          val expectedResult: Seq[LateSubmissionPenaltySummaryCard] = Seq(LateSubmissionPenaltySummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
              ),
              helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
              helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate))),
              helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
            ),
            Tag(content = Text("active")),
            "3",
            "12345678901234",
            isReturnSubmitted = true,
            dueDate = Some(dateToString(sampleDate))
          ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
                ),
                helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
                helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate))),
                helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
              ),
              Tag(content = Text("active")),
              "2",
              "12345678901234",
              isReturnSubmitted = true,
              dueDate = Some(dateToString(sampleDate))
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
                ),
                helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
                helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate))),
                helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
              ),
              Tag(content = Text("active")),
              "1",
              "12345678901234",
              isReturnSubmitted = true,
              dueDate = Some(dateToString(sampleDate))
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
                ),
                helper.summaryListRow(reason, Html("reason"))
              ),
              Tag(content = Text("removed")),
              "",
              "12345678901234",
              isReturnSubmitted = true,
              isAdjustedPoint = true,
              dueDate = Some(dateToString(sampleDate))
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
              Html(periodValueLPPOnePeriod("VAT", dateToString(sampleDate), dateToString(sampleDate)))
            ),
            helper.summaryListRow(chargeDue, Html(dateToString(sampleDate))),
            helper.summaryListRow(datePaid, Html(dateToString(sampleDate)))
          ),
          Tag(content = Text("£200 due"), classes = "penalty-due-tag"),
          penaltyChargeReference = Some("PEN1234567"),
          principalChargeReference = "12345678901234",
          amountDue = 400.0,
          isPenaltyPaid = false,
          isVatPaid = true,
          penaltyCategory = LPP1,
          dueDate = "1 January 2021",
          taxPeriodEndDate = sampleDate.toString,
          taxPeriodStartDate = sampleDate.toString
        )

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Return 1st LPP (4703)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForVAT = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnFirstLPP), outstandingAmount = Some(0), timeToPay = None
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForVAT))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("VAT")))
          }
        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is VAT Return 2nd LPP (4704)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForVAT = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnSecondLPP), outstandingAmount = Some(0), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForVAT))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Central assessment 1st LPP (4723)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForCentralAssessment = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Central assessment of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Central assessment 2nd LPP (4724)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForCentralAssessment = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Central assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Error correction 1st LPP (4743)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForErrorCorrectionNotice = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ErrorCorrectionFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForErrorCorrectionNotice))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Error correction of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Error correction 2nd LPP (4744)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForErrorCorrectionNotice = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ErrorCorrectionSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForErrorCorrectionNotice))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Error correction of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Officer's assessment 1st LPP (4741)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForOfficersAssessment = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(OfficersAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              )
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForOfficersAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Officer’s assessment of VAT")))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Officer's assessment 2nd LPP (4742)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForOfficersAssessment = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(OfficersAssessmentSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForOfficersAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Officer’s assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is Additional assessment 1st LPP (4758)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForAdditionalAssessment = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AdditionalAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForAdditionalAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Additional assessment of VAT")))
          }
        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is Additional assessment 2nd LPP (4759)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForAdditionalAssessment = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AdditionalAssessmentSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForAdditionalAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Additional assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is Protective assessment 1st LPP (4761)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForProtectiveAssessment = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ProtectiveAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForProtectiveAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Protective assessment of VAT")))
          }
        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is Protective assessment 2nd LPP (4762)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForProtectiveAssessment = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ProtectiveAssessmentSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForProtectiveAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Protective assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is a POA Return Charge 1st LPP (4716)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForPOAReturnCharge = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(POAReturnChargeFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForPOAReturnCharge))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Payment on account instalment")))
          }

        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is a POA Return Charge 2nd LPP (4717)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForPOAReturnCharge = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(POAReturnChargeSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForPOAReturnCharge))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Payment on account instalment")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is AA Return charge 1st LPP (4718)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForAAReturnCharge = Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AAReturnChargeFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForAAReturnCharge))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Annual accounting balance")))
          }

        }

        "return SummaryCards when given Second Late Payment penalty and chargeType is AA Return charge 1st LPP (4719)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForAAReturnCharge = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AAReturnChargeSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = Some(0),
              penaltyAmountPaid = Some(1001.45),
              principalChargeLatestClearing = Some(sampleDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForAAReturnCharge))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Annual accounting balance")))
          }
        }

        "return SummaryCards with VAT payment date in LPP " when {
          "populateLatePaymentPenalty for is called" in {
            val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleLPP1Paid.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnFirstLPP), outstandingAmount = Some(1), timeToPay = None
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
                Html(periodValueLPPOnePeriod("VAT", dateToString(sampleDate), dateToString(sampleDate)))
              ),
              helper.summaryListRow(chargeDue, Html(dateToString(sampleDate))),
              helper.summaryListRow(datePaid, Html("Payment not yet received"))
            ),
            isPenaltyPaid = false,
            isVatPaid = false,
            status = Tag(content = Text("estimate")))

          val result = helper.populateLatePaymentPenaltyCard(
            Some(
              Seq(sampleLPP1.copy(
                appealInformation = None,
                penaltyAmountOutstanding = Some(400),
                penaltyAmountPaid = Some(0)
              ))
            )
          )
          result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyUnpaidVAT))
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
            Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
          helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate))),
          helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(sampleDate)))
        )
      }

      "given a PenaltyPoint and the threshold has been met" in {
        val result = helper.returnSubmittedCardBody(samplePenaltyPoint, thresholdMet = true)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
          helper.summaryListRow(returnSubmitted, Html(dateToString(sampleDate)))
        )
      }
    }

    "returnNotSubmittedCardBody is called" in {
      val result = helper.returnNotSubmittedCardBody(samplePenaltyPointNotSubmitted.lateSubmissions.map(_.head).get)
      result shouldBe Seq(
        helper.summaryListRow(
          period,
          Html(vatPeriodValue(dateToString(sampleDate), dateToString(sampleDate)))
        ),
        helper.summaryListRow(returnDue, Html(dateToString(sampleDate))),
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
        val result = helper.tagStatus(Some(samplePenaltyPointAppeal(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an appealed point is provided - accepted" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - accepted by tax tribunal" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.Tribunal)), None)
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "an appealed point is provided - rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an appealed point is provided - tribunal rejected" in {
        val result = helper.tagStatus(Some(samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.Tribunal)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "an overdue penaltyPointSubmission is provided" in {
        val result = helper.tagStatus(Some(sampleFinancialPenalty), None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "an active penalty point is provided" in {
        val result = helper.tagStatus(Some(samplePenaltyPoint.copy(chargeAmount = None)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "a penalty is submitted but the appeal is rejected - return the appropriate tag" in {
        val result = helper.tagStatus(Some(samplePenaltyPoint.copy(penaltyStatus = LSPPenaltyStatusEnum.Active,
          chargeAmount = None,
          appealInformation = Some(Seq(
            AppealInformationType(
              appealStatus = Some(AppealStatusEnum.Rejected),
              appealLevel = None
            )
          )))), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "a financial penalty has been added and the user has paid" in {
        val result = helper.tagStatus(Some(sampleFinancialPenalty.copy(chargeOutstandingAmount = Some(0))), None)
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }

      "a financial penalty has been added and the user has paid - appealStatus Upheld" in {
        val result = helper.tagStatus(None, Some(sampleLPP1AppealPaid(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)))
        result shouldBe Tag(
          content = Text(cancelledTag)
        )
      }

      "a financial penalty has been added and the user has paid - appealStatus Rejected" in {
        val result = helper.tagStatus(None, Some(sampleLPP1AppealPaid(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC)))
        result shouldBe Tag(
          content = Text(paidTag)
        )
      }

      "a financial penalty has been added and the user has not paid the penalty - appealStatus Rejected" in {
        val result = helper.tagStatus(None, Some(sampleLPP1AppealUnpaid(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC)))
        result shouldBe Tag(
          content = Text(overduePartiallyPaidTag(200)),
          classes = "penalty-due-tag"
        )
      }

      "a financial penalty has been added and the user has estimated penalty" in {
        val result = helper.tagStatus(None, Some(sampleLPP1))
        result shouldBe Tag(
          content = Text(estimate)
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
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (under tribunal review) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Under_Appeal, AppealLevelEnum.Tribunal), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (accepted) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (accepted by tribunal) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Upheld, AppealLevelEnum.Tribunal), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed point (rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed point (tribunal rejected) - set the relevant fields" in {
      val result = helper.pointSummaryCard(samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.Tribunal), thresholdMet = false)
      result.isAppealedPoint shouldBe true
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }
  }

  "lppSummaryCard" should {
    "when given a LPP where VAT has not been paid - set the correct field" in {
      val result = helper.lppSummaryCard(sampleLPP1)
      result.isVatPaid shouldBe false
    }

    "when given an appealed LPP (under review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed LPP (under tribunal review) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.Tribunal))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Under_Appeal)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed LPP (accepted) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed LPP (accepted by tribunal) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Upheld, AppealLevelEnum.Tribunal))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Upheld)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given an appealed LPP (rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.HMRC)
    }

    "when given an appealed LPP (tribunal rejected) - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP1AppealPaid(AppealStatusEnum.Rejected, AppealLevelEnum.Tribunal))
      result.appealStatus.isDefined shouldBe true
      result.appealStatus shouldBe Some(AppealStatusEnum.Rejected)
      result.appealLevel shouldBe Some(AppealLevelEnum.Tribunal)
    }

    "when given a LPP2 - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleLPP2)
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
