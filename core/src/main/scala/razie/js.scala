/**
 *   ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import org.json.{JSONArray, JSONObject}
import scala.collection.mutable.{HashMap, ListBuffer}

/**
 * json helpers
 *
 *  a json is represented as maps of (name,value) and lists of values, either of which can be recursive, representing
 *  a json object via a map and an array as a list.
 *
 *  we do not have a common Node class - but simple Map and List
 */
object js {

  def quote(s: String): String = {
    val res = JSONObject.quote(s)
    res
  }

  /** turn a map of name,value into json */
  def anytojsons(x:Any): String = {
      x match {
        case m: Map[_, _] => tojsons(m)
        case s: String => s
        case l: List[_] => tojsons(l,0)
        case h @ _ => h.toString
      }
  }

  /** turn a map of name,value into json */
  def tojson(x: Map[_, _]): JSONObject = {
    val o = new JSONObject()
    x foreach {t:(_,_) =>
      t._2 match {
        case m: Map[_, _] => o.put(t._1.toString, tojson(m))
        case s: String => o.put(t._1.toString, s)
        case i: Int => o.put(t._1.toString, i)
        case f: Double => o.put(t._1.toString, f)
        case f: Float => o.put(t._1.toString, f)
        case l: List[_] => o.put(t._1.toString, tojson(l))
        case h @ _ => o.put(t._1.toString, h.toString)
      }
    }
    o
  }

  /** turn a list into json */
  def tojson(x: List[_]): JSONArray = {
    val o = new JSONArray()
    x.foreach { t:Any =>
      t match {
        case s: Map[_, _] => o.put(tojson(s))
        case l: List[_] => o.put(tojson(l))
        case s: String => o.put(s)
        case i: Int => o.put(i)
        case f: Float => o.put(f)
        case f: Double => o.put(f)
        case s: JSONObject => o.put(s)
      }
    }
    o
  }

  /** recursively transform a name,value map
    *
    * the transformation is f(path, name, value) => (name, value)
    */
  def jt(map: Map[_, _], path: String = "/")(f: PartialFunction[(String, String, Any), (String, Any)]): Map[String, Any] = {
    val o = new HashMap[String, Any]()
    map.foreach { t:(_,_) =>
      val ts = t._1.toString
      val r = if (f.isDefinedAt(path, ts, t._2)) f(path, ts, t._2) else (ts, t._2)
      if (r._1 != null && r._1.length() > 0)
        r._2 match {
          case s: Map[_, _] => o put (r._1.toString, jt(s, path + "/" + ts)(f))
          case l: List[_] => o put (r._1.toString, jt(l, path + "/" + ts)(f))
          case s @ _ => o put (r._1.toString, s)
        }
    }
    o.toMap
  }

  /** recursively transform a name,value map
    *
    * the transformation is f(path, name, value) => (name, value)
    */
  def jt(x: List[_], path: String)(f: PartialFunction[(String, String, Any), (String, Any)]): List[_] = {
    val o = new ListBuffer[Any]()
    x.foreach { t:Any =>
      t match {
        case m: Map[_, _] => o.append(jt(m, path)(f))
        case l: List[_] => o.append(jt(l, path)(f))
        case x@_ => o.append(x)
      }
    }
    o.toList
  }

  /** @see jt */
  def jt(x: List[_])(f: PartialFunction[(String, String, Any), (String, Any)]): List[_] = jt(x, "/")(f)

  val q = "\""

  private def q(str:String) = quote(str)

  /** turn a map of name,value into json */
  def tojsons(x: Map[_, _], i:Int = 1): String = {
    var o = " "*(i-1) + "{\n"
    x.zipWithIndex.toSeq.sortBy(_._2) foreach {t =>
      val (k,v) = t._1
      def comma = if(t._2 < x.size-1) ",\n" else "\n"
      v match {
        case m: Map[_, _] => o += (" "*i) + q + k.toString  + q+ ":"+tojsons(m, i+1) + comma
        case s: String => o += (" "*i) + q + k.toString + q+ ":"+ q(s) + comma
        case ix: Int => o += (" "*i) + q + k.toString + q+ ":"+ ix + comma
        case fx: Float => o += (" "*i) + q + k.toString + q+ ":"+ fx + comma
        case fx: Double => o += (" "*i) + q + k.toString + q+ ":"+ fx + comma
        case l: List[_] => o += " "*i + q + k.toString + q + ":"+tojsons(l, i+1) + comma
        case h @ _ => o += " "*i + q + k.toString + q+ ":" + q(h.toString) + comma
      }
    }
    o + " "*(i-1) + "}"
  }

  /** turn a list into json
    *
    * @param i is the level - start with 0
    */
  def tojsons(x: List[_], i:Int): String = {
    var o = " "*(i-1) + "[" + (if(x.headOption.exists(!_.isInstanceOf[String]))"\n" else "")
    x.zipWithIndex.toSeq.sortBy(_._2) foreach { t =>
      def comma = if(t._2 < x.size-1) "," else ""
      t._1 match {
        case m: Map[_, _] => o += tojsons(m, i+1) +comma+"\n"
        case l: List[_] => o += tojsons(l, i+1) +comma+"\n"
        case s: String => o += " "*i+q(s) +comma
        case ix: Int => o += " "*i+ix +comma
        case fx: Float => o += " "*i+fx +comma
        case fx: Double => o += " "*i+fx +comma
        case s: JSONObject => o += " "*i+q(s.toString) +comma
        case s => o += " "*i+q(s.toString) +comma
      }
    }
    o + (if(x.headOption.exists(!_.isInstanceOf[String])) " "*(i-1) else "") + "]"
  }

  /** build a List from a JSON parsed Array */
  def fromArray (a:JSONArray) : List[Any] = {
    (for (i <- 0 until a.length())
    yield a.get(i) match {
      case s: String => s
      case s: JSONObject => fromObject(s)
    }).toList
  }

  /** build a Map from a JSON parsed object */
  def fromObject (a:JSONObject) : Map[String, Any] = {
    val r = new HashMap[String, Any]
    if(a.names != null) for (k <- 0 until a.names.length)
      r.put(a.names.get(k).toString, a.get(a.names.get(k).toString) match {
        case s: String => s
        case s: JSONObject => fromObject(s)
        case s: JSONArray => fromArray(s)
        case s => s.toString
      })
    r.toMap
  }

  /** build a Map from a JSON string */
  def parse (a:String) : Map[String, Any] = {
    fromObject(new JSONObject(a))
  }

  /** turn a map of name,value into json */
  def toJava(x: Map[_, _]): java.util.HashMap[String, Any] = {
    val o = new java.util.HashMap[String, Any]()
    x foreach {t:(_,_) =>
      t._2 match {
        case m: Map[_, _] => o.put(t._1.toString, toJava(m))
        case s: String => o.put(t._1.toString, s)
        case i: Int => o.put(t._1.toString, i)
        case f: Double => o.put(t._1.toString, f)
        case f: Float => o.put(t._1.toString, f)
        case l: List[_] => o.put(t._1.toString, toJava(l))
        case h @ _ => o.put(t._1.toString, h.toString)
      }
    }
    o
  }

  /** turn a list into json */
  def toJava(x: List[_]): java.util.List[Any] = {
    val o = new java.util.ArrayList[Any]
    x.foreach { t:Any =>
      t match {
        case s: Map[_, _] => o.add(toJava(s))
        case l: List[_] => o.add(toJava(l))
        case s: String => o.add(s)
        case i: Int => o.add(i)
        case f: Float => o.add(f)
        case f: Double => o.add(f)
      }
    }
    o
  }

}


