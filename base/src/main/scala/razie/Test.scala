/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

/** much simplified unit test primitives: expect() */
trait Test {
  var failed = 0

  def expect    (x: Any)(f: => Any) : Any = expect ((a:Any,b:Any)=>a==b)(x)(f)
  def expectnot (x: Any)(f: => Any) : Any = expect ((a:Any,b:Any)=>a!=b)(x)(f)

  def expect(eq:(Any, Any) => Boolean)(x:Any)(f: => Any) : Any = {
    val r = f;
    if (eq(x, r))
      println (x + "...as expected")
    else {
      println ("Expected " + x + " : " + x.asInstanceOf[AnyRef].getClass + " but got " + r + " : " + r.asInstanceOf[AnyRef].getClass)
      failed = failed + 1
    }
    r
  }

  def dontexpect(x: Any)(f: => Any) = {
    println ("Skipping...")
  }

  def report {
    if (failed > 0)
      println ("====================FAILED " + failed + " tests=============")
    else
      println ("ok")
  }
}

object TestSample extends App with razie.Test {
  expect (3) { 3 }
  
  expect ("12") { "12" }

  expect ("12") { "13" }

  expect (true) { true }

  report
}
