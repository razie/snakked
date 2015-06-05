/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp

import razie.{GPath, XP, M}

object XXX {
   type HASHEAD[A] = { def head : Option[A] }
}


/** 
 * Generic data sourcing API
 */
trait XPUntypedDataSouce3[T<:Any, L[_] <: XXX.HASHEAD[_]] { 
   
   /** find a list of elements */
   def xpl (xpath:GPath) : L[T]
   /** find a list of attributes */
   def xpla (xpath:GPath) : L[String]
   
//   /** find one element */
//   def xpe (xpath:GPath) : Option[T] = xpl (xpath) head
//   /** find one attribute */
//   def xpa (xpath:GPath) : Option[String] = razie.M firstOpt xpla (xpath) 
}

/** 
 * Generic data sourcing API
 */
trait XPUntypedDataSouce2[T<:Any] {
   /** find a list of elements */
   def xpl (xpath:GPath) : M[T]
   /** find a list of attributes */
   def xpla (xpath:GPath) : M[String]
   
   /** find one element */
   def xpe (xpath:GPath) : Option[T] = razie.M firstOpt xpl (xpath) 
   /** find one attribute */
   def xpa (xpath:GPath) : Option[String] = razie.M firstOpt xpla (xpath) 
}

/** 
 * Generic data sourcing API
 */
trait XPUntypedDataSouce[T<:Any] {
   /** find something... */
   def xp (xpath:GPath) : M[T]
   def xpa (xpath:GPath) : M[String]
}

/** 
 * Generic data sourcing API
 * 
 * Types are: L - container, E - element (any), A - attribute (String)
 */
trait XPDataSouce[E<:Any, L[_], A] {
   /** find one element */
   def xpe (xpath:GPath) : Option[E]
   /** find a list of elements */
   def xpl (xpath:GPath) : L[E]
   /** find one attribute */
   def xpa (xpath:GPath) : Option[A]
   /** find a list of attributes */
   def xpla (xpath:GPath) : L[A]
}
