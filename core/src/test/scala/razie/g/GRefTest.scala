/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.g

import org.scalatest.junit._
//import org.scalatest.SuperSuite
import razie.{G}
import razie.g._
import org.junit.Test

/**
 * testing the assets
 * 
 * @author razvanc99
 */
class GRefTest extends MustMatchersForJUnit {
   val k1 = GRef ("Meta", "id")
   //val k1a = GRef ("Meta", null)
   val k2 = GRef ("Meta", "id", GLoc("c:\\Video"))
   val k3 = GRef ("Meta", "id", GLoc ("http://gigi.com"))
   val k4 = GRef ("Meta", "id", GLoc ("http://gigi.com", "c:\\Video"))
//   val k5 = new AssetCtxKey ("Meta", "id", new AssetLocation ("http://gigi.com::c:\\Video"), new AssetContext(razie.AA("someattr=somevalue")))
//   val k6 = new AssetCtxKey ("Meta", "id", new AssetLocation ("http://gigi.com::c:\\Video"), new AssetContext())
//   k6.ctx.role("k4", k4)
   
   val k7 = GRef id ("Meta", "id", GLoc ("http://gigi.com", "c:\\Video"))
   val k8 = GRef aq ("Meta", razie.AA("name=john"), GLoc ("http://gigi.com", "c:\\Video"))
//   val k9 = GRef rq ("Meta", "id", GLoc ("http://gigi.com", "c:\\Video"))
   val k10= GRef xq ("/Meta[@name=='john']", GLoc ("http://gigi.com", "c:\\Video"))
   
   val s1 = "razie.gidref:Meta:id"
   //val s1a = "razie.gidref:Meta"
   val s2 = "razie.gidref:Meta:id@c:\\Video"
//   val ss2 = "razie.uri:Meta:id@c:/Video/"
   val s3 = "razie.gidref:Meta:id@http://gigi.com"
   val s4 = "razie.gidref:Meta:id@http://gigi.com::c:\\Video"
   val s4old = "razie.uri:Meta:id@http://gigi.com::c:\\Video"
      
//   val s5 = "razie.uri:Meta:id@http://gigi.com::c:\\Video@@someattr=somevalue&ctx.name="

   @Test def testk1() = expect (k1) { GRef.parse(s1) }
   //def testk1a() = expect (k1a) { GRef.parse(s1a) }
   @Test def testk2() = expect (k2) { GRef.parse(s2) }
   @Test def testk3() = expect (k3) { GRef.parse(s3) }
   @Test def testk4() = expect (k4) { GRef.parse(s4) }
   @Test def testk4old() = expect (k4) { GRef.parse(s4old) }

   @Test def testk1s() = expect (s1) { k1.toString }
   @Test def testk2s() = expect (s2) { k2.toString }
   @Test def testk3s() = expect (s3) { k3.toString }
   @Test def testk4s() = expect (s4) { k4.toString }
  
   @Test def testk7() = expect (k7) { GRef.parse("razie.gidref:Meta:id@c:\\Video") }
   @Test def testk8() = expect (k8) { GRef.parse("razie.gaqref:Meta:name%3Djohn@http://gigi.com::c:\\Video") }
//   @Test def testk9() = expect (k9) { GRef.parse("razie.gaqref:Meta:name%3Djohn@http://gigi.com::c:\Video") }
   @Test def testk10() = expect (k10) { GRef.parse("razie.gxqref:Meta:%2FMeta%5B%40name%3D%3D%27john%27%5D@http://gigi.com::c:\\Video") }
//   @Test def testZ() = expect (1) { k4.toString; println (k7); println (k8); println (k10); 1 }
   
//   @Test def testk4l() = expect ("c:\\Video/") { k4.loc.localPath  }
//   @Test def testk4ll() = expect ("http://gigi.com:8080") { k4.loc.toHttp } // this is really a mutant url so...

//   // TODO 3-1 complete the asset context key implementation and test
//   @Test def testk5s() = expect (s5) { k5.toString } 
//   @Test def testk5() = expect (s5) { AssetKey.fromString(s5).toString }
   
//   @Test def testk6() = expect(k4){  k6.asInstanceOf[AssetCtxKey].ctx.role("k4") } 
}
