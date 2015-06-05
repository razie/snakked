/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp.test

import org.junit.Test
import org.scalatest.junit.MustMatchersForJUnit
import razie._
import org.scalatest.matchers.MustMatchers

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class TestXpString extends MustMatchers {

  @Test def test11a = expect(List("root")) {
    XP("/root").xpl(StringXpSolver, "/root")
  }

  @Test def test11b = expect(List("root")) {
    XP("/root").xpl(StringXpSolver, "root")
  }

  @Test def test12 = expect(List("s1")) {
    XP("/r/s1").xpl(StringXpSolver, "/r/s1")
  }

  @Test def test21 = expect(List(("a", List(null)))) { xp("/a", "a") }
  @Test def test22 = expect(List(("a", List(null)))) { xp("a", "a") }
  @Test def test23 = expect(List(("a", List("/b")))) { xp("/a/b", "a") }
  @Test def test24 = expect(List(("a", List("/b")))) { xp("a/b", "a") }

  def xp(src: String, path: String) = StringXpSolver.getNext((src, List(src)), path, null)
}
