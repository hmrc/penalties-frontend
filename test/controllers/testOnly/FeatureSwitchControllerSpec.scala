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

package controllers.testOnly

import base.SpecBase
import config.AppConfig
import config.featureSwitches.{FeatureSwitch, FeatureSwitching}
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

class FeatureSwitchControllerSpec extends SpecBase with FeatureSwitching {
  val controller: FeatureSwitchController = injector.instanceOf[FeatureSwitchController]
  val mockConfig = mock(classOf[Configuration])
  val mockServicesConfig = mock(classOf[ServicesConfig])
  val config: AppConfig = new AppConfig(mockConfig, mockServicesConfig)

  class Setup {
    reset(mockConfig)
    reset(mockServicesConfig)
    val controller = new FeatureSwitchController(mcc)(config)
    FeatureSwitch.listOfAllFeatureSwitches.foreach(sys.props -= _.name)
    sys.props -= TIME_MACHINE_NOW
  }

  "enableOrDisableFeature" should {
    "return NOT FOUND when the feature switch is not defined" in {
      val result = controller.enableOrDisableFeature("fake", true)(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }
  }

  "setTimeMachineDate" should {

    s"return $NOT_FOUND (NOT_FOUND) when the date provided is invalid" in new Setup {
      val result = controller.setTimeMachineDate(Some("invalid date"))(FakeRequest())
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "The date provided is in an invalid format"
    }

    s"return $OK (OK) when the date provided is valid" in new Setup {
      val result = controller.setTimeMachineDate(Some("2022-01-01"))(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.of(2022,1,1).toString}"
      (sys.props get "TIME_MACHINE_NOW") shouldBe Some(LocalDate.of(2022,1,1).toString)
    }

    s"return $OK (OK) and the systems current date when no date is provided" in new Setup {
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(None)
      val result = controller.setTimeMachineDate(None)(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.now().toString}"
      controller.getFeatureDate shouldBe LocalDate.now()
    }
  }
}