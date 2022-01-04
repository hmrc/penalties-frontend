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
import utils.SessionKeys
import viewmodels.{LateSubmissionPenaltySummaryCard, SummaryCardHelper, TimelineHelper}
import views.html.errors.Unauthorised

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val injector: Injector = app.injector

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val summaryCardHelper: SummaryCardHelper = injector.instanceOf[SummaryCardHelper]

  val timelineHelper: TimelineHelper = injector.instanceOf[TimelineHelper]

  val vrn: String = "123456789"

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)
  val sampleOldestDate: LocalDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1)

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
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        Some(LocalDateTime.now),
        SubmissionStatusEnum.Submitted
      )
    ))),
    communications = Seq.empty
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
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Overdue
      )
    ))),
    communications = Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00, outstandingAmountDue = 200.00, dueDate = LocalDateTime.now()
      )
    )
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
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Overdue
      )
    ))),
    communications = Seq.empty
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
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Submitted
      )
    ))),
    communications = Seq.empty
  )

  val sampleLatePaymentPenaltyDue: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_30_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Paid
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
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
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Paid,
      Some(LocalDateTime.now)
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 00.00,
      outstandingAmountDue = 00.00,
      dueDate = LocalDateTime.now
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
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Paid
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 123.45,
      outstandingAmountDue = 12.34,
      dueDate = LocalDateTime.now
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
      PaymentStatusEnum.Paid
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 123.45,
      outstandingAmountDue = 12.34,
      dueDate = LocalDateTime.now
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
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Paid
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
    )
  )

  val sampleLatePaymentPenaltyUnpaidVAT: LatePaymentPenalty = LatePaymentPenalty(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    reason = PaymentPenaltyReasonEnum.VAT_NOT_PAID_WITHIN_15_DAYS,
    dateCreated = LocalDateTime.now,
    status = PointStatusEnum.Due,
    appealStatus = None,
    period = PaymentPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Due
    ),
    communications = Seq.empty,
    financial = Financial(
      amountDue = 400.00,
      outstandingAmountDue = 200.00,
      dueDate = LocalDateTime.now
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
      LocalDateTime.now,
      LocalDateTime.now,
      LocalDateTime.now,
      PaymentStatusEnum.Due,
      Some(LocalDateTime.now)
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
  val samplePenaltyPointAppealedAcceptedByTribunal: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal), status = PointStatusEnum.Removed)
  val samplePenaltyPointAppealedRejected: PenaltyPoint = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val samplePenaltyPointAppealedReinstated: PenaltyPoint = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  val samplePenaltyPointAppealedTribunalRejected: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))
  val samplePenaltyPointAppealedUnderTribunalReview: PenaltyPoint =
    samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))

  val sampleLatePaymentPenaltyAppealedUnderReview: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Review))
  val sampleLatePaymentPenaltyAppealedUnderTribunalReview: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))
  val sampleLatePaymentPenaltyAppealedAccepted: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted))
  val sampleLatePaymentPenaltyAppealedAcceptedTribunal: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal))
  val sampleLatePaymentPenaltyAppealedRejected: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val sampleLatePaymentPenaltyAppealedRejectedLPPPaid: LatePaymentPenalty = sampleLatePaymentPenaltyPaid.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val sampleLatePaymentPenaltyAppealedRejectedTribunal: LatePaymentPenalty =
    sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))
  val sampleLatePaymentPenaltyAppealedReinstated: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  val sampleLatePaymentPenaltyEstimated: LatePaymentPenalty = sampleLatePaymentPenaltyDue.copy(status = PointStatusEnum.Estimated)

  val sampleLatePaymentPenaltyReasonCentralAssessmentNotPaidWithin15Days: LatePaymentPenalty =
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
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    None,
    LocalDateTime.now,
    Some(LocalDateTime.now),
    PointStatusEnum.Removed,
    Some("reason"),
    Some(Seq(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        Some(LocalDateTime.now),
        SubmissionStatusEnum.Submitted
      )
    ))),
    Seq.empty
  )

  val sampleReturnNotSubmittedPenaltyPeriod: PenaltyPeriod = PenaltyPeriod(
    LocalDateTime.now,
    LocalDateTime.now,
    Submission(
      LocalDateTime.now,
      None,
      SubmissionStatusEnum.Overdue
    )
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

  val sampleReturnNotSubmittedPenaltyPointData: Seq[PenaltyPoint] = Seq(
    PenaltyPoint(
      PenaltyTypeEnum.Point,
      "123456789",
      "1",
      None,
      LocalDateTime.now,
      Some(LocalDateTime.now),
      PointStatusEnum.Active,
      None,
      Some(Seq(PenaltyPeriod(
        LocalDateTime.now,
        LocalDateTime.now,
        Submission(
          LocalDateTime.now,
          None,
          SubmissionStatusEnum.Overdue
        )
      ))),
      Seq.empty
    )
  )

  val sampleSummaryCard: LateSubmissionPenaltySummaryCard = LateSubmissionPenaltySummaryCard(
    Seq.empty,
    Tag.defaultObject,
    "1",
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
