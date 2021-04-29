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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.ETMPPayload
import models.point.PenaltyPoint
import play.api.http.Status
import play.api.libs.json.Json

object PenaltiesStub {
  val vrn: String = "HMRC-MTD-VAT~VRN~123456789"
  val getLspDataUrl: String = s"/penalties/etmp/penalties/$vrn"
  val sampleLspData: ETMPPayload = ETMPPayload(
    0,
    0,
    0.0,
    0.0,
    0.0,
    4,
    Seq.empty[PenaltyPoint]
  )

  def lspDataStub(): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(sampleLspData).toString()
        )
    )
  )

  def invalidLspDataStub(): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody("{}")
    )
  )

  def upstreamErrorStub(): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR).withBody("Upstream Error")
    )
  )
}
