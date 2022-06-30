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

package base

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import models.compliance._
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
import models.v3.{GetPenaltyDetails, Totalisations}
import models.v3.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum => AppealStatusEnumv2}
import models.v3.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, MainTransactionEnum,
  LatePaymentPenalty => LatePaymentPenaltyv2}
import models.v3.lsp.{LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LateSubmission, LateSubmissionPenalty, TaxReturnStatusEnum}
import models.{ETMPPayload, FilingFrequencyEnum, User}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.AuthService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.govukfrontend.views.Aliases.Tag
import uk.gov.hmrc.http.HeaderCarrier
import utils.SessionKeys
import viewmodels.{LateSubmissionPenaltySummaryCard, SummaryCardHelper, TimelineHelper}
import viewmodels.v2.{SummaryCardHelper => SummaryCardHelperv2}
import views.html.errors.Unauthorised
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val injector: Injector = app.injector

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val cyMessages: Messages = messagesApi.preferred(fakeRequest.withTransientLang("cy"))

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val summaryCardHelper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  val summaryCardHelperv2: SummaryCardHelperv2 = injector.instanceOf[SummaryCardHelperv2]

  val timelineHelper: TimelineHelper = injector.instanceOf[TimelineHelper]

  val vrn: String = "123456789"

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)
  val sampleDateV2: LocalDate = LocalDate.of(2021, 4, 23)
  val sampleOldestDate: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)
  val sampleOldestDatev2: LocalDate = LocalDate.of(2021, 1, 1)

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  val sampleEmptyLspData: ETMPPayload = ETMPPayload(
    pointsTotal = 0,
    lateSubmissions = 0,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 4,
    otherPenalties = Some(false),
    vatOverview = Some(Seq.empty),
    penaltyPoints = Seq.empty[PenaltyPoint],
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )

  val sampleCompliancePayload: CompliancePayload = CompliancePayload(
    identification = ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    ),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.open,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      )
    )
  )

  val sampleComplianceData: ComplianceData = ComplianceData(
    sampleCompliancePayload,
    amountOfSubmissionsRequiredFor24MthsHistory = None,
    filingFrequency = FilingFrequencyEnum.quarterly
  )
  val samplePenaltyPoint: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(Seq(PenaltyPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      submission = Submission(
        dueDate = LocalDateTime.now,
        submittedDate = Some(LocalDateTime.now),
        status = SubmissionStatusEnum.Submitted
      )
    ))),
    communications = Seq.empty
  )

  val samplePenaltyPointv2: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.now,
    penaltyExpiryDate = LocalDate.now,
    expiryReason = None,
    communicationsDate = LocalDate.now,
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.now),
        taxPeriodEndDate = Some(LocalDate.now),
        taxPeriodDueDate = Some(LocalDate.now),
        returnReceiptDate = Some(LocalDate.now),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = None,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(0.00),
    chargeDueDate = Some(LocalDate.now)
  )

  val sampleFinancialPenaltyPoint: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(Seq(PenaltyPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      submission = Submission(
        dueDate = LocalDateTime.now,
        submittedDate = None,
        status = SubmissionStatusEnum.Overdue
      )
    ))),
    communications = Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00, outstandingAmountDue = 200.00, dueDate = LocalDateTime.now()
      )
    )
  )

  val sampleFinancialPenaltyPointv2: LSPDetails = LSPDetails(
    penaltyNumber = "123456789",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Charge,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.now,
    penaltyExpiryDate = LocalDate.now,
    expiryReason = Some("FAP"),
    communicationsDate = LocalDate.parse("2069-10-30"),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.now),
        taxPeriodEndDate = Some(LocalDate.now),
        taxPeriodDueDate = Some(LocalDate.now),
        returnReceiptDate = None,
        taxReturnStatus = TaxReturnStatusEnum.Open
      )
    )),
    appealInformation = None,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  val samplePenaltyDetailsModel: GetPenaltyDetails = GetPenaltyDetails(
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
          activePenaltyPoints = 1,
          inactivePenaltyPoints = 0,
          regimeThreshold = 4,
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
              appealStatus = Some(AppealStatusEnumv2.Unappealable),
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenaltyv2(
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
          appealStatus = Some(AppealStatusEnumv2.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("PEN1234567"),
        principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99)
        )
      ))
    ))
  )

  val samplePenaltyDetailsModelWithoutMetadata: GetPenaltyDetails = GetPenaltyDetails(
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
          activePenaltyPoints = 1,
          inactivePenaltyPoints = 0,
          regimeThreshold = 4,
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
              appealStatus = Some(AppealStatusEnumv2.Unappealable),
              appealLevel = Some(AppealLevelEnum.HMRC)
            )
          )),
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.parse("2069-10-30"))
        ))
      )
    ),
    latePaymentPenalty = Some(LatePaymentPenaltyv2(
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
          appealStatus = Some(AppealStatusEnumv2.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        ))),
        principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
        principalChargeBillingTo = LocalDate.parse("2069-10-30"),
        principalChargeDueDate = LocalDate.parse("2069-10-30"),
        penaltyChargeReference = Some("PEN1234567"),
        principalChargeLatestClearing = None,
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = None,
          outstandingAmount = None
        )
      ))
    ))
  )

  val sampleFinancialPenaltyPointWithMultiplePenaltyPeriod: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(Seq(PenaltyPeriod(
      startDate = sampleOldestDate,
      endDate = sampleOldestDate.plusDays(15),
      submission = Submission(
        dueDate = sampleOldestDate.plusMonths(4).plusDays(7),
        submittedDate = Some(sampleOldestDate.plusMonths(4).plusDays(12)),
        status = SubmissionStatusEnum.Submitted
      )
    ),
      PenaltyPeriod(
        startDate = sampleOldestDate.plusDays(16),
        endDate = sampleOldestDate.plusDays(31),
        submission = Submission(
          dueDate = sampleOldestDate.plusMonths(4).plusDays(23),
          submittedDate = Some(sampleOldestDate.plusMonths(4).plusDays(25)),
          status = SubmissionStatusEnum.Submitted
        )
      )
    )),
    communications = Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00, outstandingAmountDue = 200.00, dueDate = LocalDateTime.now()
      )
    )
  )

  val sampleFinancialPenaltyPointWithMultiplePenaltyPeriodv2: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Charge,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = Some("FAP"),
    communicationsDate = LocalDate.parse("2069-10-30"),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(sampleOldestDatev2),
        taxPeriodEndDate = Some(sampleOldestDatev2.plusDays(15)),
        taxPeriodDueDate = Some(sampleOldestDatev2.plusMonths(4).plusDays(7)),
        returnReceiptDate = Some(sampleOldestDatev2.plusMonths(4).plusDays(12)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      ),
      LateSubmission(
        taxPeriodStartDate = Some(sampleOldestDatev2.plusDays(16)),
        taxPeriodEndDate = Some(sampleOldestDatev2.plusDays(31)),
        taxPeriodDueDate = Some(sampleOldestDatev2.plusMonths(4).plusDays(23)),
        returnReceiptDate = Some(sampleOldestDatev2.plusMonths(4).plusDays(25)),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = None,
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  val sampleOverduePenaltyPoint: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(Seq(PenaltyPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      submission = Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Overdue
      )
    ))),
    communications = Seq.empty
  )

  val sampleOverduePenaltyPointv2: LSPDetails = LSPDetails(
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
        appealStatus = Some(AppealStatusEnumv2.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )),
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  val samplePenaltyPointAppealedUnderReview: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = Some(AppealStatusEnum.Under_Review),
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(Seq(PenaltyPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      submission = Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Submitted
      )
    ))),
    communications = Seq.empty
  )

  val samplePenaltyPointAppealedUnderReviewv2: LSPDetails = LSPDetails(
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
        appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )),
    chargeAmount = Some(0.00),
    chargeOutstandingAmount = Some(0.00),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  val sampleLatePaymentPenaltyDue: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Due
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
    )
  )

  val sampleLatePaymentPenaltyDuev2: LPPDetails = LPPDetails(
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
      appealStatus = Some(AppealStatusEnumv2.Rejected),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))),
    principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
    principalChargeBillingTo = LocalDate.parse("2069-10-30"),
    principalChargeDueDate = LocalDate.parse("2069-10-30"),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )

  val sampleLatePaymentPenaltyVATPaymentDueDate: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Paid,
      paymentReceivedDate = Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
    )
  )

  val sampleLPPDetailsVATPaymentDue: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountOutstanding = Some(200.00),
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
      appealStatus = Some(AppealStatusEnumv2.Rejected),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))),
    principalChargeBillingFrom = LocalDate.now,
    principalChargeBillingTo = LocalDate.now,
    principalChargeDueDate = LocalDate.now,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )

  val sampleLatePaymentPenaltyAdditional: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Additional,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_AFTER_30_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Paid,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Paid,
      paymentReceivedDate = Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 123.45,
      outstandingAmountDue = 0,
      dueDate = LocalDateTime.now
    )
  )

  val sampleLatePaymentPenaltyAdditionalv2: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountOutstanding = Some(0),
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("31"),
    LPP2Days = Some("31"),
    LPP1LRCalculationAmount = Some(99.99),
    LPP1HRCalculationAmount = Some(99.99),
    LPP2Percentage = Some(4.00),
    LPP1LRPercentage = Some(2.00),
    LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
    penaltyChargeCreationDate = LocalDate.now(),
    communicationsDate = LocalDate.now(),
    penaltyChargeDueDate = LocalDate.now(),
    appealInformation = None,
    principalChargeBillingFrom = LocalDate.now(),
    principalChargeBillingTo = LocalDate.now(),
    principalChargeDueDate = LocalDate.now(),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = Some(LocalDate.now()),
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )

  val sampleLatePaymentPenaltyReasonVATNotPaidWithin30Days: LatePaymentPenalty =LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Paid,
    appealStatus = None,
    period = PaymentPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Paid,
      paymentReceivedDate = Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 123.45,
      outstandingAmountDue = 0,
      dueDate = LocalDateTime.now
    )
  )
  
  val sampleLatePaymentPenaltyReasonVATNotPaidWithin30Daysv2: LPPDetails = LPPDetails(
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
      appealStatus = Some(AppealStatusEnumv2.Rejected),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))),
    principalChargeBillingFrom = LocalDate.parse("2069-10-30"),
    principalChargeBillingTo = LocalDate.parse("2069-10-30"),
    principalChargeDueDate = LocalDate.parse("2069-10-30"),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )
  
  val sampleLatePaymentPenaltyPaid: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Paid,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Paid,
      paymentReceivedDate = Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 0,
      dueDate = LocalDateTime.now
    )
  )

  val sampleLatePaymentPenaltyPaidv2: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountOutstanding = Some(0.00),
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("31"),
    LPP2Days = Some("31"),
    LPP1LRCalculationAmount = Some(99.99),
    LPP1HRCalculationAmount = Some(99.99),
    LPP2Percentage = Some(4.00),
    LPP1LRPercentage = Some(2.00),
    LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
    penaltyChargeCreationDate = LocalDate.now(),
    communicationsDate = LocalDate.now(),
    penaltyChargeDueDate = LocalDate.now(),
    appealInformation =   None,
    principalChargeBillingFrom = LocalDate.now(),
    principalChargeBillingTo = LocalDate.now(),
    principalChargeDueDate = LocalDate.now(),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = Some(LocalDate.now()),
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )

  val sampleLPPDetailsVATPaid: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(400.00),
    penaltyAmountOutstanding = Some(0.00),
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
    penaltyChargeDueDate = LocalDate.parse("2020-03-30"),
    appealInformation = None,
    principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
    principalChargeBillingTo = LocalDate.of(2020, 2, 1),
    principalChargeDueDate = LocalDate.of(2020, 3, 7),
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )

  val LSPDetailsAsModelNoFAP = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = None,
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = None,
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
        appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )),
    chargeAmount = None,
    chargeOutstandingAmount = None,
    chargeDueDate = None
  )

  val sampleLatePaymentPenaltyUnpaidVAT: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Due
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
    )
  )
  val sampleLatePaymentPenaltyUnpaidVATv2: LPPDetails = LPPDetails(
    principalChargeReference = "123456789",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountOutstanding = Some(400.00),
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
    appealInformation = None,
    principalChargeBillingFrom = LocalDate.now,
    principalChargeBillingTo = LocalDate.now,
    principalChargeDueDate = LocalDate.now,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99)
    )
  )
  val sampleLatePaymentPenaltyVATPaymentDate: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      dueDate = LocalDateTime.now,
      paymentStatus = PaymentStatusEnum.Due,
      paymentReceivedDate = Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
    )
  )

  val samplePenaltyPointAppealedAccepted: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted), status = PointStatusEnum.Removed)

  val samplePenaltyPointAppealedAcceptedv2: LSPDetails =
    samplePenaltyPointAppealedUnderReviewv2.copy(chargeAmount = Some(0.00),
      chargeOutstandingAmount = Some(0.00),
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnumv2.Upheld),
      appealLevel = Some(AppealLevelEnum.HMRC)
    ))))

  val samplePenaltyPointAppealedAcceptedByTribunal: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal), status = PointStatusEnum.Removed)
  val samplePenaltyPointAppealedAcceptedByTribunalv2: LSPDetails =
    samplePenaltyPointAppealedUnderReviewv2.copy(chargeAmount = Some(0.00),
      chargeOutstandingAmount = Some(0.00),
      penaltyStatus = LSPPenaltyStatusEnum.Inactive,
      appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnumv2.Upheld),
      appealLevel = Some(AppealLevelEnum.Tribunal)
    ))),
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
          taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
          returnReceiptDate = None,
          taxReturnStatus = TaxReturnStatusEnum.Open
        )
      )))

  val samplePenaltyPointAppealedRejected: PenaltyPoint = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val samplePenaltyPointAppealedRejectedv2: LSPDetails = samplePenaltyPointAppealedUnderReviewv2.copy(chargeAmount = Some(0.00),
    chargeOutstandingAmount = Some(0.00),
    appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Rejected),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
        returnReceiptDate = None,
        taxReturnStatus = TaxReturnStatusEnum.Open
      )
    )))
  val samplePenaltyPointAppealedReinstated: PenaltyPoint = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  val samplePenaltyPointAppealedTribunalRejected: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))
  val samplePenaltyPointAppealedTribunalRejectedv2: LSPDetails = samplePenaltyPointAppealedUnderReviewv2.copy(chargeAmount = None, chargeOutstandingAmount = None,
    appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Rejected),
    appealLevel = Some(AppealLevelEnum.Tribunal)
  ))),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
        returnReceiptDate = None,
        taxReturnStatus = TaxReturnStatusEnum.Open
      )
    ))
  )

  val samplePenaltyPointAppealedUnderTribunalReview: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))
  val samplePenaltyPointAppealedUnderTribunalReviewv2: LSPDetails = samplePenaltyPointAppealedUnderReviewv2.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
    appealLevel = Some(AppealLevelEnum.Tribunal)
  ))))

  val sampleLatePaymentPenaltyAppealedUnderReview: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Review))
  val sampleLatePaymentPenaltyAppealedUnderReviewv2: LPPDetails = sampleLatePaymentPenaltyDuev2.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))))
  val sampleLatePaymentPenaltyUnderAppealv2: LPPDetails = sampleLPPDetailsVATPaymentDue.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))))
  val sampleLatePaymentPenaltyAppealedUnderTribunalReview: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))
  val sampleLatePaymentPenaltyAppealedUnderTribunalReviewv2: LPPDetails = sampleLPPDetailsVATPaymentDue.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Under_Appeal),
    appealLevel = Some(AppealLevelEnum.Tribunal)
  ))))

  val sampleLatePaymentPenaltyAppealedAccepted: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted))
  val sampleLatePaymentPenaltyAppealedAcceptedv2: LPPDetails = sampleLPPDetailsVATPaymentDue.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Upheld),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))))
  val sampleLatePaymentPenaltyAppealedAcceptedTribunal: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal))
  val sampleLatePaymentPenaltyAppealedAcceptedTribunalv2: LPPDetails = sampleLPPDetailsVATPaymentDue.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Upheld),
    appealLevel = Some(AppealLevelEnum.Tribunal)
  ))))

  val sampleLatePaymentPenaltyAppealedRejected: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val sampleLatePaymentPenaltyAppealedRejectedv2: LPPDetails = sampleLPPDetailsVATPaymentDue.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Rejected),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))))
  val sampleLatePaymentPenaltyAppealedRejectedLPPPaid: LatePaymentPenalty = sampleLatePaymentPenaltyPaid.copy(appealStatus = Some(AppealStatusEnum.Rejected))

  val sampleLatePaymentPenaltyAppealedRejectedLPPPaidv2: LPPDetails = sampleLatePaymentPenaltyPaidv2.copy(appealInformation = Some(Seq(AppealInformationType(
    appealStatus = Some(AppealStatusEnumv2.Rejected),
    appealLevel = Some(AppealLevelEnum.HMRC)
  ))),
    penaltyStatus = LPPPenaltyStatusEnum.Posted)

  val sampleLatePaymentPenaltyAppealedRejectedTribunal: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))

  val sampleLatePaymentPenaltyAppealedRejectedTribunalv2: LPPDetails =
    sampleLatePaymentPenaltyDuev2.copy(appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnumv2.Rejected),
      appealLevel = Some(AppealLevelEnum.Tribunal)
    ))))

  val sampleLatePaymentPenaltyAppealedReinstated: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  //TODO: Update for Reinstated
  val sampleLatePaymentPenaltyAppealedReinstatedv2: LPPDetails = sampleLatePaymentPenaltyDuev2
  val sampleLatePaymentPenaltyEstimated: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(status = PointStatusEnum.Estimated)

  val sampleLatePaymentPenaltyEstimatedv2: LPPDetails = sampleLatePaymentPenaltyDuev2.copy(penaltyStatus = LPPPenaltyStatusEnum.Accruing)

  val sampleLatePaymentPenaltyReasonCentralAssessmentNotPaidWithin15Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS)
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS)
  val sampleLatePaymentPenaltyReasonCentralAssessmentNotPaidWithin30Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS)
  val sampleLatePaymentPenaltyAdditionalReasonCentralAssessment: LatePaymentPenalty =
    sampleLatePaymentPenaltyAdditional.copy(reason = PaymentPenaltyReasonEnum.CENTRAL_ASSESSMENT_NOT_PAID_AFTER_30_DAYS)

  val sampleLatePaymentPenaltyReasonErrorCorrectionNoticeNotPaidWithin15Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_15_DAYS)
  val sampleLatePaymentPenaltyReasonErrorCorrectionNoticeNotPaidWithin30Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_30_DAYS)
  val sampleLatePaymentPenaltyAdditionalReasonErrorCorrectionNotice: LatePaymentPenalty =
    sampleLatePaymentPenaltyAdditional.copy(reason = PaymentPenaltyReasonEnum.ERROR_CORRECTION_NOTICE_NOT_PAID_AFTER_30_DAYS)

  val sampleLatePaymentPenaltyReasonOfficersAssessmentNotPaidWithin15Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS)
  val sampleLatePaymentPenaltyReasonOfficersAssessmentNotPaidWithin30Days: LatePaymentPenalty =
    sampleLatePaymentPenaltyPaid.copy(reason = PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS)
  val sampleLatePaymentPenaltyAdditionalReasonOfficersAssessment: LatePaymentPenalty =
    sampleLatePaymentPenaltyAdditional.copy(reason = PaymentPenaltyReasonEnum.OFFICERS_ASSESSMENT_NOT_PAID_AFTER_30_DAYS)

  val sampleRemovedPenaltyPoint: PenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Removed,
    reason = Some("reason"),
    period = Some(Seq(PenaltyPeriod(
      startDate = LocalDateTime.now,
      endDate = LocalDateTime.now,
      submission = Submission(
        dueDate = LocalDateTime.now,
        submittedDate = Some(LocalDateTime.now),
        status = SubmissionStatusEnum.Submitted
      )
    ))),
    communications = Seq.empty
  )

  val sampleRemovedPenaltyPointv2: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "02",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Inactive,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.now,
    penaltyExpiryDate = LocalDate.now,
    expiryReason = Some("FAP"),
    communicationsDate = LocalDate.now,
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.now),
        taxPeriodEndDate = Some(LocalDate.now),
        taxPeriodDueDate = Some(LocalDate.now),
        returnReceiptDate = Some(LocalDate.now),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = None,
    chargeAmount = None,
    chargeOutstandingAmount = None,
    chargeDueDate = None
  )

  val sampleReturnNotSubmittedPenaltyPeriod: PenaltyPeriod = PenaltyPeriod(
    startDate = LocalDateTime.now,
    endDate = LocalDateTime.now,
    submission = Submission(
      dueDate = LocalDateTime.now,
      submittedDate = None,
      status = SubmissionStatusEnum.Overdue
    )
  )

  val sampleReturnNotSubmittedPenaltyPeriodv2: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "02",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = Some("X"),
    penaltyCreationDate = LocalDate.now,
    penaltyExpiryDate = LocalDate.now,
    expiryReason = Some("FAP"),
    communicationsDate = LocalDate.parse("2069-10-30"),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.now),
        taxPeriodEndDate = Some(LocalDate.now),
        taxPeriodDueDate = Some(LocalDate.now),
        returnReceiptDate = Some(LocalDate.now),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnumv2.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )
    )),
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  val etmpDataWithOneLSP: ETMPPayload = ETMPPayload(
    pointsTotal = 1,
    lateSubmissions = 1,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 0,
    penaltyAmountsTotal = 0,
    penaltyPointsThreshold = 2,
    otherPenalties = None,
    vatOverview = None,
    penaltyPoints = Seq(sampleOverduePenaltyPoint),
    latePaymentPenalties = None
  )

  val sampleLspDataWithDueFinancialPenalties: ETMPPayload = ETMPPayload(
    pointsTotal = 3,
    lateSubmissions = 3,
    adjustmentPointsTotal = 0,
    fixedPenaltyAmount = 400.0,
    penaltyAmountsTotal = 0.0,
    penaltyPointsThreshold = 2,
    vatOverview = None,
    penaltyPoints = Seq(
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        id = "1236",
        number = "3",
        appealStatus = None,
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Due,
        reason = None,
        period = Some(Seq(
          PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        )),
        communications = Seq.empty,
        financial = Some(
          Financial(
            amountDue = 200.00,
            outstandingAmountDue = 200.00,
            dueDate = sampleDate,
            estimatedInterest = None,
            crystalizedInterest = None
          )
        )
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Financial,
        id = "1235",
        number = "2",
        appealStatus = None,
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Due,
        reason = None,
        period = Some(
          Seq(PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        )),
        communications = Seq.empty,
        financial = Some(
          Financial(
            amountDue = 200.00,
            outstandingAmountDue = 200.00,
            dueDate = sampleDate,
            estimatedInterest = None,
            crystalizedInterest = None
          )
        )
      ),
      PenaltyPoint(
        `type` = PenaltyTypeEnum.Point,
        id = "1234",
        number = "1",
        appealStatus = None,
        dateCreated = sampleDate,
        dateExpired = Some(sampleDate),
        status = PointStatusEnum.Active,
        reason = None,
        period = Some(
          Seq(PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        )),
        communications = Seq.empty,
        financial = None
      )
    ),
    latePaymentPenalties = Some(Seq.empty[LatePaymentPenalty])
  )

  val sampleReturnSubmittedPenaltyPointData: Seq[PenaltyPoint] = Seq(
    samplePenaltyPoint
  )

  val sampleReturnSubmittedPenaltyPointDatav2: Seq[LSPDetails] = Seq(
    samplePenaltyPointv2.copy(chargeAmount = None,
      chargeOutstandingAmount = None,
      FAPIndicator = None,
      lateSubmissions = Some(Seq(
        LateSubmission(
          taxPeriodStartDate = Some(LocalDate.now),
          taxPeriodEndDate = Some(LocalDate.now),
          taxPeriodDueDate = Some(LocalDate.now),
          returnReceiptDate = Some(LocalDate.now),
          taxReturnStatus = TaxReturnStatusEnum.Fulfilled
        )
      )))
  )

  val sampleLatePaymentPenaltyData: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyPaid
  )

  val sampleLatePaymentPenaltyReason: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyPaid
  )
    val sampleLatePaymentPenaltyAdditionalReason: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyAdditional
  )

  val sampleLatePaymentPenaltyDataUnpaidVAT: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyUnpaidVAT
  )

  val sampleLatePaymentPenaltyDataUnpaidVATv2: Seq[LPPDetails] = Seq(
    sampleLatePaymentPenaltyUnpaidVATv2
  )

  val sampleLatePaymentPenaltyDataVATPaymentDate: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyVATPaymentDate
  )

  val sampleLatePaymentPenaltyAppealedData: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyAppealedAccepted
  )

  val sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPoint: Seq[PenaltyPoint] = Seq(
    samplePenaltyPoint.copy(number = "4"),
    samplePenaltyPoint.copy(number = "3"),
    samplePenaltyPoint.copy(number = "2"),
    sampleRemovedPenaltyPoint
  )

  val sample3ReturnsSubmittedPenaltyPointDataAndOneRemovedPointv2: Seq[LSPDetails] = Seq(
    samplePenaltyPointv2.copy(penaltyOrder = "4"),
    samplePenaltyPointv2.copy(penaltyOrder = "3"),
    samplePenaltyPointv2.copy(penaltyOrder = "2"),
    sampleRemovedPenaltyPointv2
  )

  val sampleReturnNotSubmittedPenaltyPointData: Seq[PenaltyPoint] = Seq(
    PenaltyPoint(
      `type` = PenaltyTypeEnum.Point,
      id = "123456789",
      number = "1",
      appealStatus = None,
      dateCreated = LocalDateTime.now,
      dateExpired = Some(LocalDateTime.now),
      status = PointStatusEnum.Active,
      reason = None,
      period = Some(Seq(PenaltyPeriod(
        startDate = LocalDateTime.now,
        endDate = LocalDateTime.now,
        submission = Submission(
          dueDate = LocalDateTime.now,
          submittedDate = None,
          status = SubmissionStatusEnum.Overdue
        )
      ))),
      communications = Seq.empty
    )
  )

  val sampleSummaryCard: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
    cardRows = Seq.empty,
    status = Tag.defaultObject,
    penaltyPoint = "1",
    penaltyId = "123456789",
    isReturnSubmitted = true
  )

  val quarterlyThreshold: Int = 4

  val annualThreshold: Int = 2

  val monthlyThreshold: Int = 5

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  val penaltyId = "123456789"

  val redirectToAppealUrlForLSP: String =
    controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isObligation = false, isAdditional = false).url

  val redirectToAppealUrlForLPP: String =
    controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = false, isAdditional = false).url

  val redirectToAppealObligationUrlForLSP: String =
    controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isObligation = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPP: String =
    controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPPAdditional: String =
    controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = false, isAdditional = true).url

  val vatTraderUser: User[AnyContent] = User("123456789", arn = None)(fakeRequest)
  val agentUser: User[AnyContent] = User("123456789", arn = Some("AGENT1"))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234"))
}
