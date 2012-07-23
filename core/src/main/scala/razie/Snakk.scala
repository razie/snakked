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

/** wraps an URL with some arguments to be passed in the call */
class SnakkUrl(val url: java.net.URL, val attr: AA, val method:String="GET") {
  /** transform this URL in one with basic authentication */
  def basic(user: String, password: String) =
    new SnakkUrl(url, attr ++ AA("Authorization", "Basic " + new sun.misc.BASE64Encoder().encode((user + ":" + password).getBytes)))
}

/**
 * rapid decomposition of data in different formats, from different sources
 *
 * NOTE that snakking will wrap the snacked so you'll need to unwrap at the end, so an expression like
 * { root \ "j" map identity }  is the same as { for ( n <- root \ "j" ) yield n }
 */
object Snakk {
  /** build a URL */
  def url(s: String, attr: AA = AA.EMPTY, method:String="GET") = new SnakkUrl(new java.net.URL(s), attr, method)

  
  /** retrieve the content from URL, as String */
  def body(url: SnakkUrl) = url.method match {
    case "GET" => Option(Comms.readUrl(url.url.toString, url.attr)).getOrElse("")
    case "POST" =>Option(Comms.readStream(Comms.xpoststreamUrl2(url.url.toString, url.attr, ""))).getOrElse("")
    case x@_ => throw new IllegalArgumentException ("unknown URL method: "+x)
  }
  
  /** retrieve the content from URL, as String and strip html wrappers, leave just body */
  def htmlBody(url: SnakkUrl) = {
    val b = body(url)
    Option(b).map(_.replaceFirst("^.*<body[^>]*>","")).map(_.replaceFirst("</body[^>]*>.*$","")).getOrElse("")
  }

  def apply(node: scala.xml.Elem) = xml(node)
  def xml(node: scala.xml.Elem) = new Wrapper(node, ScalaDomXpSolver)
  def xml(body: String) = new Wrapper(scala.xml.XML.load(body), ScalaDomXpSolver)
  def xml(url: SnakkUrl) = new Wrapper(scala.xml.XML.load(body(url)), ScalaDomXpSolver) // TODO use AA for auth

  def str(node: String) = new Wrapper(node, StringXpSolver)
  def str(url: SnakkUrl) = new Wrapper(body(url), StringXpSolver)

  def bean(node: Any) = new Wrapper(node, BeanSolver)
  /** if you need to exclude certain methods/fields like generated fields, use some matching rules */
  def bean(node: Any, excludeMatches: List[String => Boolean]) = new Wrapper(node, new MyBeanSolver(excludeMatches))

  def apply(node: JSONObject) = json(node)
  def json(node: JSONObject) = new Wrapper[JsonWrapper](JsonSolver.WrapO(node), JsonSolver)
  def json(node: String) = new Wrapper[JsonWrapper](JsonSolver.WrapO(new JSONObject(node)), JsonSolver)
  def json(url: SnakkUrl) = new Wrapper[JsonWrapper](JsonSolver.WrapO(new JSONObject(body(url))), JsonSolver)

  /** this will go to the URL and try to figure out what the url is */
  def apply(node: String) = new Wrapper(node, StringXpSolver)

  /** OO wrapper for self-solving XP elements HEY this is like an open monad :) */
  class ListWrapper[T](val nodes: List[T], val ctx: XpSolver[T]) {
    /** factory method - overwrite with yours*/
    def wrapList (nodes:List[T], ctx:XpSolver[T]) = new ListWrapper(nodes, ctx)
    def wrapNode (node:T, ctx:XpSolver[T]) = new Wrapper(node, ctx)
    
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

    def size = nodes.size
    override def toString = nodes.toString
  }

  /** OO wrapper for self-solving XP elements */
  class Wrapper[T](val node: T, val ctx: XpSolver[T]) {
    /** factory method - overwrite with yours*/
    def wrapList (nodes:List[T], ctx:XpSolver[T]) = new ListWrapper(nodes, ctx)
    def wrapNode (node:T, ctx:XpSolver[T]) = new Wrapper(node, ctx)

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
  }
  
  implicit def toD (orig:String) = new DString(orig)
  implicit def toi (d:DfltStringVal) : Int = d.toString.toInt
}
