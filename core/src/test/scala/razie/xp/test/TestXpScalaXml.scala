/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp.test

import org.junit.Test
import org.scalatest.junit.MustMatchersForJUnit
import razie.{ XP, DomXpSolver }
import razie.xp.RazElement._
import razie.xp.RazElement
import org.scalatest.matchers.MustMatchers

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class TestXpScalaXml extends MustMatchers {

  @Test def test40 = expect(List("a")) { xpl("/").map(_.label) }
  @Test def test41 = expect(List("a")) { xpl("/a").map(_.label) }
  @Test def test42 = expect(List("a")) { xpl("a").map(_.label) }
  @Test def test43 = expect(List("b1", "b2")) { xpl("/a/b").map(x => (x \ "@ba").toString) }
  @Test def test43a = expect(List("b1", "b2")) { xpl("a/b").map(x => (x \ "@ba").toString) }
//  def test43b = expect(List("b1", "b2")) { xpl("/a").flatMap(XP.forScala("b").xpl(_)).map(x => (x \ "@ba").toString) }
  @Test def test44 = expect(List("b1", "b2")) { xpla("/a/b/@ba") }
  @Test def test45 = expect(List("c11", "c12", "c13")) { xpla("/a/b[@ba=='b1']/c/@ca") }
  @Test def test46 = expect(List("b1", "b2")) { xpla("/a/*/@ba") }
  @Test def test47 = expect(List("b1", "b2")) { xpla("/*/*/@ba") }
  
  @Test def test51 = expect(List("c11", "c12", "c13", "c21", "c22", "c23")) { xpla("/a/**/c/@ca") }
  @Test def test52 = expect(List("c11", "c12", "c13", "c21", "c22", "c23")) { xpla("/**/c/@ca") }

  def xpl(path: String) = XP forScala (path) xpl (TXXmls.x)
  def xpla(path: String) = XP forScala (path) xpla (TXXmls.x)

  @Test def test31 = expect(List("a")) { sx("/a").map(_.name) }
  @Test def test32 = expect(List("a")) { sx("a").map(_.name) }
  @Test def test33 = expect(List("b1", "b2")) { sx("/a/b").map(_ a "ba") }

  def sx(path: String) = XP[RazElement](path).xpl(new DomXpSolver, TXXmls.x)
}

object TXXmls {
  def x = {
    <a aa="a1">
      <b ba="b1">
        <c ca="c11"/>
        <c ca="c12"/>
        <c ca="c13"/>
      </b>
      <b ba="b2">
        <c ca="c21"/>
        <c ca="c22"/>
        <c ca="c23"/>
      </b>
    </a>
  }
}
