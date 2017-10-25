/*
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.learnscala

trait Mona[+A] {
  def map[B](f: A => B): Mona[B] 
  def flatMap[B](f: A => Mona[B]): Mona[B] 
  def filter(p: A => Boolean): Mona[A] 
  def foreach[U](f: A => U)

  def iterator: Iterator[A] 
  def toList: List[A] 
}

object Mona {
   implicit def apply[A] (l:List[A]) : Mona[A] = new MonaLista[A] (l)
   class MonaLista[A] (val l:List[A]) extends Mona[A] {
     override def map[B](f: A => B): Mona[B] = new MonaLista(l.map(f))
     override def flatMap[B](f: A => Mona[B]): Mona[B] = new MonaLista (l.flatMap(x => f(x).toList))
     override def filter(p: A => Boolean): Mona[A] = new MonaLista(l.filter(p))
     override def foreach[U](f: A => U) = l.foreach(f)

     override def iterator: Iterator[A]  = l.iterator
     override def toList: List[A] = l.toList
   }
   
   implicit def apply[A] (l:Option[A]) : Mona[A] = new MonaOpta[A] (l)
   class MonaOpta[A] (val l:Option[A]) extends Mona[A] {
     override def map[B](f: A => B): Mona[B] = new MonaOpta(l.map(f))
     override def flatMap[B](f: A => Mona[B]): Mona[B] = new MonaOpta (l.flatMap((x:A) => toOption (f(x))))
     override def filter(p: A => Boolean): Mona[A] = new MonaOpta(l.filter(p))
     override def foreach[U](f: A => U) = l.foreach(f)

     override def iterator: Iterator[A]  = l.iterator
     override def toList: List[A] = l.toList
     
     def toOption[B] (m:Mona[B]) : Option[B] = { val i = m.iterator; if (i.hasNext) Some(i.next) else None }
   }
}

// how do i make this covariant in A? filter doesn't let me do it
trait Lisa[Z[_],A] {
  def map[B](f: A => B): Z[B] 
  def flatMap[B](f: A => Lisa[Z,B]): Z[B] 
  def filter(p: A => Boolean): Z[A] 
  def foreach[U](f: A => U)

  def iterator: Iterator[A] 
  def toList: List[A] 
}

object Lisa {
   def apply[A] (l:List[A]) : Lisa[List,A] = new Lisa[List,A] {
     override def map[B](f: A => B): List[B] = l.map(f)
     override def flatMap[B](f: A => Lisa[List,B]): List[B] = l.flatMap((x:A) => f(x).toList)
     override def filter(p: A => Boolean): List[A] = l.filter(p)
     override def foreach[U](f: A => U) = l.foreach(f)

     override def iterator: Iterator[A]  = l.iterator
     override def toList: List[A] = l.toList
   }
   
   def apply[A] (l:Option[A]) : Lisa[List,A] = new Lisa[List,A] {
     override def map[B](f: A => B): List[B] = l.map(f).toList
     override def flatMap[B](f: A => Lisa[List,B]): List[B] = l.flatMap((x:A) => toOption (f(x))).toList
     override def filter(p: A => Boolean): List[A] = l.filter(p).toList
     override def foreach[U](f: A => U) = l.foreach(f)

     override def iterator: Iterator[A]  = l.iterator
     override def toList: List[A] = l.toList
     
     def toOption[B] (m:Lisa[List,B]) : Option[B] = { val i = m.iterator; if (i.hasNext) Some(i.next) else None }
   }
}

//---------------

object MonaLisa extends App {
   
   def simple (s:String) : Option [String] = s match {
      case "o" => Option ("oo")
      case "l" => Option ("ll")
   }
   
   def mona (s:String) : Mona[String] = s match {
      case "o" => Mona(Option ("oo"))
      case "l" => Mona(List ("ll"))
   }
   
   def lisa (s:String) : Lisa[List,String] = s match {
      case "o" => Lisa(Option ("oo"))
      case "l" => Lisa(List ("ll"))
   }
   
   for (e <- mona ("o")) println ((x:String)=>x.toString)
   for (e <- mona ("o")) yield ((x:String)=>x.toString)
   for (e <- mona ("o"); f <- mona("l")) yield ((x:String, y:String)=>x.toString+y.toString)
   
   for (e <- lisa ("o")) println ((x:String)=>x.toString)
   for (e <- lisa ("o")) yield ((x:String)=>x.toString)
   // and here's the bugger - doesn't compile:
  // for (e <- lisa ("o"); f <- lisa("l")) yield ((x:String, y:String)=>x.toString+y.toString)
   
   for (e <- simple ("o")) println ((x:String)=>x.toString)
   for (e <- simple ("o")) yield ((x:String)=>x.toString)
   for (e <- simple ("o"); f <- simple("l")) yield ((x:String, y:String)=>x.toString+y.toString)
   
   import Mona._  // to bring the implicits in scope for the for
   for (e <- mona ("o"); f <- simple("l")) yield ((x:String, y:String)=>x.toString+y.toString)

   // and here's the bugger - doesn't compile:
//   for (e <- mona ("o"); f <- lisa("l")) yield ((x:String, y:String)=>x.toString+y.toString)
}
