/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets

import razie.base.{ActionItem, ActionContext}
import razie.draw.Drawable;
import razie.assets._

/**
 * inventories manage entities - we need invnetories to GET entities from a key, or QUERY/FIND
 * entities
 * 
 * invnetories are indexed by managed entity types in the AssetMgr
 * 
 * inventories manage entities - basic functionality is locating assets
 * 
 * inventories should not be accessed directly, but via the AssetMgr.
 * 
 * @author razvanc
 */
trait AssetInventory {
    /** get an asset by key - it should normally be an AssetBase */
    def getAsset(key : AssetKey) : AnyRef

    /**
     * get/make the brief for an asset given its key. The idea around briefs is that I don't always
     * need the full asset - often i can get around by just a proxy brief...it's a matter of cost
     */
    def getBrief(key : AssetKey) : AssetBrief 

    /** execute command on asset. the asset can be local or remote */
    def doAction(cmd : String, key : AssetKey, ctx : ActionContext) : AnyRef

    def getSupportedActions(key : AssetKey) : Array[ActionItem]

    /** initialize this instance for use with this Meta - note that these metas would 
     * have been registered as supported by this inventory, otherwise throw some exception */
    def init (meta : Meta) : Unit

    /** queries can run in the background, they are multithreaded safe etc */
    def query(criteria:QueryCriteria, env:AssetLocation , recurse:Boolean , toUse:AssetMap) : AssetMap
    
    /** queries can run in the background, they are multithreaded safe etc */
    // TODO remove
    def queryAll(meta:String, env:AssetLocation , recurse:Boolean , toUse:AssetMap) : AssetMap
}


class AssetMap {
  val m = new scala.collection.mutable.HashMap[AssetKey, AssetBrief]
  
  def put (k:AssetKey, v:AssetBrief) = this.m.put (k,v)
  def values () : Iterable[AssetBrief] = this.m.values
  def jvalues () : java.lang.Iterable[AssetBrief] = razie.RSJ apply values

  /** when you use these methods, it will strictify this - i.e. wait for all to be collected and then transform to a map - blocking call */
  def toMap : scala.collection.mutable.Map[AssetKey,AssetBrief] = m
//  def toJavaMap : java.util.Map[AssetKey,AssetBrief] = razie.RSJ apply m
  
}

trait QueryCriteria {
   def isAll = false
}

class QueryBase extends QueryCriteria {} 
case class WithMeta (meta:String) extends QueryBase
/** this is the only mandatory query */
case class AllOfType (override val meta:String) extends WithMeta (meta) { override def isAll=true }
case class ByAttributes (override val meta:String, attrs:razie.AA) extends WithMeta (meta)
case class ByAssoc (startFrom:AssetKey, assoc:String) extends QueryCriteria

