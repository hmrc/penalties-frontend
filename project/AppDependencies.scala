import sbt._

object AppDependencies {

  lazy val bootstrapVersion = "8.6.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30"         % "8.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"         % bootstrapVersion     % "test, it",
    "org.jsoup"                    %  "jsoup"                          % "1.16.2"             % "test, it",
    "org.mockito"                  %  "mockito-all"                    % "1.10.19"            % "test, it"
  )
}
