/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import com.razie.pub.lightsoa._
import com.razie.pub.assets._
import com.razie.pub.base._
import com.razie.pub.resources._
import com.razie.pub.agent._
import com.razie.pub.assets._
import com.razie.pub.draw._
import com.razie.pub.comms._
import razie.draw.widgets._
import razie.assets._
import razie.base._

/** 
 * this is a blank asset - use it any way you see fit, to simplify testing - find its uses in our tests
 */
@SoaAsset(descr = "blank self defined asset", bindings=Array("http"))
class BlankAsset (val META:String, val k:String, var myAttr:AttrAccess=null) extends AssetImpl with HasMeta with HasAttrAccess {
   // TODO 3-1 CODE i wanted this to be a defaulted val
   if (myAttr == null) myAttr = new AttrAccessImpl ()
   
   def attr = myAttr

   def metaSpec = new MetaSpec (new Meta (razie.AI cmdicon(META, "/public/pics/web.png"), null))
   
   /* funny initialization after redefining meta() */
   setKey (new AssetKey(META, k, null))
   
   @SoaMethod (descr="test method")
   def sayhi() = "hi<p>paragraph</p> " + this.key
   
   @SoaMethod (descr="test method", args = Array("what"))
   def say(what:String) = myAttr a what
}
