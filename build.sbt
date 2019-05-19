
scalaVersion := "2.11.12"

name := "snakked"

lazy val commonSettings = Seq(
  organization := "com.razie",
  version := "0.9.3-SNAPSHOT",
  scalaVersion := "2.11.12",

  organizationName     := "DieselApps",
  organizationHomepage := Some(url("http://www.dieselapps.com")),
  licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
  homepage := Some(url("http://www.dieselapps.com"))
)

libraryDependencies in Global ++= Seq(
  "org.json"                % "json"               % "20160810",

  "commons-jxpath"          % "commons-jxpath"     % "1.3",
  "org.scala-lang.modules" %% "scala-xml"          % "1.0.3",

  "ch.qos.logback"          % "logback-classic"    % "1.0.13",

  "junit"                   % "junit"              % "4.5"      % "test->default",
  "org.scalatest"          %% "scalatest"          % "2.1.3"
)

lazy val root = (project in file("."))
  .settings(
    commonSettings
  )
  .aggregate(snakk_base, snakk_core)

lazy val snakk_base = (project in file("base"))
  .settings(
    commonSettings //, libraryDependencies ++= deps
  )

lazy val snakk_core = (project in file("core"))
  .settings(
    commonSettings  //, libraryDependencies ++= deps
  )
  .dependsOn(snakk_base)

retrieveManaged := true // copy libs in lib_managed

resolvers ++= Seq(
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/public"
)

publishTo in Global := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

//pomIncludeRepository := { x => false }

pomExtra := (
  <scm>
    <url>git@github.com:razie/snakked.git</url>
    <connection>scm:git:git@github.com:razie/snakked.git</connection>
  </scm>
  <developers>
    <developer>
      <id>razie</id>
      <name>Razvan Cojocaru</name>
      <url>http://www.dieselapps.com</url>
    </developer>
  </developers>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
</licenses>
)


