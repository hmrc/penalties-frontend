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
import models.ETMPPayload
import models.financial.Financial
import models.penalty.PenaltyPeriod
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
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
import viewmodels.{SummaryCard, SummaryCardHelper, TimelineHelper}
import views.html.errors.Unauthorised

import java.time.LocalDateTime
import models.compliance.{CompliancePayload, MissingReturn, Return}

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
    0,
    0,
    0,
    0.0,
    0.0,
    4,
    Seq.empty[PenaltyPoint]
  )

  val sampleComplianceData: CompliancePayload = CompliancePayload(
    "0",
    "0",
    LocalDateTime.now(),
    Seq.empty[MissingReturn],
    Seq.empty[Return]
  )

  val samplePenaltyPoint = PenaltyPoint(
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
        Some(LocalDateTime.now),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty
  )

  val sampleFinancialPenaltyPoint = PenaltyPoint(
    PenaltyTypeEnum.Financial,
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
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00, dueDate = LocalDateTime.now()
      )
    )
  )

  val sampleOverduePenaltyPoint = PenaltyPoint(
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

  val samplePenaltyPointAppealedUnderReview = PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    Some(AppealStatusEnum.Under_Review),
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
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty
  )

  val samplePenaltyPointAppealedAccepted = samplePenaltyPointAppealedUnderReview.copy(appealStatus = Some(AppealStatusEnum.Accepted))

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


  val sampleReturnSubmittedPenaltyPointData: Seq[PenaltyPoint] = Seq(
    samplePenaltyPoint
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

  val sampleSummaryCard: SummaryCard = SummaryCard(
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

  val redirectToAppealUrl: String = controllers.routes.IndexController.redirectToAppeals(penaltyId).url

  val sampleDate: LocalDateTime = LocalDateTime.of(2021, 4, 23, 18, 25, 43)
    .plus(511, ChronoUnit.MILLIS)
}
