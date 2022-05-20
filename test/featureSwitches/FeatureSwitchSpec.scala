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

package featureSwitches

import base.SpecBase
import config.AppConfig
import org.mockito.Matchers.{any, eq => equalTo}
import org.mockito.Mockito.{mock, reset, when}

class FeatureSwitchSpec extends SpecBase {

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup {
    reset(mockAppConfig)
    val featureSwitching: FeatureSwitching = new FeatureSwitching {
      override implicit val appConfig: AppConfig = mockAppConfig
    }
  }

  "FeatureSwitch listOfAllFeatureSwitches" should {
    "be all the featureswitches in the app" in {
      FeatureSwitch.listOfAllFeatureSwitches shouldBe List(CallAPI1812ETMP, UseAPI1812Model)
    }
  }

  "return true if CallAPI1812ETMP feature switch is enabled" in new Setup {
    when(mockAppConfig.isFeatureSwitchEnabled(equalTo(CallAPI1812ETMP.name)))
      .thenReturn(true)
    featureSwitching.isEnabled(CallAPI1812ETMP) shouldBe true
  }

  "return false if CallAPI1812ETMP feature switch is disabled" in new Setup {
    when(mockAppConfig.isFeatureSwitchEnabled(equalTo(CallAPI1812ETMP.name)))
      .thenReturn(false)
    featureSwitching.isEnabled(CallAPI1812ETMP) shouldBe false
  }

  "return true if UseAPI1812Model feature switch is enabled" in new Setup {
    when(mockAppConfig.isFeatureSwitchEnabled(equalTo(UseAPI1812Model.name)))
      .thenReturn(true)
    featureSwitching.isEnabled(UseAPI1812Model) shouldBe true
  }

  "return false if UseAPI1812Model feature switch is disabled" in new Setup {
    when(mockAppConfig.isFeatureSwitchEnabled(equalTo(UseAPI1812Model.name)))
      .thenReturn(false)
    featureSwitching.isEnabled(UseAPI1812Model) shouldBe false
  }
}
