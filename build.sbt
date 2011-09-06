name := "snakked"

organization := "com.razie"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.0-1"

retrieveManaged := true // copy libs in lib_managed

publishTo <<= version { (v: String) =>
  if(v endsWith "-SNAPSHOT")
    Some(ScalaToolsSnapshots)
  else
    Some(ScalaToolsReleases)
}

credentials += Credentials((Path.userHome / ".ivy2.credentials").asFile) 

