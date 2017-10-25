/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.xp.test

import org.json.JSONObject
import org.junit.Test
import org.scalatest.junit.MustMatchersForJUnit

import razie.xp.JsonSolver
import razie.XP

/**
 * junit tests for the XP stuff
 * 
 * @author razvanc99
 */
class XpJsonTest extends MustMatchersForJUnit {
     val json = """
{
  "errorCode": 203, 
  "errorMessage": "You must be authenticated to access recent", 
  "statusCode": "ERROR",
  "a" : { 
    "value": "a1" ,
    "b" : { 
      "value": "b1" 
    }
  }
}
"""
        // TODO how to handle json arrays?
//          "["+
//          "\"b\" : { \"value\": \"b1\" }," +
//          "\"b\" : { \"value\": \"b2\" }," +
//          "]"+
       
   @Test def test1  = expect ("203") { xpa("/root/@errorCode")}
   @Test def test2  = expect ("a1") { xpa("/root/a/@value")}
   @Test def test2a = expect ("a1") { xpa("/root/*/@value")}
   @Test def test2b = expect ("a1") { xpa("root/a/@value")}
   @Test def test3  = expect ("b1") { xpa("/root/a/b/@value")}
   @Test def test4  = expect ("b1") { xpa("/root/*/b/@value")}
 
   def xpe(path:String) = XP.forJson(path) xpe root
   def xpl (path:String) = XP.forJson(path) xpl root
   def xpla(path:String) = XP.forJson(path) xpla root
   def xpa(path:String) = XP.forJson(path) xpa root

   val root = JsonSolver.WrapO(new JSONObject(json))
}

