/*
 * Copyright 2023 HM Revenue & Customs
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

package config.featureSwitches

import base.SpecBase
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterAll
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.language.postfixOps

class FeatureSwitchingSpec extends SpecBase with BeforeAndAfterAll with FeatureSwitching {
  val mockConfig = mock(classOf[Configuration])
  val mockServicesConfig = mock(classOf[ServicesConfig])
  implicit val config: AppConfig = new AppConfig(mockConfig, mockServicesConfig)

  class Setup {
    reset(mockConfig)
    reset(mockServicesConfig)
    val featureSwitching: FeatureSwitching = new FeatureSwitching {
    }
    sys.props -= featureSwitching.TIME_MACHINE_NOW
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    sys.props -= TIME_MACHINE_NOW
  }

  "constants" should {
    "be true and false" in new Setup {
      featureSwitching.FEATURE_SWITCH_ON shouldBe "true"
      featureSwitching.FEATURE_SWITCH_OFF shouldBe "false"
    }
  }

  "isEnabled" should {
    FeatureSwitch.featureSwitches.foreach(
      featureSwitch => {
        s"return true if ${featureSwitch.name} is enabled" in new Setup {
          featureSwitching.enableFeatureSwitch(featureSwitch)
          featureSwitching.isEnabled(featureSwitch) shouldBe true
        }

        s"return false if ${featureSwitch.name} is disabled" in new Setup {
          featureSwitching.disableFeatureSwitch(featureSwitch)
          featureSwitching.isEnabled(featureSwitch) shouldBe false
        }

        s"return false if ${featureSwitch.name} does not exist" in new Setup {
          featureSwitching.isEnabled(featureSwitch) shouldBe false
        }
      }
    )
  }

  "enableFeatureSwitch" should {
    FeatureSwitch.featureSwitches.foreach(
      featureSwitch => {
        s"set ${featureSwitch.name} property to true" in new Setup {
          featureSwitching.enableFeatureSwitch(featureSwitch)
          (sys.props get featureSwitch.name get) shouldBe "true"
        }
      }
    )
  }

  "disableFeatureSwitch" should {
    FeatureSwitch.featureSwitches.foreach(
      featureSwitch => {
        s"set ${featureSwitch.name} property to false" in new Setup {
          featureSwitching.disableFeatureSwitch(featureSwitch)
          (sys.props get featureSwitch.name get) shouldBe "false"
        }
      }
    )
  }

  "FeatureSwitching setFeatureDate" should {
    lazy val dateNow: LocalDate = LocalDate.now()
    s"set the date when the parameter is $Some" in new Setup {
      val dateMinus1Day: LocalDate = dateNow.minusDays(1)
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe None
      featureSwitching.setFeatureDate(Some(dateMinus1Day))
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe Some(dateMinus1Day.toString)
    }

    s"overwrite an existing date when the parameter is $Some" in new Setup {
      val dateMinus1Day: LocalDate = dateNow.minusDays(1)
      val dateMinus2Days: LocalDate = dateNow.minusDays(2)
      featureSwitching.setFeatureDate(Some(dateMinus1Day))
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe Some(dateMinus1Day.toString)
      featureSwitching.setFeatureDate(Some(dateMinus2Days))
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe Some(dateMinus2Days.toString)
    }

    s"remove an existing date when the parameter is $None" in new Setup {
      featureSwitching.setFeatureDate(Some(dateNow))
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe Some(dateNow.toString)
      featureSwitching.setFeatureDate(None)
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe None
    }
  }

  "FeatureSwitching getFeatureDate" should {
    lazy val dateNowMinus1Day: LocalDate = LocalDate.now().minusDays(1)
    s"get the date when it exists in system properties" in new Setup {
      featureSwitching.setFeatureDate(Some(dateNowMinus1Day))
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe Some(dateNowMinus1Day.toString)
      featureSwitching.getFeatureDate shouldBe dateNowMinus1Day
    }

    s"get the date from config when the key value exists and is non-empty" in new Setup {
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe None
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(Some(dateNowMinus1Day.toString))
      featureSwitching.getFeatureDate shouldBe dateNowMinus1Day
    }

    s"get the date from the system when it does not exist in properties nor in config (value empty)" in new Setup {
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe None
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(Some(""))
      featureSwitching.getFeatureDate shouldBe LocalDate.now()
    }

    s"get the date from the system when it does not exist in properties nor in config (kv not present)" in new Setup {
      (sys.props get featureSwitching.TIME_MACHINE_NOW) shouldBe None
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(None)
      featureSwitching.getFeatureDate shouldBe LocalDate.now()
    }
  }
}
