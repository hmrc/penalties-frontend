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

import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import config.{AppConfig, ErrorHandler}
import org.scalamock.scalatest.MockFactory
import controllers.predicates.AuthPredicate
import models.ETMPPayload
import models.point.PenaltyPoint
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar.mock
import play.twirl.api.Html
import services.AuthService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.govukfrontend.views.Aliases.Tag
import viewmodels.SummaryCard
import views.html.errors.Unauthorised

import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends WordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val injector = app.injector

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

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
    0.0,
    0.0,
    0.0,
    4,
    Seq.empty[PenaltyPoint]
  )

  val sampleSummaryCard: SummaryCard = SummaryCard(
    Seq.empty,
    Tag.defaultObject,
    "1"
  )

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())
}
