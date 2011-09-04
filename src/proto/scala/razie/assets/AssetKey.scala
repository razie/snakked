/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import razie.base._
import razie.AA
import scala.collection._

/**
 * Each entity/asset has a unique key, which identifies the asset's type, id and location. Borrowed
 * from OSS/J's ManagedEntityKey (type, key, location), this is lighter and designed to pass through
 * URLs and be easily managed as a string form.
 * 
 * <p>
 * asset-URI has this format: <code>"razie.uri:entityType:entityKey@location"</code>
 * <p>
 * asset-context URI has this format: <code>"razie.puri:entityType:entityKey@location&context"</code>
 *
 * When the asset support framework is used, this key can have this other form:
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey"</code>
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/KEY-ENCODED"</code>
 * <p>
 * OR: REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey?context"</code>
 * 
 * Actions can be invoked like so
 * <p>
 * REST action asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey/action?args"</code>
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/KEY-ENCODED/action&args"</code>
 * <p>
 * OR: REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey?context"</code>
 * 
 * <ul>
 * <li>type is the type of entity, should be unique among all other types. HINT: do not keep
 * defining "Movie" etc - always assume someone else did...use "AlBundy.Movie" for instance :D
 * <li>key is the unique key of the given entity, unique in this location and for this type. The key
 * could be anything that doesn't have an '@' un-escaped. it could contain ':' itself like an
 * XCAP/XPATH etc, which is rather cool...?
 * <li>location identifies the location of the entity: either URL or folder or a combination. 
 * A folder implies it's on the localhost. An URL implies it's in that specific server.
 * </ul>
 * 
 * <p>
 * Keys must have at least type. By convention, if the key is missing, the key refers to all
 * entities of the given type.
 * 
 * @author razvanc99
 */
class AssetKey (_meta:String, _id:String, _loc:AssetLocation) 
extends razie.g.GIDRef (_meta, _id, if (_loc==null) null else _loc.toGLoc) {
// TODO optimize - don't need the internal _xxx vars above...   
//   val meta:String = _meta
//   val id:String = if (_id == null) AssetKey.uid() else _id
   
   // TODO LOC  - search for TODO LOC
//   override val loc:AssetLocation = if(_loc == null) AssetLocation.LOCAL else _loc
   val aloc:AssetLocation = if(_loc == null) AssetLocation.LOCAL else _loc
   
   def this (_meta:String, _id:String) = this (_meta, _id, null)
   def this (_meta:String) = this (_meta, AssetKey.uid, null)

   // these have to be functions rather than vars for existing java code...
   def getMeta() = meta
   def getId() = id
   // TODO LOC  - search for TODO LOC
//   def getLoc() = loc
   def getLoc() = aloc
   
   // TODO 1-1 inline
   def getType() = meta
   // TODO LOC  - search for TODO LOC
   def getLocation() = aloc

    override def equals(o:Any):Boolean = o match {
       case r : AssetKey =>  meta.equals(r.meta) && id.equals(r.id)
       case _ => false
    }

    override def hashCode() : Int = meta.hashCode() + (if(id != null ) id.hashCode() else 0)

    /** short descriptive string */
    override def toString = 
       AssetKey.PREFIX+":" + meta + ":" + (if(id == null ) "" else java.net.URLEncoder.encode(id, "UTF-8")) + (if (loc == null || AssetLocation.LOCAL.equals(loc)) "" else ("@" + loc.toString()))

    /**
     * Use this method to get a string that is safe to use in a URL. Note that whenever the string
     * is encoded when you want to use it it must be decoded with fromUrlEncodedString(String).
     */
    def toUrlEncodedString : String = 
            java.net.URLEncoder.encode(toString, "UTF-8")
}

/**
 * Each entity/asset has a unique key, which identifies the asset's type, id and location. Borrowed
 * from OSS/J's ManagedEntityKey (type, key, location), this is lighter and designed to pass through
 * URLs and be easily managed as a string form.
 * 
 * <p>
 * asset-URI has this format: <code>"razie.uri:entityType:entityKey@location"</code>
 * <p>
 * asset-context URI has this format: <code>"razie.puri:entityType:entityKey@location&context"</code>
 *
 * When the asset support framework is used, this key can have this other form:
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey"</code>
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/KEY-ENCODED"</code>
 * <p>
 * OR: REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey?context"</code>
 * 
 * Actions can be invoked like so
 * <p>
 * REST action asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey/action?args"</code>
 * <p>
 * REST asset-URI has this format: <code>"http://SERVER:PORT/asset/KEY-ENCODED/action&args"</code>
 * <p>
 * OR: REST asset-URI has this format: <code>"http://SERVER:PORT/asset/entityType/entityKey?context"</code>
 * 
 * <ul>
 * <li>type is the type of entity, should be unique among all other types. HINT: do not keep
 * defining "Movie" etc - always assume someone else did...use "AlBundy.Movie" for instance :D
 * <li>key is the unique key of the given entity, unique in this location and for this type. The key
 * could be anything that doesn't have an '@' un-escaped. it could contain ':' itself like an
 * XCAP/XPATH etc, which is rather cool...?
 * <li>location identifies the location of the entity: either URL or folder or a combination. 
 * A folder implies it's on the localhost. An URL implies it's in that specific server.
 * </ul>
 * 
 * <p>
 * Keys must have at least type. By convention, if the key is missing, the key refers to all
 * entities of the given type.
 * 
 * @author razvanc99
 */
////class AssetKey (_meta:String, _id:String, val loc:AssetLocation) extends razie.GRef (_meta, _id, loc.gloc) {
//class AssetKey (_meta:String, _id:String, val loc:AssetLocation) extends razie.GRef (_meta, _id, loc.gloc) {
////   override val loc = new AssetLocation (super.loc)
//   
//   def this (_meta:String, _id:String) = this (_meta, _id, null)
//   def this (_meta:String) = this (_meta, razie.G.uid, null)
//
//   // these have to be functions rather than vars for existing java code...
//   def getMeta() = meta
//   def getId() = id
//   def getLoc() = loc
//   
//   // TODO 1-1 inline
//   def getType() = meta
//   def getLocation() = loc
//}

object AssetKey {
   def fromRef (x:razie.g.GRef) = 
      new AssetKey (x.meta, x.asInstanceOf[razie.g.GIDRef].id, AssetLocation.fromGLoc(x.loc)) 
   
   def PREFIX = "razie.uri"
   
    /** to allocate next UID...this should be done better */
    private var seqNum : Int = 1;

    /**
     * just a simple UID implementation, to fake IDs for objects that don't have them.
     */
    def uid() =  "Uid-" + {seqNum+=1; seqNum} + "-" + String.valueOf(System.currentTimeMillis());

    /**
     * make up from an entity-URI. see class javadocs for details on URI
     * 
     * TODO it's not efficient - creates too many objects to parse the string
     * 
     * @return the entity-URI
     */
    def fromString(inurl:String) :AssetKey = {
        var url = inurl;

        var news:String="";
        if (url.startsWith(AssetKey.PREFIX)) {
            news = url.replace(AssetKey.PREFIX+":", "");
        } else {
            val map1 = url.split("://", 2);

            // with the following, i support also a missing PREFIX, i.e. a simplified KEY with just
            // type:key@loc
            news = (if(map1.length > 1 ) map1(1) else (if (map1.length == 1 ) map1(0) else null))
        }

        if (news != null) {
            // i have a class nm
            val map2 = news.split(":", 2);
            if (map2.length > 1 && map2(1) != null) {
                // i have a key/id
                val map3 = map2(1).split("@", 2);
                if (map3.length > 1) {
                   // i have an appEnv
                   if (map3(1).contains ("@@")) {
                      // i have a context
                      val map4 = map3(1).split("@@", 2);
                       return new AssetCtxKey(map2(0), decode(map3(0)), new AssetLocation(
                               map4(0)), new AssetContext (razie.AA(map4(1))));
                   } else 
                    return new AssetKey(map2(0), decode(map3(0)), new AssetLocation(
                            map3(1)));
                } else {
                    // no appEnv
                    return new AssetKey(map2(0), decode(map3(0)), null);
                }
            } else {
                // no key/id
                return new AssetKey(map2(0), null, null);
            }
        }
        return null;
    }

    val ROLE = "role."
       
//    def ctx (s:String) = {
//       val ac = new AssetContext (razie.AA(s))
//       for (x <- razie.RJS apply ac.attrs.getPopulatedAttr)
//          if (x.startsWith(ROLE))
//             ac.env.put(x.replaceFirst(ROLE, ""), fromString(ac.attrs.sa(x)))
////       ac.attrs.foreach ((x,y) => if (x.startsWith(ROLE)) ac.env.put(x.replaceFirst(ROLE, ""), fromString(y)))
//    }
    
   def decode (s:String) = java.net.URLDecoder.decode(s, "UTF-8")
            
//   implicit def toac (a : AttrAccess) : AssetContext = new AssetContext (a)
}

/** Context is an important notion. see detailed blurb on our wiki.homecloud.ca
 * 
 * <p>Basically, a reference to an entity can contain the context in which it was made. The same 
 * entity may do or mean different things depending on its context.
 * 
 * <p>Recommend this be used sparingly.
 */
class AssetContext (val name:String, val attrs : AttrAccess) {
//   val assocs : List[AssetAssoc]   
   // TODO 1-1 lazy map

//   if (attrs != null)
//      (razie.RJS apply attrs.getPopulatedAttr).filter(_.startsWith(AssetKey.ROLE)).foreach (
//             x => env.put(x.replaceFirst(AssetKey.ROLE, ""), AssetKey.fromString(AssetKey.decode(attrs.sa(x))))
//   )
      
   def this () = this ("", AA.EMPTY)
   def this (a:AttrAccess) = this ({a.sa ("ctx.name")}, a) 
   
   // cleanup
   
   def sa (name:String) : String = attrs.sa(name)
   def role(name:String) : AssetKey = attrs.sa (AssetKey.ROLE+name) match {
      case s:String => AssetKey.fromString(AssetKey.decode(s))
      case null => null
   }
   def role(name:String, who:AssetKey) : AssetContext = {
      attrs.set(AssetKey.ROLE+name, who.toUrlEncodedString)
      this
      }

   override def toString : String = {
      attrs.set ("ctx.name", name)
      attrs.addToUrl("")
   }

    override def equals(o:Any):Boolean = o match {
       case r : AssetContext =>  false // TODO 2-1 implement
       case _ => false
    }

}

   // TODO 3-1 complete the asset context key implementation and test

/** this type of key is used when an asset is in context */
class AssetCtxKey (_meta:String, _id:String, _loc:AssetLocation, val ctx:AssetContext) extends AssetKey (_meta, _id, _loc) {
    /** short descriptive string */
    override def toString = 
       AssetKey.PREFIX+":" + meta + ":" + (if(id == null ) "" else java.net.URLEncoder.encode(id, "UTF-8")) + (if (loc == null) "" else ("@" + loc.toString())) + ( if (ctx == null) "" else ("@@" + ctx.toString))

    // NOTE that for now i ignore the context... it is an interesting question wether the context should be ignored...
    override def equals(o:Any):Boolean = o match {
//       case r : AssetCtxKey => super.equals(o) && r.ctx .equals(ctx)
       case r : AssetKey => super.equals(o) 
       case _ => false
    }
}
