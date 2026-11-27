/*
 * Copyright 2026 HM Revenue & Customs
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

package views.layouts

import base.SpecBase
import config.featureSwitches.FeatureSwitch.ShowUserResearchBanner
import config.featureSwitches.FeatureSwitching
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.views.Aliases.UserResearchBanner
import utils.UrBannerUtil
import views.html.layouts.Layout

class LayoutSpec extends SpecBase with FeatureSwitching with BeforeAndAfterAll {

  val layout: Layout = injector.instanceOf[Layout]

  object Selectors {
    val banner = ".hmrc-user-research-banner"
    val bannerLink = ".hmrc-user-research-banner a.hmrc-user-research-banner__link"
    val bannerCloseButton = ".hmrc-user-research-banner__close"
  }

  def view(urBanner: Option[UserResearchBanner]): Document =
    Jsoup.parse(layout("Title", urBanner = urBanner)(Html(""))(fakeRequest, messages, appConfig).toString)

  override def afterAll(): Unit = {
    sys.props -= ShowUserResearchBanner.name
    super.afterAll()
  }

  "Layout" when {

    "the ShowUserResearchBanner feature switch is enabled" should {

      "show the banner without the close button when hideCloseButton is true" in {
        enableFeatureSwitch(ShowUserResearchBanner)
        val doc = view(Some(UrBannerUtil.getUrBanner()(appConfig, messages)))
        doc.select(Selectors.banner).size shouldBe 1
        doc.select(Selectors.bannerLink).attr("href") shouldBe appConfig.userResearchBannerUrl
        doc.select(Selectors.bannerCloseButton).size shouldBe 0
      }

      "show the banner with the close button when hideCloseButton is false" in {
        enableFeatureSwitch(ShowUserResearchBanner)
        val doc = view(Some(UrBannerUtil.getUrBanner(hideCloseButton = false)(appConfig, messages)))
        doc.select(Selectors.banner).size shouldBe 1
        doc.select(Selectors.bannerCloseButton).size shouldBe 1
      }

      "not show the banner when no banner is passed in" in {
        enableFeatureSwitch(ShowUserResearchBanner)
        view(None).select(Selectors.banner).size shouldBe 0
      }
    }

    "the ShowUserResearchBanner feature switch is disabled" should {

      "not show the banner" in {
        disableFeatureSwitch(ShowUserResearchBanner)
        view(Some(UrBannerUtil.getUrBanner()(appConfig, messages))).select(Selectors.banner).size shouldBe 0
      }
    }
  }
}
