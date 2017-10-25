/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import java.{lang => jl}
import java.util.{concurrent => juc}


/** multithreading helpers, to tie me over until I learn to effectively exploit actors or other 
 * inferior beings
 * @author razvanc
 */
object Threads {

   /** fork a bunch of threads, then join them and return the results...all within a timeout. 
    * The return should have an element for each input that finished in time... */
   def forkjoinWithin[A,B>:Null<:AnyRef] (msec:Int)(as:Iterable[A]) (f:A =>B) : Iterable[Option[B]] = {
      val threads = (for (a <- as) yield new FuncValThread (a, f)).toList
      threads.foreach (_.start())
      threads.foreach (new KillerThread (msec, _).start)
      threads.foreach (_.join)
      threads.map (_.res).toList // use toList since the iterable may not be strict
   }

   def forkWithin (msec:Int) (f: =>Unit) : jl.Thread = {
      val thread = new jl.Thread(new jl.Runnable() {
         override def run() = {
            f
         }
      })
      thread.start()
     
      new KillerThread (msec, thread).start
      
      thread
   }
   
   /** make a promise and start it in the background */
   def promise[A] (f: => A) : juc.Future[A] = {
     val callable = new juc.Callable[A] {
       override def call() = f
     }
     val fut = new juc.FutureTask[A] (callable)
      val thread = new jl.Thread(fut)
      thread.start()
      fut
   }

   /** run a func in its own, separate thread - don't wait for result */
   def fork (f: =>Unit) : java.lang.Thread = {
      val thread = new java.lang.Thread(new java.lang.Runnable() {
         override def run() = {
            f
         }
      })
      thread.start()
      thread
   }

   /** fork one thread per element, each running the func */
   def forkForeach[A] (as:Iterable[A]) (f:A =>Unit) : Iterable[java.lang.Thread] = {
	   val threads = (for (a <- as) yield
	      new java.lang.Thread(new java.lang.Runnable() {
	         override def run() = {
	            f(a)
	         }
	      })).toList // use toList since the iterable may not be strict
	   threads.foreach (_.start())
	   threads
	   }

   /** same as forkForeach, but use the given number of threads and slice the iterable */
   def sliceForeach[A] (t:Int, as:List[A]) (f:A =>Unit) : List[java.lang.Thread] = {
       val slice = as.size / t
	   val threads = (for (i <- 0 until t) yield
	      new java.lang.Thread(new java.lang.Runnable() {
	         override def run() = {
	           for (j <- i*slice until (i+1)*slice) 
	             f(as(j))
	           if (i == t-1) // last loop does all
	             for (j <- (i+1)*slice until as.size) 
                   f(as(j))
	         }
	      })).toList // use toList since the iterable may not be strict
	   threads.foreach (_.start())
	   threads
	   }

   /** join a bunch of thread - this is more to have a symmetry for fork */
   def join (threads:Iterable[java.lang.Thread]) = {
      threads.foreach (_.join)
   }

   /** fork a bunch of threads, then join them and return the results */
   def forkjoin[A,B <: Any] (as:Iterable[A]) (f:A =>B) : Iterable[Option[B]] = {
      val threads = (for (a <- as) yield new FuncValThread (a, f)).toList
      threads.foreach (_.start())
      threads.foreach (_.join)
      threads.map (_.res).toList // use toList since the iterable may not be strict
      }

   /** just repeat the func on as many threads */
   def repeat (i:Int) (f: =>Unit) {
      for (t <- 0 until i)
         new java.lang.Thread(new java.lang.Runnable() {
            override def run() = {
               f
            }
         }).start();
      }
  
   /** repeat the func on as many threads, but each has a result which is joined and then collected 
    * 
    * @param f is passed the thread number, for no good reason :)
    */
   def repeatAndWait[A>:Null<:AnyRef] (i:Int) (f: Int => A)(implicit m:scala.reflect.Manifest[A]) : Iterable[A] = {
      val threads = Array.tabulate (i) {x:Int => {new FuncThread (x, f) }}
      threads.foreach (_.start)
      threads.foreach (_.join)
      threads.map (_.res).toList  // use toList since the iterable may not be strict
      }

   class FuncValThread[A, B<:Any] (val a:A, val f:A=>B) extends java.lang.Thread {
      var res:Option[B] = None
      
      override def run() = res = Option(f(a))
   }

   class FuncThread[A>:Null<:AnyRef] (thread:Int, f:Int =>A) extends java.lang.Thread {
      var res: A = null

      override def run() = res = f (thread)
   }
  
   class ThreadTimeoutRtException (val msec:Int) extends RuntimeException (
         "Thread timed out - took more than " + msec + " msec")
   
   class KillerThread (val msec:Int, val c:jl.Thread) extends jl.Thread {
      require(msec > 0)
      override def run() = {
         jl.Thread.sleep (msec)
         c stop new ThreadTimeoutRtException (msec)
      }
   }
}
