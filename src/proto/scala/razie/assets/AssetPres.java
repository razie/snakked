/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets;

import java.util.List;

import razie.draw.DrawStream;
import razie.draw.Drawable;

import com.razie.pubstage.AssetListVisual;

/**
 * concentrates asset presentation commonalities - gives you a chance to change that if desired...
 * 
 * to use this class thus, you need the mutant jar file
 * 
 * @author razvanc
 */
public abstract class AssetPres {

   /**
    * draw a list of assets into drawable
    * 
    * @param movies a collection of assets
    * @param stream the stream to draw on
    * @param context current context for actions
    * @param visuals visual preferences
    * @return the resulting drawable - note that it has already been drawn on the stream, if any
    *         stream was passed in
    */
   public abstract Drawable toDrawable(Iterable<AssetBrief> movies, DrawStream stream,
         razie.assets.Affordance context, AssetListVisual... visuals);

   /**
    * make all the buttons for a given asset
    * 
    * @param movie the asset to make buttons for
    * @param drawTiny if true then the buttons are small for table-like list of assets. If false,
    *        then the buttons are large for a details page.
    * @return
    */
   public abstract List<Drawable> makeAllButtons(AssetBrief movie, boolean drawTiny);
}
