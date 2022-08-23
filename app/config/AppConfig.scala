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

package config

import config.featureSwitches.FeatureSwitch
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

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

  lazy val signInUrl: String = config.get[String]("signIn.url")

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl + controllers.routes.IndexController.onPageLoad.url).encodedUrl

  lazy val signOutUrl: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val timeoutPeriod: Int = config.get[Int]("timeout.period")

  lazy val timeoutCountdown: Int = config.get[Int]("timeout.countDown")

  lazy val contactFrontendHost: String = s"${servicesConfig.baseUrl("contact-frontend")}/"
  val contactFrontendStartUrl: String = "feedback.url"
  val contactFrontendServiceId: String = "contact-frontend.serviceId"
  lazy val feedbackUrl: String =
    contactFrontendHost +
      config.get[String](contactFrontendStartUrl) +
      "?service=" + config.get[String](contactFrontendServiceId) +
      s"&backUrl=" + config.get[String]("host") + "/penalties"

  lazy val penaltiesAppealsBaseUrl: String = config.get[String]("urls.penaltiesAppealsBaseurl") + "/penalties-appeals"

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  private lazy val platformHost = servicesConfig.getString("host")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => SafeRedirectUrl(platformHost + uri).encodedUrl

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  def isFeatureSwitchEnabled(featureSwitch: FeatureSwitch): Boolean = config.get[Boolean](featureSwitch.name)
}
