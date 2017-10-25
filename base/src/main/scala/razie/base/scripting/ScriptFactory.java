/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;

import razie.base.ActionContext;

/**
 * minimal factory to decouple scripting
 * 
 * TODO use JSR 264 or whatever the thing is and ditch custom code...
 * 
 * @author razvanc
 */
public class ScriptFactory {
    public static ScriptFactory singleton = null;
    public static ScriptContext EMPTY = new ScriptContextImpl();

    private static ScriptFactory auto () {
       if (singleton == null)
          singleton = new ScriptFactory();
       return singleton;
    }
    public static void init(ScriptFactory theOneToUse) {
        singleton = theOneToUse;
    }

    public static RazScript make(String lang, String script) {
        return auto().makeImpl(lang, script);
    }

    /** make a new context, using the global as parent - all contexts can mess with the global one */
    public static ScriptContext mkContext (String lang, ActionContext... parent) {
        return auto().mkContextImpl(
            lang, 
            parent.length > 0 && parent[0] != null ? parent[0] : ScriptContextImpl.global());
    }

    // TODO make protected
    public RazScript makeImpl(String lang, String script) {
        throw new UnsupportedOperationException ("no default script maker...");
    }
    
    // TODO make protected
    public ScriptContext mkContextImpl(String lang, ActionContext parent) {
        return new ScriptContextImpl (parent);
    }
}
