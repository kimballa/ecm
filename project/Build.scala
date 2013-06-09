import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ecm"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    // https://github.com/timmolter/XChange
    "com.xeiam.xchange" % "xchange-core" % "1.7.0",
    "com.xeiam.xchange" % "xchange-mtgox" % "1.7.0"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
