import sbt.*

object AppDependencies {

  lazy val bootstrapVersion = "10.0.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"  % bootstrapVersion % "test, it",
    "org.jsoup"    % "jsoup"                   % "1.21.1"         % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "2.0.0"          % "test, it",
    "org.mockito"  % "mockito-core"            % "5.18.0"         % "test, it"
  )
}
