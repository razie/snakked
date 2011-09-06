/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.razie.pub.assets._
import com.razie.pub.base._
import com.razie.pub.base.data.HtmlRenderUtils;
import com.razie.pub.base.data.HttpUtils;
import com.razie.pub.base.data.MimeUtils;
import com.razie.pub.comms.SimpleActionToInvoke;
import com.razie.pub.comms.Agents;
import com.razie.pub.comms.LightAuth;
import com.razie.pub.comms.ServiceActionToInvoke;
import razie.draw._
import com.razie.pub.resources._
import razie.base._

object NoAssetBrief extends AssetBriefImpl (new AssetKey("Unknown", "Unknown")) {}

class AssetBriefImpl (var key : AssetKey) extends AttrAccessImpl with AssetBrief { 

   def this () = this (null)
  
   def this (key:AssetKey, name:String, icon:String, briefDesc:String) = {
      this (key)
      this.name=name
      this.icon=icon
      this.briefDesc=briefDesc
   }
   
   override def getKey() = this.key 
   def setKey(k:AssetKey ) = this.key = k

   protected def toAA() = {
      val a:AttrAccess = this;
      a.set("ref", getKey().toUrlEncodedString);
      a.set("name", name)
      a.set("icon", icon)
      a.set("image", image)
      a.set("briefDesc", briefDesc)
      a.set("largeDesc", largeDesc)
      a.set("urlForDetails", getUrlForDetails().makeActionUrl());
      a;
   }

//   override def toString = toAA.toString

   def toJSONString =  toAA.toJson(new JSONObject()).toString()
   
   override def toXml = "<SdkAssetBrief>" + toAA().toXml() + "</SdkAssetBrief>"

    /** shortcut to render self - don't like controllers that much */
   override def render(t:Technology , stream:DrawStream ) :AnyRef = 
        Renderer.Helper.draw(this, t, stream);
   override def getRenderer(t:Technology ) : Renderer[AnyRef] = new ABRender(DetailLevel.LIST)
   
   /**
    * for streamable assets (movie, music, photo) an URL you can use to stream the asset
    * 
    * TODO define the protocol to serve/get/use the description
    * 
    * @return a url you can use to stream the asset or null if streaming is not supported. the url
    *         you can use to stream the file - it includes the filename just so IE can figure out
    *         what to do with it (open the right player etc)...
    */
   private[this] var _urlForStreaming:ActionToInvoke = null
   override def urlForStreaming = _urlForStreaming
   override def urlForStreaming_= (a:ActionToInvoke) = _urlForStreaming=a
}

class FileAssetBriefImpl extends AssetBriefImpl with FileAssetBrief {
   override protected def toAA() = {
      val a:AttrAccess = super.toAA()
      a.set("localDir", getLocalDir());
      a.set("fileName", getFileName());
      a;
   }
   
   def toUpnpItem(parentID:String ) = {
      var s = "\n<item id=\"" + getKey().toUrlEncodedString + "\" parentID=\"" + parentID + "\" restricted=\"false\"" + ">\n";

      val a = new AttrAccessImpl();

      a.set("dc\\:title", getName());
      a.set("upnp\\:class", AssetBrief.upnptypes.get(getKey().getType()).getOrElse("UNKNOWN type: "+getKey().getType()));
      // if (series != null)
      // a.setAttr("series", getSeries().toUrlEncodedString());

      // a.setAttr("upnp\\:genre", "");
      // a.setAttr("upnp\\:longDescription", getLargeDesc());
      a.set("dc\\:description", getBriefDesc());

      // a.setAttr("upnp\\:storageMedium", "");
      // a.setAttr("upnp\\:channelName", "");

      s += a.toXml();

      val p = "\n<res protocolInfo=\"http-get:*:" + getMimeType() + ":*\" size=\"200000\">" + getUrlForStreaming().makeActionUrl() + "</res>";

      s + p + "\n</item>";
   }

   /**
    * for streamable assets (movie, music, photo) an URL you can use to stream the asset
    * 
    * TODO define the protocol to serve/get/use the description
    * 
    * @return a url you can use to stream the asset or null if streaming is not supported. the url
    *         you can use to stream the file - it includes the filename just so IE can figure out
    *         what to do with it (open the right player etc)...
    */
   override def urlForStreaming = {
      // TODO 1-1 series
//      if (series != null) {
//         new ActionToInvoke(new ActionItem(AssetBrief.STREAM.name + "/" + this.fileName,
//               RazIcons.DOWNLOAD), "ref", getKey().toUrlEncodedString, "series", this.series.toString());
         new SimpleActionToInvoke(new ActionItem(AssetBrief.STREAM.name + "/" + this.fileName,
               RazIcons.DOWNLOAD.name()), "ref", getKey().toUrlEncodedString);
   }
   
   override def urlForStreaming_= (a:ActionToInvoke) = throw new UnsupportedOperationException()
}

class ABDrawable (val brief:AssetBrief, val detailLevel:DetailLevel) extends Drawable3 {
    /** shortcut to render self - don't like controllers that much */
   override def render(t:Technology , stream:DrawStream ) :AnyRef = 
        Renderer.Helper.draw(brief, t, stream);
   override def getRenderer(t:Technology ) : Renderer[AnyRef] = new ABRender(DetailLevel.LIST)
}

class ABRender (val detailLevel:DetailLevel) extends Renderer[AnyRef] {

   override def render(o:AnyRef , t:Technology , stream:DrawStream ) : AnyRef = {
            val b : AssetBrief = o match {
               case b:AssetBrief => b
               case abd : ABDrawable => abd.brief
            }
            
            if (Technology.HTML.equals(t)) {
                return toHtml(b, detailLevel);
            } else if (Technology.XML.equals(t)) {
                return b.toXml;
            } else if (Technology.JSON.equals(t)) {
                return b.toJSONString;
            } else if (Technology.UPNP.equals(t)) {
                return b.asInstanceOf[FileAssetBrief].toUpnpItem(b.getParentID());
            }

            // default rendering
            return b.toString();
        }

        /**
         * @param b
         * @return
         */
        protected def toHtml(b:AssetBrief, detailLevel:DetailLevel) : AnyRef = {
            var s = "<table align=middle><tr>";
            val img = b.getIconImgUrl();

            // NavButton button = new NavButton(DETAILS, "");

            var width = detailLevel match {
               case DetailLevel.LIST => "80"
               case DetailLevel.LARGE => "150" 
               case DetailLevel.BRIEFLIST => "30" 
               case DetailLevel.FULL =>"400" 
               case _ => "300"
            }

            // THIS IS FUCKED - the nokia770 tablet browser has a problem if only height is
            // specified. it accepts width however !!!!

            if (detailLevel.equals(DetailLevel.LARGE)) {
                s += "<td align=center>" + "<a href=\"" + b.getUrlForDetails().makeActionUrl() + "\">" + "<img border=0 " + " width=\"" + width + "\" " + " src=\"" + img + "\"/>" + "</a>";
                s += "<br><b>" + b.getName() + "</b>";
            } else {
                s += "<td>" + "<a href=\"" + b.getUrlForDetails().makeActionUrl() + "\">" + "<img border=0 " + " width=\"" + width + "\" " + " src=\"" + img + "\"/>" + "</a>";
                s += "</td>";
                s += "<td><b>" + b.getName() + "</b><br>";
                s += b.getBriefDesc() + "<br>";
                if (detailLevel.equals(DetailLevel.LIST)) {
                    s += b.getLargeDesc() + "<br>";
                } else if (detailLevel.equals(DetailLevel.FULL)) {
                    s += b.getLargeDesc() + "<br>";

                    if (b.isInstanceOf[FileAssetBrief] && b.asInstanceOf[FileAssetBriefImpl].getLocalDir() != null)
                        s += b.asInstanceOf[FileAssetBriefImpl].getLocalDir().replace("/", " /") + "<br>";
                }
            }

            // IF full details, then print all buttons inside:
            if (detailLevel.equals(DetailLevel.FULL)) {
                val l = new DrawList();
                for (a <- razie.M apply AssetMgr.pres().makeAllButtons(b, false))
                    l.write(a);

                s += l.render(Technology.HTML, null);
            }

            s += "</td>";

            s += "</tr>";
            s += "</table>";

            return s;//HtmlRenderUtils.textToHtml(s);
        }

}


//   // TODO 1-2 setup the entire rss feed thing. implies format specs
//   public String toRssMediaItem() {
//       String s = "\n<item>\n";
//
//       AttrAccess a = new AttrAccessImpl();
//
//       a.setAttr("title", getName());
//       a.setAttr("media:title", getName());
//       a.setAttr("link", "?");// TODO build page to view the item
//       a.setAttr("media:player", "?");
//
////       a.setAttr("upnp:class", "object.item.videoItem.movie");
//       // if (series != null)
//       // a.setAttr("series", getSeries().toUrlEncodedString());
//
////       a.setAttr("upnp:genre", "");
//       a.setAttr("description", getLargeDesc());
//       a.setAttr("media:description", getBriefDesc());
//
//       a.setAttr("media:category", "?");
//
////       a.setAttr("upnp:storageMedium", "");
////       a.setAttr("upnp:channelName", "");
//
//       s += a.toXml();
//
//       // String p = "\n<res protocolInfo=\"http-get:*:" + getMimeType() + ":*\" size=\"200000\">"
//       // + "http://"
//       // + Devices.getMyUrl() + "/mutant/stream?ref=" + getRef().toUrlEncodedString() + "</res>";
//       String p = "\n<res protocolInfo=\"http-get:*:" + getMimeType() + ":*\" size=\"200000\">"
//               + getUrlForStreaming().makeActionUrl() + "</res>";
//
//       return s + p + "\n</item>";
//   }

//    @Override 
//    public String toJSONString () {
//       toAA.toJson(new JSONObject()).toString();
//    }
