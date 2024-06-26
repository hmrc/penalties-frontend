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

package config

import config.featureSwitches.FeatureSwitch._
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) {
  lazy val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String            = "en"
  val cy: String            = "cy"
  val defaultLanguage: Lang = Lang(en)

  lazy val vatOverviewUrl: String = servicesConfig.getString("urls.vatOverview")
  lazy val vatOverviewUrlAgent: String = servicesConfig.getString("urls.vatAgentClientLookUp")
  lazy val btaUrl: String = config.get[String]("urls.btaHomepage")
  lazy val penaltiesUrl: String = s"${servicesConfig.baseUrl("penalties")}/penalties"
  lazy val whatYouOweUrl: String = config.get[String]("urls.whatYouOwe")
  lazy val userResearchBannerUrl: String = config.get[String]("urls.userResearchBannerUrl")

  lazy val signInUrl: String = config.get[String]("signIn.url")

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = URLEncoder.encode(RedirectUrl(signInContinueBaseUrl + controllers.routes.IndexController.onPageLoad.url).unsafeValue, "UTF-8")

  lazy val signOutUrlUnauthorised: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val feedbackUrl: String = config.get[String]("urls.feedback")

  lazy val signOutUrl: String = config.get[String]("signOut.url") + feedbackUrl

  lazy val timeoutPeriod: Int = config.get[Int]("timeout.period")

  lazy val timeoutCountdown: Int = config.get[Int]("timeout.countDown")

  lazy val contactFrontendUrl: String = config.get[String]("urls.betaFeedbackUrl")

  lazy val contactFrontendServiceId: String = config.get[String]("contact-frontend.serviceId")

  def backUrl(url: String): String =  URLEncoder.encode(RedirectUrl(platformHost ++ url).unsafeValue, "UTF-8")

  def feedbackUrl(redirectUrl: String): String = s"$contactFrontendUrl?service=$contactFrontendServiceId&backUrl=${backUrl(redirectUrl)}"

  lazy val penaltiesAppealsBaseUrl: String = config.get[String]("urls.penaltiesAppealsBaseurl") + "/penalties-appeals"

  lazy val webChatUrl: String = config.get[String]("urls.askHmrcBaseUrl") + "/ask-hmrc/chat/vat-online?ds"

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  private lazy val platformHost = servicesConfig.getString("host")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => URLEncoder.encode(RedirectUrl(platformHost + uri).unsafeValue, "UTF-8")

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  def isFeatureSwitchEnabled(featureSwitch: FeatureSwitch): Boolean = config.get[Boolean](featureSwitch.name)

  lazy val penaltyChargeAmount: String = config.get[String]("penaltyChargeAmount")

  lazy val adjustmentLink: String = config.get[String]("urls.adjustmentUrl")

  lazy val lspGuidanceLink: String = config.get[String]("urls.lspGuidanceUrl")

  lazy val lppCalculationGuidanceLink: String = config.get[String]("urls.lppCalculationGuidance")



}


