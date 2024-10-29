/**
  *  ____    __    ____  ____  ____,,___     ____  __  __  ____
  * ( __ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \          Read
  *  )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <    README.txt
  * (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/   LICENSE.txt
  **/
package razie.snakked

import java.net.{HttpURLConnection, InetAddress, URL}
import com.razie.pub.comms.Comms
import com.razie.pub.util.Base64
import java.io.IOException
import razie.{Snakk, SnakkRequest, SnakkResponse}
import scala.collection.{JavaConverters, mutable}

/** a snakking proxy
  *
  * snakk.proxy.env = unique environment code
  * snakk.proxy.sources = URL to ping for snakk requests, separated by commas, like:
  * snakk.proxy.dests = snakk destinations managed by this proxy - selector when pinging
  *
  * sample:
  * -D snakk.proxy.env=345345345345434
  * -D snakk.proxy.sources=http://specs.dieselapps.com:9000
  * -D snakk.proxy.dests=localhost:9000
  *
  * testing:
  * -D snakk.proxy.env=raz
  * -D snakk.proxy.sources=http://localhost:9000
  * -D snakk.proxy.dests=specs.dieselapps.com:9000
  */
object SnakkProxyRemote {

  var name : String = InetAddress.getLocalHost.getHostAddress.replaceAllLiterally(".", "_")
  var dests : Array[String] = Array()
  var sources : Array[String] = Array()

  var SLEEP1 : Int = 1000 // short sleep
  var SLEEP2 : Int = 5000 // long sleep
  var DELAY : Int = 10000 // for short sleep
  var RESTART : Int = 120000 // for testing

  @volatile var isActive = false
  @volatile var counter = 0

  def dets (name:String, dflt:String) : String = {
    val res = System.getProperty("env."+name, dflt)
    log (s"env prop: $name = $res")
    res
  }

  /** main entry point */
  def main (args : Array[String]): Unit = {
    // are arguments set?
    name     = dets("snakk_proxy_env", name)
    dests    = dets("snakk_proxy_dests", "").split(",")
    sources  = dets("snakk_proxy_sources", "").split(",")
    SLEEP1   = dets("snakk_proxy_sleep1", "1000").toInt
    SLEEP2   = dets("snakk_proxy_sleep2", "5000").toInt
    DELAY    = dets("snakk_proxy_delay", "10000").toInt
    RESTART  = dets("snakk_proxy_restart", "120000").toInt

    log("ARGS: " + args.mkString)

    for(
      arg <- args
    ) {
      if(arg contains "snakk.proxy.env") name = arg.split("=").last;
      if(arg contains "snakk.proxy.dests") dests = arg.split("=").last.split(",")
      if(arg contains "snakk.proxy.sources") sources = arg.split("=").last.split(",")
      if(arg contains "snakk.proxy.sleep1") SLEEP1 = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.sleep2") SLEEP2 = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.delay") DELAY = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.restart") RESTART = arg.split("=").last.toInt
    }

    mainLoop()
  }

  /** main loop of proxy. If it ends, restart it... */
  def mainLoop (): Unit = {
    isActive = true
    counter = 0

    log("env: " + name);
    log("dests: " + dests.mkString(","))
    log("sources: " + sources.mkString(","))

    var sleep = SLEEP2
    var lastTime = System.currentTimeMillis() - DELAY - 1 // go straight to long sleep mode
    val firstTime = System.currentTimeMillis()

    log("starting loop")
    while (System.currentTimeMillis() - firstTime < RESTART) {
      var hadOne = false // when true, it won't sleep

      try {
        for (
          dest <- dests;
          source <- sources
        )
          if(checkAndProxy(dest, source)) {
            lastTime = System.currentTimeMillis()
            hadOne = true
          }
      } catch {
        case t  : Throwable => log(t.toString)
      }

      // short sleep or long sleep. Also SLEEP2/5 will get there gradually
      if(hadOne) sleep = 0
      else if(System.currentTimeMillis() - lastTime > DELAY && sleep < SLEEP2) sleep = sleep + SLEEP2/5
      else if(System.currentTimeMillis() - lastTime > DELAY ) sleep = SLEEP2
      else sleep=SLEEP1

      log("... sleep "+sleep/1000)
      if(sleep > 0) Thread.sleep(sleep)
    }
    log("ending mainLoop")
    isActive = false
  }

  /** check one destination for one source and if any, do proxy and return true */
  private def checkAndProxy(dest:String, source:String) : Boolean = {
    log(s"Checking $source for any requests for $name and $dest")
    val proto = if(source.startsWith("http")) "" else "http://"
    val resp = Snakk.body(Snakk.url(s"$proto$source/snakk/check/$name/$dest"))

    if(resp.length > 1) {
      log(s"... got $resp")

      val rq = Snakk.requestFromJson(resp)

      // handle on separate thread to relese the main
      // todo join all these before shutting down every 2 minutes
      razie.Threads.fork {
        val r = doProxy(rq)

        val content = razie.js.tojsons(r.toJson) + Snakk.SSS + r.content

        // send result and complete request
        Snakk.body(Snakk.url(proto + source + "/snakk/complete/" + rq.id, Map.empty, "POST"), Some(content))
      }
      return true
    }
    else {
      log(s"... got nothing")
      return false
    }
  }

  /** proxy one request */
  def doProxy (rq:SnakkRequest, encode64:Boolean = true) : SnakkResponse = {
//    var uc:java.net.URLConnection = null
    var uc:java.net.HttpURLConnection = null

    try {
      log(s"... snakking ${rq.url}")
      val u = rq.protocol + "://" + rq.url

      // make the call
      uc = (new URL(u).openConnection).asInstanceOf[HttpURLConnection]
      // disable auto-redirect, to force the remote browser to do the redirect and RESEND proper cookies
      uc.setInstanceFollowRedirects(false)
      uc.setConnectTimeout(5000)
      uc.setReadTimeout(50000)

      for (a <- rq.headerSeq) {
        uc.setRequestProperty(a._1, a._2)
      }

      // overwrite to disable gzip encoding
      //https://stackoverflow.com/questions/12321455/what-encoding-string-tells-a-web-server-not-to-send-gzip-content
      uc.setRequestProperty("accept-encoding", "identity")

      uc.setRequestMethod(rq.method)
      if (rq.method == "POST" || rq.method == "PUT") {
        uc.setDoOutput(true) // Triggers POST.

        try {
          val os = uc.getOutputStream
          try {
            val input = rq.content.getBytes("utf-8")
            os.write(input, 0, input.length)
          } finally if (os != null) os.close()
        } finally {}

      };

      log("...hdr: " + uc.getHeaderFields)

      val resCode = uc.getHeaderField(0)

      val head = new mutable.HashMap[String, List[String]]()
      def headVal (name:String) : Option[String] = {
        head.get(name).orElse(head.get(name.toLowerCase)).flatMap(_.headOption)
      }


      // flatten headers into a map
      import scala.collection.JavaConverters
      for (x <- JavaConverters.asScalaIterator(uc.getHeaderFields.entrySet().iterator()))
        if (x.getKey != null)
          head.put(x.getKey, JavaConverters.asScalaBuffer(x.getValue).toList)

      val in = uc.getInputStream

      val rc = Comms.getResponseCode(uc)

      if (!resCode.endsWith("200 OK")) {
        // todo - do something terrible
      }

      // read bytes to use UTF-8 encoding rather than jvm default
      val response = Comms.readStreamBytes(in)

      log(s"... response size is ${response.getData.length} bytes")

      // ---------------------------- prepare response

      // content type and encoding
      val ctype = headVal("Content-Type").getOrElse("")
      val zip = headVal("Content-Encoding").getOrElse("").contains("zip")
      val content =
        if (Snakk.isText(ctype) && !zip) {
          val x = response.toString
          log(s"... response content is ${first100(x)}")
          x
        }
        else {
          log ("---- SNAKKPROXY response is binary, encoding...")
          "SNAKK64" + Base64.encodeBytes(response.getData)
        }

      val setc = headVal("Set-Cookie").getOrElse("")

      // set-cookies (don't use local domain, use remote

      val r = SnakkResponse (resCode, rc, head.toMap, content, ctype, rq.id)

      log("... sending response: " + r)
      return r
    } catch {
      case io:IOException => {
        log("IOException: " + io.toString)
        log("...hdr: " + uc.getHeaderFields)

        val resCode = uc.getHeaderField(0)
        val rc = Comms.getResponseCode(uc)

        // flatten headers into a map
        val head = new mutable.HashMap[String, List[String]]()
        for (x <- JavaConverters.asScalaIterator(uc.getHeaderFields.entrySet().iterator()))
          if (x.getKey != null)
            head.put(x.getKey, JavaConverters.asScalaBuffer(x.getValue).toList)

        def headVal (name:String) : Option[String] = {
          head.get(name).orElse(head.get(name.toLowerCase)).flatMap(_.headOption)
        }

        val ctype = headVal("Content-Type").getOrElse("")

        val r = SnakkResponse (resCode, rc, head.toMap, "", ctype, rq.id)

        log("... sending response: " + r)
        return r
      }
    }
  }


  def log (s:String) : Unit = {
    println("SNAKKP-REMOTE " +  s);
  }

  private def first100(s:String) = {
    if(s.length > 100) s.substring(0,100)
    else s
  }

}
