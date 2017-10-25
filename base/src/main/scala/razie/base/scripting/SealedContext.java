/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;

import org.json.JSONObject;

import razie.base.AttrType;
import razie.base.ScalaAttrAccessImpl;

/**
 * seal a context before passing it on - security issue. Others may override the meaning of objects in here.
 * When sealed, a context will not allow overriding symbols or changing their value - may only define new ones
 */
public class SealedContext extends ScalaAttrAccessImpl implements ScriptContext {
   private ScriptContext wrapped;

   /** content assist options */
   public java.util.List<String> options(String script, int pos) {
      return this.wrapped.options(script, pos);
   }

   public SealedContext(ScriptContext wraped) {
      this.wrapped = wraped;
   }

   @Override
   public void define(String fun, String expr) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void guard(String name, String condition, String expr) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void undefine(String macro) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void unguard(String name, String condition, String expr) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void verbose(boolean v) {
      wrapped.verbose(v);

   }

   @Override
   public Object a(String name) {
      return wrapped.a(name);
   }

   @Override
   public String addToUrl(String url) {
      return wrapped.addToUrl(url);
   }

   @Override
   public Object getOrElse(String name, Object dflt) {
      return wrapped.getOrElse(name, dflt);
   }

   @Override
   public Object getAttr(String name) {
      return wrapped.getAttr(name);
   }

   @Override
   public AttrType getAttrType(String name) {
      return wrapped.getAttrType(name);
   }

   @Override
   public Iterable<String> getPopulatedAttr() {
      return wrapped.getPopulatedAttr();
   }

   @Override
   public boolean hasAttrType(String name) {
      return wrapped.hasAttrType(name);
   }

   @Override
   public boolean isPopulated(String name) {
      return wrapped.isPopulated(name);
   }

   @Override
   public String sa(String name) {
      return wrapped.sa(name);
   }

   @Override
   public void set(String name, Object value) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void set(String name, Object value, AttrType t) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void setAttrPair(String name, Object value) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void setAttr(Object... pairs) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public void setAttrType(String name, AttrType type) {
      throw new IllegalStateException("This context is sealed - you can't override stuff.");
   }

   @Override
   public int size() {
      return wrapped.size();
   }

   @Override
   public JSONObject toJson(JSONObject obj) {
      return wrapped.toJson(obj);
   }

   @Override
   public Object[] toPairs() {
      return wrapped.toPairs();
   }

   @Override
   public String toXml() {
      return wrapped.toXml();
   }
   
   @Override
   public void reset() { }
}
