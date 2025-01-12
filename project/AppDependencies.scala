import sbt._
object AppDependencies {

  private val bootstrapVersion = "9.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc-play-30"    % "11.8.0",
    "uk.gov.hmrc"     %% "internal-auth-client-play-30"  % "3.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"     %% "bootstrap-test-play-30"        % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                         % "1.18.3"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
