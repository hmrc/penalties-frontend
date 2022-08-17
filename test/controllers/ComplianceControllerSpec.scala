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

import base.SpecBase
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.ComplianceService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import viewmodels.TimelineHelper
import views.html.ComplianceView

import scala.concurrent.{ExecutionContext, Future}

class ComplianceControllerSpec extends SpecBase {
  val mockComplianceService: ComplianceService = mock(classOf[ComplianceService])
  val page: ComplianceView = injector.instanceOf[ComplianceView]
  override val timelineHelper: TimelineHelper = injector.instanceOf[TimelineHelper]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global


  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    reset(mockComplianceService)
    when(mockComplianceService.getDESComplianceData(any())(any(), any(), any(), any())).thenReturn(Future.successful(Some(sampleCompliancePayload)))
  }

  object Controller extends ComplianceController(
    page,
    mockComplianceService,
    timelineHelper
  )(implicitly, implicitly, authPredicate, errorHandler, stubMessagesControllerComponents())

  "onPageLoad" should {

    "the user is authorised" must {
      "return OK - calling the service to retrieve compliance data to the view" in
        new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest.withSession(
          SessionKeys.pocAchievementDate -> "2022-01-01"
        ))
        status(result) shouldBe OK
      }

      "return ISE - if the service returns None" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockComplianceService.getDESComplianceData(any())(any(), any(), any(), any())).thenReturn(Future.successful(None))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return ISE (exception thrown) - try retrieving the compliance data but failing" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockComplianceService.getDESComplianceData(any())(any(), any(), any(), any())).thenReturn(Future.failed(new Exception("Something went wrong.")))
        val result: Exception = intercept[Exception](await(Controller.onPageLoad()(fakeRequest)))
        result.getMessage shouldBe "Something went wrong."

      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
