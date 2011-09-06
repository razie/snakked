/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import razie.base.ActionItem;
import com.razie.pub.base._;
import razie.base.scripting._
import com.razie.pub.comms.Agents;
import razie.draw._
import razie.draw.widgets.NavButton;
import razie.base._

/**
 * not sure why i need this class on top of the SdkAsset...
 * 
 * @author razvanc
 */
class AssetImpl (b : AssetBrief) extends AssetBaseImpl(b) with DrawAsset {
   var nkey : AssetKey = super.key

   override def key = nkey
   def key_= (k:AssetKey) = setKey (k)

   def this (k : AssetKey) = {
      this (new FileAssetBriefImpl())
      setKey (k)
   }
   
   def this () = {
      this (new FileAssetBriefImpl())
   }

   /** be sure to set the either a key or a brief before using it, eh? */

   override def setKey(k:AssetKey ) {
      this.nkey = k;
      super.setKey(k);
      if (k != null) brief match {
         case b:FileAssetBriefImpl => {
              brief.asInstanceOf[FileAssetBriefImpl].setFileName(k.id);
              brief.asInstanceOf[FileAssetBriefImpl].setLocalDir(k.loc.localPath);
         }
      }
   }

   override def getKey() : AssetKey = if (this.nkey  == null) super.getKey() else this.nkey
}

/**
 * mix in this to get default painting. Any asset would have implemented the getBrief method
 * 
 * @author razvanc
 */
trait DrawAsset extends AssetBase with Drawable {

   override def render(t:Technology , stream:DrawStream ) : AnyRef = 
      DrawAsset_.render (this, t, stream)
}

/**
 * this is the defalut paint for any asset with a brief
 * 
 * @author razvanc
 */
object DrawAsset_ {

   def render(who:{def getBrief() : AssetBrief}, t:Technology , stream:DrawStream ) : AnyRef = {
      val movie = who.getBrief

//      if (ctx.isPopulated("series"))
//         movie.setSeries((AssetKey) ctx.getAttr("series"));

      // TODO the remote paths come here without a / - fix that!
      movie match {
         case m : FileAssetBrief => if (m.localDir != null && m.localDir.startsWith("/")) {
            m.localDir = "/" + m.localDir
         }
         case _ =>
      }

      val vert = new DrawList();
      vert.isVertical = true;

      // DrawList vert2 = new DrawList();
      // vert2.isVertical = true;

      val horiz = new DrawList();
      val actions = new DrawList();

      horiz.write(new ABDrawable (movie, DetailLevel.FULL))

       for (a <- razie.M apply AssetMgr.pres().makeAllButtons(movie, false))
          actions.write(a)

      // add more links...
      val moreActions = new DrawList();
      moreActions.write(new NavButton(new ActionItem("google"), new AttrAccessImpl("q", "movie "
            + movie.getName()).addToUrl("http://images.google.com/images")));
      moreActions.write(new NavButton(new ActionItem("imdb"), new AttrAccessImpl("s", "all", "q", movie
            .getName()).addToUrl("http://imdb.com/find")));
      movie match {
         case m : FileAssetBrief => 
      moreActions.write(new NavButton(new ActionItem("savejpg"), Agents.me.url + "/mutant/cmd" + "/saveJpg/" + "Movie/" + m.localDir + movie.getKey().getId() + "&"));
         case _ =>
      }

      // vert2.write(movie);
      // vert2.write(actions);
      horiz.write(actions);

      vert.write(horiz);
      vert.write(moreActions);

      vert
   }
}
