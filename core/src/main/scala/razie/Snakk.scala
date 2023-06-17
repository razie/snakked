/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie

import java.net.URLConnection
import org.json.JSONObject
import razie.xp.BeanSolver
import razie.xp.JsonSolver
import razie.xp.JsonWrapper
import razie.xp.MyBeanSolver
import com.razie.pub.comms.Comms
import com.razie.pub.util.Base64
import java.util.Base64
import play.api.mvc.{Cookies, Session}
import scala.collection.mutable

/** 
 *  wraps an URL with some arguments to be passed in the call 
 *  
 *  @param url the actual url
 *  @param httpAttr http request properties
 *  @param method  GET/POST/FORM
 *  @param formData added to the POST request
 */
class SnakkUrl(
                val url: java.net.URL,
                val httpAttr: Map[String,String]=Map.empty,
                val method:String="GET",
                val formData : Map[String,String]=Map.empty) {

  /** transform this URL in one with basic authentication */
  def basic(user: String, password: String) =
    new SnakkUrl(url, httpAttr++Map("Authorization" -> ("Basic " + new sun.misc.BASE64Encoder().encode((user + ":" + password).getBytes))), method, formData)

  /** add form fields and turn this into a form post */
  def form(fields:Map[String,String]) =
    new SnakkUrl(url, httpAttr, "FORM", formData ++ fields)
  
  override def toString : String = s"$method ${url.toString} ++ ${httpAttr.mkString} ++ ${formData.mkString}"
}

/**
  * rapid decomposition of data in different formats, from different sources. See more at https://github.com/razie/snakked
  *
  * POST:
  * val xmlFeed  = url ("http://feeds.razie.com/Razblog?format=xml", Map("field1"->"f1"), "POST")
  *
  * FORM:
  * val xmlFeed  = url ("http://feeds.razie.com/Razblog?format=xml").form (Map("name" -> "value"))
  *
  * Actual reading:
  * val root = Snakk.json(xmlFeed)
  *
  * NOTE that snakking will wrap the snacked so you'll need to unwrap at the end, so an expression like
  * { root \ "j" map identity }  is the same as { for ( n <- root \ "j" ) yield n }
 */
object Snakk {
  /** build a URL
    *
    * @param s the url string
    * @param attr http request properties
    * @param method  GET/POST/FORM
    */
  def url(s: String, attr: Map[String,String] = Map.empty, method:String="GET"):SnakkUrl = {
    try {
      new SnakkUrl(new java.net.URL(s), attr, method)
    } catch {
      case e : Exception => throw new IllegalArgumentException("ERROR URL: "+s).initCause(e)
    }
  }

  /** retrieve the content from URL, as String
    * @param postContent optionally some content for post
    */
  def conn(url: SnakkUrl, postContent:Option[String]=None) = url.method match {
    case "GET"  => Comms.streamUrlA(url.url.toString, postContent.getOrElse(""), AA map url.httpAttr)

    case "POST" | "PATCH" | "PUT" | "DELETE" | "TRACE" | "HEAD" | "OPTIONS" =>
      Comms.xpoststreamUrl2A(url.method, url.url.toString, AA map url.httpAttr, postContent.getOrElse(""))

    case "FORM" => {
      val content = razie.AA.map(url.formData).addToUrl("")
      Comms.xpoststreamUrl2A(
        "POST",
        url.url.toString,
        AA.map(url.httpAttr++Map("Content-Type" -> "application/x-www-form-urlencoded",
          "Content-Length" -> content.length.toString,
          "Host" -> "localhost")),
        content)
    }
    case x@_ => //throw new IllegalArgumentException ("unknown URL method: "+x)
      // default to POST
      Comms.xpoststreamUrl2A(url.method, url.url.toString, AA map url.httpAttr, postContent.getOrElse(""))
  }

  /** retrieve the content from URL, as String
    * @param postContent optionally some content for post
    */
  def body(url: SnakkUrl, postContent:Option[String]=None) = url.method match {
      // todo send body for GET, see above
    case "GET" => Option(Comms.readUrl(url.url.toString, AA map url.httpAttr)).getOrElse("")

    case "POST" | "PATCH" | "PUT" | "DELETE" | "TRACE" | "HEAD" | "OPTIONS" =>
      Option(Comms.readStream(Comms.xpoststreamUrl2(url.method, url.url.toString, AA map url.httpAttr, postContent.getOrElse("")))).getOrElse("")

    case "FORM" => {
      val content = razie.AA.map(url.formData).addToUrl("")
      Option(Comms.readStream(Comms.xpoststreamUrl2(
        "POST",
          url.url.toString, 
          AA.map(url.httpAttr++Map("Content-Type" -> "application/x-www-form-urlencoded",
                       "Content-Length" -> content.length.toString,
                       "Host" -> "localhost")), 
          content))).getOrElse("")
    }
    case x@_ => //throw new IllegalArgumentException ("unknown URL method: "+x)
      // default to POST
      Option(Comms.readStream(Comms.xpoststreamUrl2(url.method, url.url.toString, AA map url.httpAttr, postContent.getOrElse("")))).getOrElse("")
  }
  
  /** retrieve the content from URL, as String and strip html wrappers, leave just body */
  def htmlBody(url: SnakkUrl) = {
    val b = body(url)
    Option(b).map(_.replaceFirst("^.*<body[^>]*>","")).map(_.replaceFirst("</body[^>]*>.*$","")).getOrElse("")
  }

  /** snakk a DOM parsed already */
  def apply(node: scala.xml.Elem) = xml(node)
  /** snakk a DOM parsed already */
  def xml(node: scala.xml.Elem) = new XpWrapper(node, ScalaDomXpSolver)
  /** snakk an XML contained in a String */
  def xml(body: String) = new XpWrapper(scala.xml.XML.loadString(body), ScalaDomXpSolver)
  /** snakk an XML coming from an URL */
  def xml(url: SnakkUrl) = new XpWrapper(scala.xml.XML.loadString(body(url)), ScalaDomXpSolver) // TODO use AA for auth
  /** helper - simply parse an xml string */
  def xmlParsed(node: String) = scala.xml.XML.loadString(node)

  def str(node: String) = new XpWrapper(node, StringXpSolver)
  def str(url: SnakkUrl) = new XpWrapper(body(url), StringXpSolver)

  /** snakk a bean */
  def bean(node: Any) = new XpWrapper(node, BeanSolver)
  /** if you need to exclude certain methods/fields like generated fields, use some matching rules */
  def bean(node: Any, excludeMatches: List[String => Boolean]) = new XpWrapper(node, new MyBeanSolver(excludeMatches))

  /** snakk a parsed JSON document */
  def apply(node: JSONObject) = json(node)
  /** snakk a parsed JSON document */
  def json(node: JSONObject) = new XpWrapper[JsonWrapper](JsonSolver.WrapO(node), JsonSolver)
  /** snakk a JSON document contained in the String */
  def json(node: String) = new XpWrapper[JsonWrapper](JsonSolver.wrapOorA(node), JsonSolver)
  /** snakk a JSON document coming from an URL */
  def json(url: SnakkUrl):XpWrapper[JsonWrapper] = json(body(url))
  /** helper - simply parse a json string */
  def jsonParsed(node: String) = new JSONObject(node) // todo can be array too

  /** this will go to the URL and try to figure out what the url is */
  def apply(node: String) = new XpWrapper(node, StringXpSolver)

  /** an empty node */
  def empty = new ListWrapper(Nil, StringXpSolver)


 /** sugar for strings with fallback */
  class DString (orig: =>String) {
   override def toString = orig

   def OR (fallback: =>String) = new DfltStringVal(()=>orig, ()=>fallback)

   def toOption = Option(orig)
  }

  /** sugar for strings with fallback */
  class DfltStringVal (orig:()=>String, fallback: ()=>String) {
   override def toString = {
     val o = orig()
     if (o != null) o else fallback()
   }
   def OR (fallback: =>String) = new DfltStringVal(()=>this.toString, ()=>fallback)
   
//   def as[T] (sample:T)(implicit m:Manifest[T]) = {
//     m match {
//       case classOf[String]:Manifest => 
//     }
//   }

  def toOption = Option(toString)
  }
  
  implicit def toD (orig:String) = new DString(orig)
  implicit def toi (d:DfltStringVal) : Int = d.toString.toInt

  def requestFromJson (body:String) = {
    val m = razie.js.parse(body)
    val h = m("headers").asInstanceOf[mutable.HashMap[String, String]]
    SnakkRequest (
      m("protocol").toString,
      m("method").toString,
      m("url").toString,
      h.toMap,
      m("content").toString,
      m("id").toString
    )
  }

  // magic separator to bundle body and request in a POST body
  val SSS = "SNAKKSNAKKSNAKK"

  def responseFromJson (body:String) = {
    val x = body.split (SSS, 2)
    val m = razie.js.parse(x(0))
    val h = m("headers").asInstanceOf[mutable.HashMap[String, String]]
    val ctype = h.get("Content-Type").orElse(h.get("content-type")).getOrElse("")
    val dec =
      if(x(1).startsWith("SNAKK64")) com.razie.pub.util.Base64.decode(x(1).substring(7))
      else x(1).getBytes()

    SnakkResponse(
      m("responseCode").toString,
      m.get("resCode").getOrElse("200").toString.toInt,
      h.toMap.asInstanceOf[Map[String,String]],
      new String(dec, 0, dec.size),
      ctype,
      m("id").toString)
  }

  def isText (ctype:String) =
    ctype.contains ("text") || ctype.contains ("html") || ctype.contains ("script")
}

case class SnakkRequest (protocol: String, method: String, url: String, headers: Map[String, String], content: String, id:String = "") {

  private var _cookies: Cookies = null
  def cookies = _cookies
  def withCookies(u: Cookies) = {
    _cookies = u
    this
  }

  def toJson = {
    val m = new mutable.HashMap[String, Any]()
    m.put("id", id)
    m.put("protocol", protocol)
    m.put("method", method)
    m.put("url", url)
    m.put("headers", headers)
    m.put("cookies", Option(cookies).map(_.mkString))
    m.put("content", content)
    m.toMap
  }

  override def toString = razie.js.tojsons(toJson).replaceAllLiterally("\n", "")
}

/** contains all the details of a response for a snakk remote call */
case class SnakkResponse (
  responseCode:String,
  resCode:Int,
  headers: Map[String, String],
  content: String,
  ctype:String,
  id:String="") {

  private var _cookies: Cookies = null
  def cookies = _cookies
  def withCookies(u: Cookies) = {
    _cookies = u
    this
  }

  /** it will not include the content */
  def toJson = {
    val m = new mutable.HashMap[String, Any]()
    m.put("id", id)
    if(responseCode != null) m.put("responseCode", responseCode)
    m.put("resCode", resCode.toString)
    if(headers != null) m.put("headers", headers)
    m.put("cookies", Option(cookies).map(_.mkString))
    m.put("content", "")
    if(ctype != null) m.put("ctype", ctype)
    m.toMap
  }

  def decodeContent =
    if(content.startsWith("SNAKK64")) com.razie.pub.util.Base64.decode(content.substring(7))
    else content.getBytes()

  override def toString = razie.js.tojsons(toJson).replaceAllLiterally("\n", "")
}

