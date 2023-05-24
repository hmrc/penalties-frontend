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

package views.components

import base.SpecBase
import config.AppConfig
import config.featureSwitches.ShowURBanner
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import views.behaviours.ViewBehaviours
import views.html.components.Header

class HeaderSpec extends SpecBase with ViewBehaviours {
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  val headerHtml: Header = injector.instanceOf[Header]

  when(mockAppConfig.feedbackUrl(any())).thenReturn("http://example.com")

  "Header" should {
    "display the UR banner" when {
      "the show UR banner feature is enabled and the 'showURBannerIfEnabled' parameter is true" in {
        when(mockAppConfig.isFeatureSwitchEnabled(Matchers.eq(ShowURBanner))).thenReturn(true)
        when(mockAppConfig.userResearchBannerUrl).thenReturn("http://user-research.com")
        val html = headerHtml.apply(mockAppConfig, showURBannerIfEnabled = true)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-user-research-banner__title").text() shouldBe "Help make GOV.UK better"
        htmlDocument.select(".hmrc-user-research-banner__link").text() shouldBe "Sign up to take part in research (opens in new tab)"
      }
    }

    "not display the UR banner" when {
      "the show UR banner feature is disabled" in {
        when(mockAppConfig.isFeatureSwitchEnabled(Matchers.eq(ShowURBanner))).thenReturn(false)
        val html = headerHtml.apply(mockAppConfig, showURBannerIfEnabled = true)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-user-research-banner__title").isEmpty shouldBe true
        htmlDocument.select(".hmrc-user-research-banner__link").isEmpty shouldBe true
      }

      "the 'showURBannerIfEnabled' parameter is false" in {
        when(mockAppConfig.isFeatureSwitchEnabled(Matchers.eq(ShowURBanner))).thenReturn(true)
        val html = headerHtml.apply(mockAppConfig, showURBannerIfEnabled = false)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-user-research-banner__title").isEmpty shouldBe true
        htmlDocument.select(".hmrc-user-research-banner__link").isEmpty shouldBe true
      }

      "both feature and parameter return false" in {
        when(mockAppConfig.isFeatureSwitchEnabled(Matchers.eq(ShowURBanner))).thenReturn(false)
        val html = headerHtml.apply(mockAppConfig, showURBannerIfEnabled = false)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-user-research-banner__title").isEmpty shouldBe true
        htmlDocument.select(".hmrc-user-research-banner__link").isEmpty shouldBe true

      }
    }
  }
}
