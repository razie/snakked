/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets;

import razie.base.{ActionItem, ActionContext}
import com.razie.pub.base.ActionItems;
import com.razie.pub.comms.SimpleActionToInvoke;
import razie.draw._
import razie.draw.widgets.NavButton;

/**
 * just a base inventory with some defaults like delete
 * 
 * @author razvanc
 */
abstract class BaseInventory extends AssetInventory {

   /** default handles only delete... */
    override def doAction(cmd : String, key : AssetKey, ctx : ActionContext) : AnyRef = {
      if (cmd == null || cmd.length() == 0 || cmd.equals("details")) {
            AssetMgr.getAsset(key) match {
               case a:Drawable => return a
            }
         }
      else if (cmd.startsWith(AssetBrief.DELETE.name)) {
         if (ctx == null || !ctx.isPopulated("confirmed")) {
            return confirmDelete(key);
         } else {
            delete(key);
            return "Ok";
         }
      }

    return "default-no-goodnik-doAction " + cmd + ", " + key;
   }

   protected def confirmDelete(ref:AssetKey ) = {
      val list = new DrawList();
      list.write(new NavButton(ActionItems.WARN, ""));
      list.write("Confirm deletion below or click BACK...");
//      val ati = new AssetActionToInvoke(ref, AssetBrief.DELETE);
//      ati.set("confirmed", "yes");
//      laati.ti.ist.write(ati);
      list;
   }

   /**
    * destroy, deallocate and remove the asset - must implement auth&auth itself
    * 
    * TODO include in main inv interface as CRUD ops
    */
   def delete(ref:AssetKey ) : Unit = {
      throw new UnsupportedOperationException("can't delete this " + ref);
   }

   /** queries can run in the background, they are multithreaded safe etc */
   /** default only directs to queryAll ... */
   override def query(criteria:QueryCriteria, env:AssetLocation , recurse:Boolean , toUse:AssetMap) : AssetMap = 
      criteria match {
         case AllOfType (meta) => queryAll(meta, env, recurse, toUse)
         case _ => 
            throw new IllegalArgumentException ("I only support AllOfType queries right now")
      }

   override def init(meta:Meta ) = { /* nothing to init */ }
}
