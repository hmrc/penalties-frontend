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

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.compliance.{CompliancePayload, MissingReturn, Return}
import play.api.http.Status
import play.api.libs.json.Json

object ComplianceStub {
  val enrolmentKey: String = "HMRC-MTD-VAT~VRN~123456789"
  val getComplianceDataUrl: String = s"/penalties/compliance/compliance-data\\?enrolmentKey=$enrolmentKey"
  val date: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)

  val sampleComplianceData: CompliancePayload = CompliancePayload(
    "0",
    "0",
    date,
    Seq.empty[MissingReturn],
    Seq.empty[Return]
  )

  def complianceDataStub(): StubMapping = stubFor(get(urlMatching(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.OK)
      .withBody(
        Json.toJson(sampleComplianceData).toString()
      )
    )
  )

  def returnComplianceDataStub(complianceDataToReturn: CompliancePayload): StubMapping = stubFor(get(urlMatching(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.OK)
      .withBody(
        Json.toJson(complianceDataToReturn).toString()
      )
    )
  )

  def invalidComplianceDataStub(): StubMapping = stubFor(get(urlMatching(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.OK)
      .withBody("{}")
    )
  )

  def upstreamErrorStub(): StubMapping = stubFor(get(urlMatching(getComplianceDataUrl))
  .willReturn(
    aResponse()
      .withStatus(Status.INTERNAL_SERVER_ERROR).withBody("Upstream Error")
  ))
}
