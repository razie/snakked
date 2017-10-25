/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.InternalError
import java.lang.Object
import razie.Debug.toTee
import razie.XpSolver
import razie.XP
import razie.XpElement
import razie.Logging

/**
 * reflection resolved for java/scala objects
 */
object BeanSolver extends MyBeanSolver

/**
 * reflection implementation
 *
 * @param excludeMatches - custom exclusion rules: nodes and attributes with these names won't be browsed
 */
class MyBeanSolver(val excludeMatches: List[String => Boolean] = Nil) extends XpSolver[Any] {
  type T=Any
  type CONT = (String, String) => List[BeanWrapper]
  type U=CONT
  
  trait LazyB { def eval: Any } // a lazy wrapper

  abstract class BeanWrapper(val j: Any, val label: String = "root") extends LazyB {
    override def equals(other: Any) =
      other.isInstanceOf[BeanWrapper] && this.label == other.asInstanceOf[BeanWrapper].label
    override def toString = "BW(" + label + ")"
  }
  case class RootWrapper(override val j: Any, override val label: String = "root") extends BeanWrapper(j, label) with LazyB { override def eval: Any = j }
  case class FieldWrapper(override val j: Any, val f: Field, override val label: String = "root") extends BeanWrapper(j, label) with LazyB { override def eval: Any = f.get(j) }
  case class MethodWrapper(override val j: Any, val m: Method, override val label: String = "root") extends BeanWrapper(j, label) with LazyB {
    override def eval: Any = { XP.trace("invoke: " + m); m.invoke(j) }
  }
  
  def WrapO(j: Any, label: String = "root") = new RootWrapper(j, label)
  def WrapF(j: Any, f: Field, label: String) = new FieldWrapper(j, f, label)
  def WrapM(j: Any, m: Method, label: String) = new MethodWrapper(j, m, label)

  override def children(root: T): (T, U) = {
    val r = root match {
      case r1: BeanWrapper => r1
      case _ => WrapO(root, "root")
    }
    (r, ((a: String, b: String) => resolve(r.j.asInstanceOf[AnyRef], a, b)).asInstanceOf[U])
  }

  override def getNext(o: (T, U), tag: String, assoc: String): List[(T, U)] = {
    XP.debug("getNext " + tag)
    o._2.asInstanceOf[CONT].apply(tag, assoc).asInstanceOf[List[Any]].asInstanceOf[List[BeanWrapper]].
      filter(zz => XP.stareq(zz.asInstanceOf[BeanWrapper].label, tag)).teeIf(XP.debugging, "before").
      flatMap(src => {
        val res = src.eval
        XP.trace("DDDDDDDDDDDDD-" + res)
        for (y <- razie.MOLD(res))
          yield (WrapO(y.asInstanceOf[T], src.label), ((a: String, b: String) => resolve(y.asInstanceOf[AnyRef], a, b)).asInstanceOf[U])
      }).teeIf(XP.debugging, "after").toList
  }

  override def getAttr(o: T, attr: String): String = {
    val oo = if (o.isInstanceOf[BeanWrapper]) o else WrapO(o, "root")
    resolve(oo.asInstanceOf[BeanWrapper].eval.asInstanceOf[AnyRef], attr).head.eval.toString
  }

  override def reduce(curr: Iterable[(T, U)], xe: XpElement): Iterable[(T, U)] =
    (xe.cond match {
      case null => curr.asInstanceOf[List[(T, U)]]
      case _ => curr.asInstanceOf[List[(T, U)]].filter(x => xe.cond.passes(x._1, this))
    }).filter(gaga => XP.stareq(gaga._1.asInstanceOf[BeanWrapper].label, xe.name))

  override def unwrap(root: List[T]): List[T] =
    (root map (_.asInstanceOf[BeanWrapper].j)).asInstanceOf[List[T]]

  // exclude all methods from Object and some others
  lazy val meth = resolve(new Object(), "*", "", false).map(x => (x.label, x.label)).toMap ++
    List("productArity", "productElements", "productPrefix", "productIterator").map(x => (x, x)).toMap

  // some exclusion rules
  val nomatch: List[String => Boolean] =
    { x: String => x.endsWith("$outer") } ::
      { x: String => x.equals("MODULE$") } ::
      { x: String => x.equals("hashCode") } ::
      // these is because the REPL, in Scripster, adds this to any case class: apply,class $line4.$read$$iw$$iw$C1
      // and generates Malformed class name
      //      { x: String => x.equals("apply") } :: 
      { x: String => x.equals("readResolve") } ::
      Nil

  // completely skip these classes
  val nogo: List[Any] = List(
    classOf[String], classOf[Int], classOf[Boolean], classOf[Float],
    classOf[Integer])

  // attr can be: field name, method name (with no args) or property name */
  private def resolve(o: AnyRef, attr: String, assoc: String = "", check: Boolean = true): List[BeanWrapper] = {
    XP.trace("Resolving: " + attr + " from root: " + o)

    // introspection 
    def fields(crit: Field => Boolean) = o.getClass.getFields.filter(f => FILTER(f.getName)).filter(crit(_)).map(f => WrapO(f.get(o), f.getName()))
    def getters(crit: Method => Boolean) = o.getClass.getDeclaredMethods.filter(m =>
      m.getName.startsWith("get") &&
        m.getName != "getClass" &&
        FILTER(m.getName) &&
        m.getParameterTypes.isEmpty).filter(crit(_)).map(f => WrapM(o, f, fromZ(f.getName)))
    def scalas(crit: Method => Boolean) = o.getClass.getDeclaredMethods.filter(m =>
      m.getParameterTypes.size == 0 &&
        m.getReturnType.getName != "void" &&
        !m.getName.startsWith("get") &&
        FILTER(m.getName) &&
        m.getDeclaringClass() == o.getClass()).filter(crit(_)).map(f => WrapM(o, f, f.getName))

    def ALL[T](x: T) = true // filter all

    def FILTER(name: String) = {
      check &&
        !meth.contains(name) &&
        !nomatch.foldLeft(false)((x, f) => x || f(name)) &&
        !excludeMatches.foldLeft(false)((x, f) => x || f(name))
    }

    val result = if ("*" == attr) {
      // TODO restrict them by type or patter over type
      if (nogo.contains(o.getClass())) Nil
      else {
        // java getX scala x or member x while dropping duplicates
        (scalas(ALL) ++ fields(ALL) ++ getters(ALL)).map(
          p => (p.label, p)).toMap.values.toList
      }
    } else {
      // java getX scala x or member x by name
      val m: java.lang.reflect.Method = try {
        o.getClass.getMethod("get" + toZ(attr))
      } catch {
        case _:Throwable => try {
          o.getClass.getMethod(attr)
        } catch {
          case _:Throwable => null
        }
      }

      val result2 = try {
        if (m != null) List(WrapM(o, m, attr))
        //      if (m != null) m.invoke(o)
        else {
          val f = try {
            o.getClass.getField(attr)
          } catch {
            case _:Throwable => null
          }

          if (f != null) List(WrapO(f.get(o), attr))
          else Nil // TODO should probably log or debug?
        }
      } catch {
        case _:Throwable => Nil
      }

      if (result2.isEmpty) {
        // ===================== last chance - try by type
        def also(s: String) = (assoc == null || assoc.length <= 0 || assoc.equals(s))
        val s = scalas(m => attr == getSimpleName(m.getReturnType()) && also(m.getName()))
        val m = getters(m => attr == getSimpleName(m.getReturnType()) && also(m.getName()))
        val f = fields(f => attr == getSimpleName(f.getType()) && also(f.getName()))

        if (XP.debugging) {
          XP.trace("FFFFFields: " + o.getClass.getFields.map(f => (f.getName(), f.getType)).mkString("-"))
          XP.trace("FFFFMethods: " + o.getClass.getDeclaredMethods.filter(m =>
            m.getName.startsWith("get") &&
              m.getName != "getClass" &&
              m.getParameterTypes.isEmpty).map(f => (fromZ(f.getName), f.getReturnType)).mkString("-"))
          XP.trace("FFFFFScalas: " + o.getClass.getDeclaredMethods.filter(m =>
            m.getParameterTypes.size == 0 &&
              m.getReturnType.getName != "void" &&
              !m.getName.startsWith("get") &&
              m.getDeclaringClass() == o.getClass()).map(f => (f.getName, f.getReturnType)).mkString("-"))
        }

        def opt[T <: BeanWrapper](x1: => Seq[T]): Option[Seq[BeanWrapper]] = { val x = x1; if (x.isEmpty) None else Some(x) }

        opt(s).getOrElse(opt(m).getOrElse(opt(f).getOrElse(Nil))).collect(
          {
            case x1 @ RootWrapper(j, label) => RootWrapper(j, attr) // i'm using RootWrapper instead of FieldWrapper
            case FieldWrapper(j, f, l) => FieldWrapper(j, f, attr)
            case MethodWrapper(j, m, l) => MethodWrapper(j, m, attr)
          }).toList
      } else result2
    }

    XP.debug("resolved: " + result.mkString(","))
    result
  }

  private[this] def toZ(attr: String) = attr.substring(0, 1).toUpperCase + (if (attr.length > 1) attr.substring(1, attr.length - 1) else "")
  private[this] def fromZ(getter: String) = if (getter.length > 3) getter.substring(3).substring(0, 1).toLowerCase + (if (getter.length > 4) getter.substring(4, getter.length - 1) else "") else getter

  /** protect against scala interpreter screwy names, must hack this Class.getSimpleName method */
  private[this] def getSimpleName(c: java.lang.Class[_]): String = {
    var n = getSimpleClassName(c)
    // this is specifically for the interpreter - it adds this to some ofthe types can can't do beans by type in Scripster
    val REPL_PREFIX = "$read$$iw$$iw$"
    if (n.isEmpty()) n = c.getName.substring(c.getName.lastIndexOf(".") + 1); // strip the package name
    if (n.startsWith(REPL_PREFIX))
      n.substring(REPL_PREFIX.length())
    else n
  }
  /** protect against scala interpreter screwy names, must hack this Class.getSimpleName method */
  private[this] def getSimpleClassName(c: java.lang.Class[_]): String = {
    if (c.isArray())
      return getSimpleName(c.getComponentType()) + "[]";

    var simpleName = getSimpleBinaryName(c);
    if (simpleName == null) { // top level class
      simpleName = c.getName();
      return simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
    }
    val length = simpleName.length();
    if (length < 1) // || simpleName.charAt(0) != '$')
      throw new InternalError("Malformed class name");
    var index = 1;
    while (index < length && isAsciiDigit(simpleName.charAt(index)))
      index = index + 1;
    // Eventually, this is the empty string iff this is an anonymous class
    return simpleName.substring(index);
  }

  private def getSimpleBinaryName(c: Class[_]): String = {
    val enclosingClass = c.getEnclosingClass();
    if (enclosingClass == null) // top level class
      return null;
    // Otherwise, strip the enclosing class' name
    try {
      return c.getName().substring(enclosingClass.getName().length());
    } catch {
      case ex: IndexOutOfBoundsException => throw new InternalError("Malformed class name");
    }
  }
  private def isAsciiDigit(c: Char) = {
    '0' <= c && c <= '9';
  }
}
