/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base

case class AttrSpec (val name : String, val sample : String = "", val defaultValue :String="", val attrType:AttrType=AttrType.STRING) {
   // stupid java compatibility
   def n:String=name
   def s:String=sample
   def t:AttrType=attrType
   def d:String=defaultValue 
}

//case class AttrSpec (val name : String, val dataType : String = "String", val default : String = "")

object AttrSpec {
  def apply (d:String) = ScalaAttrAccessImpl.parseSpec(d)

  def factory1 (name:String, atype:AttrType, v:String) = 
    if (v == null)
      AttrSpec (name=name, attrType=atype)
    else
      AttrSpec (name=name, attrType=atype, defaultValue = v)
}