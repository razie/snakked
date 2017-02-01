package snakking.test

object SnakkDITest extends App {
  razie.Log.silent(true)

  import razie.Snakk._
  val installation = xml(url("file:xmlcfg/installation.xml"))
  for (
    m     <- installation \\ "module[name ~= 'sbjea.*']";
    jea    = xml(url("file:xmlcfg/distribution/jea/" + (m \ "@name") + ".xml"));
    mb:Int = (jea \\ "initParam[name == 'maxBeansInPool']" \@@ "value") OR ("0");
    mc:Int = (jea \\ "initParam[name == 'max_conn']" \@@ "value") OR (jea \\ "conn").size.toString
  ) {
    if (mb >= mc) println("OK:  ", m \ "@name", mb, mc)
    else println("ERR: ", m \ "@name", mb, mc)
  }
}

