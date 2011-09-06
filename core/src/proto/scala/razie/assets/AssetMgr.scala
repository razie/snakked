/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import razie.base._
import razie.base.scripting._
import com.razie.pub.base._
import razie.draw.Drawable
import razie.g.GRef

/** the default asset mgr is the InventoryAssetMgr */
object AssetMgr extends AssetMgr (null) with razie.g.GResolver[AnyRef] with razie.g.GAct {
   razie.g.GAMResolver.assetMgr = this
   override def resolve (x:GRef) : Option[AnyRef] = Option(getAsset (AssetKey.fromRef(x)))
   razie.g.GAMAct.assetMgr = this
   override def actions (k:GRef)             : Seq[ActionItem] = 
      getSupportedActions(AssetKey.fromRef(k)).toList
   override def act     (k:GRef, a:String, ctx:ActionContext) : Any   = 
      doAction(a, AssetKey.fromRef(k), ctx)
   
   protected var impl = new razie.NoStatic[AssetMgr] ("NewAssetMgr", {
      new FullAssetMgr
   } );

   def instance() : AssetMgr = impl.get //if (impl.get != null) impl.get else init (new FullAssetMgr())
      
   def init (delegate : AssetMgr) : AssetMgr = { 
      impl.set(delegate); 
      delegate 
   }
   
   protected override def proxy : AssetInventory = impl.get

//  def getOrElse (key: AssetKey, default: => AnyRef): AnyRef = instance.getAsset(key) match {
//    case Some(v) => v
//    case None => default 
//  }
   
   /** get the meta description for a certain type */
   override def meta (name:String) : Option[Meta] = instance.meta(name)
   
   /** get the supported metas*/
   override def metas() : Iterable[String] = instance.metas()

   override def register(meta:Meta)  = instance.register (meta)

   override def pres () : AssetPres = instance.pres()
   
   val NOACTIONS = Array[ActionItem]()
}

/** an AssetMgr is just a proxy to the actual implementation, which you must init() in your main() */
abstract class AssetMgr (actual:AssetMgr) extends InvProxy (actual) {

   override def getAsset(key : AssetKey) = proxy.getAsset(key)
   
   /** get the meta description for a certain type */
   def getMeta (name:String) : Meta = meta(name) match {
      case Some (m) => m
      case _ => throw new IllegalArgumentException ("Meta not found: " + name)
   }
   
   def meta (name:String) : Option[Meta]
   
   /** get the supported metas*/
   def metas() : Iterable[String]

   def register(meta:Meta) 

   def pres () : AssetPres
   
   /** list all assets of the given type at the given location */
   def find(meta:String, where:AssetLocation, recurse:Boolean) : AssetMap = 
     query (AllOfType (meta), where, recurse, new AssetMap)
}

/** simple inventory proxy - delegates to another inventory */
class InvProxy (var iproxy:AssetInventory) extends AssetInventory {

   protected def proxy : AssetInventory = iproxy
   
   def init (delegate : AssetInventory) = iproxy=delegate
   
   /** get an asset by key - it should normally be AssetBase or SdkAsset */
   def getAsset(key : AssetKey) = proxy.getAsset(key)

    /**
     * get/make the brief for an asset given its key. The idea around briefs is that I don't always
     * need the full asset - often i can get around by just a proxy brief...it's a matter of cost
     */
    def getBrief(key : AssetKey) : AssetBrief = proxy.getBrief(key)

    /** execute command on asset. the asset can be local or remote */
    def doAction(cmd : String, key : AssetKey, ctx : ActionContext) : AnyRef =
       proxy.doAction (cmd, key, ctx)

    def getSupportedActions(key : AssetKey) : Array[ActionItem] =
       proxy.getSupportedActions (key)

    /** initialize this instance for use with this Meta - note that these metas would 
     * have been registered as supported by this inventory, otherwise throw some exception */
    def init (meta : Meta) = proxy.init(meta)
   
    /** queries can run in the background, they are multithreaded safe etc */
    def query(criteria:QueryCriteria, env:AssetLocation , recurse:Boolean , toUse:AssetMap) : AssetMap = 
       proxy.query(criteria, env, recurse, toUse)
    /** queries can run in the background, they are multithreaded safe etc */
    def queryAll(meta:String, env:AssetLocation , recurse:Boolean , toUse:AssetMap) : AssetMap = 
       proxy.queryAll(meta, env, recurse, toUse)
}
