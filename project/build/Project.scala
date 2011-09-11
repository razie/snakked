import sbt._

class SnakkedPro(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {

  override def managedStyle = ManagedStyle.Maven
  val publishTo =
    if (version.toString endsWith "-SNAPSHOT")
      "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    else
      "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2.credentials", log)

  val SCALAVER = "2.9.1"
  val RAZBASEVER = "0.5" + (if (version.toString endsWith "-SNAPSHOT") "-SNAPSHOT" else "")

  def scalatest  = "org.scalatest" % "scalatest_2.9.1" % "1.6.1"
  def junit      = "junit" % "junit" % "4.5" % "test->default"
  def json       = "org.json" % "json" % "20090211"
  def jxpath     = "commons-jxpath" % "commons-jxpath" % "1.3"

  def razBase = "com.razie" %% "razbase"         % RAZBASEVER

  lazy val core = project("core", "snakk-core", new CoreProject(_))
  //lazy val web  = project("web",  "snakk-web",  new WebProject(_), core)
  //lazy val ui   = project("ui",   "snakk-ui",   new UiProject(_), core)

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) {
    override def libraryDependencies = Set(scalatest, junit, json, jxpath, razBase)
  }

  class WebProject(info: ProjectInfo) extends DefaultProject(info) {
    override def libraryDependencies = Set(scalatest, junit, json, razBase)
  }

  class UiProject(info: ProjectInfo) extends DefaultProject(info) {
    override def libraryDependencies = Set(scalatest, junit, json, razBase)
  }

  //class SwingProject(info: ProjectInfo) extends DefaultProject(info) {
    //def scalaSwing = "org.scala-lang" % "scala-swing" % SCALAVER
    //override def libraryDependencies = Set(scalatest, junit, scalaSwing)
  //}

}

