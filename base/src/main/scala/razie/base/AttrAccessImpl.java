/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;

/** moving from java to scala... */
public class AttrAccessImpl extends JavaAttrAccessImpl {

   /** dummy */
   AttrAccessImpl() {
   };

   /**
    * build from sequence of parm/value pairs or other stuff
    * 
    * @parm pairs are pais of name/value, i.e. "car", "lexus" OR a Properties, OR another
    *       AttrAccess OR a Map<String,String>. Note the parm names can contain type:
    *       "name:string"
    */
   public AttrAccessImpl(Object... pairs) {
       this.setAttr(pairs);
   }

}
