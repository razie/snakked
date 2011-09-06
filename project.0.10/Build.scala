import sbt._

object MyBuild extends Build {

  val SCALAVER = "2.9.0-1"
  val RAZBASEVER = "0.4-SNAPSHOT"

  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"
  val junit = "junit" % "junit" % "4.5"
  val json = "org.json" % "json" % "20090211"

  val razBase = "com.razie" %% "razbase"         % RAZBASEVER


  lazy val root = Project("snakked", file(".")) aggregate(core, web, ui)

  lazy val core = Project("core", file("core")) 

  lazy val web = Project("web", file("web")) dependsOn (core)

  lazy val ui = Project("ui", file("ui")) dependsOn (core)
}

