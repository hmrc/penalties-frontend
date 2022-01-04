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
import models.FilingFrequencyEnum
import models.compliance._
import org.jsoup.Jsoup
import utils.ImplicitDateFormatter

import java.time.{LocalDate, LocalDateTime}

class TimelineHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper: TimelineHelper = injector.instanceOf[TimelineHelper]

  val sampleExpiryDate: LocalDateTime = LocalDateTime.of(2023,3,1,1,1,1)

  val latestLSPCreationDateQuarterly: LocalDate = LocalDate.of(2022, 3, 8)

  val latestLSPCreationDateMonthly: LocalDate = LocalDate.of(2022, 1, 1)

  val compliancePayload: CompliancePayload = CompliancePayload(
    identification = ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    ),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 3, 31),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 2, 3)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 5, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 8, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 7, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 9, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 11, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 10, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 12, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2023, 2, 7),
        periodKey = "#001"
      )
    )
  )

  val compliancePayloadMonthly: CompliancePayload = CompliancePayload(
    identification = ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    ),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 2, 3)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 4, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 3, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 3, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 5, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 6, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 5, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 5, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 6, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 8, 7),
        periodKey = "#001"
      )
    )
  )

  val complianceDataQuarterlyWith24MthsHistory: ComplianceData = ComplianceData(
    compliancePayload,
    amountOfSubmissionsRequiredFor24MthsHistory = None,
    filingFrequency = FilingFrequencyEnum.quarterly
  )

  val complianceDataMonthlyWithout24MthsHistory: ComplianceData = ComplianceData(
    compliancePayloadMonthly,
    amountOfSubmissionsRequiredFor24MthsHistory = Some(3),
    filingFrequency = FilingFrequencyEnum.monthly
  )


  "TimelineHelper" when {
    "getTimelineContent is called and given compliance returns" should {
      "when the user is a VAT trader" must {
        "return the timeline component with VAT trader content wrapped in html" in {
          val result = helper.getTimelineContent(complianceDataQuarterlyWith24MthsHistory, latestLSPCreationDateQuarterly)(implicitly, vatTraderUser)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("ol").attr("class") shouldBe "hmrc-timeline"
          parsedHtmlResult.select("h2").get(0).text() shouldBe "VAT period 1 January 2022 to 31 March 2022"
          parsedHtmlResult.select("span").get(0).text() shouldBe "Submit VAT Return by 7 May 2022"
          parsedHtmlResult.select("strong").text shouldBe "Submitted on time"

          parsedHtmlResult.select("h2").get(1).text() shouldBe "VAT period 1 April 2022 to 30 June 2022"
          parsedHtmlResult.select("span").get(1).text() shouldBe "Submit VAT Return by 7 August 2022"

          parsedHtmlResult.select("h2").get(2).text() shouldBe "VAT period 1 July 2022 to 30 September 2022"
          parsedHtmlResult.select("span").get(2).text() shouldBe "Submit VAT Return by 7 November 2022"

          parsedHtmlResult.select("h2").get(3).text() shouldBe "VAT period 1 October 2022 to 31 December 2022"
          parsedHtmlResult.select("span").get(3).text() shouldBe "Submit VAT Return by 7 February 2023"

          parsedHtmlResult.select("#point-expiry-date").text shouldBe "If you complete these actions we will remove your points in March 2023."
        }

        "return the timeline component - adding the months required to meet 24mths filing history" in {
          val result = helper.getTimelineContent(complianceDataMonthlyWithout24MthsHistory, latestLSPCreationDateMonthly)(implicitly, vatTraderUser)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("ol").attr("class") shouldBe "hmrc-timeline"
          parsedHtmlResult.select("h2").get(0).text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
          parsedHtmlResult.select("span").get(0).text() shouldBe "Submit VAT Return by 7 March 2022"
          parsedHtmlResult.select("strong").get(0).text shouldBe "Submitted on time"

          parsedHtmlResult.select("h2").get(1).text() shouldBe "VAT period 1 February 2022 to 28 February 2022"
          parsedHtmlResult.select("span").get(1).text() shouldBe "Submit VAT Return by 7 April 2022"

          parsedHtmlResult.select("h2").get(2).text() shouldBe "VAT period 1 March 2022 to 31 March 2022"
          parsedHtmlResult.select("span").get(2).text() shouldBe "Submit VAT Return by 7 May 2022"

          parsedHtmlResult.select("h2").get(3).text() shouldBe "VAT period 1 April 2022 to 30 April 2022"
          parsedHtmlResult.select("span").get(3).text() shouldBe "Submit VAT Return by 7 June 2022"

          parsedHtmlResult.select("h2").get(4).text() shouldBe "VAT period 1 May 2022 to 31 May 2022"
          parsedHtmlResult.select("span").get(4).text() shouldBe "Submit VAT Return by 7 July 2022"

          parsedHtmlResult.select("h2").get(5).text() shouldBe "VAT period 1 June 2022 to 30 June 2022"
          parsedHtmlResult.select("span").get(5).text() shouldBe "Submit VAT Return by 7 August 2022"

          parsedHtmlResult.select("#point-expiry-date").text shouldBe "If you complete these actions we will remove your points in October 2022."
        }
      }

      "when the user is an agent" must {
        "return the timeline component with Agent content wrapped in html" in {
          val result = helper.getTimelineContent(complianceDataQuarterlyWith24MthsHistory, latestLSPCreationDateQuarterly)(implicitly, agentUser)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("ol").attr("class") shouldBe "hmrc-timeline"
          parsedHtmlResult.select("h2").get(0).text() shouldBe "VAT period 1 January 2022 to 31 March 2022"
          parsedHtmlResult.select("span").get(0).text() shouldBe "Submit VAT Return by 7 May 2022"
          parsedHtmlResult.select("strong").text shouldBe "Submitted on time"

          parsedHtmlResult.select("h2").get(1).text() shouldBe "VAT period 1 April 2022 to 30 June 2022"
          parsedHtmlResult.select("span").get(1).text() shouldBe "Submit VAT Return by 7 August 2022"

          parsedHtmlResult.select("h2").get(2).text() shouldBe "VAT period 1 July 2022 to 30 September 2022"
          parsedHtmlResult.select("span").get(2).text() shouldBe "Submit VAT Return by 7 November 2022"

          parsedHtmlResult.select("h2").get(3).text() shouldBe "VAT period 1 October 2022 to 31 December 2022"
          parsedHtmlResult.select("span").get(3).text() shouldBe "Submit VAT Return by 7 February 2023"

          parsedHtmlResult.select("#point-expiry-date").text shouldBe "If these actions are completed we will remove your client’s points in March 2023."
        }

        "return the timeline component - adding the months required to meet 24mths filing history" in {
          val result = helper.getTimelineContent(complianceDataMonthlyWithout24MthsHistory, latestLSPCreationDateMonthly)(implicitly, agentUser)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("ol").attr("class") shouldBe "hmrc-timeline"
          parsedHtmlResult.select("h2").get(0).text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
          parsedHtmlResult.select("span").get(0).text() shouldBe "Submit VAT Return by 7 March 2022"
          parsedHtmlResult.select("strong").get(0).text shouldBe "Submitted on time"

          parsedHtmlResult.select("h2").get(1).text() shouldBe "VAT period 1 February 2022 to 28 February 2022"
          parsedHtmlResult.select("span").get(1).text() shouldBe "Submit VAT Return by 7 April 2022"

          parsedHtmlResult.select("h2").get(2).text() shouldBe "VAT period 1 March 2022 to 31 March 2022"
          parsedHtmlResult.select("span").get(2).text() shouldBe "Submit VAT Return by 7 May 2022"

          parsedHtmlResult.select("h2").get(3).text() shouldBe "VAT period 1 April 2022 to 30 April 2022"
          parsedHtmlResult.select("span").get(3).text() shouldBe "Submit VAT Return by 7 June 2022"

          parsedHtmlResult.select("h2").get(4).text() shouldBe "VAT period 1 May 2022 to 31 May 2022"
          parsedHtmlResult.select("span").get(4).text() shouldBe "Submit VAT Return by 7 July 2022"

          parsedHtmlResult.select("h2").get(5).text() shouldBe "VAT period 1 June 2022 to 30 June 2022"
          parsedHtmlResult.select("span").get(5).text() shouldBe "Submit VAT Return by 7 August 2022"

          parsedHtmlResult.select("#point-expiry-date").text shouldBe "If these actions are completed we will remove your client’s points in October 2022."
        }
      }
    }

    "getReturnsAfterLSPCreationDate" should {
      "return all obligation details after the latest LSP creation date" in {
        val result = helper.getReturnsAfterLSPCreationDate(compliancePayloadMonthly, LocalDate.of(2022, 3, 8))
        result.size shouldBe 5
        result shouldBe compliancePayloadMonthly.obligationDetails.drop(1)
      }
    }

    "getExpiryDateForPenalties" should {
      val monthlyFilerComplianceDataLessThan24MthsHistory = ComplianceData(
        compliancePayload = sampleCompliancePayload,
        amountOfSubmissionsRequiredFor24MthsHistory = Some(3),
        filingFrequency = FilingFrequencyEnum.monthly
      )
      val monthlyFilerComplianceData = ComplianceData(
        compliancePayload = sampleCompliancePayload,
        amountOfSubmissionsRequiredFor24MthsHistory = None,
        filingFrequency = FilingFrequencyEnum.monthly
      )
      val quarterlyFilerComplianceData = ComplianceData(
        compliancePayload = sampleCompliancePayload,
        amountOfSubmissionsRequiredFor24MthsHistory = None,
        filingFrequency = FilingFrequencyEnum.quarterly
      )
      val annualFilerComplianceData = ComplianceData(
        compliancePayload = sampleCompliancePayload,
        amountOfSubmissionsRequiredFor24MthsHistory = None,
        filingFrequency = FilingFrequencyEnum.annually
      )
      "return 6 months plus the amount required to get to 24mths filing history for monthly filer (with less than 24mths filing history)" in {
        val result = timelineHelper.getExpiryDateForPenalties(monthlyFilerComplianceDataLessThan24MthsHistory, LocalDate.of(2020, 1, 1))
        result shouldBe LocalDate.of(2020, 10, 1)
      }

      "return 6 months for monthly filer (with more than 24mths filing history)" in {
        val result = timelineHelper.getExpiryDateForPenalties(monthlyFilerComplianceData, LocalDate.of(2020, 1, 1))
        result shouldBe LocalDate.of(2020, 7, 1)
      }

      "return 12 months for quarterly filer" in {
        val result = timelineHelper.getExpiryDateForPenalties(quarterlyFilerComplianceData, LocalDate.of(2020, 1, 1))
        result shouldBe LocalDate.of(2021, 1, 1)
      }

      "return 24 months for annual filer" in {
        val result = timelineHelper.getExpiryDateForPenalties(annualFilerComplianceData, LocalDate.of(2020, 1, 1))
        result shouldBe LocalDate.of(2022, 1, 1)
      }
    }
  }
}
