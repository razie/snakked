/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package snakking.test

import org.junit.{Test=>Testme}
import org.scalatest.junit.MustMatchersForJUnit
import razie._
import razie.Snakk

import org.json._

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class SnakkJsonTest extends MustMatchersForJUnit {
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
    "c" : [{ 
      "name": "c1",
      "value": "c1" 
    }, { 
      "name": "c2",
      "value": "c2" 
    }, { 
      "name": "c3",
      "value": "c3" 
    }]
  }
}
"""
       
  @Testme def testw1  = expect ("203") { wroot \@ "errorCode"}
  @Testme def testw2  = expect ("a1") { wroot \ "a" \@@ "value"}
  @Testme def testw2b = expect ("a1") { wroot \* "*" \@@ "value"}
  @Testme def testw3  = expect ("b1") { wroot \ "a" \ "b" \@@ "value"}
  @Testme def testw4  = expect ("b1") { wroot \* "*" \ "b" \@@ "value"}
   
  @Testme def testw5  = expect ("c2") { wroot \* "*" \ "c[name=='c2']" \@@ "value"}
  @Testme def testw5a = expect ("c2") { wroot \** "c[name=='c2']" \@@ "value"}
   
  @Testme def testw6  = expect ("c2") { wroot \* "*" \ "c" \ 1 \@@ "value"}
 
  val wroot = Snakk json json
}

