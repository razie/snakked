/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.test

import org.scalatest.junit._
//import org.scalatest.SuperSuite
import razie.SM
import org.junit.Test

/**
 * junit tests for the State Machie
 * 
 * @author razvanc99
 */
class SMTest extends MustMatchersForJUnit {
   implicit def e (i:Int) = SM.IEvent(i)
   val sm = new razie.SampleSM()

     
   @Test def testL = expect ("s2") { 
      sm move 2 move 1 move 3 move -1 move 2 move 3 move 6 move 59 
      sm.currState.name
   }
}
