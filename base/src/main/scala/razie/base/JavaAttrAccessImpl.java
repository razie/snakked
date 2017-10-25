/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;

import java.util.Map;
import java.util.Properties;

/** simple base implementation */
public class JavaAttrAccessImpl extends ScalaAttrAccessImpl implements AttrAccess {
   // lazy - using underscore since many classes may derive from here...
//   protected List<String> _order = null;

   // TODO 3-2 protect this against idiot code
   public static AttrAccess EMPTY = new JavaAttrAccessImpl();
   
   /** dummy */
   public JavaAttrAccessImpl() {
   };

   /**
    * build from sequence of parm/value pairs or other stuff
    * 
    * @parm pairs are pais of name/value, i.e. "car", "lexus" OR a Properties, OR another AttrAccess OR a
    *       Map<String,String>. Note the parm names can contain type: "name:string"
    */
   public JavaAttrAccessImpl(Object... pairs) {
      this.setAttr(pairs);
   }

   /**
    * @parm pairs are pais of name/value, i.e. setAttr("car", "lexus") OR a Properties, OR another AttrAccess
    *       OR a Map<String,String>
    */
   @SuppressWarnings("unchecked")
   public void setAttr(Object... pairs) {
      if (pairs != null && pairs.length == 1 && pairs[0] instanceof Map) {
         Map<String, String> m = (Map<String, String>) pairs[0];
         for (Map.Entry<String, String> entry : m.entrySet()) {
            this.setAttrPair(entry.getKey(), m.get(entry.getKey()));
         }
      } else if (pairs != null && pairs.length == 1 && pairs[0] instanceof Properties) {
         Properties m = (Properties) pairs[0];
         for (Map.Entry<Object, Object> entry : m.entrySet()) {
            this.setAttrPair((String) entry.getKey(), m.get((String) entry.getKey()));
         }
      } else if (pairs != null && pairs.length == 1 && pairs[0] instanceof JavaAttrAccessImpl) {
         JavaAttrAccessImpl m = (JavaAttrAccessImpl) pairs[0];
         for (String s : m.getPopulatedAttr()) {
            this.setAttrPair((String) s, m.getAttr((String) s));
         }
      } else if (pairs != null && pairs.length == 1 && pairs[0] instanceof String) {
         /* one line defn of a bunch of parms */
         /*
          * Note the funny behavior of setAttr ("attrname:type=value,attrname2:type=value")...
          */
         String m = (String) pairs[0];
         String[] n = m.split("[,&]");
         for (String s : n) {
            // TODO 3-2 share with parseSpec above
            // AttrSpec as = parseSpec (s);
            String[] ss = s.split("=", 2);

            String val = null;
            if (ss.length > 1)
               val = ss[1].trim();

            String nametype = ss[0].trim();
            this.setAttrPair(nametype, val);
         }
      } else if (pairs != null && pairs.length > 1) {
         for (int i = 0; i < pairs.length / 2; i++) {
            String name = (String) pairs[2 * i];
            if (name != null)
               this.setAttrPair(name, pairs[2 * i + 1]);
         }
      }
   }

   /** TODO reflection proxy to an object's properties */
   public final static JavaAttrAccessImpl reflect(Object o) {
      // TODO implement reflection
      throw new UnsupportedOperationException("TODO");
   }

}
