import sbt._
import Keys._

object V {
  val version      = "0.9.1" //-SNAPSHOT"
  val scalaVersion = "2.11.8" 
  val organization = "com.razie"

  def snap = (if (V.version endsWith "-SNAPSHOT") "-SNAPSHOT" else "")

  def RAZBASEVER = "0.9.1" + snap
}

object MyBuild extends Build {

  def scalatest = "org.scalatest" %% "scalatest"       % "2.1.3"
  def junit     = "junit"          % "junit"           % "4.5"      % "test->default"
  def json      = "org.json"       % "json"            % "20090211"
  def jxpath    = "commons-jxpath" % "commons-jxpath"  % "1.3"

  def scalaxml = "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
  
  def razBase   = "com.razie"     %% "base"            % V.RAZBASEVER


  lazy val root = Project(id="snakked",    base=file("."),
                          settings = defaultSettings ++ Seq()
                  ) aggregate (core) dependsOn (core)

  lazy val core = Project(id="snakk-core", base=file("core"),
                          settings = defaultSettings ++ 
                          Seq(libraryDependencies ++= Seq(scalatest, junit, json, jxpath, scalaxml, razBase))
                  )

  def defaultSettings = baseSettings ++ Seq()

  def baseSettings = Defaults.defaultSettings ++ Seq (
    scalaVersion         := V.scalaVersion,
    version              := V.version,
    organization         := V.organization,
    organizationName     := "Razie's Pub",
    organizationHomepage := Some(url("http://www.razie.com")),
    licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
    homepage := Some(url("http://www.razie.com")),

    publishTo <<= version { (v: String) =>
      if(v endsWith "-SNAPSHOT")
        Some ("Sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/")
      else
        Some ("Sonatype" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    } ,

    resolvers ++= Seq(
      "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "https://oss.sonatype.org/content/repositories/public") 
    )

}
