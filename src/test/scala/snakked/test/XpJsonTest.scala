/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.xp.test

import org.scalatest.junit._
import org.scalatest.SuperSuite
import razie._
import razie.Snakk

import org.json._

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class XpJsonTest extends JUnit3Suite {
     val json = """
{
  "errorCode": 203, 
  "errorMessage": "You must be authenticated to access recent", 
  "statusCode": "ERROR",
  "a" : { 
    "value": "a1" ,
    "b" : { 
      "name": "b1",
      "value": "b1" 
    },
    "b" : { 
      "name": "b2",
      "value": "b2" 
    }
    "c" : [{ 
      "value": "c1" 
    }, { 
      "value": "c2" 
    }, { 
      "value": "c3" 
    }]
  }
}
"""
       
   def testw1  = expect ("203") { wroot \@ "errorCode"}
   def testw2  = expect ("a1") { wroot \ "a" \@@ "value"}
   def testw2a = expect ("a1") { wroot \ "*" \@@ "value"}
   def testw3  = expect ("b1") { wroot \ "a" \ "b" \@@ "value"}
   def testw4  = expect ("b1") { wroot \ "*" \ "b" \@@ "value"}
   
   def testw5  = expect ("b2") { wroot \ "*" \ "b[name='b2']" \@@ "value"}
   
   def testw6  = expect ("c2") { wroot \ "*" \ "c" \ 1 \@@ "value"}
 
   val wroot = Snakk json json
}

