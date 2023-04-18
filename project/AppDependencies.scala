import sbt._

object AppDependencies {

  lazy val bootstrapVersion = "7.15.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "7.3.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28"         % bootstrapVersion     % "test, it",
    "org.jsoup"                    %  "jsoup"                          % "1.15.3"             % "test, it",
    "org.mockito"                  %  "mockito-all"                    % "1.10.19"            % "test, it"
  )
}
