import sbt._
object AppDependencies {

  private val bootstrapVersion = "8.4.0"

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"    % "8.3.0",
    "uk.gov.hmrc"           %% "internal-auth-client-play-30"  % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "org.mockito"             %% "mockito-scala"              % "1.16.42",
    "org.jsoup"               %  "jsoup"                      % "1.13.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
