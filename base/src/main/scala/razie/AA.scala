/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import razie.base._
import collection.JavaConverters._

object args {
  def apply (s:Any*):AA = {
    val x = new AA(); 
    x.setAttr(s.asInstanceOf[Seq[AnyRef]]:_*); 
    x 
    }
  def apply ():AA = new AA()
}

/** simplify usage of AttrAccess - attribute management
 * 
 * @author razvanc
 */
object AA {
  // TODO 3-2 protect this against idiot code
  final val EMPTY = AA()
   
  def apply (s:Any*):AA = {val x = new AA(); x.setAttr(s.asInstanceOf[Seq[AnyRef]]:_*); x }
  def map (s:Map[String,AnyRef]):AA = {val x = new AA(); s.map(t=>x.setAttr(t._1,t._2)); x }
  def apply ():AA = new AA()

  def wrap (a:AttrAccess, s:AnyRef*) = new WrapAttrAccess (a,s:_*)
   
   /** simplify accessing attributes of asset classes 
    * 
    * @param s asset, of any class
    * @param name identifies the attribute
    * @return the attribute value, if we can find something yielding attribute values...
    */
   def a (s:Any, name:String):Option[AnyRef] = s match {
      case x:AttrAccess => Some(x a name)
      case x:HasAttrAccess => Some(x.attr a name)
      case _ => None // TODO 2-3 to try reflection... both scala and java versions :)
   }
   
   /** simplify accessing attributes of asset classes 
    * 
    * @param s asset, of any class
    * @param name identifies the attribute
    * @return the attribute value, if we can find something yielding attribute values...
    */
   def sa (s:Any, name:String):Option[String] = a (s,name) match {
      case Some(null) => None
      case Some(x) => Some(x.toString)
      case _ => None 
   }
   
   def foreach (a:AttrAccess, f : (String, AnyRef) => Unit) =
     a.getPopulatedAttr.asScala foreach (x => f(x, a a x))
}

/** 
 * simplified AttributeAccess :)
 * 
 * @author razvanc
 */
class AA extends AttrAccessImpl {
   def this (xx : AnyRef*) = { this(); setAttr (xx); }
   
   def toXmlWithChildren (me:Any, tag:String)(contents:Any=>String) = {
      var s = "<"+tag + " "
      s += (for (a <- this.sgetPopulatedAttr) yield a+"=\"" + this.getAttr(a) + "\"").mkString(" ")
      s += ">\n"
      s += contents (me)
      s += "</>\n"
         s
   }
   
   override def equals (other:Any) : Boolean = {
     other match {
        case o:AA => { var b=true; this.foreach((n,v) => { if (v != o.a(n)) b=false }); b }
        case _ => false
     }
   }
   
   def ++  (other:AA) = new WrapAttrAccess (this, other)
   def ++= (other:AA) = { setAttr(other); this }

   def toMap = { (for (a <- this.sgetPopulatedAttr) yield (a -> this.sa(a))).toMap }
}

/** hierarchical implementation - this one adds stuff to a parent's */
class WrapAttrAccess (val parent:AttrAccess, s:AnyRef* ) extends AA {
  import scala.collection.{JavaConversions => JC}
   
  setAttr(s:_*); 
  
  // java compat for ScriptContext
  def this (something:Boolean, parent : AttrAccess, s:AnyRef*) = this (parent, s:_*)
  def this (parent : AttrAccess) = this (parent, Nil:_*)
  def this (parent : AttrAccess, aa:AttrAccess) = this (true, parent, aa)
  def this (parent : AttrAccess, aa:Array[AnyRef]) = this (true, parent, aa:_*)
  
  override def sgetPopulatedAttr = this.rebuild.keySet

  // TODO optimize
  def rebuild() : scala.collection.mutable.Map[String,Null] = {
//     val a : List[String] = if(this._order != null) JC.asBuffer(this._order).toList else Nil
     val a = this._order
//     val b : List[String] = 
//       if (this.parent != null) JC.asIterable(parent.getPopulatedAttr).toList  else Nil
     val b = if (this.parent != null) parent.sgetPopulatedAttr  else Nil
     
     val m = new scala.collection.mutable.HashMap[String,Null]()
     a.foreach(x=>m.put (x,null))
     b.foreach(x=>m.put (x,null))
     
     m
  }
    
  override def isPopulated(name:String ) : Boolean = 
    super.isPopulated(name) || (parent != null && parent.isPopulated(name))

  override def getPopulatedAttr() : java.lang.Iterable[String] =  JC.asJavaIterable(rebuild.keySet)

  override def size() : Int = rebuild.keySet.size

  override def hasAttrType(name:String ) : Boolean = {
      (this._types != null && this._types.get(name) != null) || (parent != null && parent.hasAttrType(name));
   }

   override def getAttrType(name:String ) : AttrType = {
      val t = super.getAttrType (name)
      if (t == null && parent != null) parent.getAttrType(name) else t
   }

   override def getAttr(name:String ) : AnyRef = {
     val o = super.getAttr (name)
     if (o == null && parent != null) parent.getAttr(name) else o
   }

}
