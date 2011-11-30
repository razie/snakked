
name := "snakked"

addSbtPlugin("net.databinder" % "posterous-sbt" % "0.3.2")

retrieveManaged := true // copy libs in lib_managed

seq(posterousSettings :_*)

(email in Posterous) := Some("y...@example.com")

(password in Posterous) := Some("yourpassword")

