/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp.test

import org.junit.Test
import org.scalatest.matchers.MustMatchers

import razie.xp.BeanSolver
import razie.XP

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class XpBeanTest extends MustMatchers {

  @Test def test1 = expect(List(root)) { xpl("/") }
  @Test def test2 = expect("s") { xpa("/@s") }
  @Test def test3 = expect("s") { xpe("s") }
  @Test def test4 = expect("t") { xpa("@t") }
  @Test def test5 = expect("u") { xpe("/root/u") }
  @Test def test6 = expect(List(new JavaB("j"))) { xpl("/root/j") }
  @Test def test7 = expect(List(new JavaB("a"), new JavaB("b"))) { xpl("/root/l") }
  @Test def test8 = expect("a") { xpa("/root/l[@value=='a']/@value") }
  @Test def test9 = expect(List("a", "b")) { xpla("/root/l/@value") }
  @Test def test0 = expect("s") { xpe("/root/j/a/s") }
  @Test def testa = expect("s") { xpa("/root/j/@s") }
//  @Test def testb = expect("s") { xpe("/root/j/*") }
//  @Test def testc = expect(List("s", "s")) { xpl("/root/j/*/s") }
//  @Test def testb = expect("s") { xpe("/root/j/*/s") }

  @Test def findByClass1 = expect ("j") { xpe ("/root/j/String")}
  @Test def findByClass2 = expect ("s") { xpe ("/root/j/JavaA/s")}
  @Test def findByClass3 = expect ("c") { xpe ("/root/**/C4/c")}
  @Test def findByClass4 = expect ("c") { xpa ("/root/**/C4/@c")}
  @Test def findByClass5 = expect (1) { xpl ("/root/**/C4").size}

  def xpe(path: String) = XP[Any](path) using BeanSolver xpe root
  def xpl(path: String) = XP[Any](path).xpl(BeanSolver, root)
  def xpla(path: String) = XP[Any](path).xpla(BeanSolver, root)
  def xpa(path: String) = XP[Any](path).xpa(BeanSolver, root)

  val root = new ScalaB("root")
//  val root = BeanXpSolver.WrapO(jroot)
}
