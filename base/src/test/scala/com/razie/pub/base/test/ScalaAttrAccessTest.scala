/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package com.razie.pub.base.test

import org.junit.Test
import org.scalatest.junit._
import razie.base._
//import org.scalatest.SuperSuite
import razie.AA

/**
 * testing the assets
 * 
 * @author razvanc99
 */
class ScalaAttrAccessTest extends MustMatchersForJUnit {
  val aa = new AttrAccessImpl("attr1:string=val1,attr2:int=2");

  @Test def testString() = expect ("2") { ScalaAttrAccessImpl.fromString(aa.toString).a("attr2") }
   
  @Test def testw1() = expect(3) {AA.wrap(aa, "attr3", "3").size}
  @Test def testw2() = expect(2) {AA.wrap(aa, "attr2", "3").size}
   
  @Test def testw3() = expect(List("val1","2","3")) {  AA.wrap(aa, "attr3", "3").mapValues((v:String)=>v)  }
  @Test def testw4() = expect(List("val1","3")) { AA.wrap(aa, "attr2", "3").mapValues((v:String)=>v) }
}
