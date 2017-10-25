/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import scala.collection._
import java.{ lang => jl, util => ju }

/** conversions from java to scala collections
 * 
 * probably the thing I hate the most about scala: interacting with Java collections 
 * 
 * use like this: RJS(javalist).foreach -OR- RJS apply javalist foreach -OR- RJS list javalist sort 
 * OR for (x <- RJS.apply(whatever-java-threw-at-you))...
 */
object RJS {
  def apply[A](ij:java.lang.Iterable[A]) : scala.collection.Iterable[A] = JavaConversions.iterableAsScalaIterable(ij)
  
  def apply[A](ij:java.util.Iterator[A]) : scala.collection.Iterator[A] = JavaConversions.asScalaIterator(ij)

  def apply[A](ij:java.util.List[A]) : scala.collection.mutable.Buffer[A] = JavaConversions.asScalaBuffer(ij)
  
  def list[A](ij:java.util.List[A]) : scala.List[A] = JavaConversions.asScalaBuffer(ij).toList
  
  def apply[A, B](ij : java.util.Map[A, B]) : scala.collection.mutable.Map[A,B] = JavaConversions.mapAsScalaMap(ij)
}

/** conversions from scala to java collections. TRY NOT TO do this, unless you absolutely have to :)
 * 
 * probably the thing I hate the most about scala: interacting with Java collections 
 * 
 * use like this RSJ(scalalist)
*/
object RSJ {
   def apply[A](ij:scala.collection.Iterable[A]) : java.lang.Iterable[A] = JavaConversions.asJavaIterable(ij)
   
   def apply[A](ij:scala.collection.mutable.Buffer[A]) : java.util.List[A] = JavaConversions.bufferAsJavaList(ij)
   
   def list[A](ij:scala.List[A]) : java.util.List[A] = 
      JavaConversions.bufferAsJavaList({val b=new scala.collection.mutable.ListBuffer[A](); b.appendAll(ij); b} )
   
//   def apply[A, B](ij : scala.collection.mutable.Map[A, B]) : java.util.Map[A,B] = JavaConversions.asMap(ij)
}
