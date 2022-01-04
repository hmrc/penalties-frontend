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
import models.User
import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}
import org.jsoup.Jsoup
import utils.SessionKeys

import java.time.{LocalDate, LocalDateTime}

class CompliancePageHelperSpec extends SpecBase {
  val sampleDate1: LocalDateTime = LocalDateTime.of(2022, 1, 1, 1, 1, 1)
  val sampleDate2: LocalDateTime = LocalDateTime.of(2022, 1, 31, 0, 0, 0)

  val pageHelper: CompliancePageHelper = injector.instanceOf[CompliancePageHelper]

  implicit val user: User[_] = User("123456789")(fakeRequest.withSession(
    SessionKeys.latestLSPCreationDate -> "2020-01-01T01:00:00.000Z",
    SessionKeys.pointsThreshold -> "5"
  ))

  "getUnsubmittedReturnContentFromSequence" should {
    "return an empty html element when there is no missing returns" in {
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(Seq.empty)
      result.body.isEmpty shouldBe true
    }

    "return bullets for one missing period" in {
      val singleMissingReturn: Seq[ObligationDetail] = Seq(
        ObligationDetail(
          status = ComplianceStatusEnum.open,
          inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
          inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
          inboundCorrespondenceDateReceived = None,
          inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
          periodKey = "#001"
        )
      )
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(singleMissingReturn)
      val parsedResult = Jsoup.parse(result.body)
      parsedResult.select("li").text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
    }

    "return bullets for multiple missing periods" in {
      val multipleMissingReturns: Seq[ObligationDetail] = Seq(
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
          inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
          inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
          inboundCorrespondenceDateReceived = None,
          inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
          periodKey = "#001"
        )
      )
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(multipleMissingReturns)
      val parsedResult = Jsoup.parse(result.body)
      parsedResult.select("li:nth-child(1)").text() shouldBe "VAT period 1 February 2022 to 28 February 2022"
      parsedResult.select("li:nth-child(2)").text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
    }
  }

  "findMissingReturns" should {
    "find only the obligations that are open and before the LSP creation date" in {
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
            inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
            inboundCorrespondenceDateReceived = None,
            inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
            periodKey = "#001"
          ),
          ObligationDetail(
            status = ComplianceStatusEnum.fulfilled,
            inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
            inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
            inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 3, 29)),
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
          )
        )
      )
      val latestLSPCreationDate = LocalDate.of(2022, 5, 8)
      val result = pageHelper.findMissingReturns(compliancePayload, latestLSPCreationDate)
      result.size shouldBe 1
      result.head shouldBe ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 3, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 3, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 5, 7),
        periodKey = "#001"
      )
    }
  }
}
