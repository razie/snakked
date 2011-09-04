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

import razie.base._
import com.razie.pub.base._
import com.razie.pub.base.data.HtmlRenderUtils;
import com.razie.pub.base.data.HttpUtils;
import com.razie.pub.base.data.MimeUtils;
import com.razie.pub.comms._
import razie.draw._
import com.razie.pub.resources.RazIconRes;
import com.razie.pub.resources.RazIcons;
import scala.reflect._

/**
 * a brief description of an asset. It includes enough information to allow users to initiate
 * actions on this (i.e. play or viewDetails). It includes a favorite player (optional, though).
 * 
 * It does not necessarily identify a file or an URL that links to the asset. But rather enough
 * information that an inventory based command back to the server of the append with this REF can
 * identify the asset and for instance start streaming it.
 * 
 * It normally includes an icon, an image, a name, small and large descriptions, plus whatever other
 * information is needed.
 * 
 * Think that this is the equivalent of an item in a Media RSS.
 * 
 * Uses for the asset brief: represent RSS items, UPNP items, etc.
 * 
 * This is split into a generic assetbrief and a fileassetbrief - for files, with directories etc.
 * these are not applicable to say web assets...
 * 
 * @author razvanc99
 */
trait AssetBrief extends Referenceable with Drawable3 {
   /** nice name (or label rather */
   var name : String = null
   
   /** thumbnail - can be an icon resource, an icon filename or an url */
   var icon : String = null
   
   /** large sort of thumbnail - can be an icon resource, an icon filename or an url */
   var image : String = null
   
   /** brief description to use in lists */
   var briefDesc : String = null
   
   /** large description */
   var largeDesc : String = null
   
      var player : String = null
      @BeanProperty var parentID = "";
   
   /**
    * an URL to a place where you can find more details about this asset...
    * 
    * TODO define the protocol to serve/get/use the description
    * 
    * @return the urlForDetails
    */
   def getUrlForDetails() = urlForDetails
   def urlForDetails = {
         // TODO use asset action
         // return new AssetActionToInvoke(getKey(), DETAILS);
         new ServiceActionToInvoke("assets", AssetBrief.DETAILS, "ref", getKey());
   }

   /**@deprecated to inline */
   def setPlayer(n:String ) = this.player = n
   def getPlayer() =  player;

   /**@deprecated to inline */
   def setName(n:String ) = this.name = n
   def getName() =  name;

   /**@deprecated to inline */
   def setIcon(n:String ) = this.icon = n
   def getIcon() =  icon;

   /**@deprecated to inline */
   def setImage(n:String ) = this.image = n
   def getImage() =  image;

   /**@deprecated to inline */
   def setBriefDesc(n:String ) = this.briefDesc = n
   def getBriefDesc() =  briefDesc;

   /**@deprecated to inline */
   def setLargeDesc(n:String ) = this.largeDesc = n
   def getLargeDesc() =  largeDesc;

   def getUrlForStreaming () : ActionToInvoke = urlForStreaming
   def urlForStreaming : ActionToInvoke
   def urlForStreaming_= (a:ActionToInvoke)

   /**
    * make a url for the image/icon representing this asset...gives you something to display for
    * this asset
    */
   def getIconImgUrl ()= iconImgUrl
   def iconImgUrl = {
      // 1. it may be the image
      // 2. it may be the icon
      // 3. default to the meta
      var img = icon;

      if (image != null && image.length() > 0) {
         img = image;
      }

      if (img == null || img.length() <= 0) // default to the meta
         AssetMgr.meta(getKey().getType()).foreach {x:Meta => img = x.id.getIconProp}

      var i = RazIconRes.getIconFile(img);
      // img = (img.startsWith("/mutant") ? img : "/mutant/getPic/" + img);
      i = if(i.startsWith("/")) i else "/getPic/" + img
      i = Agents.me.url + i;
      i = LightAuthBase.wrapUrl(i);

      i;
   }
   
  def toXml:String
  def toJSONString :String
}

/** file-based assets (i.e. mp3) have more info */
trait FileAssetBrief {
   /** does not include directory information */
   var fileName : String = null
   var localDir : String = null
   var fileSize:Long = -1;

   /**@deprecated to inline */
   def setFileName(n:String ) = this.fileName = n
   def getFileName() =  fileName;

   /**@deprecated to inline */
   def setLocalDir(n:String ) = this.localDir = n
   def getLocalDir() =  localDir;

   /**@deprecated to inline */
   def setSize(n:Long ) = this.fileSize = n
   def getSize() =  fileSize
   def setFileSize(n:Long ) = this.fileSize = n
   def getFileSize() =  fileSize

   /** get a full path - in case it's not an URL */
   def getFullPath() = fullPath
   def fullPath =  (if(localDir == null) "" else localDir) + fileName;

   def getMimeType() = MimeUtils.getMimeType(localDir + fileName)
   
  def toUpnpItem(parentID:String ) : String
}

object AssetBrief {
   final val DELETE      = razie.AI("delete", RazIcons.DELETE.name)
                                                    .setType(ActionItem.ActionType.D);

   /** standard actions on assets */
   final val DETAILS     = razie.AI("details", RazIcons.UNKNOWN.name);
   final val PLAY        = razie.AI("play", RazIcons.PLAY.name);
   final val STREAM      = razie.AI("stream", RazIcons.PLAY.name);

   val upnptypes = scala.collection.immutable.Map("Movie" -> "object.item.videoItem.movie", "Music" -> "object.item.audioItem.musicTrack")

    /**
     * @param b
     * @return
     */
    def fromJson(a:JSONObject ) : AssetBrief = {
        val brief = new AssetBriefImpl();
        throw new UnsupportedOperationException()
        // TODO - remember there's two asset types plus the ctx key
//        try {
//            brief.setName(a.getString("name"));
//            brief.setIcon(a.optString("icon"));
//            brief.setImage(a.optString("image"));
//            brief.setFileName(a.getString("fileName"));
//            brief.setBriefDesc(a.getString("briefDesc"));
//            brief.setLargeDesc(a.optString("largeDesc"));
//            // brief.setUrlForDetails(a.getString("urlForDetails"),
//            // getUrlForDetails().makeActionUrl());
//            brief.setLocalDir(a.optString("localDir"));
//            brief.setKey(AssetKey.fromString(HttpUtils.fromUrlEncodedString(a.getString("ref"))));
//            if (a.has("series"))
//                brief.setSeries(AssetKey.fromString(HttpUtils.fromUrlEncodedString(a.getString("series"))));
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }

        brief;
    }
}
