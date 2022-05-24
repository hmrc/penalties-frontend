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

package controllers

import config.AppConfig
import config.featureSwitches.{CallAPI1812ETMP, FeatureSwitching}
import models.ETMPPayload
import models.communication.{Communication, CommunicationTypeEnum}
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum}
import models.point.{PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.v3.lpp.{LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import models.v3.lpp.{LatePaymentPenalty => v3LatePaymentPenalty}
import models.v3.lsp._
import models.v3.{GetPenaltyDetails, Totalisations}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub.{returnLSPDataStub, returnPenaltyDetailsStub}
import testUtils.IntegrationSpecCommonBase
import uk.gov.hmrc.http.SessionKeys.authToken

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class CalculationControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  val controller: CalculationController = injector.instanceOf[CalculationController]
  val sampleDate1: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
    authToken -> "1234"
  )

  val etmpPayload: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = None,
        communications = Seq.empty,
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Paid,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Estimated,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 400.00,
            outstandingAmountDue = 123.00,
            outstandingAmountDay15 = Some(123),
            outstandingAmountDay31 = Some(123),
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = sampleDate1
          )
        )
      )
    )
  )

  val etmpPayloadWithAdditionalPenalty: ETMPPayload = etmpPayload.copy(latePaymentPenalties = Some(
    Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Additional,
        id = "987654321",
        PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Paid,
        appealStatus = None,
        period = PaymentPeriod(
          sampleDate1,
          sampleDate1.plusMonths(1),
          LocalDateTime.now().minusDays(40),
          PaymentStatusEnum.Paid
        ),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate1,
            documentId = "123456789"
          )
        ),
        financial = Financial(
          amountDue = 123.45,
          outstandingAmountDue = 10.00,
          dueDate = LocalDateTime.now().minusDays(40)
        )
      )
    )
  ))

  val etmpPayloadWithAdditionalDuePenalty: ETMPPayload = etmpPayloadWithAdditionalPenalty.copy(latePaymentPenalties = Some(
    Seq(
      LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Additional,
        id = "987654322",
        PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Estimated,
        appealStatus = None,
        period = PaymentPeriod(
          sampleDate1,
          sampleDate1.plusMonths(1),
          LocalDateTime.now().minusDays(40),
          PaymentStatusEnum.Due
        ),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate1,
            documentId = "123456789"
          )
        ),
        financial = Financial(
          amountDue = 123.45,
          outstandingAmountDue = 10.00,
          dueDate = LocalDateTime.now().minusDays(40)
        )
      )
    )
  ))

  val etmpPayloadWithDecimals: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = None,
        communications = Seq.empty,
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Paid,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Due,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 4.93,
            outstandingAmountDue = 2.03,
            outstandingAmountDay15 = Some(123.2),
            outstandingAmountDay31 = Some(123.2),
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = LocalDateTime.now().minusDays(29)
          )
        )
      )
    )
  )

  val etmpPayloadWithOnlyDay15Charge: ETMPPayload = etmpPayload.copy(
    latePaymentPenalties = Some(
      Seq(
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Additional,
          id = "123456790",
          PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Paid,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 123.45,
            outstandingAmountDue = 0.00,
            dueDate = sampleDate1
          )
        ),
        LatePaymentPenalty(
          `type` = PenaltyTypeEnum.Financial,
          id = "123456789",
          reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
          dateCreated = sampleDate1,
          status = PointStatusEnum.Due,
          appealStatus = None,
          period = PaymentPeriod(
            sampleDate1,
            sampleDate1.plusMonths(1),
            sampleDate1.plusMonths(2).plusDays(7),
            PaymentStatusEnum.Paid
          ),
          communications = Seq(
            Communication(
              `type` = CommunicationTypeEnum.letter,
              dateSent = sampleDate1,
              documentId = "123456789"
            )
          ),
          financial = Financial(
            amountDue = 400.00,
            outstandingAmountDue = 123.00,
            outstandingAmountDay15 = Some(123),
            outstandingAmountDay31 = None,
            percentageOfOutstandingAmtCharged = Some(2),
            dueDate = sampleDate1
          )
        )
      )
    )
  )

  val etmpPayloadWithDueDateMoreThan30days: ETMPPayload = ETMPPayload(
    pointsTotal = 2, lateSubmissions = 1, adjustmentPointsTotal = 1, fixedPenaltyAmount = 0, penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 4, otherPenalties = Some(false), vatOverview = Some(Seq.empty), penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234567890",
        number = "1",
        dateCreated = sampleDate1,
        dateExpired = Some(sampleDate1.plusMonths(1).plusYears(2)),
        status = PointStatusEnum.Added,
        reason = None,
        period = None,
        communications = Seq.empty,
        financial = Some(Financial(
          amountDue = 0,
          outstandingAmountDue = 0,
          dueDate = sampleDate1,
          estimatedInterest = Some(21.00),
          crystalizedInterest = Some(32.00)
        ))
      )
    ),
    latePaymentPenalties = Some(
      Seq(LatePaymentPenalty(
        `type` = PenaltyTypeEnum.Financial,
        id = "123456789",
        reason = PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS,
        dateCreated = sampleDate1,
        status = PointStatusEnum.Due,
        appealStatus = None,
        period = PaymentPeriod(
          sampleDate1,
          sampleDate1.plusMonths(1),
          sampleDate1.plusMonths(2).plusDays(7),
          PaymentStatusEnum.Paid
        ),
        communications = Seq(
          Communication(
            `type` = CommunicationTypeEnum.letter,
            dateSent = sampleDate1,
            documentId = "123456789"
          )
        ),
        financial = Financial(
          amountDue = 400.00,
          outstandingAmountDue = 123.00,
          outstandingAmountDay15 = Some(123),
          outstandingAmountDay31 = None,
          percentageOfOutstandingAmtCharged = Some(2),
          dueDate = LocalDateTime.now().minusDays(29)
        )
      )
      )
    )
  )

  val samplePenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(BigDecimal(200)),
      penalisedPrincipalTotal = Some(BigDecimal(2000)),
      LPPPostedTotal = Some(BigDecimal(165.25)),
      LPPEstimatedTotal = Some(BigDecimal(15.26)),
      LPIPostedTotal = Some(BigDecimal(1968.2)),
      LPIEstimatedTotal = Some(BigDecimal(7))
    )),
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
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(10000.00),
        LPP1HRCalculationAmount = Some(10000.00),
        LPP2Percentage = Some(4.00),
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = Some(2.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel =  Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  val penaltyDetailsWithDay15Charge: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = None,
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = None,
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = None,
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    )))

  val penaltyDetailsWithDueDateMoreThan30days: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "12345678901234",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(277.00),
        penaltyAmountOutstanding = Some(123.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = None,
        LPP2Days = None,
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = None,
        LPP2Percentage = None,
        LPP1LRPercentage = Some(2.00),
        LPP1HRPercentage = None,
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.parse("2021-03-08"),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    )))

  val penaltyDetailsWithAdditionalPenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "54312345678901",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(113.45),
        penaltyAmountOutstanding = Some(10.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = Some(123.00),
        LPP2Percentage = Some(2.00),
        LPP1LRPercentage = Some(1.00),
        LPP1HRPercentage = Some(1.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.now().minusDays(40),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  val penaltyDetailsWithAdditionalDuePenalty: GetPenaltyDetails = samplePenaltyDetails.copy(
    latePaymentPenalty = Some(v3LatePaymentPenalty(
      details = Seq(LPPDetails(
        principalChargeReference = "65431234567890",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(113.45),
        penaltyAmountOutstanding = Some(10.00),
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("31"),
        LPP2Days = Some("31"),
        LPP1LRCalculationAmount = Some(123.00),
        LPP1HRCalculationAmount = Some(123.00),
        LPP2Percentage = Some(2.00),
        LPP1LRPercentage = Some(1.00),
        LPP1HRPercentage = Some(1.00),
        penaltyChargeCreationDate = LocalDate.parse("2069-10-30"),
        communicationsDate = LocalDate.parse("2069-10-30"),
        penaltyChargeDueDate = LocalDate.parse("2021-03-08"),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
        principalChargeBillingTo = LocalDate.parse("2021-02-01"),
        principalChargeDueDate = LocalDate.now().minusDays(40),
        penaltyChargeReference = Some("1234567890"),
        principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30"))
      ))
    ))
  )

  class Setup(isFSEnabled: Boolean = false) {
    if (isFSEnabled) enableFeatureSwitch(CallAPI1812ETMP) else disableFeatureSwitch(CallAPI1812ETMP)
  }

  "GET /calculation when it is not an additional penalty and  penalty is shown with estimate" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in new Setup {
      returnLSPDataStub(etmpPayload)
      val request: Future[Result] = controller.onPageLoad("123456789", isAdditional = false)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody: Document = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "2% of £123.00 (VAT amount unpaid on 23 March 2021) + 2% of £123.00 (VAT amount unpaid on 7 April 2021)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£277.00"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Amount left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£123.00"
      parsedBody.select("#main-content div .govuk-warning-text").text() shouldBe "! This penalty will rise to £800.00 (a further 2% of the unpaid VAT) if you do not make a VAT payment by 7 April 2021."
      parsedBody.select("#main-content .govuk-body").get(0).text() shouldBe "Paying part of your VAT bill will reduce further penalties."
      parsedBody.select("#main-content .govuk-body").get(1).text() shouldBe "Penalties and interest will show as estimates if HMRC has not been given enough information to calculate the final amounts."
      parsedBody.select("#main-content h2").text() shouldBe "Estimates"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "the user has specified a valid penalty ID (parses decimals correctly)" in new Setup {
      returnLSPDataStub(etmpPayloadWithDecimals)
      val request = controller.onPageLoad("123456789", false)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£4.93"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "2% of £123.20 (VAT amount unpaid on 23 March 2021) + 2% of £123.20 (VAT amount unpaid on 7 April 2021)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£2.90"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Amount left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£2.03"
      parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
      parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID (only one interest charge)" in new Setup {
      returnLSPDataStub(etmpPayloadWithOnlyDay15Charge)
      val request = controller.onPageLoad("123456789", false)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "2% of £123.00 (VAT amount unpaid on 23 March 2021)"
    }

    "return 200 (OK) and render the view correctly with Penalty Amount)" in new Setup {
      returnLSPDataStub(etmpPayloadWithDueDateMoreThan30days)
      val request = controller.onPageLoad("123456789", false)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in new Setup {
      returnLSPDataStub(etmpPayload)
      val request = controller.onPageLoad("123456800", false)(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = controller.onPageLoad("12345", false)(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }

    "for new API1812 model" should {
      "return 200 (OK) and render the view correctly when the use has specified a valid penalty ID" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(samplePenaltyDetails)
        val request = controller.onPageLoad("12345678901234", false)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe
          "2% of £10,000.00 (VAT amount unpaid on 23 March 2021) + 2% of £10,000.00 (VAT amount unpaid on 7 April 2021)"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£277.00"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£123.00"
        parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
        parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
      }

      "the user has specified a valid penalty ID (parses decimals correctly)" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(samplePenaltyDetails)
        val request = controller.onPageLoad("12345678901234", false)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Late payment penalty"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content header p span").first.text() shouldBe "The period dates are"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe
          "2% of £10,000.00 (VAT amount unpaid on 23 March 2021) + 2% of £10,000.00 (VAT amount unpaid on 7 April 2021)"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Amount received"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "£277.00"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "£123.00"
        parsedBody.select("#main-content a").get(0).text() shouldBe "Return to VAT penalties and appeals"
        parsedBody.select("#main-content a").get(0).attr("href") shouldBe "/penalties"
      }

      "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID (only one interest charge)" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(penaltyDetailsWithDay15Charge)
        val request = controller.onPageLoad("12345678901234", false)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Calculation"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "2% of £123.00 (VAT amount unpaid on 23 March 2021)"
      }

      "return 200 (OK) and render the view correctly with Penalty Amount" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(penaltyDetailsWithDueDateMoreThan30days)
        val request = controller.onPageLoad("12345678901234", false)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£400.00"
      }

      "return 500 (ISE) when the user specifies a penalty not within their data" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(samplePenaltyDetails)

        val request = controller.onPageLoad("1234567890", false)(fakeRequest)
        status(request) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return 303 (SEE_OTHER) when the user is not authorised" in new Setup(isFSEnabled = true) {
        AuthStub.unauthorised()

        val request = controller.onPageLoad("12345", false)(fakeRequest)
        status(request) shouldBe Status.SEE_OTHER
      }
    }
  }

  "GET /calculation when it is an additional penalty" should {
    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in new Setup {
      returnLSPDataStub(etmpPayloadWithAdditionalPenalty)
      val request = controller.onPageLoad("987654321", true)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Additional penalty"
      parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content .govuk-body").get(0).text() shouldBe
        "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Number of days since day 31"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "9 days"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Additional penalty rate"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "4%"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Calculation"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "VAT amount unpaid × 4% × number of days since day 31 ÷ 365"
      parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dd").text() shouldBe "£113.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dt").text() shouldBe "Amount left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dd").text() shouldBe "£10.00"

      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due" in new Setup {
      returnLSPDataStub(etmpPayloadWithAdditionalDuePenalty)
      val request = controller.onPageLoad("987654322", true)(fakeRequest)
      status(request) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content h1").first().ownText() shouldBe "Additional penalty"
      parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
      parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
      parsedBody.select("#main-content p").get(1).text() shouldBe
        "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
      parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Number of days since day 31"
      parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "9 days"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Additional penalty rate"
      parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "4%"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Calculation"
      parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "VAT amount unpaid × 4% × number of days since day 31 ÷ 365"
      parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dt").text() shouldBe "Amount received"
      parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dd").text() shouldBe "£113.45"
      parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dt").text() shouldBe "Amount left to pay"
      parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dd").text() shouldBe "£10.00"
      parsedBody.select("#main-content p").get(2).text() shouldBe
        "Penalties and interest will show as estimates if HMRC does not have enough information to calculate the final amounts."
      parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
    }

    "return 500 (ISE) when the user specifies a penalty not within their data" in new Setup {
      returnLSPDataStub(etmpPayload)
      val request = controller.onPageLoad("123456800", true)(fakeRequest)
      status(request) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = controller.onPageLoad("123456800", true)(fakeRequest)
      status(request) shouldBe Status.SEE_OTHER
    }

    "for new API1812 model" should {
      "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(penaltyDetailsWithAdditionalPenalty)
        val request = controller.onPageLoad("54312345678901", true)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Additional penalty"
        parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content .govuk-body").get(0).text() shouldBe
          "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Number of days since day 31"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "9 days"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Additional penalty rate"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "4%"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Calculation"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "VAT amount unpaid × 4% × number of days since day 31 ÷ 365"
        parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dt").text() shouldBe "Amount received"
        parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dd").text() shouldBe "£113.45"
        parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dt").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dd").text() shouldBe "£10.00"
      }

      "return 200 (OK) and render the view correctly when the user has specified a valid penalty ID and the VAT is due" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(penaltyDetailsWithAdditionalDuePenalty)
        val request = controller.onPageLoad("65431234567890", true)(fakeRequest)
        status(request) shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content h1").first().ownText() shouldBe "Additional penalty"
        parsedBody.select("#main-content header p .govuk-visually-hidden").first.text() shouldBe "The period dates are"
        parsedBody.select("#main-content header p").first.text() shouldBe "The period dates are 1 January 2021 to 1 February 2021"
        parsedBody.select("#main-content p").get(1).text() shouldBe
          "The additional penalty is charged from 31 days after the payment due date, until the total is paid."
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dt").text() shouldBe "Penalty amount (estimate)"
        parsedBody.select("#main-content .govuk-summary-list__row").get(0).select("dd").text() shouldBe "£123.45"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dt").text() shouldBe "Number of days since day 31"
        parsedBody.select("#main-content .govuk-summary-list__row").get(1).select("dd").text() shouldBe "9 days"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dt").text() shouldBe "Additional penalty rate"
        parsedBody.select("#main-content .govuk-summary-list__row").get(2).select("dd").text() shouldBe "4%"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dt").text() shouldBe "Calculation"
        parsedBody.select("#main-content .govuk-summary-list__row").get(3).select("dd").text() shouldBe "VAT amount unpaid × 4% × number of days since day 31 ÷ 365"
        parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dt").text() shouldBe "Amount received"
        parsedBody.select("#main-content .govuk-summary-list__row").get(4).select("dd").text() shouldBe "£113.45"
        parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dt").text() shouldBe "Amount left to pay"
        parsedBody.select("#main-content .govuk-summary-list__row").get(5).select("dd").text() shouldBe "£10.00"
        parsedBody.select("#main-content p").get(2).text() shouldBe
          "Penalties and interest will show as estimates if HMRC does not have enough information to calculate the final amounts."
        parsedBody.select("#main-content a").attr("href") shouldBe "/penalties"
      }

      "return 500 (ISE) when the user specifies a penalty not within their data" in new Setup(isFSEnabled = true) {
        returnPenaltyDetailsStub(samplePenaltyDetails)

        val request = controller.onPageLoad("123456800", true)(fakeRequest)
        status(request) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return 303 (SEE_OTHER) when the user is not authorised" in new Setup(isFSEnabled = true) {
        AuthStub.unauthorised()
        val request = controller.onPageLoad("123456800", true)(fakeRequest)
        status(request) shouldBe Status.SEE_OTHER
      }
    }
  }
}
