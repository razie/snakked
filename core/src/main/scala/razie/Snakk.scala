/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie

import org.json.JSONObject
import razie.xp.BeanSolver
import razie.xp.JsonSolver
import razie.xp.JsonWrapper
import razie.xp.MyBeanSolver
import com.razie.pub.comms.Comms

/** 
 *  wraps an URL with some arguments to be passed in the call 
 *  
 *  @param url the actual url
 *  @param httpAttr http request properties
 *  @param method  GET/POST/FORM
 *  @param formData added to the POST request
 */
class SnakkUrl(val url: java.net.URL, val httpAttr: Map[String,String]=Map.empty, val method:String="GET", val formData : Map[String,String]=Map.empty) {
  /** transform this URL in one with basic authentication */
  def basic(user: String, password: String) =
    new SnakkUrl(url, httpAttr++Map("Authorization" -> ("Basic " + new sun.misc.BASE64Encoder().encode((user + ":" + password).getBytes))), method, formData)

  /** add form fields and turn this into a form post */
  def form(fields:Map[String,String]) =
    new SnakkUrl(url, httpAttr, "FORM", formData ++ fields)
  
  override def toString : String = s"SnakkUrl: $method ${url.toString} ++ ${httpAttr.mkString} ++ ${formData.mkString}"
}

/**
 * rapid decomposition of data in different formats, from different sources
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
  def url(s: String, attr: Map[String,String] = Map.empty, method:String="GET") = new SnakkUrl(new java.net.URL(s), attr, method)

  /** retrieve the content from URL, as String
    * @param postContent optionally some content for post
    */
  def conn(url: SnakkUrl, postContent:Option[String]=None) = url.method match {
    case "GET" => Comms.streamUrlA(url.url.toString, AA map url.httpAttr)
    case "POST" =>Comms.xpoststreamUrl2A(url.url.toString, AA map url.httpAttr, postContent.getOrElse(""))
    case "FORM" => {
      val content = razie.AA.map(url.formData).addToUrl("")
      Comms.xpoststreamUrl2A(
        url.url.toString,
        AA.map(url.httpAttr++Map("Content-Type" -> "application/x-www-form-urlencoded",
          "Content-Length" -> content.length.toString,
          "Host" -> "localhost")),
        content)
    }
    case x@_ => throw new IllegalArgumentException ("unknown URL method: "+x)
  }

  /** retrieve the content from URL, as String
    * @param postContent optionally some content for post
    */
  def body(url: SnakkUrl, postContent:Option[String]=None) = url.method match {
    case "GET" => Option(Comms.readUrl(url.url.toString, AA map url.httpAttr)).getOrElse("")
    case "POST" =>Option(Comms.readStream(Comms.xpoststreamUrl2(url.url.toString, AA map url.httpAttr, postContent.getOrElse("")))).getOrElse("")
    case "FORM" => {
      val content = razie.AA.map(url.formData).addToUrl("")
      Option(Comms.readStream(Comms.xpoststreamUrl2(
          url.url.toString, 
          AA.map(url.httpAttr++Map("Content-Type" -> "application/x-www-form-urlencoded",
                       "Content-Length" -> content.length.toString,
                       "Host" -> "localhost")), 
          content))).getOrElse("")
    }
    case x@_ => throw new IllegalArgumentException ("unknown URL method: "+x)
  }
  
  /** retrieve the content from URL, as String and strip html wrappers, leave just body */
  def htmlBody(url: SnakkUrl) = {
    val b = body(url)
    Option(b).map(_.replaceFirst("^.*<body[^>]*>","")).map(_.replaceFirst("</body[^>]*>.*$","")).getOrElse("")
  }

  /** snakk a DOM parsed already */
  def apply(node: scala.xml.Elem) = xml(node)
  /** snakk a DOM parsed already */
  def xml(node: scala.xml.Elem) = new Wrapper(node, ScalaDomXpSolver)
  /** snakk an XML contained in a String */
  def xml(body: String) = new Wrapper(scala.xml.XML.loadString(body), ScalaDomXpSolver)
  /** snakk an XML coming from an URL */
  def xml(url: SnakkUrl) = new Wrapper(scala.xml.XML.loadString(body(url)), ScalaDomXpSolver) // TODO use AA for auth
  /** helper - simply parse an xml string */
  def xmlParsed(node: String) = scala.xml.XML.loadString(node)

  def str(node: String) = new Wrapper(node, StringXpSolver)
  def str(url: SnakkUrl) = new Wrapper(body(url), StringXpSolver)

  /** snakk a bean */
  def bean(node: Any) = new Wrapper(node, BeanSolver)
  /** if you need to exclude certain methods/fields like generated fields, use some matching rules */
  def bean(node: Any, excludeMatches: List[String => Boolean]) = new Wrapper(node, new MyBeanSolver(excludeMatches))

  /** snakk a parsed JSON document */
  def apply(node: JSONObject) = json(node)
  /** snakk a parsed JSON document */
  def json(node: JSONObject) = new Wrapper[JsonWrapper](JsonSolver.WrapO(node), JsonSolver)
  /** snakk a JSON document contained in the String */
  def json(node: String) = new Wrapper[JsonWrapper](JsonSolver.WrapO(new JSONObject(node)), JsonSolver)
  /** snakk a JSON document coming from an URL */
  def json(url: SnakkUrl) = new Wrapper[JsonWrapper](JsonSolver.WrapO(new JSONObject(body(url))), JsonSolver)
  /** helper - simply parse a json string */
  def jsonParsed(node: String) = new JSONObject(node)

  /** this will go to the URL and try to figure out what the url is */
  def apply(node: String) = new Wrapper(node, StringXpSolver)

  /** an empty node */
  def empty = new ListWrapper(Nil, StringXpSolver)

  /** OO wrapper for self-solving XP elements HEY this is like an open monad :) */
  trait BaseWrapper[T] {
    /** factory method - overwrite with yours*/
    def wrapList (nodes:List[T], ctx:XpSolver[T]) = new ListWrapper(nodes, ctx)
    def wrapNode (node:T, ctx:XpSolver[T]) = new Wrapper(node, ctx)

    /** the list of children with the respective tag */
    def \(name: String): ListWrapper[T]
    /** the head of the list of children with the respective tag */
    def \\\(name: String): T
    /** the list of children two levels down with the respective tag */
    def \*(name: String): ListWrapper[T]
    /** the head of the list of children two levels down with the respective tag */
    def \\*(name: String): T
    /** the single attributes with the respective name */
    def \@@(name: String): String
    /** the attributeS with the respective name */
    def \\@(name: String): List[String]
  }

  /** OO wrapper for self-solving XP elements HEY this is like an open monad :) */
  class ListWrapper[T](val nodes: List[T], val ctx: XpSolver[T]) extends BaseWrapper[T] {
    def xpl  (path:String) = nodes.flatMap(n => XP[T](path).xpl(ctx, n))
    def xpa  (path:String) = nodes.headOption.map(n => XP[T](path).xpa(ctx, n))
    def xpla (path:String) = nodes.flatMap(n => XP[T](path).xpla(ctx, n))

    /** the list of children with the respective tag */
    def \(name: String): ListWrapper[T] = wrapList(nodes.flatMap(n => XP[T]("*/" + name).xpl(ctx, n)), ctx)
    /** the head of the list of children with the respective tag */
    def \\\(name: String): T = (this \ name).headOption.get
    /** the list of children two levels down with the respective tag */
    def \*(name: String): ListWrapper[T] = wrapList(nodes.flatMap(n => XP[T]("*/*/" + name).xpl(ctx, n)), ctx)
    /** the list of children many levels down with the respective tag */
    def \\(name: String): ListWrapper[T] = wrapList(nodes.flatMap(n => XP[T]("**/" + name).xpl(ctx, n)), ctx)
    /** the head of the list of children two levels down with the respective tag */
    def \\*(name: String): T = (this \* name).headOption.get
    /** the list of attributes with the respective name */
    def \@(name: String): List[String] = nodes map (n => XP[T](if (name.contains("@")) name else "@" + name).xpa(ctx, n))
    /** the single attributes with the respective name */
    def \@@(name: String): String = nodes.headOption.map(n => XP[T](if (name.contains("@")) name else "@" + name).xpa(ctx, n)) getOrElse null
    /** the attributeS with the respective name */
    def \\@(name: String): List[String] = nodes.flatMap(n => XP[T](if (name.contains("@")) name else "@" + name).xpla(ctx, n))

    def apply(i: Int) = wrapNode(nodes.apply(i), ctx)
    def \(i: Int) = wrapNode(nodes.apply(i), ctx)

    def foreach[B](f: T => B): Unit = nodes.foreach(f)
    def map[B](f: T => B): List[B] = nodes.map(f)
    def flatMap[B](f: T => List[B]): List[B] = nodes.flatMap(f)

    def head = headOption.get
    def head(n: Any) = headOption getOrElse n
    def headOption = nodes.headOption
    //  def firstOption = nodes.firstOption map (new Wrapper(_, ctx))

    def ++ (other:ListWrapper[T]) = new ListWrapper(nodes ++ other.nodes, ctx)

    def size = nodes.size
    override def toString = nodes.toString
  }

  /** OO wrapper for self-solving XP elements */
  class Wrapper[T](val node: T, val ctx: XpSolver[T]) extends BaseWrapper[T] {
    def xpl  (path:String) = XP[T](path).xpl(ctx, node)
    def xpa  (path:String) = XP[T](path).xpa(ctx, node)
    def xpla (path:String) = XP[T](path).xpla(ctx, node)
    
    /** the list of children with the respective tag */
    def \(name: String): ListWrapper[T] = wrapList(XP[T](name).xpl(ctx, node), ctx)
    /** the head of the list of children with the respective tag */
    def \\\(name: String): T = (this \ name).headOption.get
    /** the list of children two levels down with the respective tag */
    def \*(name: String): ListWrapper[T] = wrapList(XP[T]("*/" + name).xpl(ctx, node), ctx)
    /** the list of children many levels down with the respective tag */
    def \\(name: String): ListWrapper[T] = wrapList(XP[T]("**/" + name).xpl(ctx, node), ctx)
    /** the head of the list of children two levels down with the respective tag */
    def \\*(name: String): T = (this \* name).headOption.get
    /** the attribute with the respective name */
    def \@(name: String): String = XP[T](if (name.contains("@")) name else "@" + name).xpa(ctx, node)
    /** the single attributes with the respective name */
    def \@@(name: String): String = this \@ name
    /** the attributeS with the respective name */
    def \\@(name: String): List[String] = XP[T](if (name.contains("@")) name else "@" + name).xpla(ctx, node)

    override def toString = Option(node).map(_.toString).toString
  }

  class DString (orig: =>String) {
   override def toString = orig

   def OR (fallback: =>String) = new DfltStringVal(()=>orig, ()=>fallback)

   def toOption = Option(orig)
  }
  
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
}
