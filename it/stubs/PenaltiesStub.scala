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
import models.penalty.{LatePaymentPenalty, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDateTime

object PenaltiesStub {
  val vrn: String = "HMRC-MTD-VAT~VRN~123456789"
  val getLspDataUrl: String = s"/penalties/etmp/penalties/$vrn"
  val getLspDataUrlAgent: String = s"/penalties/etmp/penalties/$vrn\\?arn=123456789"
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

  val sampleLspData: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Option(Seq.empty[LatePaymentPenalty])
  )

  val sampleLspDataWithMultiplePenaltyPeriod: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234",
        number = "1",
        appealStatus = None,
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1),
        status = PointStatusEnum.Active,
        reason = None,
        period = Some(
          Seq(PenaltyPeriod(
            startDate = sampleDate1,
            endDate = sampleDate1.plusDays(15),
            submission = Submission(
              dueDate = sampleDate1.plusMonths(4).plusDays(7),
              submittedDate = Some(sampleDate1.plusMonths(4).plusDays(12)),
              status = SubmissionStatusEnum.Submitted
            )
          ),
            PenaltyPeriod(
              startDate = sampleDate1.plusDays(16),
              endDate = sampleDate1.plusDays(31),
              submission = Submission(
                dueDate = sampleDate1.plusMonths(4).plusDays(23),
                submittedDate = Some(sampleDate1.plusMonths(4).plusDays(25)),
                status = SubmissionStatusEnum.Submitted
              )
            )
          )
        ),
        communications = Seq.empty,
        financial = None
      )
    ),
    latePaymentPenalties = Option(Seq.empty[LatePaymentPenalty])
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

  def lspWithMultiplePenaltyPeriodDataStub(): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(sampleLspDataWithMultiplePenaltyPeriod).toString()
        )
    )
  )

  def returnLSPDataStub(lspDataToReturn: ETMPPayload): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(lspDataToReturn).toString()
        )
    )
  )

  def returnAgentLSPDataStub(lspDataToReturn: ETMPPayload): StubMapping = stubFor(get(urlMatching(getLspDataUrlAgent))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(lspDataToReturn).toString()
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
