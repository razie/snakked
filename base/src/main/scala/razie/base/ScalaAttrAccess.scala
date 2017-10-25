/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;

import org.json.JSONObject;

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
trait ScalaAttrAccess extends JavaAttrAccess {

   /** @return the value of the named attribute or null */
   def getAttr(name:String) : AnyRef

   /** @return the value of the named attribute or null, coerced into an option of the given type */
   def get[T](name:String) : Option[T]

   /** @return the value of the named attribute or null */
   def getOrElse(name:String, dflt:AnyRef ) : AnyRef
   
   /** I'm really starting to hate typing... shortcut for getAttr */
   def a(name:String) : AnyRef

   /** 
    * most of the time they're just strings - i'll typecast here... this is a() typcast to String 
    * 
    * @return "" or the actual toString of the respective value
    */
   def sa(name:String) : String

   /** set the value of the named attribute + the name can be of the form name:type */
   def set(name:String, value:Any) : Unit
   
   /** set the value of the named attribute + the name can be of the form name:type */
   def set(name:String, value:Any, t:AttrType) : Unit

   /** set the value of the named attribute + the name can be of the form name:type */
   def setAttrPair(name:String, value:Any) : Unit

//   /**
//    * set the value of one or more attributes
//    * 
//    * @parm pairs are pais of name/value, i.e. "car", "lexus" OR a Properties, OR another AttrAccess
//    *       OR a Map<String,String>. Note that the parm name can contain the type, i.e.
//    *       "name:string".
//    */
//   def setAttr(pairs:AnyRef*) : Unit

   /** the number of populated attributes */
   def size() : Int

   /** iterate through the populated attributes. BE very careful: if you set attrs with NULL values, they are NOT populated! */
   def getPopulatedAttr() : java.lang.Iterable[String]

   /** check if an attribute is populated */
   def isPopulated(name:String) : Boolean
   
   /** check if an attribute is populated */
   def hasAttrType(name:String) : Boolean

   /**
    * @return the type of the named attribute OR null if not known. Default is by convention String
    */
   def getAttrType(name:String) : AttrType

   /**
    * set the type of the named attribute
    */
   def setAttrType(name:String, ttype:AttrType ) : Unit

   /** some random xml format */
   def toXml() : String

   /** same pairs format name,value,name,value... */
   def toPairs() : Array[AnyRef]

   /**
    * add my attributes to the JSONObject passed in. If null passed in, empty object is created
    * first
    * 
    * @param obj an json object to add to or null if this is a single element
    * @return
    */
   def toJson(obj:JSONObject) : JSONObject

   /**
    * add these attributes to an url, respecting the url parm format, i.e.
    * getMovie?name=300.divx&producer=whoknows
    */
   def addToUrl(url:String ) : String
   
  def sgetPopulatedAttr : Iterable[String]
       
  def foreach (f : (String, AnyRef) => Unit) : Unit 
   
  def filter (f : (String, AnyRef) => Boolean) : Iterable[String]

  def map [A,B] (f : (String, A) => B) : ScalaAttrAccess 
  
  def mapValues [A,B] (f : (A) => B) : Seq[B]
  
//  def apply (name:String) : Any
  
//  def sset (name:String, v:Any) : Any
}