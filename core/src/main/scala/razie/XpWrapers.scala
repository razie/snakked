/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie

/** OO wrapper for self-solving XP elements HEY this is like an open monad :) */
trait BaseWrapper[T] {
  /** factory method - overwrite with yours */
  def wrapList(nodes: List[T], ctx: XpSolver[T]) = new ListWrapper(nodes, ctx)

  def wrapNode(node: T, ctx: XpSolver[T]) = new XpWrapper(node, ctx)

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

/** List wrapper for self-solving XP elements */
class ListWrapper[T](val nodes: List[T], val ctx: XpSolver[T]) extends BaseWrapper[T] {
  def xpl(path: String) = nodes.flatMap(n => XP[T](path).xpl(ctx, n))

  def xpa(path: String) = nodes.headOption.map(n => XP[T](path).xpa(ctx, n))

  def xpla(path: String) = nodes.flatMap(n => XP[T](path).xpla(ctx, n))

  def wrap(nodes: List[T]) = new ListWrapper(nodes, ctx)

  def wrap(node: T) = new XpWrapper(node, ctx)

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
  def \@@(name: String): String = nodes.headOption.map(
    n => XP[T](if (name.contains("@")) name else "@" + name).xpa(ctx, n)) getOrElse null

  /** the attributeS with the respective name */
  def \\@(name: String): List[String] = nodes.flatMap(
    n => XP[T](if (name.contains("@")) name else "@" + name).xpla(ctx, n))

  def apply(i: Int) = wrapNode(nodes.apply(i), ctx)

  def \(i: Int) = wrapNode(nodes.apply(i), ctx)

  def foreach[B](f: T => B): Unit = nodes.foreach(f)

  def map[B](f: T => B): List[B] = nodes.map(f)

  def flatMap[B](f: T => List[B]): List[B] = nodes.flatMap(f)

  def head = headOption.get

  def head(n: Any) = headOption getOrElse n

  def headOption = nodes.headOption
  //  def firstOption = nodes.firstOption map (new XpWrapper(_, ctx))

  def ++(other: ListWrapper[T]) = new ListWrapper(nodes ++ other.nodes, ctx)

  def size = nodes.size

  override def toString = nodes.toString
}

/** OO wrapper for self-solving XP elements */
class XpWrapper[T](val node: T, val ctx: XpSolver[T]) extends BaseWrapper[T] {
  def xpl(path: String) = XP[T](path).xpl(ctx, node)

  def xpa(path: String) = XP[T](path).xpa(ctx, node)

  def xpla(path: String) = XP[T](path).xpla(ctx, node)

  def wrap(nodes: List[T]) = new ListWrapper(nodes, ctx)

  def wrap(node: T) = new XpWrapper(node, ctx)

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
