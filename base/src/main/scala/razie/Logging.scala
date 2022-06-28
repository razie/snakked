package razie

import org.fusesource.scalate.{ util => sfu }

/** A Logging trait you can mix into an implementation class without affecting its public API
 *
 *  NOTE: IF you get an anon class name, you can override the logger:
 *  protected override val logger = newlog (classOf)
 *
 *  NOTE: the formatting uses java.lang.String.format NOT slf4j formatting by reason of clusterfuk
 */
trait Logging {

  protected val logger = sfu.Log(getClass)

  protected def newlog(clazz: Class[_]) = sfu.Log(clazz)
  protected def newlog(s: String) = sfu.Log(s)

  /** use this if you want to log with slf4j conventions instead of the formatting conventions implemented here (String.format).
   *
   *  Printf rules!
   */
  protected def slf4j: org.slf4j.Logger = logger.log

  /** shorten string */
  private def t(s:String) = {
    if (s.length > 5000) {
      s.take(5000) + "...(truncated at 5k)"
    } else {
      s
    }
  }

  @inline protected def tee(message: => String): String = { val m = t(message); logger.trace(m); m }

  @inline protected def error(message: => String): Unit = logger.error(t(message))
  @inline protected def error(message: => String, e: Throwable): Unit = logger.error(e, t(message))

  @inline protected def warn(message: => String): Unit = logger.warn(t(message))
  @inline protected def warn(message: => String, e: Throwable): Unit = logger.warn(e, t(message))

  @inline protected def info(message: => String): Unit = logger.info(t(message))
  @inline protected def info(message: => String, e: Throwable): Unit = logger.info(e, t(message))
  @inline protected def log(message: => String): Unit = logger.info(t(message))
  @inline protected def log(message: => String, e: Throwable): Unit = logger.info(e, t(message))

  // TODO audit shoudl go in log no matter what
  @inline protected def audit(message: => String): Unit = logger.info("AUDIT " + t(message))
  @inline protected def audit(message: => String, e: Throwable): Unit = logger.info(e, "AUDIT " + t(message))

  @inline protected def debug(message: => String): Unit = logger.debug(t(message))
  @inline protected def debug(message: => String, e: Throwable): Unit = logger.debug(e, t(message))

  @inline protected def trace(message: => String): Unit = logger.trace(t(message))
  @inline protected def trace(message: => String, e: Throwable): Unit = logger.trace(e, t(message))

  /** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
  @inline def clog = new clog
  @inline class clog() {
    def <(x: Any) = { log("< " + x); this }
    def <<(x: Any) = { log("<<  " + x); this }
    def <<<(x: Any) = { log("<<<   " + x); this }

    def |(x: Any) = this < x
    def ||(x: Any) = this << x
    def |||(x: Any) = this <<< x

    def eol = { this }
  }

  /** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
  @inline def cdebug = new cdebug()
  @inline class cdebug() {
    def <(x: Any) = { debug("< " + x); this }
    def <<(x: Any) = { debug("<<  " + x); this }
    def <<<(x: Any) = { debug("<<<   " + x); this }

    def |(x: Any) = this < x
    def ||(x: Any) = this << x
    def |||(x: Any) = this <<< x

    def eol = { this }
  }
}

