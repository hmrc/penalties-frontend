import sbt.*

object AppDependencies {

  lazy val bootstrapVersion = "10.7.1"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "13.11.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"  % bootstrapVersion % "test, it",
    "org.jsoup"    % "jsoup"                   % "1.23.1"         % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "2.2.3"          % "test, it",
    "org.mockito"  % "mockito-core"            % "5.23.0"         % "test, it"
  )
}
