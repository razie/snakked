/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets;

import java.lang.reflect.Method;
import org.w3c.dom._
import com.razie.pub.agent._
import com.razie.pub.lightsoa._
import com.razie.pub.assets._
import com.razie.pub.base._
import razie.base.data._
import com.razie.pub.base.data._
import razie.assets._


import razie.base.ActionItem;

/**
 * a meta-description of an asset type
 * 
 * The inventory and asset class specs are used elsewhere...didn't bother extending...you don't have
 * to use them
 * 
 * TODO 1-1 metas need a namespace
 */
class Meta (val id:ActionItem, val baseMeta:String, val assetCls:String, var inventory:String, val namespace:String) {

   var supportedActions : Array[ActionItem] = null

   // for older java code
   def getId = id
   def getBaseMeta = baseMeta
   def assetCld = assetCls
   def getInv = inventory
   
   /** basic constructor - the inventory/class are set automatically when registered */
   def this (id:ActionItem, base:String ) = this (id, base, null, null, null)

   /** basic constructor - the inventory/class are set automatically when registered */
   def this (id:ActionItem) = this (id, null, null, null, null)

   /** a meta for assets with an inventroy */
   def this(id:ActionItem, base:String , inv:String ) = this (id, base, null, inv, null)

//   private void reflectActions () {
//      List<ActionItem> l = new ArrayList<ActionItem>();
//      if (assetCls != null || assetCls.length() > 0)
//         try {
//            for (Method m : Class.forName(assetCls, true, ClassLoader.getSystemClassLoader()).getMethods()) {
//                 if (m.getAnnotation(SoaMethod.class) != null 
//                       && m.getAnnotation(SoaMethodSink.class) == null) {
//                     l.add(new ActionItem (m.getName()));
//                 }
//             }
//         } catch (Exception e) {
//            e.printStackTrace();
//         }
//  }

   def toAA : razie.AA = {
      val aa = razie.AA ()
      aa.set("name", id.name)
      aa.set("inventory", inventory)
      if (baseMeta != null && baseMeta.length() > 0)
         aa.set("base", baseMeta)

      aa
   }
   
   def toBriefXml() : String = {
      toAA.toXmlWithChildren(this, "metaspec")(x=>"")
   }

   def toDetailedXml() : String = {
      toAA.toXmlWithChildren(this, "metaspec") ( meta => {
         meta.asInstanceOf[Meta].supportedActions.foldRight ("") (
            (ai, prev) => prev+"<action name=\"" + ai.name + "\""+"/>\n")
      // TODO 1-1 add assocs
      })
   }

   override def toString() : String = toBriefXml()
}

/** describes an association between metas 
 * 
 * TODO 1-1 do assocs need a namespace?
 * */
class MetaAssoc (
      val name:String,
      val aEnd:String, val zEnd:String, val stereotype:String, 
      val card:String="0..1-0..n", val aRole:String="", val zRole:String="") { 
   // TODO i need to implement the traceback: who injected/defined this association
   val traceback:String = null
   
   def toXml = {
      var s = "<metaassoc"
      s += " name=\"" + name + "\""
      s += " aEnd=\"" + aEnd + "\""
      s += " zEnd=\"" + zEnd + "\""
      s += " stereotype=\"" + stereotype + "\""
      s += " card=\"" + card + "\""
      s += " aRole=\"" + aRole + "\""
      s += " zRole=\"" + zRole + "\""
      s + "/>\n"
   }
}

/** this is a full specification of a meta: the meta itself together with all neccessary associations. Note that the associations can be only from and to the current meta
 * 
 * @param m the meta specification 
 * @param assocs all associations to and from the meta
 */
class MetaSpec (val meta:Meta, val assocs:List[MetaAssoc]){
   def this (m:Meta) = this (m, List())
   def this (m:String) = this (new Meta (razie.AI.stoai(m)), List())
}

/** implemented by all classes who know their metas - simplifies code Assets.manage(new MyAsset()) */
trait HasMeta {
   def metaSpec : MetaSpec
}

object Meta extends SimpleXml {
   def fromAnn (cls:Class[_]) = {
      val am = cls.getAnnotation(classOf[AssetMeta])
      new Meta (
//         id = razie.AI.stoai(am.name), baseMeta=am.base, inventory=am.inventory.getName, namespace=am.namespace 
         id = razie.AI.stoai(am.name), baseMeta=am.base, inventory=am.inventory.getName, 
         namespace=am.namespace, assetCls = cls.getName
        )
   }

   def fromXml (e:RazElement) =
      new Meta(
            razie.AI cmdicon (a(e, "name"), a(e, "icon")), 
            "", a(e, "inventory"))
}

trait SimpleXml {
   def ax (e:Element, name:String, dflt:String="") = 
      if (e.hasAttribute (name)) e.getAttribute(name) else dflt

   def a (e:RazElement, name:String, dflt:String="") = 
      if (e.ha(name)) e.a(name) else dflt
}

object MetaAssoc  extends SimpleXml {
   /** when defined by itself */
   def fromXml (e:RazElement) =
      new MetaAssoc (
            name = a(e,"name"),
            aEnd = a(e,"aEnd"),
            zEnd = a(e, "zEnd"),
            stereotype = a(e, "stereotype", "assoc"), 
            card = a(e, "aCard", "0-n"), 
            aRole = a(e, "aRole"), 
            zRole = a(e, "zRole")
            )
   
   /** when defined under a meta parent tag, which is the "aEnd" */
   def fromXml (e:RazElement, m:RazElement) =
      new MetaAssoc (
            name = a(e, "name"),
            aEnd = a(e,"aEnd", a(m, "name")),
            zEnd = a(e, "zEnd"),
            stereotype = a(e, "stereotype", "assoc"), 
            card = a(e, "aCard", "0-n"), 
            aRole = a(e, "aRole"), 
            zRole = a(e, "zRole")
            )
   /** when defined under a meta parent tag, which is the "aEnd" */
   def fromAnno (e:AssetAssoc) =
      new MetaAssoc (
            name = e.name,
            aEnd = e.a,
            zEnd = e.z,
            stereotype = e.stereotype,
            card = e.card,
            aRole = e.aRole,
            zRole = e.zRole
            )
}