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

package connectors

import config.AppConfig
import connectors.httpParsers.ComplianceDataParser._
import connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import models.User
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.ComplianceStub._
import stubs.PenaltiesStub._
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.HeaderCarrier

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit val vatTraderUser: User[_] = User("1234", active = true, None)(FakeRequest())

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  "getPenaltyDetails" should {
    "generate a valid PenaltyDetails model when valid JSON is returned" in {
      getPenaltyDetailsStub()
      val result = await(connector.getPenaltyDetails(vrn))
      result shouldBe Right(samplePenaltyDetails)
    }

    s"return $BAD_REQUEST (Bad Request) when invalid JSON is returned" in {
      getPenaltyDetailsStub(sampleInvalidPenaltyDetailsJson)
      val result = await(connector.getPenaltyDetails(vrn))
      result.isLeft shouldBe true
      result shouldBe Left(InvalidJson)
    }

    "throw an exception when an upstream error is returned from penalties" in {
      penaltyDetailsUpstreamErrorStub()
      val result = await(connector.getPenaltyDetails(vrn))
      result.isLeft shouldBe true
      result shouldBe Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))
    }
  }

  "getObligationData" should {
    "generate a CompliancePayload when valid JSON is returned from penalties" in {
      complianceDataStub()
      val result: CompliancePayloadResponse = await(connector.getObligationData("123456789", startDate, endDate))
      result.isRight shouldBe true
      result.toOption.get.model shouldBe sampleCompliancePayload
    }

    s"return a $CompliancePayloadMalformed when the data is malformed" in {
      invalidComplianceDataStub()
      val result: CompliancePayloadResponse = await(connector.getObligationData("123456789", startDate, endDate))
      result.isLeft shouldBe true
      result.left.getOrElse(false) shouldBe CompliancePayloadMalformed
    }

    s"return a $CompliancePayloadNoData when the response status is Not Found (${Status.NOT_FOUND})" in {
      errorStatusStub(Status.NOT_FOUND)
      val result: CompliancePayloadResponse = await(connector.getObligationData("123456789", startDate, endDate))
      result.isLeft shouldBe true
      result.left.getOrElse(false) shouldBe CompliancePayloadNoData
    }

    s"return a $CompliancePayloadFailureResponse when the response status is ISE (${Status.INTERNAL_SERVER_ERROR})" in {
      errorStatusStub(Status.INTERNAL_SERVER_ERROR)
      val result: CompliancePayloadResponse = await(connector.getObligationData("123456789", startDate, endDate))
      result.isLeft shouldBe true
      result.left.getOrElse(false) shouldBe CompliancePayloadFailureResponse(Status.INTERNAL_SERVER_ERROR)
    }

    s"return a $CompliancePayloadFailureResponse when the response status is unmatched i.e. Service Unavailable (${Status.SERVICE_UNAVAILABLE})" in {
      errorStatusStub(Status.SERVICE_UNAVAILABLE)
      val result: CompliancePayloadResponse = await(connector.getObligationData("123456789", startDate, endDate))
      result.isLeft shouldBe true
      result.left.getOrElse(false) shouldBe CompliancePayloadFailureResponse(Status.SERVICE_UNAVAILABLE)
    }
  }
}
