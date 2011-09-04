import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {

  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  //val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2.credentials", log)

  val SCALAVER = "2.9.0-1"
  val RAZBASEVER = "0.4-SNAPSHOT"

  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1"
  val junit      = "junit" % "junit" % "4.5" % "test->default"
  val json       = "org.json" % "json" % "20090211"

  val razBase = "com.razie" %% "razbase"         % RAZBASEVER
}

