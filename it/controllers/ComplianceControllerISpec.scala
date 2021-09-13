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

package controllers

import java.time.LocalDateTime

import models.compliance.{CompliancePayload, MissingReturn, Return, ReturnStatusEnum}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.ComplianceStub._
import testUtils.IntegrationSpecCommonBase
import utils.SessionKeys

class ComplianceControllerISpec extends IntegrationSpecCommonBase {
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleDate2: LocalDateTime = LocalDateTime.of(2021, 2, 1, 1, 1, 1)
  val sampleDate3: LocalDateTime = LocalDateTime.of(2021, 2, 28, 1, 1, 1)
  val sampleDate4: LocalDateTime = LocalDateTime.of(2021, 4, 1, 1, 1, 1)
  val sampleDate5: LocalDateTime = LocalDateTime.of(2021, 1, 31, 1, 1, 1)

  val controller: ComplianceController = injector.instanceOf[ComplianceController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(SessionKeys.agentSessionVrn -> "123456789")

  val compliancePayloadWithMissingReturns: CompliancePayload = CompliancePayload(
    noOfMissingReturns = "1",
    noOfSubmissionsReqForCompliance = "1",
    expiryDateOfAllPenaltyPoints = sampleDate1.plusMonths(1).plusYears(2),
    missingReturns = Seq(
      MissingReturn(
        startDate = sampleDate1,
        endDate = sampleDate5
      )
    ),
    returns = Seq(
      Return(
        startDate = sampleDate2,
        endDate = sampleDate3,
        dueDate = sampleDate3.plusMonths(1).plusDays(7),
        status = Some(ReturnStatusEnum.Submitted)
      )
    )
  )

  val compliancePayloadWithNoMissingReturns: CompliancePayload = CompliancePayload(
    noOfMissingReturns = "1",
    noOfSubmissionsReqForCompliance = "1",
    expiryDateOfAllPenaltyPoints = sampleDate1.plusMonths(1).plusYears(2),
    missingReturns = Seq.empty,
    returns = Seq(
      Return(
        startDate = sampleDate1,
        endDate = sampleDate5,
        dueDate = sampleDate5.plusMonths(1).plusDays(7),
        status = None
      )
    )
  )

  "GET /compliance" should {

    "return 200" when {
      "the service call succeeds to get previous and future compliance data" in {
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
      }

      "there is missing returns - show the 'missing returns' content" in {
        returnComplianceDataStub(compliancePayloadWithMissingReturns)
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#submit-these-missing-returns").text shouldBe "Submit these missing returns"
        parsedBody.body().toString.contains("VAT period 1 January 2021 to 31 January 2021") shouldBe true
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT period 1 February 2021 to 28 February 2021") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 4 April 2021") shouldBe true
        parsedBody.body().toString.contains("Submitted on time") shouldBe true
      }

      "there is no missing returns - do not show the 'missing returns' content" in {
        returnComplianceDataStub(compliancePayloadWithNoMissingReturns)
        val request = await(buildClientForRequestToApp(uri = "/compliance").get())
        request.status shouldBe OK
        val parsedBody = Jsoup.parse(request.body)
        parsedBody.select("#submit-these-missing-returns").text.isEmpty shouldBe true
        parsedBody.body().toString.contains("VAT period 1 February 2021 to 28 February 2021") shouldBe false
        parsedBody.select("#complete-these-actions-on-time").text shouldBe "Complete these actions on time"
        parsedBody.body().toString.contains("VAT period 1 January 2021 to 31 January 2021") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7 March 2021") shouldBe true
        parsedBody.body().toString.contains("Submitted on time") shouldBe false
      }

      "an agent is present" in {
        AuthStub.agentAuthorised()
        returnComplianceDataStub(compliancePayloadWithNoMissingReturns)
        val request = controller.onPageLoad()(fakeAgentRequest)
        await(request).header.status shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("Your client needs to take action to bring their VAT account up to date.") shouldBe true
        parsedBody.body().toString.contains("allow HMRC to remove all your client’s penalty points") shouldBe true
        parsedBody.body().toString.contains("help your client to stop paying late submission financial penalties") shouldBe true
        parsedBody.body().toString.contains("If these actions are completed we will remove your client’s points in February 2023.") shouldBe true
      }
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/compliance").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
