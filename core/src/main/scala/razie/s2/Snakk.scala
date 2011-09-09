/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.s2

import razie.AA
import org.json.JSONObject
import org.apache.commons.jxpath.JXPathContext

/** wraps an URL with some arguments to be passed in the call */
class SnakkUrl(val url: java.net.URL, val attr: AA) {
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
  def url(s: String, attr: AA = AA.EMPTY) = new SnakkUrl(new java.net.URL(s), attr)

  /** retrieve the content from URL, as String */
  def body(url: SnakkUrl) = com.razie.pub.comms.Comms.readUrl(url.url.toString, url.attr)

//  def apply(node: scala.xml.Elem) = xml(node)
//  def xml(node: scala.xml.Elem) = new Wrapper(node, ScalaDomXpSolver)
//  def xml(body: String) = new Wrapper(scala.xml.XML.load(body), ScalaDomXpSolver)
//  def xml(url: SnakkUrl) = new Wrapper(scala.xml.XML.load(url.url), ScalaDomXpSolver) // TODO use AA for auth
//
//  def str(node: String) = new Wrapper(node, StringXpSolver)
//  def str(url: SnakkUrl) = new Wrapper(body(url), StringXpSolver)

  def bean(node: Any) = new Wrapper[Any](node, JXPathContext.newContext(node))

//  def apply(node: JSONObject) = json(node)
//  def json(node: JSONObject) = new Wrapper[Any](XpJsonSolver.WrapO(node), XpJsonSolver)
//  def json(node: String) = new Wrapper[Any](XpJsonSolver.WrapO(new JSONObject(node)), XpJsonSolver)
//  def json(url: SnakkUrl) = new Wrapper[Any](XpJsonSolver.WrapO(new JSONObject(body(url))), XpJsonSolver)

  /** this will go to the URL and try to figure out what the url is */
//  def apply(node: String) = new Wrapper(node, StringXpSolver)

}
/** OO wrapper for self-solving XP elements HEY this is like an open monad :) */
class ListWrapper[T](val nodes: Iterator[T], val ctx: JXPathContext) {
  import scala.collection.JavaConversions._
  /** the list of children with the respective tag */
  def \(path: String): ListWrapper[T] = new ListWrapper(nodes.flatMap(n => asScalaIterator(ctx.iterate(path)).asInstanceOf[Iterator[T]]), ctx)
  /** the head of the list of children with the respective tag */
  def \\(path: String): T = (this \ path).headOption.get
  /** the list of children two levels down with the respective tag */
  def \*(path: String): ListWrapper[T] = new ListWrapper(nodes.flatMap(n => asScalaIterator(ctx.iterate("*/"+path)).asInstanceOf[Iterator[T]]), ctx)
  /** the list of children many levels down with the respective tag */
//  def \**(name: String): ListWrapper[T] = new ListWrapper(nodes.flatMap(n => XP[T]("**/" + name).xpl(ctx, n)), ctx)
  /** the head of the list of children two levels down with the respective tag */
  def \\*(name: String): T = (this \* name).headOption.get
  /** the list of attributes with the respective name */
  def \@(path: String): Iterator[Any] = nodes map (n => ctx.getValue(path))
  /** the single attributes with the respective name */
  def \@@(path: String): String = list.headOption.map(n => ctx.getValue(path).toString) getOrElse null

  lazy val list = nodes.toList
  
  def apply(i: Int) = new Wrapper(list.apply(i), ctx)
  def \(i: Int) = new Wrapper(list.apply(i), ctx)

  def foreach[B](f: T => B): Unit = nodes.foreach(f)
  def map[B](f: T => B): Iterator[B] = nodes.map(f)
  def flatMap[B](f: T => Iterator[B]): Iterator[B] = nodes.flatMap(f)

  def first(n: Any = null) = headOption getOrElse n
  def headOption = list.headOption

  override def toString = nodes.toString
}

/** OO wrapper for self-solving XP elements */
class Wrapper[T <: Any](val node: T, val ctx: JXPathContext) {
  import scala.collection.JavaConversions._
  /** the list of children with the respective tag */
  def \(path: String): ListWrapper[T] = new ListWrapper[T](asScalaIterator(ctx.iterate(path)).asInstanceOf[Iterator[T]], ctx)
  /** the head of the list of children with the respective tag */
  def \\(path: String): T = (this \ path).headOption.get
  /** the list of children two levels down with the respective tag */
  def \*(path: String): ListWrapper[T] = this \ ("*/" + path)
  /** the list of children many levels down with the respective tag */
  def \**(path: String): ListWrapper[T] = this \ ("//"+path)
  /** the head of the list of children two levels down with the respective tag */
  def \\*(name: String): T = (this \* name).headOption.get
  /** the attribute with the respective name */
  def \@(path: String): Any = ctx.getValue(path)
  /** the single attributes with the respective name */
  def \@@(path: String): String = ctx.getValue(path).toString()

  override def toString = Option(node).map(_.toString).toString
}