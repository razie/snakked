/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import com.razie.pub.base.{ log => pblog }

/** some logging basics 
 * 
 * @author razvanc
 * @deprecated use the Logging trait
 */
object Log extends razie.Logging {
  override val logger = newlog ("razie")  // default log

  def silent (b:Boolean) = {} // TODO
  
  //TODO implement the audit properly - send to special audit logger
  
  // make these public
  @inline override def error(message: => String): Unit = logger.error(message)
  @inline override def error(message: => String, e: Throwable): Unit = logger.error(e, message)

  @inline override def warn(message: => String): Unit = logger.warn(message)
  @inline override def warn(message: => String, e: Throwable): Unit = logger.warn(e, message)

  @inline override def audit(message: => String): Unit = logger.info("AUDIT "+message)
  @inline override def audit(message: => String, e: Throwable): Unit = logger.info(e, "AUDIT "+message)
  @inline override def log(message: => String): Unit = logger.info(message)
  @inline override def log(message: => String, e: Throwable): Unit = logger.info(e, message)
  @inline override def info(message: => String): Unit = logger.info(message)
  @inline override def info(message: => String, e: Throwable): Unit = logger.info(e, message)

  @inline override def debug(message: => String): Unit = logger.debug(message)
  @inline override def debug(message: => String, e: Throwable): Unit = logger.debug(e, message)

  @inline override def trace(message: => String): Unit = logger.trace(message)
  @inline override def trace(message: => String, e: Throwable): Unit = logger.trace(e, message)


  def apply(msg: String) = log (msg)

  /** @return the same message, so you can return it */
  def alarmThis(msg: String) = {
    error(msg)
    msg
  }

  /** @return the same message, so you can return it */
  def alarmThis(msg: String, e: Throwable) = {
    error (msg, e)
    msg
  }

  /** optimized so the code is not even invoked if tracing is off ... don't suppose this will cause side-effects? */
  def traceThis(f: => Any) = {
    if (pblog.Log.dflt.isTraceOn()) {
      val p = f
      p match {
        case s: String => pblog.Log.traceThis (s)
        case (s: String, e: Throwable) => pblog.Log.traceThis (s, e)
        case _ => trace(p.toString)
      }
    }
  }

}

object Audit {
  def apply(f: => Any) = Log.audit (f.toString)
}

object Debug {
  def apply(f: => Any) = Log.debug(f.toString)
  
  implicit def toTee[T](l: Seq[T]): TeeSeq[T] = new TeeSeq[T](l)
  class TeeSeq[T](l: Seq[T]) {
    def tee: Seq[T] = {
      razie.Debug("TEE- " + l.mkString(", "))
      l
    }
    def tee(prefix: String): Seq[T] = {
      razie.Debug("TEE-" + prefix + " - " + l.mkString(", "))
      l
    }
    def teeIf(should:Boolean, prefix: String): Seq[T] = {
      if (should) razie.Debug("TEE-" + prefix + " - " + l.mkString(", "))
      l
    }
  }
}

object Warn {
  def apply(f: => Any) = Log.warn (f.toString)
}

object Alarm {
  def apply(f: => Any) = Log.error (f.toString)
}

object Error {
  def apply(f: => Any) = Log.error (f.toString)
}

