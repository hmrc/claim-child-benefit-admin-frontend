import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

//  private val excludedPackages: Seq[String] = Seq(
//    "<empty>",
//    "Reverse.*",
//    "uk.gov.hmrc.BuildInfo",
//    "app.*",
//    "prod.*",
//    ".*Routes.*",
//    "testOnly.*",
//    "testOnlyDoNotUseInAppConf.*"
//  )

  val settings: Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageExcludedPackages:= ".*Reverse.*;.*Routes.*;view.*",
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
