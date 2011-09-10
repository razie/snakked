/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package snakking.test

import org.junit.Test
import org.scalatest.junit.MustMatchersForJUnit
import razie.Snakk
import org.apache.commons.jxpath.JXPathContext

/**
 * junit tests for the XP stuff
 *
 * @author razvanc99
 */
class SnakkBeanTest extends MustMatchersForJUnit {

  //  @Test def test1 = expect(List(root)) { root }
  @Test def test2 = expect("s") { root \@ "s" }
  @Test def test3 = expect("s") { root \\\ "s" }
  @Test def test4 = expect("t") { root \@ "t" }
  @Test def test5 = expect("u") { root \\\ "u" }

  @Test def test6 = expect(List(new JavaB("j"))) { root \ "j" map identity }
  @Test def test7 = expect(List(new JavaB("a"), new JavaB("b"))) { root \ "l" map identity }

  @Test def test8 = expect("a") { root \ "l[value=='a']" \@@ "value" }
  @Test def test9 = expect(List("a", "b")) { root \ "l" \@ "value" }
  @Test def test0 = expect("s") { root \ "j" \ "a" \\\ "s" }
  @Test def testa = expect("s") { root \ "j" \@@ "s" }
  @Test def testb = expect("s") { root \ "j" \\* "s" }
  @Test def testc = expect(List("s", "s")) { root \ "j" \* "s" map identity }

  val root = Snakk bean new ScalaB("root")

  case class Student(name: String, age: Int)
  case class Class(name: String, students: Student*)
  case class School(name: String, classes: Class*)

  val jschool = School("my school",
    Class("1st grade",
      Student("Joe", 6),
      Student("Ann", 7)),
    Class("2nd grade",
      Student("Mary", 8),
      Student("George", 7)))
  val school = Snakk bean jschool

  @Test def test11  = expect("Ann" :: "George" :: Nil) { school \ "classes" \ "students[age==7]" \@ "name" }
  @Test def test12  = expect("Ann" :: "George" :: Nil) { school \\ "students[age==7]" \@ "name" }
  
//  @Test def tt = expect ("buci") { (JXPathContext.newContext (jschool)).getValue("name")}
}
