/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp.test

import org.scalatest.junit._
import org.scalatest.SuperSuite
import razie.{ XP, DomXpSolver }
import razie.base.data.RazElement._
import razie.Snakk

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class TestXpScalaXml extends JUnit3Suite {

  def testw41 = expect(List("a")) { xxml \ "/" map (_.label) }
//  def testw42 = expect(List("a")) { xxml \ "a" map (_.label) }
  def testw43 = expect(List("b1", "b2")) { xxml \ "b" map(x => (x \ "@ba").toString) }
  def testw44 = expect(List("b1", "b2")) { xxml \ "b" \@ "ba"}
  def testw45 = expect(List("c11", "c12", "c13")) { xxml \ "b[@ba=='b1']" \ "c" \@ "ca" }
  def testw46 = expect(List("b1", "b2")) { xxml \ "*" \@ "ba" }

  val xxml = Snakk (TXXmls.x)
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
