/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.test

import org.scalatest.junit._
//import org.scalatest.SuperSuite
import org.junit.Test

// sample of how to implement a domain model for XP access

// TODO 

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class MTest extends MustMatchersForJUnit {
   import razie.M._
   
   @Test def testL = expect (List("a")) { razie.M apply { val l = new java.util.ArrayList[String](); l.add("a"); l } }
   @Test def testI = expect (razie.M("a"::Nil)) { razie.M apply { val l = new java.util.ArrayList[String](); l.add("a"); l }.iterator }
   @Test def testB = expect (List("a")) { (razie.M apply { val l = new java.util.HashMap[String, String](); l.put("a", "a"); l.values }).toList }
   @Test def testM = expect (List("a")) { (razie.M apply { val l = new java.util.HashMap[String, String](); l.put("a", "a"); l}).toList }
   @Test def testA = expect (List("a")) { (razie.M apply { Array("a") }).toList }
   @Test def testS = expect (List("a")) { (razie.M apply { for (x <- Array("a")) yield x }).toList }
   @Test def testN = expect (List()) { razie.MOLD apply { null } }
   @Test def testV = expect (List("a")) { razie.MOLD apply { "a" } }
   
   @Test def testL1 = expect ("a"::Nil) { razie.M apply ala }
   @Test def testL2 = expect ("a"::Nil) { razie.M (ala) }
   val ala = { val l = new java.util.ArrayList[String](); l.add("a"); l }

   import razie.M._
   @Test def testEq = expect (true) {razie.M.equals (1::2::Nil, 1::2::Nil) (_==_)}
   
}
