/**
 *   ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

import java.lang
import org.json.{JSONArray, JSONObject}
import scala.collection.mutable.{HashMap, ListBuffer}

/** implement by anything with an internal json structure */
trait HasJsonStructure {
  def hasJsonStructure:Boolean
  def getJsonStructure:collection.Map[String,Any]
}

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
        case m: collection.Map[_, _] => tojsons(m)
        case s: String => s
        case l: collection.Seq[_] => tojsons(l,0)
        case h @ _ => h.toString
      }
  }

  /** turn a map of name,value into json */
  def tojson(x: collection.Map[_, _]): JSONObject = {
    val o = new JSONObject()
    x foreach {t:(_,_) =>
      t._2 match {
        case m: collection.Map[_, _] => o.put(t._1.toString, tojson(m))
        case s: String => o.put(t._1.toString, s)
        case i: Int => o.put(t._1.toString, i)
        case i: Long => o.put(t._1.toString, i)
        case f: Double => o.put(t._1.toString, f)
        case f: Float => o.put(t._1.toString, f)
        case f: Boolean => o.put(t._1.toString, f)
        case l: collection.Seq[_] => o.put(t._1.toString, tojson(l))
        case j: HasJsonStructure if j.hasJsonStructure => o.put(t._1.toString, tojson(j.getJsonStructure))
        case h @ _ => o.put(t._1.toString, h.toString)
      }
    }
    o
  }

  /** turn a list into json */
  def tojson(x: collection.Seq[_]): JSONArray = {
    val o = new JSONArray()
    x.foreach { t:Any =>
      t match {
        case s: collection.Map[_, _] => o.put(tojson(s))
        case l: collection.Seq[_] => o.put(tojson(l))
        case s: String => o.put(s)
        case i: Int => o.put(i)
        case i: Long => o.put(i)
        case f: Float => o.put(f)
        case f: Double => o.put(f)
        case f: Boolean => o.put(f)
        case j: HasJsonStructure if j.hasJsonStructure => o.put(tojson(j.getJsonStructure))
        case s: JSONObject => o.put(s)
      }
    }
    o
  }

  /** recursively transform a name,value map
    *
    * the transformation is f(path, name, value) => (name, value)
    */
  def jt(map: collection.Map[_, _], path: String = "/")(f: PartialFunction[(String, String, Any), (String, Any)]): Map[String, Any] = {
    val o = new HashMap[String, Any]()
    map.foreach { t:(_,_) =>
      val ts = t._1.toString
      val r = if (f.isDefinedAt(path, ts, t._2)) f(path, ts, t._2) else (ts, t._2)
      if (r._1 != null && r._1.length() > 0)
        r._2 match {
          case s: collection.Map[_, _] => o put (r._1.toString, jt(s, path + "/" + ts)(f))
          case l: collection.Seq[_] => o put (r._1.toString, jt(l, path + "/" + ts)(f))
          case s @ _ => o put (r._1.toString, s)
        }
    }
    o.toMap
  }

  /** recursively transform a name,value map
    *
    * the transformation is f(path, name, value) => (name, value)
    */
  def jt(x: collection.Seq[_], path: String)(f: PartialFunction[(String, String, Any), (String, Any)]): List[_] = {
    val o = new ListBuffer[Any]()
    x.foreach { t:Any =>
      t match {
        case m: collection.Map[_, _] => o.append(jt(m, path)(f))
        case l: collection.Seq[_] => o.append(jt(l, path)(f))
        case x@_ => o.append(x)
      }
    }
    o.toList
  }

  /** @see jt */
  def jt(x: collection.Seq[_])(f: PartialFunction[(String, String, Any), (String, Any)]): List[_] = jt(x, "/")(f)

  val q = "\""

  private def q(str:String) = quote(str)

  /** turn a map of name,value into json */
  def tojsons(x: collection.Map[_, _], i:Int = 1, sorted:Boolean=false): String = {
    var o = " "*(i-1) + "{\n"
    (
//        if(sorted) (new SortedMap[String, _]).x.toSeq.sortBy(_._1.toString)
//        else
    x.zipWithIndex.toSeq.sortBy(_._2)
    ) foreach {t =>
      val (k,v) = t._1
      def comma = if(t._2 < x.size-1) ",\n" else "\n"
      v match {
        case m: collection.Map[_, _] => o += (" "*i) + q + k.toString  + q+ ":"+tojsons(m, i+1) + comma
        case s: String => o += (" "*i) + q + k.toString + q+ ":"+ q(s) + comma
        case ix: Int  => o += (" "*i) + q + k.toString + q+ ":"+ ix + comma
        case ix: Long => o += (" "*i) + q + k.toString + q+ ":"+ ix + comma
        case fx: Float => o += (" "*i) + q + k.toString + q+ ":"+ fx + comma
        case fx: Double => o += (" "*i) + q + k.toString + q+ ":"+ fx + comma
        case fx: Boolean => o += (" "*i) + q + k.toString + q+ ":"+ fx + comma
        case l: collection.Seq[_] => o += " "*i + q + k.toString + q + ":"+tojsons(l, i+1) + comma
        case j: HasJsonStructure if j.hasJsonStructure => {
          val m = j.getJsonStructure
          o += (" "*i) + q + k.toString  + q+ ":"+tojsons(m, i+1) + comma
        }
        case h @ _ => o += " "*i + q + k.toString + q+ ":" + q(h.toString) + comma
      }
    }
    o + " "*(i-1) + "}"
  }

  /** turn a list into json
    *
    * @param i is the level - start with 0
    */
  def tojsons(x: collection.Seq[_], i:Int): String = {
    var o = " "*(i-1) + "[" + (
        if(
          x.headOption.exists(!_.isInstanceOf[String]) &&
          x.headOption.exists(!_.isInstanceOf[Int]) &&
          x.headOption.exists(!_.isInstanceOf[Float]) &&
          x.headOption.exists(!_.isInstanceOf[Double])
        ) "\n"
        else ""
        )
    x.zipWithIndex.toSeq.sortBy(_._2) foreach { t =>
      def comma = if(t._2 < x.size-1) "," else ""
      t._1 match {
        case m: collection.Map[_, _] => o += tojsons(m, i+1) +comma+"\n"
        case l: collection.Seq[_] => o += tojsons(l, i+1) +comma+"\n"
        case s: String => o += " "*i+q(s) +comma
        case ix: Int => o += " "*i+ix +comma
        case ix: Long => o += " "*i+ix +comma
        case fx: Float => o += " "*i+fx +comma
        case fx: Double => o += " "*i+fx +comma
        case fx: Boolean => o += " "*i+fx +comma
        case s: JSONObject => o += " "*i+q(s.toString) +comma
        case j: HasJsonStructure if j.hasJsonStructure => {
          val m = j.getJsonStructure
          o += tojsons(m, i+1) +comma+"\n"
        }
        case s => o += " "*i+q(s.toString) +comma
      }
    }
    o + (if(x.headOption.exists(!_.isInstanceOf[String])) " "*(i-1) else "") + "]"
  }

  /** build a List from a JSON parsed Array */
  def fromArray (a:JSONArray) : collection.Seq[Any] = {
    val l = new ListBuffer[Any]()
    l.appendAll(
      (for (i <- 0 until a.length())
        yield a.get(i) match {
      case s: String => s
      case i: Integer => i.toInt
      case i: lang.Boolean => i
      case i: lang.Long => i.toLong
      case f: Number if f.intValue() == f.doubleValue() => f.intValue()
      case f: Number => f.doubleValue()
      case s: JSONObject => fromObject(s)
      case s: JSONArray => fromArray(s)
      case s: String => s.toString
      case s => {
        // use this to find out other missing types
//        cout << "YYYYYYYYYYYYYYY "+s.getClass.getName
        s.toString
      }
    })
    )
    l//.toList
  }

  /** build a Map from a JSON parsed object */
  def fromObject (a:JSONObject) : HashMap[String, Any] = {
    val r = new HashMap[String, Any]
    if(a.names != null) for (k <- 0 until a.names.length)
      r.put(a.names.get(k).toString, a.get(a.names.get(k).toString) match {
        case s: String => s
        case i: Integer => i.toInt
        case i: java.lang.Long => i.toLong
        case i: java.lang.Boolean => i
        case f: Number if f.intValue() == f.doubleValue() => f.intValue()
        case f: Number => f.doubleValue()
        case s: JSONObject => fromObject(s)
        case s: JSONArray => fromArray(s)
        case s: String => s.toString
        case s => {
          // use this to find out other missing types
//          cout << "YYYYYYYYYYYYYYY "+s.getClass.getName
          s.toString
        }
      })
    r//.toMap
  }

  /** build a Map from a JSON string */
  def parse (a:String) : HashMap[String, Any] = {
    fromObject(new JSONObject(a))
  }

  /** from scala map to java map, recurssive, for JSON integration */
  def toJava(x: collection.Map[_, _]): java.util.HashMap[String, Any] = {
    val o = new java.util.HashMap[String, Any]()
    x foreach {t:(_,_) =>
      t._2 match {
        case m: collection.Map[_, _] => o.put(t._1.toString, toJava(m))
        case s: String => o.put(t._1.toString, s)
        case i: Int => o.put(t._1.toString, i)
        case i: Long => o.put(t._1.toString, i)
        case f: Double => o.put(t._1.toString, f)
        case f: Float => o.put(t._1.toString, f)
        case f: Boolean => o.put(t._1.toString, f)
        case l: collection.Seq[_] => o.put(t._1.toString, toJava(l))
        case h @ _ => o.put(t._1.toString, h.toString)
      }
    }
    o
  }

  /** from scala list to java list, recursive, for JSON integration */
  def toJava(x: collection.Seq[_]): java.util.List[Any] = {
    val o = new java.util.ArrayList[Any]
    x.foreach { t:Any =>
      t match {
        case s: collection.Map[_, _] => o.add(toJava(s))
        case l: collection.Seq[_] => o.add(toJava(l))
        case s: String => o.add(s)
        case i: Int => o.add(i)
        case i: Long => o.add(i)
        case f: Float => o.add(f)
        case f: Double => o.add(f)
        case f: Boolean => o.add(f)
      }
    }
    o
  }

}

