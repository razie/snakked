/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.xp

import razie.GPath

trait Monad[L[_]] {
   def fmap[A,B] (a:L[A]) (f:A=>B) : Monad[L] ={null}
}

trait Mona1 {
   def fmap[A,B,L[_]] (a:L[A]) (f:A=>B) : Mona1
}

class MonaL extends Monad [List] {
   override def fmap[A,B] (a:List[A]) (f:A=>B) : MonaL = new MonaL
}

class MonaL1 extends Mona1 {
   override def fmap[A,B,L[_]] (a:L[A]) (f:A=>B) : MonaL1 = new MonaL1
}

/** 
 * Generic data sourcing API
 */
trait MXPUntypedDataSouce[E<:Any, L[_], A] {
//trait XPUntypedDataSouce[T<:Any] {
   /** find something... */
   //def xp (xpath:GPath) : Mona[Any]
  
  // Mona is in razie.learnscala if you want to ressurect
}

/** 
 * Generic data sourcing API
 * 
 * Types are: L - container, E - element (any), A - attribute (String)
 */
trait MXPDataSouce[E<:Any, L[_], A] {
   /** find one element */
   def xpe (xpath:GPath) : Option[E]
   /** find a list of elements */
   def xpl (xpath:GPath) : L[E]
   /** find one attribute */
   def xpa (xpath:GPath) : Option[A]
   /** find a list of attributes */
   def xpla (xpath:GPath) : L[A]
}

//// how do i make this covariant in A? filter doesn't let me do it
//trait MO[L[_], Z[_], A] {
//  def map[B](f: A => B): Z[B] 
//  def flatMap[B](f: A => L[B]): Z[B] 
//  def filter(p: A => Boolean): Z[A] 
//  def foreach[U](f: A => U)
//
//  def iterator: Iterator[A] 
//  def toList: List[A] 
//}
//
//object MOKA {
//   def list[A] (l:List[A]) : MO[List,List,A] = new MO[List,List,A] {
//     override def map[B](f: A => B): List[B] = l.map(f)
//     override def flatMap[B](f: A => List[B]): List[B] = l.flatMap(f)
//     override def filter(p: A => Boolean): List[A] = l.filter(p)
//     override def foreach[U](f: A => U) = l.foreach(f)
//
//     override def iterator: Iterator[A]  = l.iterator
//     override def toList: List[A] = l.toList
//   }
//   
//   def opt[A] (l:Option[A]) : MO[Option,Option,A] = new MO[Option,Option,A] {
//     override def map[B](f: A => B): Option[B] = l.map(f)
//     override def flatMap[B](f: A => Option[B]): Option[B] = l.flatMap(f)
//     override def filter(p: A => Boolean): Option[A] = l.filter(p)
//     override def foreach[U](f: A => U) = l.foreach(f)
//
//     override def iterator: Iterator[A]  = l.iterator
//     override def toList: List[A] = l.toList
//   }
//}
