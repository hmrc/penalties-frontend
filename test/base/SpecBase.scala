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

package base

import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import models.compliance.{CompliancePayload, MissingReturn, Return}
import models.financial.Financial
import models.penalty.{LatePaymentPenalty, PaymentPeriod, PaymentStatusEnum, PenaltyPeriod}
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.reason.PaymentPenaltyReasonEnum
import models.submission.{Submission, SubmissionStatusEnum}
import models.{ETMPPayload, User}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.AuthService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.govukfrontend.views.Aliases.Tag
import utils.SessionKeys
import viewmodels.{LateSubmissionPenaltySummaryCard, SummaryCardHelper, TimelineHelper}
import views.html.errors.Unauthorised

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val injector = app.injector

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val summaryCardHelper = injector.instanceOf[SummaryCardHelper]

  val timelineHelper = injector.instanceOf[TimelineHelper]

  val vrn: String = "123456789"

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  val sampleLspData: ETMPPayload = ETMPPayload(
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

  val sampleComplianceData: CompliancePayload = CompliancePayload(
    noOfMissingReturns = "0",
    noOfSubmissionsReqForCompliance = "0",
    expiryDateOfAllPenaltyPoints = LocalDateTime.now(),
    missingReturns = Seq.empty[MissingReturn],
    returns = Seq.empty[Return]
  )

  val samplePenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        Some(LocalDateTime.now),
        SubmissionStatusEnum.Submitted
      )
    )),
    communications = Seq.empty
  )

  val sampleFinancialPenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Financial,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Overdue
      )
    )),
    communications = Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00, outstandingAmountDue = 200.00, dueDate = LocalDateTime.now()
      )
    )
  )

  val sampleOverduePenaltyPoint = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = None,
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Overdue
      )
    )),
    communications = Seq.empty
  )

  val samplePenaltyPointAppealedUnderReview = PenaltyPoint(
    `type` = PenaltyTypeEnum.Point,
    id = "123456789",
    number = "1",
    appealStatus = Some(AppealStatusEnum.Under_Review),
    dateCreated = LocalDateTime.now,
    dateExpired = Some(LocalDateTime.now),
    status = PointStatusEnum.Active,
    reason = None,
    period = Some(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        None,
        SubmissionStatusEnum.Submitted
      )
    )),
    communications = Seq.empty
  )

  val sampleLatePaymentPenaltyDue = LatePaymentPenalty(
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

  val sampleLatePaymentPenaltyAdditional = LatePaymentPenalty(
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

  val sampleLatePaymentPenaltyPaid = LatePaymentPenalty(
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

  val sampleLatePaymentPenaltyUnpaidVAT = LatePaymentPenalty(
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

  val samplePenaltyPointAppealedAccepted = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted), status = PointStatusEnum.Removed)
  val samplePenaltyPointAppealedAcceptedByTribunal = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal), status = PointStatusEnum.Removed)
  val samplePenaltyPointAppealedRejected = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val samplePenaltyPointAppealedReinstated = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  val samplePenaltyPointAppealedTribunalRejected = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))
  val samplePenaltyPointAppealedUnderTribunalReview = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))

  val sampleLatePaymentPenaltyAppealedUnderReview = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Review))
  val sampleLatePaymentPenaltyAppealedUnderTribunalReview = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))
  val sampleLatePaymentPenaltyAppealedAccepted = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted))
  val sampleLatePaymentPenaltyAppealedAcceptedTribunal = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal))
  val sampleLatePaymentPenaltyAppealedRejected = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val sampleLatePaymentPenaltyAppealedRejectedLPPPaid = sampleLatePaymentPenaltyPaid.copy(appealStatus = Some(AppealStatusEnum.Rejected))
  val sampleLatePaymentPenaltyAppealedRejectedTribunal = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))
  val sampleLatePaymentPenaltyAppealedReinstated = sampleLatePaymentPenaltyDue.copy(appealStatus = Some(AppealStatusEnum.Reinstated))
  val sampleLatePaymentPenaltyEstimated = sampleLatePaymentPenaltyDue.copy(status = PointStatusEnum.Estimated)

  val sampleRemovedPenaltyPoint = PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    None,
    LocalDateTime.now,
    Some(LocalDateTime.now),
    PointStatusEnum.Removed,
    Some("reason"),
    Some(PenaltyPeriod(
      LocalDateTime.now,
      LocalDateTime.now,
      Submission(
        LocalDateTime.now,
        Some(LocalDateTime.now),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty
  )

  val sampleReturnNotSubmittedPenaltyPeriod = PenaltyPeriod(
    LocalDateTime.now,
    LocalDateTime.now,
    Submission(
      LocalDateTime.now,
      None,
      SubmissionStatusEnum.Overdue
    )
  )

  val etmpDataWithOneLSP = ETMPPayload(
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
        period = Some(
          PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        ),
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
          PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        ),
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
          PenaltyPeriod(
            startDate = sampleDate,
            endDate = sampleDate,
            submission = Submission(
              dueDate = sampleDate,
              submittedDate = Some(sampleDate),
              status = SubmissionStatusEnum.Submitted
            )
          )
        ),
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

  val sampleLatePaymentPenaltyDataUnpaidVAT: Seq[LatePaymentPenalty] = Seq(
    sampleLatePaymentPenaltyUnpaidVAT
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
      Some(PenaltyPeriod(
        LocalDateTime.now,
        LocalDateTime.now,
        Submission(
          LocalDateTime.now,
          None,
          SubmissionStatusEnum.Overdue
        )
      )),
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

  val redirectToAppealUrlForLSP: String = controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isObligation = false, isAdditional = false).url

  val redirectToAppealUrlForLPP: String = controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = false, isAdditional = false).url

  val redirectToAppealObligationUrlForLSP: String = controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = false, isObligation = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPP: String = controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = true, isAdditional = false).url

  val redirectToAppealObligationUrlForLPPAdditional: String = controllers.routes.IndexController.redirectToAppeals(penaltyId, isLPP = true, isObligation = false, isAdditional = true).url

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)

  val vatTraderUser = User("123456789", arn = None)(fakeRequest)
  val agentUser = User("123456789", arn = Some("AGENT1"))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234"))
}
