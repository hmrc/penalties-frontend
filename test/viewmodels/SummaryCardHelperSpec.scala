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
import models.lpp.{LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import models.lsp._
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

class SummaryCardHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  implicit val user: User[_] = vatTraderUser

  val sampleLSPSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
      ),
      helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
      helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate))),
      helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(expiryDate)))
    ),
    Tag(content = Text("active")),
    "1",
    "12345678901234",
    isReturnSubmitted = true,
    dueDate = Some(dateToString(taxPeriodDue)),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Point)
  )

  def sampleLPPSummaryCardPenaltyPaid(chargeType: String, isAgent: Boolean = false, isCentralAssessment: Boolean = false): LatePaymentPenaltySummaryCard = {
    val periodText = getPeriodText(chargeType)
    LatePaymentPenaltySummaryCard(
      Seq(
        helper.summaryListRow(
          penaltyType,
          Html("First penalty for late payment")
        ),
        helper.summaryListRow(
          overdueCharge,
          Html(periodText(chargeType, dateToString(principleChargeBillingStartDate), dateToString(principleChargeBillingEndDate)))
        ),
        helper.summaryListRow(chargeDue, Html(dateToString(principleChargeBillingDueDate))),
        helper.summaryListRow(datePaid, Html(dateToString(lpp1PrincipleChargePaidDate)))
      ),
      Tag(content = Text("paid")),
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeReference = "12345678901234",
      isVatPaid = true,
      amountDue = 1001.45,
      isPenaltyPaid = true,
      penaltyCategory = LPP1,
      dueDate = "7\u00A0June\u00A02021",
      taxPeriodStartDate = principleChargeBillingStartDate.toString,
      taxPeriodEndDate = principleChargeBillingEndDate.toString,
      isAgent = isAgent,
      isCentralAssessment = isCentralAssessment,
      vatOutstandingAmountInPence = 12345
    )
  }

  def sampleLPPAdditionalSummaryCardPenaltyPaid(chargeType: String, isAgent: Boolean = false, isCentralAssessment: Boolean = false): LatePaymentPenaltySummaryCard = {
    val periodText = getPeriodText(chargeType)
    LatePaymentPenaltySummaryCard(
      Seq(
        helper.summaryListRow(
          penaltyType,
          Html("Second penalty for late payment")
        ),
        helper.summaryListRow(
          overdueCharge,
          Html(periodText(chargeType, dateToString(principleChargeBillingStartDate), dateToString(principleChargeBillingEndDate)))
        ),
        helper.summaryListRow(chargeDue, Html(dateToString(principleChargeBillingDueDate))),
        helper.summaryListRow(datePaid, Html(dateToString(lpp2PrincipleChargePaidDate)))
      ),
      Tag(content = Text("paid")),
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeReference = "12345678901234",
      isPenaltyPaid = true,
      isVatPaid = true,
      amountDue = 1001.45,
      penaltyCategory = LPP2,
      dueDate = "7\u00A0June\u00A02021",
      taxPeriodStartDate = principleChargeBillingStartDate.toString,
      taxPeriodEndDate = principleChargeBillingEndDate.toString,
      isAgent = isAgent,
      isCentralAssessment = isCentralAssessment,
      vatOutstandingAmountInPence = 12345
    )
  }

  def sampleManualLPPSummaryCard: LatePaymentPenaltySummaryCard = {
    LatePaymentPenaltySummaryCard(
      cardRows = Seq(
        helper.summaryListRow(
          penaltyType,
          Html("Penalty for late payment – details are in the letter we sent you")
        ),
        helper.summaryListRow(
          "Added on",
          Html(dateToString(penaltyChargeCreationDate)))
      ),
      status = Tag(content = Text("due"), classes = "penalty-due-tag"),
      penaltyChargeReference = None,
      principalChargeReference = "09876543210987",
      isPenaltyPaid = false,
      amountDue = 999.99,
      appealStatus = None,
      appealLevel = None,
      penaltyCategory = LPPPenaltyCategoryEnum.MANUAL,
      dueDate = "7\u00A0June\u00A02021",
      taxPeriodStartDate = principleChargeBillingStartDate.toString,
      taxPeriodEndDate = principleChargeBillingEndDate.toString,
      vatOutstandingAmountInPence = 12345
    )
  }

  private def getPeriodText(chargeType: String): (String, String, String) => String = {
    chargeType match {
      case "VAT" | "Central assessment of VAT" => periodValueLPPOnePeriod
      case _ => periodValueLPPMultiplePeriods
    }
  }


  "SummaryCardHelper" should {
    "findAndReindexPointIfIsActive" should {
      "reindex the point with the associated index + 1 when the point is in the indexed list of active points" in {
        val pointToPassIn: LSPDetails = sampleLateSubmissionPoint.copy(penaltyOrder = Some("02"))
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (pointToPassIn, 0),
          (sampleLateSubmissionPenaltyCharge, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn.copy(penaltyOrder = Some("1"))
        actualResult shouldBe expectedResult
      }

      "NOT reindex when the point is not in the indexed list" in {
        val pointToPassIn: LSPDetails = sampleLateSubmissionPoint.copy(penaltyOrder = Some("02"))
        val indexedPoints: Seq[(LSPDetails, Int)] = Seq(
          (sampleLateSubmissionPenaltyCharge, 0),
          (sampleLateSubmissionPoint, 1)
        )
        val actualResult = helper.findAndReindexPointIfIsActive(indexedPoints, pointToPassIn)
        val expectedResult = pointToPassIn
        actualResult shouldBe expectedResult
      }
    }

    "getPenaltyNumberBasedOnThreshold" should {
      "when given a penalty number greater than the threshold - return an empty string" in {
        val penaltyNumber = "5"
        val result = helper.getPenaltyNumberBasedOnThreshold(Some(penaltyNumber), quarterlyThreshold)
        result shouldBe ""
      }

      "when given a penalty number that is not defined - return an empty string" in {
        val result = helper.getPenaltyNumberBasedOnThreshold(None, quarterlyThreshold)
        result shouldBe ""
      }

      "when given a penalty number at the threshold - return the penalty number" in {
        val penaltyNumber = "4"
        val result = helper.getPenaltyNumberBasedOnThreshold(Some(penaltyNumber), quarterlyThreshold)
        result shouldBe penaltyNumber
      }

      "when given a penalty number below the threshold - return the penalty number" in {
        val penaltyNumber = "3"
        val result = helper.getPenaltyNumberBasedOnThreshold(Some(penaltyNumber), quarterlyThreshold)
        result shouldBe penaltyNumber
      }
    }

    "financialSummaryCard" should {
      "hide the penalty number when the active penalty number exceeds the threshold" in {
        val expectedResult: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate)))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "",
          "12345678901234",
          isReturnSubmitted = true,
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          totalPenaltyAmount = 200,
          multiplePenaltyPeriod = None,
          dueDate = Some(dateToString(taxPeriodDue))
        )

        val pointToPassIn: LSPDetails = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("05"))
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        actualResult shouldBe expectedResult
      }

      "have the penalty number when it DOES NOT exceeds the threshold" in {
        val sampleSummaryCardReturnSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate)))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = true,
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
          totalPenaltyAmount = 200,
          dueDate = Some(dateToString(taxPeriodDue))
        )

        val pointToPassIn: LSPDetails = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("01"), penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold))
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "show the appeal status when the point has been appealed - for under review" in {
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
        val result = helper.financialSummaryCard(sampleLateSubmissionPenaltyCharge.copy(appealInformation = Some(Seq(
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
              Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
            helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate)))
          ),
          Tag(content = Text("due"),
            classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = true,
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          totalPenaltyAmount = 200,
          multiplePenaltyPeriod = Some(Html(lspMultiplePenaltyPeriodMessage(dateToString(taxPeriodDue.plusMonths(1))))),
          dueDate = Some(dateToString(taxPeriodDue))
        )

        val actualResult = helper.financialSummaryCard(sampleLateSubmissionPenaltyChargeWithMultiplePeriods, monthlyThreshold)
        actualResult shouldBe expectedResult
      }

      "display return not yet received for penalty with unsubmitted return" in {
        val sampleSummaryCardReturnNotSubmitted: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
            ),
            helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "penalty-due-tag"),
          "1",
          "12345678901234",
          isReturnSubmitted = false,
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
          totalPenaltyAmount = 200,
          dueDate = Some(dateToString(taxPeriodDue))
        )

        val pointToPassIn: LSPDetails = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("01"), penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
          lateSubmissions = Some(
            Seq(
              LateSubmission(
                taxPeriodStartDate = Some(taxPeriodStart),
                taxPeriodEndDate = Some(taxPeriodEnd),
                taxPeriodDueDate = Some(taxPeriodDue),
                returnReceiptDate = None,
                taxReturnStatus = Some(TaxReturnStatusEnum.Open)
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
          val result = helper.populateLateSubmissionPenaltyCard(Seq(sampleLateSubmissionPointReturnSubmitted), quarterlyThreshold, quarterlyThreshold - 1)
          result shouldBe Seq(sampleLSPSummaryCardReturnSubmitted)
        }

        "user has removed points below active points - active points should be reindexed so that the points are logically numbered correctly" in {
          val sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPointv2: Seq[LSPDetails] = Seq(
            sampleLateSubmissionPoint.copy(penaltyOrder = Some("4")),
            sampleLateSubmissionPoint.copy(penaltyOrder = Some("3")),
            sampleLateSubmissionPoint.copy(penaltyOrder = Some("2")),
            sampleRemovedPenaltyPoint
          )
          val expectedResult: Seq[LateSubmissionPenaltySummaryCard] = Seq(LateSubmissionPenaltySummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
              ),
              helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
              helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate))),
              helper.summaryListRow(pointExpiration, Html(dateToString(expiryDate)))
            ),
            Tag(content = Text("active")),
            "3",
            "12345678901234",
            isReturnSubmitted = true,
            dueDate = Some(dateToString(taxPeriodDue)),
            penaltyCategory = Some(LSPPenaltyCategoryEnum.Point)
          ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(taxPeriodStart.plusMonths(1)), dateToString(taxPeriodEnd.plusMonths(1))))
                ),
                helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue.plusMonths(1)))),
                helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate.plusMonths(1)))),
                helper.summaryListRow(pointExpiration, Html(dateToString(expiryDate.plusMonths(1))))
              ),
              Tag(content = Text("active")),
              "2",
              "12345678901234",
              isReturnSubmitted = true,
              dueDate = Some(dateToString(taxPeriodDue.plusMonths(1))),
              penaltyCategory = Some(LSPPenaltyCategoryEnum.Point)
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(taxPeriodStart.plusMonths(2)), dateToString(taxPeriodStart.plusMonths(2))))
                ),
                helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue.plusMonths(2)))),
                helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate.plusMonths(2)))),
                helper.summaryListRow(pointExpiration, Html(dateToString(expiryDate.plusMonths(2))))
              ),
              Tag(content = Text("active")),
              "1",
              "12345678901234",
              isReturnSubmitted = true,
              dueDate = Some(dateToString(taxPeriodDue.plusMonths(2))),
              penaltyCategory = Some(LSPPenaltyCategoryEnum.Point)
            ),
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(taxPeriodStart.minusMonths(1)), dateToString(taxPeriodEnd.minusMonths(1))))
                ),
                helper.summaryListRow(reason, Html("reason"))
              ),
              Tag(content = Text("removed")),
              "",
              "12345678901234",
              isReturnSubmitted = true,
              isAddedOrRemovedPoint = true,
              dueDate = Some(dateToString(taxPeriodDue.minusMonths(1))),
              penaltyCategory = Some(LSPPenaltyCategoryEnum.Point)
            ))


          val result = helper.populateLateSubmissionPenaltyCard(sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPointv2,
            quarterlyThreshold, quarterlyThreshold - 1)
          result.head.penaltyPoint shouldBe expectedResult.head.penaltyPoint
          result(1).penaltyPoint shouldBe expectedResult(1).penaltyPoint
          result(2).penaltyPoint shouldBe expectedResult(2).penaltyPoint
          result(3).penaltyPoint shouldBe expectedResult(3).penaltyPoint
        }

        "treat a undefined LSPPenaltyCategoryEnum as a Point" in {
          val expectedResult = Seq(
            LateSubmissionPenaltySummaryCard(
              Seq(
                helper.summaryListRow(
                  period,
                  Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
                ),
                helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
                helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate))),
                helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(expiryDate)))
              ),
              Tag(content = Text("active")),
              "1",
              "0987654321",
              isReturnSubmitted = true,
              dueDate = Some(dateToString(taxPeriodDue)),
              penaltyCategory = None
            ))
          val result = helper.populateLateSubmissionPenaltyCard(Seq(sampleLateSubmissionPointReturnWithNoPenaltyCategory), quarterlyThreshold, quarterlyThreshold - 1)
          result.head shouldBe expectedResult.head
        }
      }

      "given a Late Payment penalty" when {

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Return 1st LPP (4703)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForVAT = Seq(samplePaidLPP1.copy(
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForVAT))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Central assessment 1st LPP (4723)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForCentralAssessment = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(firstLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("Central assessment of VAT", isCentralAssessment = true)))
          }
        }

        "return SummaryCards when given SecondLate Payment penalty and chargeType is VAT Central assessment 2nd LPP (4724)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val secondLatePaymentPenaltyForCentralAssessment = Seq(sampleLPP2.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(CentralAssessmentSecondLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForCentralAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Central assessment of VAT", isCentralAssessment = true)))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Error correction 1st LPP (4743)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForErrorCorrectionNotice = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ErrorCorrectionFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForErrorCorrectionNotice))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Error correction of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is VAT Officer's assessment 1st LPP (4741)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForOfficersAssessment = Seq(samplePaidLPP1.copy(
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForOfficersAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Officer’s assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is Additional assessment 1st LPP (4758)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForAdditionalAssessment = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AdditionalAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForAdditionalAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Additional assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is Protective assessment 1st LPP (4761)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForProtectiveAssessment = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(ProtectiveAssessmentFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForProtectiveAssessment))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Protective assessment of VAT")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is a POA Return Charge 1st LPP (4716)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForPOAReturnCharge = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(POAReturnChargeFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForPOAReturnCharge))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Payment on account instalment")))
          }
        }

        "return SummaryCards when given First Late Payment penalty and chargeType is AA Return charge 1st LPP (4718)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val firstLatePaymentPenaltyForAAReturnCharge = Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(AAReturnChargeFirstLPP), outstandingAmount = Some(1), timeToPay = None
              ),
              appealInformation = None,
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
              penaltyAmountOutstanding = None,
              penaltyAmountPaid = Some(1001.45),
              penaltyAmountPosted = 1001.45,
              penaltyAmountAccruing = 0,
              principalChargeLatestClearing = Some(lpp2PrincipleChargePaidDate),
              penaltyStatus = LPPPenaltyStatusEnum.Posted
            ))
            val result = helper.populateLatePaymentPenaltyCard(Some(secondLatePaymentPenaltyForAAReturnCharge))
            result shouldBe Some(Seq(sampleLPPAdditionalSummaryCardPenaltyPaid("Annual accounting balance")))
          }
        }

        "return SummaryCards when given a Manual LPP (4787)" when {
          "populateLatePaymentPenaltyCard is called" in {
            val result = helper.populateLatePaymentPenaltyCard(Some(Seq(sampleManualLPP)))
            result shouldBe Some(Seq(sampleManualLPPSummaryCard))
          }
        }

        "return SummaryCards with VAT payment date in LPP " when {
          "populateLatePaymentPenalty for is called" in {
            val result = helper.populateLatePaymentPenaltyCard(Some(Seq(samplePaidLPP1.copy(
              LPPDetailsMetadata = LPPDetailsMetadata(
                mainTransaction = Some(VATReturnFirstLPP), outstandingAmount = Some(1), timeToPay = None
              )
            ))))
            result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyPaid("VAT")))
          }
        }

        "set the isVatPaid boolean to false when the VAT is unpaid" in {
          val periodText = getPeriodText("VAT")
          val sampleLPPSummaryCardPenaltyUnpaidVAT: LatePaymentPenaltySummaryCard = sampleLPPSummaryCardPenaltyPaid("VAT").copy(
            cardRows =
              Seq(
                helper.summaryListRow(
                  penaltyType,
                  Html("First penalty for late payment")
                ),
                helper.summaryListRow(
                  overdueCharge,
                  Html(periodText("VAT", dateToString(principleChargeBillingStartDate), dateToString(principleChargeBillingEndDate)))
                ),
                helper.summaryListRow(chargeDue, Html(dateToString(principleChargeBillingDueDate))),
                helper.summaryListRow(datePaid, Html(paymentNotReceived)),
                SummaryListRow()
              ),
            isPenaltyPaid = false,
            isVatPaid = false,
            status = Tag(Text(estimate)))

          val result = helper.populateLatePaymentPenaltyCard(
            Some(
              Seq(sampleUnpaidLPP1.copy(
                appealInformation = None,
                penaltyAmountOutstanding = None,
                penaltyAmountAccruing = 1001.45,
                penaltyAmountPosted = 0,
                penaltyAmountPaid = None
              ))
            )
          )
          result shouldBe Some(Seq(sampleLPPSummaryCardPenaltyUnpaidVAT))
        }
      }
    }
  }

  "return Seq[SummaryListRow] when give a PenaltyPoint" when {
    "pointCardBody is called" when {
      "given a PenaltyPoint and the threshold has not been met and the Return has been submitted" in {
        val result = helper.pointCardBody(sampleLateSubmissionPoint, thresholdMet = false)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
          helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate))),
          helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(expiryDate)))
        )
      }

      "given a PenaltyPoint and the threshold has not been met and the Return has not been submitted" in {
        val result = helper.pointCardBody(samplePenaltyPointNotSubmitted, thresholdMet = false)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
          helper.summaryListRow(returnSubmitted, Html(notSubmitted)),
          helper.summaryListRow(pointExpiration, Html(dateToMonthYearString(expiryDate)))
        )
      }

      "given a PenaltyPoint and the threshold has been met and the Return has been submitted" in {
        val result = helper.pointCardBody(sampleLateSubmissionPoint, thresholdMet = true)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
          helper.summaryListRow(returnSubmitted, Html(dateToString(receiptDate)))
        )
      }

      "given a PenaltyPoint and the threshold has been met and the Return has not been submitted" in {
        val result = helper.pointCardBody(samplePenaltyPointNotSubmitted, thresholdMet = true)
        result shouldBe Seq(
          helper.summaryListRow(
            period,
            Html(vatPeriodValue(dateToString(taxPeriodStart), dateToString(taxPeriodEnd)))
          ),
          helper.summaryListRow(returnDue, Html(dateToString(taxPeriodDue))),
          helper.summaryListRow(returnSubmitted, Html(notSubmitted))
        )
      }
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
        val result = helper.tagStatus(Some(sampleLateSubmissionPenaltyCharge), None)
        result shouldBe Tag(
          content = Text(overdueTag),
          classes = "penalty-due-tag"
        )
      }

      "an active penalty point is provided" in {
        val result = helper.tagStatus(Some(sampleLateSubmissionPoint.copy(chargeAmount = None)), None)
        result shouldBe Tag(
          content = Text(activeTag)
        )
      }

      "a penalty is submitted but the appeal is rejected - return the appropriate tag" in {
        val result = helper.tagStatus(Some(sampleLateSubmissionPoint.copy(penaltyStatus = LSPPenaltyStatusEnum.Active,
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
        val result = helper.tagStatus(Some(sampleLateSubmissionPenaltyCharge.copy(chargeOutstandingAmount = Some(0))), None)
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
        val result = helper.tagStatus(None, Some(sampleUnpaidLPP1))
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
      val result = helper.lppSummaryCard(sampleUnpaidLPP1)
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
      result.cardRows.exists(_.key.content == Text("VAT due")) shouldBe true
    }

    "when given a Manual LPP - set the relevant fields" in {
      val result = helper.lppSummaryCard(sampleManualLPP)
      result.penaltyCategory.equals(MANUAL) shouldBe true
      result.cardRows.exists(_.key.content == Text("Added on")) shouldBe true
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

      "render a paid tag - when there is no financial data for the penalty" in {
        val penaltyAmountPaid = BigDecimal(00.00)

        val result = helper.showDueOrPartiallyPaidDueTag(None, penaltyAmountPaid)
        result shouldBe Tag(
          content = Text("paid"),
          classes = "penalty-paid-tag"
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
