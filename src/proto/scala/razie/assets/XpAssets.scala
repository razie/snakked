/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets

import razie._
import com.razie.pub.base.data._
import com.razie.pub.assets._
import com.razie.pub.base._
import com.razie.pub.base.data._
import razie.assets._

/** this resolves assets with the /x/y/z format. it's stateless so it's a singleton */
object XpAssetsSolver extends XpSolver[AssetBase,AssetBase] {
   override def getNext[T>:AssetBase,U>:AssetBase](o:(T,U),tag:String, assoc:String) : Iterable[(T,U)]={
      if (o._1 == AROOT || o._1 == null) {
         // starting point
         val ret = for (val b <- AssetMgr.find (tag, null, true).values) yield {
            val a = AssetMgr.getAsset(b.key);
            (a.asInstanceOf[T],a.asInstanceOf[U])
         }
         ret.toList
      } else {
         // not the first...
         val assocs = if (assoc == null || assoc.length() <= 0) {
           // assoc not specified - find the ONLY parent/child or association between those two
           razie.Metas.assocsBetween (o._1.asInstanceOf[Referenceable].getKey.meta, tag)
         } else {
            // assoc specified: we know who...
           razie.Metas.assocsBetween (o._1.asInstanceOf[Referenceable].getKey.meta, tag).filter (_.name == assoc)
         }
         
         val assocToUse = if (assocs.size != 1)
           throw new IllegalArgumentException ("ERR_ASSOC: there's NOT ONLY one assoc to/from: "+tag)
         else
           assocs.head
           
         for (x <- razie.AssetAssocs.associated (o._1.asInstanceOf[AssetBase], assocToUse)) 
            yield (x.asInstanceOf[T], null)
      }
   } 
  
   override def getAttr[T>:AssetBase] (o:T,attr:String) : String = razie.AA.sa (o, attr) match {
      case Some("") | None => attr match {
         case "key" => o.asInstanceOf[AssetBase].getKey.id
         case _ => ""
      }
      case Some(s) => s
   }

   override def reduce[T>:AssetBase,U>:AssetBase] (o:Iterable[(T,U)],cond:XpCond) : Iterable[(T,U)] =
      if (cond == null) o else o.filter(x => cond.passes(x._1, this))
}

/** root for asset queries */
private object AROOT {
}

