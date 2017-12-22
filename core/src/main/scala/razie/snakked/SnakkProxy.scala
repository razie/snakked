/**
  *  ____    __    ____  ____  ____,,___     ____  __  __  ____
  * ( __ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \          Read
  *  )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <    README.txt
  * (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/   LICENSE.txt
  **/
package razie.snakked

import java.net.URL

import com.razie.pub.comms.Comms
import com.razie.pub.util.Base64
import razie.{Snakk, SnakkRequest, SnakkResponse}

import scala.collection.mutable

/** a snakking proxy
  *
  * snakk.proxy.sources = URL to ping for snakk requests, separated by commas, like:
  * snakk.proxy.dests = snakk destinations managed by this proxy - selector when pinging
  *
  * -D snakk.proxy.dests=http://host1.me.com:9000,http://host2.me.com:9000
  *
  */
object SnakkProxy {

  var dests : Array[String] = Array()
  var sources : Array[String] = Array()

  var SLEEP1 : Int = 1000 // short sleep
  var SLEEP2 : Int = 5000 // long sleep
  var DELAY : Int = 10000 // for short sleep
  var RESTART : Int = 120000 // for testing

  def main (args : Array[String]) = {
    // are arguments set?
    dests = System.getProperty("snakk.proxy.dests", "").split(",")
    sources = System.getProperty("snakk.proxy.sources", "").split(",")
    SLEEP1 = System.getProperty("snakk.proxy.sleep1", "1000").toInt
    SLEEP2 = System.getProperty("snakk.proxy.sleep2", "5000").toInt
    DELAY = System.getProperty("snakk.proxy.delay", "10000").toInt
    RESTART = System.getProperty("snakk.proxy.restart", "120000").toInt

    log("ARGS: " + args.mkString)

    for(
      arg <- args
    ) {
      if(arg contains "snakk.proxy.dests") dests = arg.split("=").last.split(",")
      if(arg contains "snakk.proxy.sources") sources = arg.split("=").last.split(",")
      if(arg contains "snakk.proxy.sleep1") SLEEP1 = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.sleep2") SLEEP2 = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.delay") DELAY = arg.split("=").last.toInt
      if(arg contains "snakk.proxy.restart") RESTART = arg.split("=").last.toInt
    }

    log("dests: " + dests.mkString)
    log("sources: " + sources.mkString)

    var sleep = SLEEP2
    var lastTime = System.currentTimeMillis() - DELAY - 1 // go straight to long sleep mode
    var firstTime = System.currentTimeMillis()

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
  }

  /** check one destination for one source and if any, do proxy and return true */
  def checkAndProxy (dest:String, source:String) : Boolean = {
    log(s"Checking $source for any requests for $dest")
    val resp = Snakk.body(Snakk.url("http://" + source + "/snakk/check/" + dest))

    if(resp.size > 1) {
      log(s"... got $resp")

      val rq = Snakk.requestFromJson(resp)

      val r = doProxy(rq)

      val content = razie.js.tojsons(r.toJson) + Snakk.SSS + r.content

      // send result and complete request
      Snakk.body(Snakk.url("http://" + source + "/snakk/complete/" + rq.id, Map.empty, "POST"), Some(content))
      return true
    }
    else {
      log(s"... got nothing")
      return false
    }
  }

  /** proxy one request */
  def doProxy (rq:SnakkRequest) : SnakkResponse = {
      log(s"... snakking ${rq.url}")
      val u = rq.protocol + "://" + rq.url

      // make the call
      val uc = new URL(u).openConnection
      for (a <- rq.headers) {
        uc.setRequestProperty(a._1, a._2)
      }

      log("...hdr: " + uc.getHeaderFields)

      val resCode = uc.getHeaderField(0)

      val head = new mutable.HashMap[String,String]()

    // flatten headers into a map
      import scala.collection.JavaConversions._
      for(x <- uc.getHeaderFields.entrySet().iterator())
        if(x.getKey() != null)
          head.put(x.getKey, x.getValue.mkString)

      val in = uc.getInputStream

      if (! resCode.endsWith("200 OK")) {
        // todo - do something terrible
      }

    // read bytes to use UTF-8 encoding rather than jvm default
    val response = Comms.readStreamBytes (in)
//      val response = Comms.readStream(in)

      log(s"... response ${response.getData.size} bytes")
//      log(s"... response ${first100(response)}")

      val ctype = head("Content-Type").toString
      val content =
        if(false && Snakk.isText(ctype)) response.toString
        else "SNAKK64" + Base64.encodeBytes(response.getData())

      val r = SnakkResponse(resCode, head.toMap, content, head("Content-Type").toString, rq.id)

      return r
  }


  def log (s:String) : Unit = {
    println("now - " + " " +  s);
  }

  def first100 (s:String) = {
    if(s.length > 100) s.substring(0,100)
    else s
  }

}


