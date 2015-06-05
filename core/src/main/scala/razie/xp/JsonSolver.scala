/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.xp

import razie._
import org.json._

/** "/root" denotes the root of a deduction */
class JsonWrapper(val j: Any, val label: String = "root")
case class JsonOWrapper(override val j: JSONObject, override val label: String = "root") extends JsonWrapper(j, label)
case class JsonAWrapper(override val j: JSONArray, override val label: String = "root") extends JsonWrapper(j, label)

/**
 * NOTE that JSON xpath must start with "/root/..."
 *
 * resolving JSON structures
 *
 * NOTE to use JSON you need the json library, add this SBT/maven dependency:
 *
 * val json = "org.json" % "json" % "20090211"
 *
 * In Eclipse, pick up this library from lib_managed/
 */
object JsonSolver extends XpSolver[JsonWrapper] {

  type T=JsonWrapper
  type CONT=List[JsonWrapper]
  type U=CONT
  
  def WrapO(j: JSONObject, label: String = "root") = new JsonOWrapper(j, label)
  def WrapA(j: JSONArray, label: String = "root") = new JsonAWrapper(j, label)

  import razie.Debug._
  
  override def children(root: T): (T, U) =
    root match {
      case x: JsonOWrapper => (x, children2(x, "*").toList.tee("C").asInstanceOf[U])
      case _ => throw new IllegalArgumentException()
    }
    
  // TODO 2-2 need to simplify - this is just mean...
  /** browsing json is different since only the parent konws the name of the child... a JSON Object doesn't know its own name/label/tag */
  override def getNext(o: (T, U), tag: String, assoc: String): List[(T, U)] =
    o._2.asInstanceOf[List[JsonWrapper]].filter(zz => XP.stareq(zz.asInstanceOf[JsonWrapper].label, tag)).tee("D").flatMap (_ match {
      case x: JsonOWrapper => (x, children2(x, "*").toList.asInstanceOf[U]) :: Nil
      case x: JsonAWrapper => wrapElements(x.j, x.label) map (t=>(t, children2(t, "*").toList.asInstanceOf[U]))
    }).tee("E").toList

  private def children2(node: JsonWrapper, tag: String): Seq[JsonWrapper] = {
    val x = node match {
      case b: JsonOWrapper =>
        razie.MOLD(b.j.keys) filter ("*" == tag || tag == _) map (_.toString) map (n => Tuple2(n, b.j.get(n))) flatMap (t => t match {
          case (name: String, o: JSONObject) => WrapO(o, name) :: Nil
          case (name: String, a: JSONArray) => wrapElements(a, name)
          case _ => Nil
        })
      case what @ _ => throw new IllegalArgumentException("Unsupported json type here: " + what)
    }
    //        println(tag, x)
    x
  }

  private def wrapElements(node: JSONArray, tag: String) =
    (0 until node.length()) map (node.get(_)) collect {
      case o: JSONObject => WrapO(o, tag)
      case a: JSONArray => WrapA(a, tag)
    }

  override def getAttr(o: T, attr: String): String = {
    val ret = o match {
      case o: JsonOWrapper => o.j.get(attr)
      case o: JSONObject => o.get(attr)
      case _ => null
    }
    ret.toString
  }
  
  override def reduce(curr: Iterable[(T, U)], xe: XpElement): Iterable[(T, U)] =
    (xe.cond match {
      case null => curr.asInstanceOf[List[(T, U)]]
      case _ => curr.asInstanceOf[List[(T, U)]].filter(x => xe.cond.passes(x._1, this))
    }).filter(gaga => XP.stareq(gaga._1.asInstanceOf[JsonWrapper].label, xe.name))

}