/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;

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
public interface JavaAttrAccess {

   /**
    * set the value of one or more attributes
    * 
    * @parm pairs are pais of name/value, i.e. "car", "lexus" OR a Properties, OR another AttrAccess
    *       OR a Map<String,String>. Note that the parm name can contain the type, i.e.
    *       "name:string".
    */
   public void setAttr(Object... pairs);

}