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
import models.{GetPenaltyDetails, Totalisations}
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp._
import models.lsp._
import play.api.http.Status
import play.api.libs.json.Json

import java.time.{LocalDate, LocalDateTime}

object PenaltiesStub {
  val vrn: String = "HMRC-MTD-VAT~VRN~123456789"
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

  val getPenaltyDetailsUrlVATTrader: String = s"/penalties/etmp/penalties/$vrn"
  val getPenaltyDetailsUrlAgent: String = s"/penalties/etmp/penalties/$vrn\\?arn=123456789"

  val samplePenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      LPIPostedTotal = Some(1968.2),
      LPIEstimatedTotal = Some(7)
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 5,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
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
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
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
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        principalChargeLatestClearing = None,
        penaltyChargeReference = Some("PEN1234567"),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  val samplePenaltyDetailsNoMetaData: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      LPIPostedTotal = Some(1968.2),
      LPIEstimatedTotal = Some(7)
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = LocalDate.of(2022, 1, 1)
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = "01",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
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
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenalty(
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
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        principalChargeLatestClearing = None,
        penaltyChargeReference = Some("PEN1234567"),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = None,
          outstandingAmount = None
        )
      ))
    ))
  )

  val sampleInvalidPenaltyDetailsJson = Json.obj(
    "totalisations" -> {
      "LSPTotalValue" -> 200
    }).toString()

  def getPenaltyDetailsStub: StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(samplePenaltyDetails).toString()
        )
    )
  )

  def returnPenaltyDetailsStub(penaltyDetailsToReturn: GetPenaltyDetails): StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(penaltyDetailsToReturn).toString
        )
    )
  )

  def returnPenaltyDetailsStubAgent(penaltyDetailsToReturn: GetPenaltyDetails): StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlAgent))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(penaltyDetailsToReturn).toString
        )
    )
  )

  def penaltyDetailsUpstreamErrorStub: StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
      .willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR).withBody("Upstream Error")
      )
  )

  def invalidPenaltyDetailsStub: StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(sampleInvalidPenaltyDetailsJson)
    )
  )
}
