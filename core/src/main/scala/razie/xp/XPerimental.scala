/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp

import razie.GPath
import razie.XP

object PreDef {
  implicit def stog (s:String) : GPath = GPath (s)
}

/** 
 * the simplest solver: can solve an attribute - basically AttrAccess. 
 * In this case, the expression can only a single attribute name
 */
trait AXP {
   /** find one attribute */
   def xpa (path:String) : Option[String]
}

/** 
 * smart object with simple strings. the internal recursive structure of the object is irrelevant.
 * 
 * also, i assume most structures will implement GSOXP[Any], but you can also feeed your type-coucious yourself
 */
trait GOXP[A] extends AXP {
   /** find one element */
   def xpe (path:GPath) : Option[A]
   /** find a list of elements */
   def xpl (path:GPath) : Seq[A]
   /** find one attribute */
   def xpa (path:GPath) : Option[String]
   /** find a list of attributes */
   def xpla (path:GPath) : Seq[String]

   /** find one element */
   final def xpe (path:String) : Option[A] = xpe (GPath(path))
   /** find a list of elements */
   final def xpl (path:String) : Seq[A] = xpl (GPath(path))
   /** find one attribute */
   final def xpa (path:String) : Option[String] = xpa (GPath(path))
   /** find a list of attributes */
   final def xpla (path:String) : Seq[String] = xpla (GPath(path))
}

/** 
 * IC stands for idiot controller. these are more like a visitor, who can resolve gpath on an R
 */
trait ICOXP[R,A] extends AXP {
   /** find one element */
   def xpe (path:GPath, root:R) : Option[A]
   /** find a list of elements */
   def xpl (path:GPath, root:R) : Seq[A]
   /** find one attribute */
   def xpa (path:GPath, root:R) : Option[String]
   /** find a list of attributes */
   def xpla (path:GPath, root:R) : Seq[String]

   /** find one element */
   final def xpe (path:String, root:R) : Option[A] = xpe (GPath(path), root)
   /** find a list of elements */
   final def xpl (path:String, root:R) : Seq[A] = xpl (GPath(path), root)
   /** find one attribute */
   final def xpa (path:String, root:R) : Option[String] = xpa (GPath(path), root)
   /** find a list of attributes */
   final def xpla (path:String, root:R) : Seq[String] = xpla (GPath(path), root)
}

/** 
 * IC stands for idiot controller. these are more like a visitor, who can resolve gpath on an R
 * 
 * F stands for "From" - i.e. this is a resolved
 */
trait FICOXP[R,A] extends AXP {
   /** find one element */
   def xpe (path:GPath, root:R) : Option[A]
   /** find a list of elements */
   def xpl (path:GPath, root:R) : Seq[A]
   /** find one attribute */
   def xpa (path:GPath, root:R) : Option[String]
   /** find a list of attributes */
   def xpla (path:GPath, root:R) : Seq[String]

   /** find one element */
   final def xpe (path:String, root:R) : Option[A] = xpe (GPath(path), root)
   /** find a list of elements */
   final def xpl (path:String, root:R) : Seq[A] = xpl (GPath(path), root)
   /** find one attribute */
   final def xpa (path:String, root:R) : Option[String] = xpa (GPath(path), root)
   /** find a list of attributes */
   final def xpla (path:String, root:R) : Seq[String] = xpla (GPath(path), root)
}

/** 
 * XP resolution from a given node
 */
trait XPFrom[T] {
   /** find one element */
   def xpe (root:T) : T
   /** find a list of elements */
   def xpl (root:T) : List[T]
   /** find one attribute */
   def xpa (root:T) : String
   /** find a list of attributes */
   def xpla (root:T) : List[String]
}

    
class Person {}
class SuperOrder {}

object People {
   def xpe (path:String) : Person = null
}

object Samples {
   val john = People xpe ("/Person[@areGroovy=='nah!']")
   val me = xpe ("/{hasFriends}Person[@areGroovy=='yeah, baby!']") from john
   val minime = XP[Person] ("/Person[@areGroovy=='yeah, baby!']")
}

trait XPathType {
//   def from[T] (root:T) : T
//   def apply[T] = from(null)
   def from[T] (root:T) (implicit m:scala.reflect.Manifest[T]) : T
}

case class xpe (expr:String) extends XPathType {
    override def from [T] (root:T) (implicit m:scala.reflect.Manifest[T]) : T = (XP[T] (expr) using null) xpe (root)
//    override def apply [T] : T = from(null)
//    def apply [T] = XP[T] (expr) 
   
}



trait SuperMan_ager {
   def xpl (path:String) : List[SuperOrder]  = XP[SuperOrder] (path).xpl(null, null) 
   def openOrders () : List[SuperOrder]  = xpl ("/SuperOrder[@state=='Open']")
}




/** 
 * This is a simplified API so the calls are transparent
 */
trait GXPLike[T] {
   /** find one element */
   def xpe (root:T) : T
   /** find a list of elements */
   def xpl (root:T) : List[T]
   /** find one attribute */
   def xpa (root:T) : String
   /** find a list of attributes */
   def xpla (root:T) : List[String]
}
