/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import razie.xp.RazElement
import java.lang.reflect.Method
import java.lang.reflect.Field

/**
 * a simple resolver for x path like stuff. note the limitation at the bottom
 *
 * can resolve the following expressions
 *
 * /a/b/c
 * /a/b/@c
 * /a/b[cond]/...
 * /a/{assoc}b[cond]/...
 *
 * / a / * / c    - ignore one level: explore all possibilities for just that level
 *
 * One difference from classic xpath is that the root node can be specified, see "a" above
 *
 * It also differs from a classic xpath by having the {assoc} option. Useful when
 * navigating models that use assocations as well as composition (graphs). Using
 * "/a/{assoc}b" means that it will use association {assoc} to find the b starting
 * from a...
 *
 * TODO the type system here is all gefuckt...need better understanding of variance in scala.
 * See this http://www.nabble.com/X-String--is-not-a-subtype-of-X-AnyRef--td23428970.html
 *
 * Example usage:
 * <ul>
 * <li> on Strings: XP("/root").xpl(new StringXpSolver, "/root")
 * <li> on scala xml: XP[scala.xml.Elem] ("/root").xpl(new ScalaDomXpSolver, root)
 * </ul>
 *
 * NOTE - this is stateless with respect to the parsed object tree - it only keeps the pre-compiled xpath
 * expression so you should reuse them as much as possible
 *
 * Note that this is a limited play-type thing. There are full XPATH implementations to browse stuff,
 * like Apache's JXpath.
 *
 * The main features of this implementation are: 1) small and embeddable 2) works for most every-day things and
 * 3) extensiblity: you can easily plugin resolvers.
 */
case class XP[T](val gp: GPath) {
  XP.debug("building XP with GPath: " + gp.elements.mkString("/"))

  /** return the matching list - solve this path starting with the root and the given solving strategy */
  def xpl(ctx: XpSolver[T], root: T): List[T] = {
    gp.requireNotAttr
    ixpl(ctx, root)
  }

  /** return the matching single element - solve this path starting with the root and the given solving strategy */
  def xpe(ctx: XpSolver[T], root: T): T = xpl(ctx, root).head

  /** return the matching attribute - solve this path starting with the root and the given solving strategy */
  def xpa(ctx: XpSolver[T], root: T): String = {
    gp.requireAttr
    ixpl(ctx, root).headOption.map {
      ctx.getAttr(_, gp.elements.last.name)
    }.getOrElse("")
  }

  /** return the list of matching attributes - solve this path starting with the root and the given solving strategy */
  def xpla(ctx: XpSolver[T], root: T): List[String] = {
    gp.requireAttr
    ixpl(ctx, root).map(ctx.getAttr(_, gp.elements.last.name))
  }

  /** if you'll keep using the same context there's no point dragging it around */
  def using(ctx: XpSolver[T]) = new XPSolved(this, ctx)

  /** internal implementation - a simple fold */
  private def ixpl(ctx: XpSolver[T], root: T): List[T] =
    if (gp.nonaelements.size == 0)
      root :: Nil
    else
      ctx.unwrap(
        if (gp.nonaelements.size == 1) {
          val c = ctx.children(root, None) // TODO this approach means we always introspec all to find one...
          val ret = ctx.reduce(List(c), gp.head).toList.map(_._1)
          // if unlucky, try children
          if (ret.size > 0) ret
          else solve(gp.head, ctx, List(c)).toList.asInstanceOf[List[(T, ctx.U)]].map(_._1)
        } else for (
          e <- (if (gp.head.name == "**") gp.nonaelements else gp.exceptFirst).foldLeft(
            ctx.reduce(List(ctx.children(root, None)), gp.head).toList)((x, xe) => solve(xe, ctx, x).asInstanceOf[List[(T, ctx.U)]])
        ) yield e._1)

  /** from res get the path and then reduce with condition looking for elements */
  private def solve(xe: XpElement, ctx: XpSolver[T], res: LCC): LCC = {
    if ("**" == xe.name)
      ctx.reduce(recurseSS(xe, gp.afterSS, ctx, res.asInstanceOf[List[(T, ctx.U)]]).asInstanceOf[List[(T, ctx.U)]], xe).toList
    else
      for (e <- res.asInstanceOf[List[(T, ctx.U)]]; x <- ctx.reduce(ctx.getNext(e, xe.name, xe.assoc, Some(xe)), xe)) yield x
  }

  type LCC = List[Any] // sorry, can't workaround this - is typecasted everywhere

  // must collect all possibilities recursively
  private def recurseSS(xe: XpElement, next: XpElement, ctx: XpSolver[T], res: LCC): LCC = {
    val m = for (
      e <- res.asInstanceOf[List[(T, ctx.U)]];
      x <- {
        if (!ctx.getNext(e, next.name, next.assoc, Some(next)).isEmpty) e :: Nil
        else recurseSS(xe, next, ctx, ctx.getNext(e, "*", "", null).toList)
      }
    ) yield x
    m
  }

  /* looking for an attribute */
  private def solvea(xe: XpElement, ctx: XpSolver[Any], res: Any): String =
    ctx.getAttr(res, xe.name)
}

/** Example of creating a dedicated solver */
object XP extends Logging {
  def forScala(xpath: String) = XP[scala.xml.Elem](xpath) using ScalaDomXpSolver
  def forString(xpath: String) = XP[String](xpath) using StringXpSolver
  def forBean(xpath: String) = XP[Any](xpath) using razie.xp.BeanSolver
  def forJson(xpath: String) = XP[razie.xp.JsonWrapper](xpath) using razie.xp.JsonSolver

  def apply[T](expr: String) = { new XP[T](GPath(expr)) }

  /** check tag matches what with * and ** - TODO make it private */
  def stareq(what: String, tag: String) =
    if ("*" == tag || "**" == tag) true
    else what == tag
    
  var debugging = false
  
  override def debug (s: => String) = if(debugging) debug(s)
  override def trace (s: => String) = if(debugging) trace(s)
}

/**
 * Simple helper to simplify client code when the context doesn't change:
 * it pairs an XP with a particular context/solver.
 *
 * So you can just use it after creation and not worry about carrying both arround.
 */
class XPSolved[T](val xp: XP[T], val ctx: XpSolver[T]) {
  /** find one element */
  def xpe(root: T): T = xp.xpe(ctx, root)
  /** find a list of elements */
  def xpl(root: T): List[T] = xp.xpl(ctx, root)
  /** find one attribute */
  def xpa(root: T): String = xp.xpa(ctx, root)
  /** find a list of attributes */
  def xpla(root: T): List[String] = xp.xpla(ctx, root)
}

/** simple base class to decouple parsing the elements from their actual functionality */
case class GPath(val expr: String) {
  // list of parsed elements
  lazy val elements =
    for (e <- (expr split "/").filter(_ != "")) yield new XpElement(e)

  lazy val nonaelements = elements.filter(_.attr != "@")

  def head = nonaelements.head
  def exceptFirst = if (nonaelements.size > 0) nonaelements drop 1 else nonaelements

  lazy val startsFromRoot = expr.startsWith("/")

  def isAttr = (elements.size > 0 && elements.last.attr == "@")

  def requireAttr =
    if (elements.last.attr != "@")
      throw new IllegalArgumentException("ERR_XP result should be attribute but it's an entity, in " + expr)

  def requireNotAttr =
    if (elements.size > 0 && elements.last.attr == "@")
      throw new IllegalArgumentException("ERR_XP result should be entity but it's an attribute, in " + expr)

  /** the first element after a ** */
  def afterSS: XpElement = elements(elements.indexWhere(_.name == "**") + 1)
}

/** overwrite this if you want other scriptables for conditions...it's just a syntax marker */
object XpCondFactory {
  def make(s: String) = if (s == null) null else new XpCond(s)
}

/**
 * the condition of an element in the path.
 *
 * TODO 3-2 maybe i should extract a trait and use it?
 *
 * this default implementation supports something like "[@attrname==15]"
 */
class XpCond(val expr: String) {
  // TODO 1-2 implement something better
  val parser = """\[[@]*(\w+)[ \t]*([=!~]+)[ \t]*[']*([^']*)[']*\]""".r
  val parser(a, eq, v) = expr

  /** returns all required attributes, in the AA format */
  def attributes: Iterable[String] = List(a)

  def passes[T](o: T, ctx: XpSolver[T]): Boolean = {
    lazy val temp = ctx.getAttr(o, a)

    eq match {
      case "==" => v == temp
      case "!=" => v != temp
      case "~=" => temp.matches(v)
      case _ => throw new IllegalArgumentException("ERR_XPCOND operator unknown: " + eq + " in expr \"" + expr + "\"")
    }
  }

  override def toString = expr
}

/** the strategy to break down the input based on the current path element. The solving algorithm is: apply current sub-path to current sub-nodes, get the results and RESTs. Filter by conditions and recurse.  */
trait XpSolver[T] {

  /** type U stands for the continuation that following methods pass to themselves. 
   *  
   *  Ideally it's just a closure that can implement getNext(curr: (T, U), tag: String, assoc: String): Iterable[(T, U)]
   *  
   *  So options include
   *  
   *  type U = List[MyWrapper] 
   *  and
   *  type U = PartialFunction[(String, String), List[MyWrapper]]  // basically getNext (tag, assoc)
   *  
   *  The continuation is very good when getting the kids is expensive, like a DB query, eh?
   */
  type U

  /**
   * prepare to start from a node, figure out the continuations.
   *
   * This is only used to start, from the root - then getNext is used
   * 
   * You can return an actual list of nodes or a callback that you then call from getNext to get the actuals
   *
   * @param root the node we'll start resolving from
   * @param xe - optional the current path element - you can use cond to filter result set directly when querying
   * @return
   */
  def children(root: T, xe: Option[XpElement]): (T, U)

  /**
   * get the next list of nodes at the current position in the path.
   * For each, return a tuple with the respective value and the REST to continue solving
   * 
   * You can return an actual list of nodes or a callback that you then call from getNext to get the actuals
   *
   * @param curr the list of (currelement, continuation) to analyze
   * @param xe - optional the current path element - you can use cond to filter result set directly when querying
   * @return
   */
  def getNext(curr: (T, U), tag: String, assoc: String, xe: Option[XpElement]): Iterable[(T, U)]

  /**
   * get the value of an attribute from the given node
   *
   * @param curr the current element
   * @return the value, toString, of the attribute
   */
  def getAttr(curr: T, attr: String): String

  /**
   * reduce the current set of possible nodes based on the given condition.
   * Note that the condition may be null - this is still called to give you a chance to cleanup?
   *
   * This default implementation may be ok for most resolvers
   *
   * NOTE - if you choose to filter in getNext, then you don't have to filter here...
   *
   * @param curr the list of (currelement, continuation) to reduce
   * @param cond the condition to use for filtering - may be null if there's no condition at this point
   * @return
   */
  def reduce(curr: Iterable[(T, U)], xe: XpElement): Iterable[(T, U)] =
    xe.cond match {
      case null => curr //.asInstanceOf[List[(T, U)]]
      case _ => curr.filter(x => xe.cond.passes(x._1, this))
    }

  /**
   * finally unwrap whatever and serve plain objects
   *
   * @param root the node we'll start resolving from
   * @return
   */
  def unwrap(root: List[T]): List[T] = root

}

/** an element in the path: "{assoc}@prefix:name[cond]" */
class XpElement(val expr: String) {
  // todo maybe not allow space, but wrap the name in something automatically if spaces present?
  val parser = """(\{.*\})*([@])*([\w]+\:)*([\$|\w\. -]+|\**)(\[.*\])*""".r
  val parser(assoc_, attr, prefix, name, scond) = expr
  val cond = XpCondFactory.make(scond)

  def assoc = assoc_ match {
    // i don't know patterns very well this is plain ugly... :(
    case s: String => { val p = """\{(\w)\}""".r; val p(aa) = s; aa }
    case _ => assoc_
  }

  override def toString = List(assoc_, attr, prefix, name, scond) mkString ","
}

/** this example resolves strings with the /x/y/z format */
object StringXpSolver extends XpSolver[String] {
  type T = String
  type U = List[String]

  override def children(root: T, xe:Option[XpElement]): (T, U) = getNext((root, List(root)), "*", "", None).head

  override def getNext(o: (T, U), tag: String, assoc: String, xe:Option[XpElement]): Iterable[(T, U)] = {
    val pat = """/*(\w+)(/.*)*""".r
    val pat(result, next) = o._2.head
    println ("DDDDDDDDDDD "+result+" = "+next)
    List((result, List(next)))
  }

  override def getAttr(o: T, attr: String): String = attr match {
    case "name" => o.toString
    case _ => throw new UnsupportedOperationException("can't really get attrs from a str...")
  }

  // there is no solver in a string, eh?
  override def reduce(o: Iterable[(T, U)], x: XpElement): Iterable[(T, U)] =
    o.filter { zz: ((T, U)) => XP.stareq(zz._1, x.name) }

}

/** this resolves XML dom trees*/
class DomXpSolver extends XpSolver[RazElement] {
  type T = RazElement
  type U = List[RazElement]

  override def children(root: T, xe:Option[XpElement]): (T, U) =
    (root, root.children.asInstanceOf[U])

  override def getNext(o: (T, U), tag: String, assoc: String, xe:Option[XpElement]): Iterable[(T, U)] = {
    val n = o._2 filter (zz => XP.stareq(zz.name, tag))
    for (e <- n) yield children(e, xe)
  }

  override def getAttr(o: T, attr: String): String = o.asInstanceOf[RazElement] a attr

  //  override def reduce[T >: RazElement, U >: List[RazElement]](o: Iterable[(T, U)], cond: XpCond): Iterable[(T, U)] = 
  //    o.asInstanceOf[List[(T, U)]]
}

/** this resolves scala xml dom trees*/
object ScalaDomXpSolver extends XpSolver[scala.xml.Elem] {
  type T = scala.xml.Elem
  type U = List[scala.xml.Elem]

  /** TODO can't i optimize this? how do i inline it at least? */
  override def children(root: T, xe:Option[XpElement]): (T, U) =
    (root, root.child.collect { case x:scala.xml.Elem => x }.toList)

  override def getNext(o: (T, U), tag: String, assoc: String, xe:Option[XpElement]): Iterable[(T, U)] =
    o._2.filter(zz => XP.stareq(zz.label, tag)).map(x => children(x, xe)).toList

  // return node values as well, if proper atribute not found...
  override def getAttr(o: T, attr: String): String = {
    val ns = (o \ ("@" + attr))
    if (!ns.isEmpty) ns.text
    else (o \ attr) text
  }

  override def reduce(curr: Iterable[(T, U)], xe: XpElement): Iterable[(T, U)] =
    (xe.cond match {
      case null => curr
      case _ => curr.filter(x => xe.cond.passes(x._1, this))
    }).filter(gaga => XP.stareq(gaga._1.label, xe.name))
}


// TODO 2-2 build a hierarchical context/solver structure - to rule the world. It would include registration

//class MyFailTypes {
//   def getAttr[T>:AnyRef] (o:T,attr:String) : String = {
//      resolve(o, attr).toString
//   }
//   
//   def resolve[T>:AnyRef] (o:T,attr:String) : Any = o.getClass.getField(attr) 
//}
