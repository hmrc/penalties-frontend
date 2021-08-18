import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % "5.12.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "0.94.0-play-28",
    "uk.gov.hmrc"             %% "play-frontend-govuk"        % "0.84.0-play-28",
    "uk.gov.hmrc"             %% "play-language"              % "5.1.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"         % "5.12.0"            % "test, it",
    "org.scalatest"           %% "scalatest"                      % "3.2.9"            % "test, it",
    "org.jsoup"               %  "jsoup"                          % "1.14.2"           % "test, it",
    "com.typesafe.play"       %% "play-test"                      % PlayVersion.current  % "test, it",
    "org.mockito"             %  "mockito-all"                    % "1.10.19"          % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"             % "5.1.0"            % "test, it",
    "com.vladsch.flexmark"     %  "flexmark-all"                    % "0.36.8"           % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"                  % "2.26.3"           % "it",
    "org.scalamock"           %% "scalamock-scalatest-support"    % "3.6.0"            % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test"       % "1.1.0-play-28"    % "test, it"
  )
}
