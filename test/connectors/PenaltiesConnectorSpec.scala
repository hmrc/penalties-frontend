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

package connectors

import base.SpecBase
import config.AppConfig
import models.ETMPPayload
import models.v3.appealInfo.{AppealInformationType, AppealStatusEnum}
import models.v3.{PenaltyDetails, Totalisations}
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty}
import models.v3.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LateSubmission, LateSubmissionPenalty, TaxReturnStatusEnum}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnectorSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)

    val connector: PenaltiesConnector = new PenaltiesConnector(mockHttpClient, mockAppConfig)
    when(mockAppConfig.penaltiesUrl).thenReturn("/")
  }

  val samplePenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
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
          appealLevel = Some("01")
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30")
      ))
    ))
  )

  "getPenaltiesData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in new Setup {
      when(mockHttpClient.GET[ETMPPayload](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.successful(sampleEmptyLspData))

      val result: ETMPPayload = await(connector.getPenaltiesData(vrn)(vatTraderUser, HeaderCarrier()))
      result shouldBe sampleEmptyLspData
    }

    "return an error when an error occurs upstream" in new Setup {
    when(mockHttpClient.GET[ETMPPayload](any(),
      any(),
      any())
      (any(),
        any(),
        any()))
      .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

    val result: Exception = intercept[Exception](await(connector.getPenaltiesData("123456789")(vatTraderUser, HeaderCarrier())))
    result.getMessage shouldBe "Upstream error"
  }
  }

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the body can be parsed" in new Setup{
      when(mockHttpClient.GET[PenaltyDetails](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.successful(samplePenaltyDetailsModel))

      val result = await(connector.getPenaltyDetails(vrn)(vatTraderUser, HeaderCarrier()))
      result shouldBe samplePenaltyDetailsModel
    }

    "return an error when an error occurs upstream" in new Setup{
      when(mockHttpClient.GET[PenaltyDetails](any(),
        any(),
        any())
        (any(),
          any(),
          any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result: Exception = intercept[Exception](await(connector.getPenaltyDetails("123456789")(vatTraderUser, HeaderCarrier())))
      result.getMessage shouldBe "Upstream error"
    }
  }

}
