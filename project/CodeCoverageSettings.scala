import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  val settings: Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageExcludedPackages:= ".*Reverse.*;.*Routes.*;view.*",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
