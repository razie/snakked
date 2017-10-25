/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.test

import scala.collection._ 
import scala.collection.JavaConversions._
import java.{ lang => jl, util => ju }

object Learn1 {
   def apply (f:Any) : List[_] = {
      f match {
         case l : java.util.List[_] => (for (x <- l) yield x).toList
         case i : java.util.Iterator[_] => (for (x <- i) yield x).toList
         case b : java.lang.Iterable[_] => (for (x <- b) yield x).toList
         case m : java.util.Map[_,_] => (for (x <- m.values) yield x).toList
         case _ => (for (x <- Some(f)) yield x).toList
      }
   }
   
   def f (x: => Any) = apply (x)
}

// simulate stupid container
class Stupid {
   val l = List(1,2,3)
   def getX (i:Int):String = l(i).toString
}

// satisfies identity + composition
//trait Functor[F] {
//   def fmap[A, B](a: F[A], f: A => B): F[B]
//} 

// satisfies identity + composition
trait Functor[F[_]] {
   def fmap[A, B](a: F[A], f: A => B): F[B]
} 

// satisfies identity + composition
abstract class FixedFunctor[F[_]] {
   def fmap[A, B](f: A => B): F[B]
} 

// satisfies identity + composition
//class SFixedFunctor extends FixedFunctor[Stupid] {
//   def fmap[A, B](f: A => B): F[B]
//} 

trait forable[A] {
   def foreach(f: A => Unit): Unit
   def map[B](f: A => B): forable[A]
   def flatMap[B](f: A => forable[A]): forable[A]
   def filter(p: A => Boolean): forable[A]
}

class stupidforable[A] extends forable[A] {
   def foreach(f: A => Unit): Unit = {}
   override def map[B](f: A => B): forable[A] = this
   override def flatMap[B](f: A => forable[A]): forable[A] = this
   override def filter(p: A => Boolean): forable[A] = this
}

//class FFforable[O[_],FixedFunctor[O[A]],A] (val ff:FixedFunctor[O]) extends forable[A] {
//   override def foreach(f: A => Unit): Unit = {}
//   override def map[B](f: A => B): forable[B] = ff.fmap(f)
//   override def flatMap[B](f: A => forable[A]): forable[A]
//   override def filter(p: A => Boolean): forable[A]
//}

trait forable3[O,F,_] {
   def map[A,B](f: A => B): forable3[O,F,B]
   def flatMap[A,B](f: A => forable3[O,F,B]): forable3[O,F,B]
   def filter[A](p: A => Boolean): forable3[O,F,A]
}

//class FFforable3[O[_],FixedFunctor[O[A]],A] (val ff:FixedFunctor[O]) {
//   def map[A,B](f: A => B): forable3[O[_],FixedFunctor[O],B]  
////   def flatMap[A,B](f: A => forable[O,FixedFunctor[O[A]],B]): forable[O[_],FixedFunctor[O],B]
////   def filter[A](p: A => Boolean): forable[O[_],FixedFunctor[O],B]
//}

//class FL extends Functor[ju.List] {
//   override def fmap[A, B](a: ju.List[A], f: A => B): ju.List[B] = a.map(f)
//}

object M1 {
//   def apply [L[_], A] (l:L[A]) : forable[A] = 
      

   def apply1 [L[_], A] (l:L[A]) : FixedFunctor[L] = l match {
      case m : { def map[A, B](f: A => B): L[B] } => new FixedFunctor[L] {
         override def fmap[A, B](f: A => B): L[B] = m.map(f)
         }
//      case m : { def iterator() : ju.Iterator[A] } => new FixedFunctor[L] {
//         override def fmap[A, B](f: A => B): L[B] = {
//            val result = ??
//           for (s <- m.iterator()) result.add () 
//         }
//         }
   }
}

object RunMe2 extends App {
   val l = new ju.ArrayList[String](); 
   l.add("a")
   val stu = new Stupid()
   val sl = List(1,2,3)

   var f:forable[String] = new stupidforable()
   for (i <- f) println (i) 
   for (i <- f) yield i+"+" 
   for (i <- f; j <- f) yield i+j 
   
//   for (i <- M1(sl)) println (i) 
//   for (i <- M1(sl)) println (i) 
}


//class canf[A] {
//   def map[A,B](f: A => B): canf[B] = error("")
//   def flatMap[A,B](f: A => canf[B]): canf[B] = error("")
//   def filter[A](p: A => Boolean): canf[A] = error("")
//}
//class canf2[A] {
//   def foreach[A](f: A => Unit) {}
//}
//object nope {
//   for (i:Int <- new canf[Int]()) yield i+1
//}
//object yup {
//   for (i:Int <- new canf2[Int]()) println (i) 
//}
//
//class isthisfor[C[_]] (val c:C[_]) {
//   def map[A,B](f: A => B): List[B] = c match {
//      case m : { def map[A, B](f: A => B): List[B] } => m.map(f)
//      case m : { def foreach[A](f: A => Unit): Unit } => {
//         var b = scala.collection.mutable.Buffer[B]()
//         m.foreach {(x:A) => b add f(x)}
//         b.toList
//         }
//   }
//}

//-----------------

   object TRACE { def apply (x: => Any) = if (true) println(x.toString) }

   object test {
      TRACE (println ("12"))
      TRACE { 1+2 }
      
   }
   
   
//--------------------

trait T { def nm : String = "T" }
trait TA extends T { override def nm = super.nm + "-TA" }
trait TB extends T { override def nm = super.nm + "-TB" }
class TC extends T with TA with TB { override def nm = super.nm + "-TC" }
class TD extends TC with TA with TB { override def nm = super.nm + "-TD" }

object LearnTraits extends App {
   println (new TD().nm)
}

//------------------
object LearnStrict extends App {
var prefix = "expected-"
val list = for (i <- Map ("1"->"a","2"->"b", "3"->"c").values) yield prefix + i
prefix = "UNEXPECTED-1-"
println (list mkString)
prefix = "UNEXPECTED-2-"
println (list mkString)
}

object LearnString extends App {
   println ()
   "" split "\\s+"
   " " split "\\s+"
}

