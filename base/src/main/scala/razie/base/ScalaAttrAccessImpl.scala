/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map

import scala.collection.mutable;
//import razie.base.AttrAccess;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

/** simple base implementation */
abstract class ScalaAttrAccessImpl extends AttrAccess {
  lazy val _attrs: scala.collection.mutable.HashMap[String, Any] = new scala.collection.mutable.HashMap[String, Any]()
  lazy val _types: scala.collection.mutable.HashMap[String, AttrType] = new scala.collection.mutable.HashMap[String, AttrType]()
  lazy val _order: scala.collection.mutable.ListBuffer[String] = new scala.collection.mutable.ListBuffer[String]()

  /**
   * build from sequence of parm/value pairs or other stuff
   * 
   * @param pairs are pais of name/value, i.e. "car", "lexus" OR a Properties, OR another
   *       AttrAccess OR a Map<String,String>. Note the parm names can contain type:
   *       "name:string"
   */
  //       def this(pairs:Any*) = { this(); setAttr(pairs) }

  def sgetPopulatedAttr = (razie.RJS apply (this.getPopulatedAttr))

  override def toString(): String =
    (for (a <- this.sgetPopulatedAttr)
      yield a + (if (this.hasAttrType(a)) (":" + getAttrType(a)) else "") + "=" + sa(a)).mkString(",")

  override def toXml(): String =
    (for (a <- this.sgetPopulatedAttr)
      yield "<" + a + ">" + this.getAttr(a) + "</" + a + ">").mkString("")

  override def addToUrl(url: String): String = {
    val s = (for (a <- sgetPopulatedAttr)
      yield java.net.URLEncoder.encode(a, "UTF-8") + "=" + java.net.URLEncoder.encode(toStr(getAttr(a)), "UTF-8")).mkString("&")

    if (s.length <= 0)
      url
    else if (url != null && url != "" && !url.endsWith("?") && !url.endsWith("&"))
      url + (if (url.contains("=")) "&" else "?") + s
    else if (url != null)
      url + s
    else
      s
  }

  override def a(name: String): AnyRef = getAttr(name)

  override def sa(name: String): String = {
    val v = this a name
    if (v == null) ""
    else v.toString
  }

  private def toStr(o: Object): String = if (o != null) o.toString() else ""

  //   def getAttr(name:String) : AnyRef =  _attrs.getOrElse(name, null)
  //  def apply (name:String) : Any = _attrs (name)

  //  def sset (name:String, v:Any) : Any = { _attrs.put (name, v); v }

  def getOrElse(name: String, dflt: AnyRef): AnyRef = if (isPopulated (name)) getAttr(name) else dflt
  def sgetOrElse(name: String, dflt: String): String = getOrElse (name, dflt).toString

  override def foreach(f: (String, AnyRef) => Unit): Unit =
    this.sgetPopulatedAttr.foreach (x => f(x, this a x))

  override def filter(f: (String, AnyRef) => Boolean): Iterable[String] =
    this.sgetPopulatedAttr.filter (x => f(x, this a x))

  override def map[A, B](f: (String, A) => B): ScalaAttrAccess = {
    val aa = new AttrAccessImpl()
    this.sgetPopulatedAttr.foreach (x => aa set (x, f(x, (this a x).asInstanceOf[A]).asInstanceOf[AnyRef]))
    aa
  }

  override def mapValues[A, B](f: (A) => B): Seq[B] = {
    val aa = mutable.ListBuffer[B]()
    this.sgetPopulatedAttr.foreach (x => aa append (f((this a x).asInstanceOf[A])))
    aa
  }

  def getPopulatedAttr(): java.lang.Iterable[String] = {
    import scala.collection.JavaConversions._
    this._order;
  }

  def tempso(name: String): Unit = _order append name
  def tempcl() = _order.clear

  /* TODO should setAttr(xx,null) remove it so it's not populated? */
  def setAttrPair(name: String, value: Any) {
    val s = ScalaAttrAccessImpl.parseSpec(name);

    if (s.t != AttrType.DEFAULT)
      this.setAttrType(s.n, s.t);

    if (value == null) unpopulate (name);
    else {
      if (!this._attrs.contains(s.n))
        //         this._order.add(s.n());
        tempso (s.n);
      this._attrs.put(s.n, value);
    }
  }

  // remove it - it's unpopulated
  def unpopulate(name: String) {
    this._attrs -= name
    this._order -= name
    this._types -= name
  }

  def getAttr(name: String): AnyRef = {
    this._attrs.get(name).getOrElse(null).asInstanceOf[AnyRef]
  }

   /** @return the value of the named attribute or null, coerced into an option of the given type */
   def get[T](name:String) : Option[T] = {
    this._attrs.get(name).map(_.asInstanceOf[T])
  }

  def size(): Int = {
    if (this._attrs == null) 0 else this._attrs.size
  }

  def isPopulated(name: String): Boolean = {
    this._attrs != null && this._attrs.contains(name);
  }

  def toJson(iobj: JSONObject): JSONObject = {
    var obj = iobj
    try {
      if (obj == null)
        obj = new JSONObject();
      for (name <- this.sgetPopulatedAttr) {
        obj.put(name, this.getAttr(name));
      }
    } catch {
      case e: JSONException => throw new RuntimeException(e);
    }
    return obj;
  }

  /** same pairs format name,value,name,value... */
  def toPairs(): Array[AnyRef] = {
    val size = if (this._attrs == null) 0 else this._attrs.size
    val ret = new Array[AnyRef](size * 2)

    var i = 0;
    for (name <- this.sgetPopulatedAttr) {
      ret(i) = name;
      ret(i + 1) = getAttr(name);
      i += 2;
    }
    ret;
  }

  def hasAttrType(name: String): Boolean = {
    this._types != null && this._types.contains(name)
  }

  def getAttrType(name: String): AttrType = {
    this._types.get(name).getOrElse (AttrType.STRING)
  }

  def setAttrType(name: String, ttype: String) {
    this.setAttrType(name, AttrType.valueOf(ttype.toUpperCase()));
  }

  def setAttrType(name: String, ttype: AttrType) {
    this._types.put(name, ttype);
  }

  /* TODO should setAttr(xx,null) remove it so it's not populated? */
  def set(name: String, v: Any) {
    setAttrPair(name, v);
  }

  def set(name: String, v: Any, t: AttrType) {
    setAttrPair(name, v);
    setAttrType(name, t);
  }

  def clear() {
    _attrs.clear
    _types.clear
    _order.clear
  }
}

object ScalaAttrAccessImpl {
  // TODO 3-2 implement nice scala pattern matching
  /** parse "name:type=val" into a spec */
  def parseSpec(spec: String): AttrSpec = {
    val ss = spec.split("=", 2);

    var v: String = null;
    if (ss.length > 1)
      v = ss(1);

    var name = ss(0);

    // - parse the rest
    var t = AttrType.DEFAULT;

    // check name for type definition
    if (name.contains(":")) {
      var n: Array[String] = Array[String]()

      // type defn can be escaped by a \
      val idx = name.indexOf("\\:");
      if (idx >= 0 && idx == name.indexOf(":") - 1) {
        n = new Array[String](2)

        // let's see if it does have a type...
        val s2 = name.substring(idx + 2);
        val idx2 = s2.indexOf(":");
        if (idx2 >= 0) {
          n(0) = name.substring(0, idx + 2 + idx2);
          n(1) = name.substring(idx + 2 + idx2 + 1);
        } else {
          n(0) = name;
          n(1) = null;
        }

        n(0) = n(0).replaceAll("\\\\:", ":");
        name = n(0)
      } else
        n = name.split(":", 2);

      // basically, IF there's a ":" AND what's after is a recognied type...otherwise i'll
      // assume the parm name is "a:b"
      if (n.length > 1 && n(1) != null) {
        val tt = AttrType.valueOf(n(1).toUpperCase());
        if (tt != null) {
          name = n(0);
          t = tt;
        }
      }
    }

    AttrSpec.factory1(name, t, v);
  }

  /** TODO implement */
  def fromJsonString(s: String): ScalaAttrAccessImpl = {
    try {
      fromJson(new JSONObject(s));
    } catch {
      case e: JSONException =>
        throw new IllegalArgumentException(e);
    }
  }

  def fromString(s: String): ScalaAttrAccessImpl = {
    new JavaAttrAccessImpl(s);
  }

  /** TODO implement */
  def fromJson(o: JSONObject): ScalaAttrAccessImpl = {
    val a = new AttrAccessImpl();
    for (n <- JSONObject.getNames(o))
      try {
        a.setAttrPair(n, o.getString(n));
      } catch {
        case e: JSONException =>
          throw new IllegalArgumentException(e);
      }
    a;
  }

}