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

package controllers

import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.{AuthStub, ComplianceStub}
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

import java.time.LocalDate

class ComplianceControllerISpec extends IntegrationSpecCommonBase {

  val controller: ComplianceController = injector.instanceOf[ComplianceController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    SessionKeys.latestLSPCreationDate -> "2022-03-01T12:00:00.000",
    SessionKeys.pointsThreshold -> "5",
    authToken -> "1234"
  )
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    SessionKeys.latestLSPCreationDate -> "2022-03-01T12:00:00.000",
    SessionKeys.pointsThreshold -> "5",
    authToken -> "1234"
  )

  val compliancePayloadWithMissingReturns: CompliancePayload = CompliancePayload(
    identification = ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    ),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(2021, 12, 1),
        inboundCorrespondenceToDate = LocalDate.of(2021, 12, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 2, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 2, 2)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 3, 29)),
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

  val compliancePayloadWithNoMissingReturns: CompliancePayload = CompliancePayload(
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
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 3, 2)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 4, 5)),
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

  "GET /compliance" should {

    "return 200" when {
      "the service call succeeds to get compliance data" in {
        ComplianceStub.complianceDataStub()
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
      }

      "there is missing returns - show the 'missing returns' content" in {
        ComplianceStub.complianceDataStub(Some(compliancePayloadWithMissingReturns))
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#submit-these-missing-returns").text shouldBe "Submit these missing returns"
        parsedBody.body().toString.contains("VAT period 1 December 2021 to 31 December 2021") shouldBe true
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT period 1 January 2022 to 31 January 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 March 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 February 2022 to 28 February 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 April 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 March 2022 to 31 March 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 May 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 April 2022 to 30 April 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 June 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 May 2022 to 31 May 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 July 2022") shouldBe true
      }

      "there is no missing returns - do not show the 'missing returns' content" in {
        ComplianceStub.complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#submit-these-missing-returns").text.isEmpty shouldBe true
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT period 1 January 2022 to 31 January 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 March 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 February 2022 to 28 February 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 April 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 March 2022 to 31 March 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 May 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 April 2022 to 30 April 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 June 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 May 2022 to 31 May 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 July 2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1 June 2022 to 30 June 2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 August 2022") shouldBe true
      }

      "an agent is present" in {
        AuthStub.agentAuthorised()
        ComplianceStub.complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val request = controller.onPageLoad()(fakeAgentRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("Your client needs to take action to bring their VAT account up to date.") shouldBe true
        parsedBody.body().toString.contains("allow HMRC to remove all your client’s penalty points") shouldBe true
        parsedBody.body().toString.contains("help your client to stop paying late submission financial penalties") shouldBe true
      }
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/compliance").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
