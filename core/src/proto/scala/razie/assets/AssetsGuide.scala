package razie.assets

import com.razie.pub.lightsoa._

/** this is the guide to using assets 
 * 
 * Assets have a 
 * 
 * */
object AssetsGuide {

   def sampleAssetClass = new AssetGuideAsset1
   
}

@SoaAsset(meta="AGA1", descr="a sample asset")
class AssetGuideAsset1 extends AssetBaseImpl (NoAssetBrief) {
   this.brief = new AssetBriefImpl (new AssetKey ("AGA1", "somekey"), 
         name="a sample asset", icon="unknown", briefDesc="an instance of a sample asset")
}
