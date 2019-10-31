/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import scala.collection._
import java.{lang => jl, util => ju}

import scala.collection.mutable.ListBuffer

/** helper class - has all the conversions */
object M {

  type M[A] = List[A]

   // ------------------- common monad stuff

   /** apply f on each pair (A,B) and contain result 
    * 
    * TODO return a nice non-strict monad
    */
   def parmap[A,B,C] (x:M[A], y:M[B]) (f:(A,B)=>C) : M[C] = {
      // TODO optimize
    val i1 = x.iterator
    val i2 = y.iterator
    var b = false
    val res = ListBuffer[C]()

    while (i1.hasNext && i2.hasNext && !b) {
      val x1 = i1.next
      val x2 = i2.next
      res += f(x1, x2)
    }

    // TODO see quals - what if the lists are not equals? get pissed?
    res.toList
   }

   /** compare two monads, regardless of the element's order. It's expensive
    * 
    * it will basically make sure that EACH element in x will have a match in y 
    * 
    * TODO should it and then that each element in y has a match in x?
    * 
    * @param x - to compare
    * @param y - to compare
    * @param eeq - eq function 
    */
   def equalsNotOrdered[A,B] (x:M[A], y:M[B]) (eeq:(A,B)=>Boolean) = {
      // TODO optimize
    val i1 = x.iterator
    val i2 = y.iterator
    var bad = false

    for (i1 <- x if (!bad)) {
      var loopDone = false
      
      for (i2 <- y if (!loopDone)) if (eeq(i1, i2))  loopDone = true
       
      if (!loopDone)
         bad = true
    }
    
    !bad 
   }
   
   /** compare two monads, given a comparison function 
    * 
    * @param x - to compare
    * @param y - to compare
    * @param eeq - eq function 
    */
   def equals[A,B] (x:List[A], y:List[B]) (eeq:(A,B)=>Boolean) = {
      // TODO optimize
    val i1 = x.iterator
    val i2 = y.iterator
    var bad = false

    while (i1.hasNext && i2.hasNext && !bad) {
      val x1 = i1.next
      val x2 = i2.next

      if (! eeq(x1,x2)) {
        bad = true
      }
    }

    !(bad || i1.hasNext || i2.hasNext)
   }
   
}

object MOLD {
  import JavaConversions._

  def apply (f:Any) : List[_] = {
    f match {
      case l : java.util.List[_] => (for (x <- l) yield x).toList
      case i : java.util.Iterator[_] => (for (x <- i) yield x).toList
      case b : java.lang.Iterable[_] => (for (x <- b) yield x).toList
      case m : java.util.Map[_,_] => (for (x <- m.values) yield x).toList
      case a : Array[_] => (for (x <- a) yield x).toList
      case s : scala.Seq[_] => (for (x <- s) yield x).toList
      case null => (for (x <- None) yield x).toList
      case _ => (for (x <- Some(f)) yield x).toList
    }
  }
  def f (x: => Any) = apply (x)
}
