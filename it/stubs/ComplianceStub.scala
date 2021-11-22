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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}
import play.api.http.Status
import play.api.libs.json.Json

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

object ComplianceStub {
  val getComplianceDataUrl: String = s"/penalties/compliance/des/compliance-data"
  val date: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)
  val startDate: LocalDate = LocalDate.of(2020, 1, 1)
  val endDate: LocalDate = LocalDate.of(2020, 1, 31)

  val sampleCompliancePayload: CompliancePayload = CompliancePayload(
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


  def complianceDataStub(compliancePayload: Option[CompliancePayload] = None): StubMapping = stubFor(get(urlPathEqualTo(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.OK)
      .withBody(
        Json.toJson(compliancePayload.fold(sampleCompliancePayload)(identity)).toString()
      )
    )
  )

  def invalidComplianceDataStub(): StubMapping = stubFor(get(urlPathEqualTo(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.OK)
      .withBody("{}")
    )
  )

  def upstreamErrorStub(): StubMapping = stubFor(get(urlPathEqualTo(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.INTERNAL_SERVER_ERROR).withBody("Upstream Error")
  ))
}
