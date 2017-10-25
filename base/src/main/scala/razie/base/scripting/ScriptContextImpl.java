/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;

import java.util.HashMap;
import java.util.Map;

import razie.base.*;
import razie.base.AttrAccessImpl;

/** a simple context - supports parents, function overrides and guards */
public class ScriptContextImpl extends razie.WrapAttrAccess implements ScriptContext {
   private static ScriptContext main = new ScriptContextImpl();

   protected Map<String, String> macros = new HashMap<String, String>();
   protected Map<String, String[]> guards = new HashMap<String, String[]>();

   protected boolean verbose = false;

   public static ScriptContext global() {
      return main;
   }

   public ScriptContextImpl() {
      this((ScriptContext) null);
   };

   /** supports a map as well */
   public ScriptContextImpl(Object... pairs) {
      this(null, pairs);
   }

   public ScriptContextImpl(AttrAccess parent) {
      super(parent);
   };

   public ScriptContextImpl(AttrAccess parent, AttrAccessImpl aa) {
      super(parent, aa);
   };

   /** supports a map as well */
   public ScriptContextImpl(AttrAccess parent, Object... pairs) {
      super(parent, pairs);
   }

   public Object getAttr(String name) {
      if (macros.containsKey(name)) {
         // TODO 3-1 cache these pre-compiled macros
         return ScriptFactory.make(null, macros.get(name)).eval(this);
      }
      return super.getAttr(name);
   }

   public boolean isPopulated(String name) {
      return macros.containsKey(name) ? true : super.isPopulated(name);
   }

   /**
    * DO NOT forget to seal a context before passing it to untrusted plugins
    */
   public void define(String macro, String expr) {
      macros.put(macro, expr);
   }

   /**
    * reset all overloads of a parm DO NOT forget to seal a context before passing it to untrusted plugins
    */
   public void undefine(String macro) {
      macros.remove(macro);
   }

   /**
    * TODO 3 FUNC use the guards - currently i'm not using them. I think i want them to be what, rules???
    */
   public void guard(String name, String condition, String expr) {
      String[] g = new String[2];
      g[0] = condition;
      g[1] = expr;
      guards.put(name, g);
   }

   public void unguard(String name, String condition, String expr) {
      guards.remove(name);
   }

   /** more verbose or not? */
   public void verbose(boolean v) {
      this.verbose = v;
   }

   /** content assist options
    * @param script - the line that needs assist
    * @param pos - the position it needs assist - normally script.length-1
    */
   public java.util.List<String> options(String script, int pos) {
      return new java.util.ArrayList<String>();
   }

   @Override
   public void reset() {
   } // not much state here...
}
