/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import razie.base.{ActionItem, ActionContext}
import com.razie.pub.base.log.Log;
import razie.draw.Drawable;
//import com.razie.pub.lightsoa.SoaAsset;

/**
 * simple inventory-based manager, a proxy for all inventories
 * 
 * @author razvanc
 */
class InventoryAssetMgr extends AssetMgr (null) {
   val invByMeta      = new collection.mutable.HashMap[String, AssetInventory]();
   val invByJavaClass = new collection.mutable.HashMap[String, AssetInventory]();
   val metaByName     = new collection.mutable.HashMap[String, Meta]();
   var proxyInv : ProxyInventory =null

   /** get the meta description for a certain type */
   override def meta (name:String) : Option[Meta] = metaByName.get(name)
   
   /** get the supported metas*/
   override def metas() : Iterable[String] = metaByName.keySet

   override def getBrief(ref:AssetKey ) =
      findInventory(ref.getType()).getBrief(ref);

   override def getAsset(ref:AssetKey ) = {
      findInventory(ref.getType()).getAsset(ref) match {
         case a:AssetBase => a
         case o:Any => error("ERR_ASSET_INV inventories should return SdkAsset(s)...this returned " + o.getClass().getName())
      }
   }

   override def queryAll(meta:String, env:AssetLocation , recurse:Boolean , ret:AssetMap) : AssetMap =
      findInventory(meta).queryAll(meta, env, recurse, ret)
   
   override def query(criteria:QueryCriteria, env:AssetLocation , recurse:Boolean , ret:AssetMap) : AssetMap =
      criteria match {
         case WithMeta (meta) =>
            findInventory(meta).query(criteria, env, recurse, ret)
         case _ => error ("I only support WithMeta queries right now")
      }

   override def getSupportedActions(ref:AssetKey ) =
      findInventory(ref.getType()).getSupportedActions(ref) match {
      // check for non-compliant implementations
      case s : Array[ActionItem] => s
      case _ => AssetMgr.NOACTIONS 
   }

   override def doAction(cmd:String, ref:AssetKey, ctx:ActionContext) = 
      findInventory(ref.meta).doAction(cmd, ref, ctx)
      
   val pres : AssetPres          = new razie.assets.pres.TempAssetPres();
//   val pres : AssetPres          = null
//   def pres () : com.razie.pub.assets.AssetPres =  pres;
   
   def instance() : InventoryAssetMgr = AssetMgr.instance().asInstanceOf[InventoryAssetMgr]
   
   override def register(meta:Meta) = {
      val classnm = meta.inventory;
      var inv : AssetInventory = null;

      if (classOf[ProxyInventory].getName().equals(classnm)) {
         // there can be just one - stupid singleton per AssetMgr
        inv = proxyInventory(); 
      } 
      
         try {
      invByJavaClass.get(classnm) match {
         case Some(i) => { inv = i; invByMeta.put(meta.id.name, inv) }
         case None =>
            inv = Class.forName(classnm, true, ClassLoader.getSystemClassLoader())
                  .newInstance().asInstanceOf[AssetInventory];
            invByJavaClass.put(classnm, inv);
            invByMeta.put(meta.id.name, inv);
            razie.Log("CREATE_INV " + classnm);
      }

      inv.init(meta);
      metaByName.put(meta.id.name, meta);
         } catch {
            case e:Exception => {
               razie.Log("ERR_CANT_CREATE_INV " + classnm, e);
            }
         }
   }

   def proxyInventory() : ProxyInventory = {
      if (proxyInv == null) {
         proxyInv = new ProxyInventory();
         invByJavaClass.put(classOf[ProxyInventory].getName(), proxyInv);
      }

      return proxyInv;
   }

   def hasInventory(meta:String )  = invByMeta.get(meta).isDefined

   def findInventory(meta:String) :AssetInventory = 
      invByMeta.get(meta) match {
      case Some(s) => s
      case None => error("ERR_ASSET_INV cannot find inventory for meta: " + meta)
   }
}

object InventoryAssetMgr {
   def instance() : InventoryAssetMgr = AssetMgr.instance().asInstanceOf[InventoryAssetMgr]
}

/**
 * simple inventory for all assets - this is used for smart assets that do not have their own inventories
 * 
 * you can define new assets in user.xml and they automatically are managed here. Assets will have
 * "key" and other attributes. you can add logic via scripting, see "sampleassets" in user.xml
 * 
 * @author razvanc
 */
class ProxyInventory extends AssetInventory {
   
   razie.Log("INIT proxyiinv")

   val assets = new collection.mutable.HashMap[AssetKey, AssetBase]()

   /** find this inventory in assetmgr and register each object */
   def register(k:AssetKey , o:AssetBase) = assets.put(k, o);

   /** use the base and add details */
   override def query(criteria:QueryCriteria, env:AssetLocation , recurse:Boolean , ret:AssetMap) : AssetMap = 
      criteria match {
         case WithMeta (meta) => queryAll (meta, env, recurse, ret)
         case _ => 
            throw new IllegalArgumentException ("I only support WithMeta queries right now")
      }

   /** use the base and add details */
   override def queryAll(meta:String, env:AssetLocation , recurse:Boolean , ret:AssetMap) : AssetMap =  {
      assets.values.filter (_.key.meta == meta).foreach (x => ret.put(x.key, x.brief))
      ret;
   }

   override def getBrief(ref:AssetKey ) : AssetBrief = assets.get(ref) match {
      case Some(a) => a.brief
      case _ => null
   }
      
   override def getAsset(ref:AssetKey ):AnyRef = assets.get(ref) match {
      case Some(a) => a
      case _ => null
   }
   
   override def doAction(cmd:String , ref:AssetKey , ctx:ActionContext ) = null

   override def getSupportedActions(ref:AssetKey ) : Array[ActionItem] = Array()
      // TODO Auto-generated method stub

   override def init(meta:Meta ) = { /* nothing to init */ }
}
