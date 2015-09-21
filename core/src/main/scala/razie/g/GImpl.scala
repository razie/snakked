/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.g

import razie.G
import razie.base.{ActionContext=>AC}
import razie.base.ActionItem

/** 
 * the entire world is a graph of regferenceable entities...Here's all we need to mess with it :D
 */
object snakked {
}

/** generic location: it's either an url or a directory...or both - this basically keeps track of the gremlin's whereabouts */
case class GLoc (val url:String, val dir:String = null) {
   override def toString : String = {
      val sep = if (url != null && dir != null) "::" else ""
      (if (url != null) url else "") + sep + (if (dir != null) dir else "")
   }
   
   def toXX = if (url != null) url else dir
   
   def localPath : String = 
     if (dir != null && ! dir.endsWith("/")) 
        dir + "/"
        else dir
}

/** generic reference - uniquely identifies one (or more?) entities */
trait GRef {
   /** the meta may include a schema */
   def meta:String
   def loc:GLoc
   override def toString : String
   def urlEncoded : String = java.net.URLEncoder.encode(toString, "UTF-8")
}

/** gref utilities */
object GRef {
   def from       (s:String) : GRef = parse(s)
   def fromString (s:String) : GRef = parse(s)
   
   def apply(m:String, id:String, loc:GLoc=G.LOCAL) = GIDRef (m, id, loc)
   def id   (m:String, id:String, loc:GLoc=G.LOCAL) = GIDRef (m, id, loc)
   def aq   (m:String, attrs:razie.AA, loc:GLoc=G.LOCAL) = GAQRef (m, attrs, loc)
   def rq   (m:String, ql:String, loc:GLoc=G.LOCAL) = GRQRef (m, ql, loc)
   def xq   (x:String, loc:GLoc=G.LOCAL) = GXQRef (x, loc)
//   def xp   (x:String, root:GRef, loc:GLoc=G.LOCAL) = GXQRef (x, loc)
   
   def parseLoc(inurl:String) : GLoc = {
      inurl match {
         case null | "" => G.LOCAL
         case _ => {
            if (inurl contains "::") {
               val Pat1 = """(.*)::(.*)""".r
               var Pat1 (u,d) = inurl
               GLoc (u, d)
            } else if (inurl startsWith "http://") GLoc (inurl, null) 
            else GLoc (null, inurl)
         }
      }
   }

   // TODO 2-2 support cases like just meta etc
   def parse(inurl:String) : GRef = {
        val Pat1 = """([^:]*):([^:]*):(.*)""".r
        var Pat1 (p,m,i1) = inurl
        var l1:String = null

        if (i1 != null && i1.contains("@")) {
           val Pat2 = """([^@]*)@(.*)""".r
           var Pat2 (i2, l2) = i1
           i1=i2
           l1=l2
        }
           
        val i = if (i1 == null) null else decode(i1)
        val l = parseLoc (l1)
        
        p match {
           case G.GIDREF => GRef id (m,i,l)
           case G.GAQREF => GRef aq (m,razie.AA(i),l)
           case G.GRQREF => GRef rq (m,i,l)
           case G.GXQREF => GRef xq (i,l)
           case G.PREFIX => GRef (m,i,l)
        }
   }
        
   def decode (s:String) = java.net.URLDecoder.decode(s, "UTF-8")
   
    /** to allocate next UID...this should be done better */
    private var seqNum : Int = 1;

    /**
     * just a simple UID implementation, to fake IDs for objects that don't have them.
     */
    def uid() = synchronized { "Uid-" + {seqNum+=1; seqNum} } // + "-" + String.valueOf(System.currentTimeMillis());
}

/** strait-forward unique Id of a single entity */
case class GIDRef (_meta:String, _id:String, _loc:GLoc) extends GRef {
   val meta:String = _meta
   val id:String = if (_id == null) GRef.uid() else _id
   val loc:GLoc = if(_loc == null) G.LOCAL else _loc
   
    override def equals(o:Any):Boolean = o match {
       case GIDRef (m,i,l) =>  meta.equals(m) && id.equals(i)
       case _ => false
    }

    override def hashCode() : Int = meta.hashCode() + (if(id != null ) id.hashCode() else 0)

    /** short descriptive string */
    override def toString = 
       G.GIDREF+":" + meta + ":" + (if(id == null ) "" else java.net.URLEncoder.encode(id, "UTF-8")) + (if (loc == null || G.LOCAL.equals(loc)) "" else ("@" + loc.toString()))
}

/** identifies en entity with the given properties/attribute query */
case class GAQRef (val meta:String, val attrs:razie.AA, val loc:GLoc) extends GRef {
   
    override def equals(o:Any):Boolean = o match {
       case GAQRef (m,a,l) =>  meta.equals(m) && attrs.equals(a)
       case _ => false
    }

    override def hashCode() : Int = meta.hashCode() + (if(attrs != null ) attrs.hashCode() else 0)

    /** short descriptive string */
    override def toString = 
       G.GAQREF+":" + meta + ":" + (if(attrs == null ) "" else java.net.URLEncoder.encode(attrs.toString, "UTF-8")) + (if (loc == null || G.LOCAL.equals(loc)) "" else ("@" + loc.toString()))
}

/** TODO identifies en entity with the given relational query */
case class GRQRef (val meta:String, val ql:String, val loc:GLoc) extends GRef {
   
    override def equals(o:Any):Boolean = o match {
       case GRQRef (m,q,l) =>  meta.equals(m) && ql.equals(q)
       case _ => false
    }

    override def hashCode() : Int = meta.hashCode() + (if(ql != null ) ql.hashCode() else 0)

    /** short descriptive string */
    override def toString = 
       G.GRQREF+":" + meta + ":" + (if(ql == null ) "" else java.net.URLEncoder.encode(ql, "UTF-8")) + (if (loc == null || G.LOCAL.equals(loc)) "" else ("@" + loc.toString()))
}

/** identifies en entity with the given gpath query */
case class GXQRef (xp:String, val loc:GLoc) extends GRef {
  lazy val gpath = razie.GPath (xp)
  override lazy val meta:String = gpath.nonaelements.last.name 
      
    override def equals(o:Any):Boolean = o match {
       case GXQRef (x,l) =>  xp.equals(x) 
       case _ => false
    }

    override def hashCode() : Int = meta.hashCode() + (if(xp != null ) xp.hashCode() else 0)

    /** short descriptive string */
    override def toString = 
       G.GXQREF+":" + meta + ":" + (if(xp == null ) "" else java.net.URLEncoder.encode(xp, "UTF-8")) + (if (loc == null || G.LOCAL.equals(loc)) "" else ("@" + loc.toString()))
}

/** generic referenceable entities */
trait GReferenceable {
   def key : GRef
}

/** generic referenceable entity resolver / manager */
trait GResolver [T] {
   def resolve (key : GRef) : Option[T]
   // TODO add query API ?
}

/** static entry point for a resolver */
object GAMResolver {
   var assetMgr : GResolver[AnyRef] = null
   /** main entry point for finding entities: resolve them */
   def resolve (key : GRef) : Option[AnyRef] = assetMgr.resolve(key)
   /** Java jas issues using Some<A> so this returns the object or null */
   def jresolve (key : GRef) : AnyRef = resolve(key).get
}

/** association resolver */
trait GAssocResolver [From, To] {
   def resolve (from:From, as:String) : Option[To]
}

object GUid {
   /** TODO allocate next UID...this should be done better */
   private var seqNum : Long = 1;

   /**
    * just a simple UID implementation, to fake IDs for objects that don't have them.
    */
   def apply () = synchronized { "Uid-" + {seqNum+=1; seqNum} } // + "-" + String.valueOf(System.currentTimeMillis());
}

// TODO handling exception scenarios
trait GCRUD [A] {
   def c (k:GRef, ctx:AC) : Option[A]  // create an entity - return the "skeleton"
   def r (k:GRef)         : Option[A]  // read, i.e. retrieve an entity
   def u (k:GRef, x:A)    : Option[A]  // update the entity with the given values
   def d (k:GRef)         : Option[A]  // delete the entity
   
   def q (k:GRef) : Seq[GRef]
}

trait GAct {
   def actions (k:GRef) : Seq[ActionItem]
   def act     (k:GRef, a:String, ctx:AC) : Any   
}

object GAMAct extends GAct {
   var assetMgr : GAct = null
   def actions (k:GRef) : Seq[ActionItem] = assetMgr.actions(k)
   def act     (k:GRef, a:String, ctx:AC) : Any   = assetMgr.act(k, a, ctx)
}

