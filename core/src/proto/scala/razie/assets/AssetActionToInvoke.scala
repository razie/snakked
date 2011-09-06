/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets

import java.net.MalformedURLException;
import java.net.URL;

import com.razie.pub.agent.AgentHttpService;
import razie.base._
import com.razie.pub.base._
import com.razie.pub.comms._

/**
 * an action to invoke on an asset. See ActionToInvoke for details.
 * 
 * There's a target on top of the asset's own location, since the action can be invoked via a
 * proxy.Also, for remote controlled assets, the commands are invoked on the remote control not the
 * assets' own home.
 * 
 * @author razvanc99
 */
class AssetActionToInvoke (target:String , protected val key:AssetKey , item:ActionItem , pairs:AnyRef*) extends SimpleActionToInvoke (target, item, pairs:_*) {

    /**
     * in this case the target is this agent
     * 
     * @param item this is the action, contains the actual command name and label to display
     * @param pairs
     */
   def this(key:AssetKey , item:ActionItem , pairs:AnyRef*) =
      // TODO 1-1 this should be the key's URL, not ME....it's action on asset not on me, is it? why even have a target?
      this(Agents.me().url, key, item, pairs);

    // TODO 1-2 this is just for java compatibility - it can't call varargs
   def this(target:String , key:razie.assets.AssetKey , item:ActionItem) =
      this(target, key, item, Seq():_*)

      // TODO 1-2 this is just for java compatibility - it can't call varargs
   def this(key:razie.assets.AssetKey, item:ActionItem) =
      this(Agents.me().url, key, item)

    override def clone() = new AssetActionToInvoke(this.target, this.key, this.actionItem.clone(), this);

    /**
     * i'm smart and can call local assets without a channel...
     * 
     * default implementation assumes i need to call an url and get the first line of response
     */
    override def act(ctx:ActionContext ) : AnyRef = {
        if (this.target == null || this.target.length() <= 0) {
            return AgentHttpService.instance().assetBinding.invokeLocal(key, actionItem.name, this);
        } else {
            try {
                val url = new URL(this.makeActionUrl());
                return Comms.readUrl(url.toExternalForm());
            } catch {
               case e : MalformedURLException =>
                  throw new RuntimeException("while getting the command url: " + this.makeActionUrl(), e);
            }
        }
    }

    // http://SER:PORT/asset/KEY/action?parms
    override def makeActionUrl() : String = {
        var url = if (target.endsWith("/") ) target else target + "/"
        url += "asset/" + key.toUrlEncodedString + "/";
        url += actionItem.name;
        url = addToUrl(url);
        LightAuthBase.wrapUrl(url);
    }

   override def args(pairs:AnyRef*) : AssetActionToInvoke = {
      new AssetActionToInvoke(this.target, this.key, this.actionItem.clone(), pairs);
   }

//    public static AssetActionToInvoke fromActionUrl(String url) {
//        return null;
//    }
}
