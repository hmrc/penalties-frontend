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

package viewmodels

import java.time.LocalDateTime
import assets.messages.IndexMessages._
import base.SpecBase
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import utils.ImplicitDateFormatter

class SummaryCardHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  val sampleSummaryCardReturnSubmitted: SummaryCard = SummaryCard(
    Seq(
      helper.summaryListRow(
        period,
        Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
      ),
      helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
      helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
    ),
    Tag(content = Text("active"), classes = "govuk-tag "),
    "1",
    "123456789"
  )

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
        val sampleSummaryCardReturnSubmitted: SummaryCard = SummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "govuk-tag penalty-due-tag"),
          "",
          "123456789",
          isFinancialPoint = true,
          amountDue = 200.0
        )

        val pointToPassIn: PenaltyPoint = sampleFinancialPenaltyPoint.copy(number = "5")
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }

      "have the penalty number when it DOES NOT exceeds the threshold" in {
        val sampleSummaryCardReturnSubmitted: SummaryCard = SummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(returnSubmitted, Html(notSubmitted))
          ),
          Tag(content = Text("due"), classes = "govuk-tag penalty-due-tag"),
          "1",
          "123456789",
          isFinancialPoint = true,
          amountDue = 200.0
        )

        val pointToPassIn: PenaltyPoint = sampleFinancialPenaltyPoint.copy(number = "1")
        val actualResult = helper.financialSummaryCard(pointToPassIn, quarterlyThreshold)
        val expectedResult = sampleSummaryCardReturnSubmitted
        actualResult shouldBe expectedResult
      }
    }

    "return SummaryCards when given Penalty point" when {
      "populateCard is called" in {
        val result = helper.populateCard(sampleReturnSubmittedPenaltyPointData, quarterlyThreshold)
        result shouldBe Seq(sampleSummaryCardReturnSubmitted)
      }

      "user has removed points below active points - active points should be reindexed so that the points are logically numbered correctly" in {
        val expectedResult: Seq[SummaryCard] = Seq(SummaryCard(
          Seq(
            helper.summaryListRow(
              period,
              Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
            ),
            helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
            helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
          ),
          Tag(content = Text("active"), classes = "govuk-tag "),
          "3",
          "123456789"
        ),
          SummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
              ),
              helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
            ),
            Tag(content = Text("active"), classes = "govuk-tag "),
            "2",
            "123456789"
          ),
          SummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
              ),
              helper.summaryListRow(returnDue, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(returnSubmitted, Html(dateTimeToString(LocalDateTime.now))),
              helper.summaryListRow(pointExpiration, Html(dateTimeToMonthYearString(LocalDateTime.now)))
            ),
            Tag(content = Text("active"), classes = "govuk-tag "),
            "1",
            "123456789"
          ),
          SummaryCard(
            Seq(
              helper.summaryListRow(
                period,
                Html(vatPeriodValue(dateTimeToString(LocalDateTime.now), dateTimeToString(LocalDateTime.now)))
              ),
              helper.summaryListRow(reason, Html("reason"))
            ),
            Tag(content = Text("removed"), classes = "govuk-tag "),
            "",
            "123456789",
            isAdjustedPoint = true
          ))


        val result = helper.populateCard(sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPoint, quarterlyThreshold)
        result shouldBe expectedResult
      }
    }

    "return Seq[SummaryListRow] when give a PenaltyPoint" when {
      "returnSubmittedCardBody is called" in {
        val result = helper.returnSubmittedCardBody(samplePenaltyPoint)
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
          content = Text("test"),
          classes = s"govuk-tag "
        )
      }

      "tagStatus is called" when {
        "an overdue penaltyPointSubmission is provided" in {
          val result = helper.tagStatus(sampleOverduePenaltyPoint)
          result shouldBe Tag(
            content = Text(overdueTag),
            classes = "govuk-tag penalty-due-tag"
          )
        }

        "an active penalty point is provided" in {
          val result = helper.tagStatus(samplePenaltyPoint)
          result shouldBe Tag(
            content = Text(activeTag),
            classes = "govuk-tag "
          )
        }

        "a penalty is submitted but the appeal is rejected - return the appropriate tag" in {
          val result = helper.tagStatus(samplePenaltyPoint.copy(status = PointStatusEnum.Rejected))
          result shouldBe Tag(
            content = Text(rejectedTag),
            classes = "govuk-tag "
          )
        }

        "a financial penalty has been added and the user has paid" in {
          val result = helper.tagStatus(samplePenaltyPoint.copy(status = PointStatusEnum.Paid))
          result shouldBe Tag(
            content = Text(paidTag),
            classes = "govuk-tag "
          )
        }
      }
    }
  }
}
