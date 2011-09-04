/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package snakked.base;

//import org.json.JSONObject;

import scala.collection.mutable.Map

/**
 * simple attribute access interface and implementation - a bunch of name-value pairs with many
 * different constructors - everything these days has attributes.
 *
 * it is used throughout to access parms in a unified manner: from http requests, method arguments,
 * properties etc
 *
 * <p>
 * It has a skeleton type definition.
 *
 * <p>
 * Note the funny behavior of setAttr ("attrname:type", value)...
 *
 * <p>
 * Note the funny behavior of setAttr ("attrname:type=value,attrname2:type=value")...
 *
 * @author razvanc99
 */

/** simple Java access interface - needs sync-ing with some Java typical interfaces, probably Properties */
trait JavaAttrAccess {
  /** set the value of the named attribute + the name can be of the form name:type */
  def setValue(name: String, value: AnyRef, t: AttrType): Unit
  def setValue(name: String, value: AnyRef): Unit
  def getValue(name: String): AnyRef
  def getType(name: String): AttrType
}

/** simple name-value pairs with optional type */
trait AttrAccess extends Map[String, AnyRef] with JavaAttrAccess {
  def types: Map[String, AttrType]
}

class AttrAccessImpl extends AttrAccess {
  val types = new collection.mutable.HashMap[String, AttrType]()

  def setValue(name: String, value: AnyRef, t: AttrType) {
    this put (name, value)
    types put (name, t)
  }

  def setValue(name: String, value: AnyRef) {
    this put (name, value)
  }

  def getValue(name: String): AnyRef = this(name)

  def getType(name: String): AttrType = types.getOrElse(name, AttrType.DEFAULT)
}
