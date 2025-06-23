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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.breathingSpace.BreathingSpace
import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}

import java.time.{LocalDate, LocalDateTime}

object PenaltiesStub {
  val vrn: String = "123456789"
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

  val getPenaltyDetailsUrlVATTrader: String = s"/penalties/VATC/etmp/penalties/VRN/$vrn"
  val getPenaltyDetailsUrlAgent: String = s"/penalties/VATC/etmp/penalties/VRN/$vrn\\?arn=123456789"

  val samplePenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = Some(10432.21),
      totalAccountPostedInterest = Some(4.32),
      totalAccountAccruingInterest = Some(1.23)
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 5,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some(ExpiryReasonEnum.Adjustment),
          communicationsDate = Some(LocalDate.parse("2069-10-30")),
          lateSubmissions = Some(Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
              returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
              taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
        penaltyAmountPaid = None,
        penaltyAmountOutstanding = None,
        penaltyAmountPosted = 0,
        penaltyAmountAccruing = 1001.45,
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(99.99),
        LPP1HRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
        communicationsDate = Some(LocalDate.parse("2069-10-30")),
        penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        principalChargeLatestClearing = None,
        penaltyChargeReference = Some("PEN1234567"),
        vatOutstandingAmount = Some(BigDecimal(123.45)),
          LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99),
          timeToPay = None
        )
      ))
    )),
    breathingSpace = Some(Seq(BreathingSpace(
      BSStartDate = LocalDate.of(2023, 1, 1),
      BSEndDate = LocalDate.of(2023, 12, 31)
    )))
  )

  val samplePenaltyDetailsNoMetaData: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = Some(10432.21),
      totalAccountPostedInterest = Some(4.32),
      totalAccountAccruingInterest = Some(1.23)
    )),
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 10,
          inactivePenaltyPoints = 12,
          regimeThreshold = 10,
          penaltyChargeAmount = 684.25,
          PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
        ),
        details = Seq(LSPDetails(
          penaltyNumber = "12345678901234",
          penaltyOrder = Some("01"),
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = Some("X"),
          penaltyCreationDate = LocalDate.parse("2069-10-30"),
          penaltyExpiryDate = LocalDate.parse("2069-10-30"),
          expiryReason = Some(ExpiryReasonEnum.Adjustment),
          communicationsDate = Some(LocalDate.parse("2069-10-30")),
          lateSubmissions = Some(Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
              taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
              returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
              taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
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
        penaltyAmountPaid = None,
        penaltyAmountOutstanding = None,
        penaltyAmountPosted = 0,
        penaltyAmountAccruing = 1001.45,
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(99.99),
        LPP1HRCalculationAmount = Some(99.99),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
        penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
        communicationsDate = Some(LocalDate.parse("2069-10-30")),
        penaltyChargeDueDate = Some(LocalDate.parse("2069-10-30")),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        principalChargeLatestClearing = None,
        penaltyChargeReference = Some("PEN1234567"),
        vatOutstandingAmount = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = None,
          outstandingAmount = None,
          timeToPay = None
        )
      ))
    )),
    breathingSpace = None
  )

  val sampleInvalidPenaltyDetailsJson = Json.obj(
    "totalisations" -> {
      "LSPTotalValue" -> 200
    })

  def getPenaltyDetailsStub(penaltyDetailsToReturn: Option[GetPenaltyDetails] = None, isAgent: Boolean = false): StubMapping =
    stubFor(get(urlMatching(if(isAgent) getPenaltyDetailsUrlAgent else getPenaltyDetailsUrlVATTrader))
    .willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(penaltyDetailsToReturn.fold(samplePenaltyDetails)(identity)).toString()
        )
    )
  )

  def getPenaltyDetailsStub(penaltyDetailsToReturn: JsValue): StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            penaltyDetailsToReturn.toString()
          )
      )
    )

  def penaltyDetailsUpstreamErrorStub(status: Int = Status.INTERNAL_SERVER_ERROR): StubMapping =
    stubFor(get(urlMatching(getPenaltyDetailsUrlVATTrader))
      .willReturn(
        aResponse()
          .withStatus(status).withBody("Upstream Error")
      )
  )
}
