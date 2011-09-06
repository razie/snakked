/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets;

import java.util.List;

import com.razie.pub.comms.SimpleActionToInvoke;
import razie.base._

/**
 * context-specific actions are built here. This is invoked just before those are needed for
 * display. Find usage for details...
 * 
 * @author razvanc
 */
trait Affordance {
   /**
    * make the actions / affordances for this element
    * 
    * @param assetInQuestion
    * @param assetIfPresent
    * @return
    */
   def make(assetInQuestion:AssetKey, assetIfPresent:AnyRef) : Array[ActionToInvoke]
}

object NoAffordance extends Affordance {
   def make(assetInQuestion:AssetKey, assetIfPresent:AnyRef) : Array[ActionToInvoke]=Array()
}