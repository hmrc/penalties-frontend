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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.ETMPPayload
import models.penalty.{LatePaymentPenalty, PenaltyPeriod}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import models.v3.appealInfo.{AppealInformationType, AppealStatusEnum}
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty => v3LatePaymentPenalty}
import models.v3.lsp._
import models.v3.{PenaltyDetails, Totalisations}
import play.api.http.Status
import play.api.libs.json.Json

import java.time.{LocalDate, LocalDateTime}

object PenaltiesStub {
  val vrn: String = "HMRC-MTD-VAT~VRN~123456789"
  val getLspDataUrl: String = s"/penalties/etmp/penalties/$vrn\\?newApiModel=false"
  val getLspDataUrlAgent: String = s"/penalties/etmp/penalties/$vrn\\?arn=123456789&newApiModel=false"
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

  val getPenaltyDetailsUrl: String = s"/penalties/etmp/penalties/$vrn\\?newApiModel=true"

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

  val samplePenaltyDetails: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = 200,
      penalisedPrincipalTotal = 2000,
      LPPPostedTotal = 165.25,
      LPPEstimatedTotal = 15.26,
      LPIPostedTotal = 1968.2,
      LPIEstimatedTotal = 7)),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = "X",
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some("FAP"),
          communicationsDate = LocalDate.parse("2069-10-30"),
          lateSubmissions = Some(Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
              returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
              taxReturnStatus = TaxReturnStatusEnum.Fulfilled
            )
          )),
          appealInformation = Some(Seq(
            AppealInformationType(
              appealStatus = Some(AppealStatusEnum.Unappealable),
              appealLevel = Some("01")
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(1001.45),
        penaltyAmountOutstanding = Some(99.99),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(99.99),
        LPP1HRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2069-10-30"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some("01")
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30")
      ))
    ))
  )

  val sampleInvalidPenaltyDetailsJson = Json.obj(
    "totalisations" -> {
      "LSPTotalValue" -> 200
    }).toString()

  def lspDataStub(): StubMapping = stubFor(get(urlMatching(getLspDataUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(sampleLspData).toString()
        )
    )
  )

  def getPenaltyDetailsStub(): StubMapping = stubFor(get(urlMatching(getPenaltyDetailsUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(samplePenaltyDetails).toString()
        )
    )
  )

  def returnPenaltyDetailsStub(penaltyDetailsToReturn: PenaltyDetails): StubMapping = stubFor(get(urlMatching(getPenaltyDetailsUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(penaltyDetailsToReturn).toString
        )
    ))

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


  def penaltyDetailsUpstreamErrorStub(): StubMapping = stubFor(get(urlMatching(getPenaltyDetailsUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR).withBody("Upstream Error")
    )
  )

  def invalidPenaltyDetailsStub(): StubMapping = stubFor(get(urlMatching(getPenaltyDetailsUrl))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(sampleInvalidPenaltyDetailsJson)
    )
  )
}
