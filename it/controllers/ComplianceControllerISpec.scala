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

package controllers

import config.AppConfig
import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub.{agentAuthorised, unauthorised}
import stubs.ComplianceStub.complianceDataStub
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.SessionKeys

import java.time.LocalDate

class ComplianceControllerISpec extends IntegrationSpecCommonBase {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  val controller: ComplianceController = injector.instanceOf[ComplianceController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    SessionKeys.pocAchievementDate -> "2024-01-01",
    SessionKeys.regimeThreshold -> "5",
    authToken -> "1234"
  )
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    SessionKeys.agentSessionVrn -> "123456789",
    SessionKeys.pocAchievementDate -> "2022-09-01",
    SessionKeys.regimeThreshold -> "5",
    authToken -> "1234"
  )

  val compliancePayloadWithMissingReturns: CompliancePayload = CompliancePayload(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2021, 12, 1),
        inboundCorrespondenceToDate = LocalDate.of(2021, 12, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 2, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 2, 2)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 3, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 4, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 3, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 3, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 5, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 6, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 5, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 5, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 6, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 8, 7),
        periodKey = "#001"
      )
    )
  )

  val compliancePayloadWithNoMissingReturns: CompliancePayload = CompliancePayload(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 1, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 1, 31),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 3, 2)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 3, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 2, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 2, 28),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 4, 5)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 4, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 3, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 3, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 5, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 6, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 5, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 5, 31),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 7),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
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
        complianceDataStub()
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
      }

      "there is missing returns - show a late tag next to those that are missing" in {
        setFeatureDate(Some(LocalDate.of(2022, 5, 8)))
        complianceDataStub(Some(compliancePayloadWithMissingReturns))
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("VAT period 1&nbsp;March&nbsp;2022 to 31&nbsp;March&nbsp;2022") shouldBe true
        parsedBody.select(".govuk-tag--red").get(0).text() shouldBe "Late"
        parsedBody.body().toString.contains("Submit this missing VAT Return now") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;April&nbsp;2022 to 30&nbsp;April&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;June&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;May&nbsp;2022 to 31&nbsp;May&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;July&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;June&nbsp;2022 to 30&nbsp;June&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;August&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Points to be removed:") shouldBe true
        parsedBody.body().toString.contains("September&nbsp;2022") shouldBe true
      }

      "there is no missing returns - do not show a late tag" in {
        setFeatureDate(Some(LocalDate.of(2022, 3, 6)))
        complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("VAT period 1&nbsp;March&nbsp;2022 to 31&nbsp;March&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;May&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;April&nbsp;2022 to 30&nbsp;April&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;June&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;May&nbsp;2022 to 31&nbsp;May&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;July&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("VAT period 1&nbsp;June&nbsp;2022 to 30&nbsp;June&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Submit VAT Return by 7&nbsp;August&nbsp;2022") shouldBe true
        parsedBody.body().toString.contains("Points to be removed:") shouldBe true
        parsedBody.body().toString.contains("September&nbsp;2022") shouldBe true
      }

      "for a monthly filer" in {
        complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val request = controller.onPageLoad()(fakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("If you miss a return deadline, you will have to submit 6 more returns on time before we can remove your points.") shouldBe true
      }

      "for a quarterly filer" in {
        complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val quarterlyFilerFakeRequest = fakeRequest.withSession(SessionKeys.regimeThreshold -> "4")
        val request = controller.onPageLoad()(quarterlyFilerFakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("If you miss a return deadline, you will have to submit 4 more returns on time before we can remove your points.") shouldBe true
      }

      "for a annual filer" in {
        complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val annualFilerFakeRequest = fakeRequest.withSession(SessionKeys.regimeThreshold -> "2")
        val request = controller.onPageLoad()(annualFilerFakeRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("If you miss a return deadline, you will have to submit 2 more returns on time before we can remove your points.") shouldBe true
      }

      "an agent is present" in {
        agentAuthorised()
        complianceDataStub(Some(compliancePayloadWithNoMissingReturns))
        val request = controller.onPageLoad()(fakeAgentRequest)
        status(request) shouldBe OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.body().toString.contains("Points to be removed:") shouldBe true
        parsedBody.body().toString.contains("January&nbsp;2024") shouldBe true
        parsedBody.body().toString.contains("If your client misses a return deadline, they will have to submit 6 more returns on time before we can remove their points.") shouldBe true
      }
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/compliance").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
