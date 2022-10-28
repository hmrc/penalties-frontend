import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % "7.2.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "3.24.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28"         % "7.2.0"             % "test, it",
    "org.jsoup"                    %  "jsoup"                          % "1.14.3"             % "test, it",
    "org.mockito"                  %  "mockito-all"                    % "1.10.19"            % "test, it"
  )
}
