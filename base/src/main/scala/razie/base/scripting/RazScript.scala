/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import razie.base.ActionContext

/**
 * some statics
 *
 * @author razvanc
 */
object RazScript {
  /** the result of running a smart script */
  class RSResult[+A] {
    def map[B >: A](f: A => B): RSResult[B] = RSUnsupported("by default")
    def getOrElse[B >: A](f: => B): B = f
    def getOrThrow: A = throw new IllegalArgumentException(this.toString)
    def jgetOrElse(f: Any): Any = getOrElse(f)
  }

  /** successful - may have a result accessible as if this is an option */
  case class RSSucc[A](res: A) extends RSResult[A] {
    override def map[B](f: A => B): RSResult[B] = RSSucc(f(res))
    override def getOrElse[B >: A](f: => B): B = res
    override def getOrThrow: A = res
  }

  /** evaluation generated an error */
  case class RSError(err: String) extends RSResult[String]

  /** expression is incomplete - needs more input */
  object RSIncomplete extends RSResult[Any]

  /** interactive mode unsupported */
  case class RSUnsupported(what: String) extends RSResult[Nothing]

  /** interactive mode unsupported */
  object RSUnsupported extends RSUnsupported("todo")

  /** successful, but no value returned */
  object RSSuccNoValue extends RSResult[Any]

  def err(msg: String) = RSError(msg)
  def succ(res: AnyRef) = RSSucc(res)

  def apply(s: String) = ScriptFactory.make("scala", s)
}

/**
 * minimal script interface
 *
 * TODO use JSR 223 or whatever the thing is and ditch custom code...
 *
 * @author razvanc
 */
trait RazScript {
  import RazScript._

  /**
   * try to compile - optimisation usually. If RSUnsupported, it will still be able to evaluate it
   *
   * @return SError or SSuccNoValue...or others
   */
  def compile(ctx: ActionContext): RSResult[Any]

  /**
   * strait forward evaluation and return result of expression.
   * If there's no expected result , use interactive() instead
   */
  def eval(ctx: ActionContext): RSResult[Any]

  /** interactive evaluation - more complex interaction, when this script is part of a sequence 
   * and there's no result expected */
  def interactive(ctx: ActionContext): RSResult[Any]

  /** the language needs sometimes passed around to factories etc */
  def lang: String
}
