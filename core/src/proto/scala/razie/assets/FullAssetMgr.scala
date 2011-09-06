/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import com.razie.pub.base._
import com.razie.pub.assets._
import com.razie.pub.base.data._
import razie.base._
import razie.base.scripting._

/** complete assetmgr - allows injection of actions on assets 
 * 
 * @author razvanc
 */
class FullAssetMgr extends InventoryAssetMgr with AssetMgrInjector {

   /** this version takes into account possible injections first */
   override def getSupportedActions(ref:AssetKey) : Array[ActionItem] =
      super.getSupportedActions(ref) ++  injections (ref.getType())

   /** this version takes into account possible injections first */
   override def doAction(cmd:String, ref:AssetKey, ctx:ActionContext) : Object = injection (ref.getType(), cmd) match {
      case Some(x) => x(ref, null, cmd, ctx)
      case None => super.doAction(cmd,ref,ctx);
      }

}

object FullAssetMgr {
   def instance : FullAssetMgr = AssetMgr.instance.asInstanceOf[FullAssetMgr]
}
