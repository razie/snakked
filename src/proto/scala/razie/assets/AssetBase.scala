/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.assets

/**
 * an asset has a brief and is Referenceable...this is the deepest base for an asset
 * 
 * Assets have a brief and a unique key.
 * 
 * @author razvanc 
 * @stereotype thing
 */
trait AssetBase extends Referenceable {
    def brief:AssetBrief 
    // this is for java compatibility
    def getBrief():AssetBrief = brief
}

class AssetBaseImpl (var brief:AssetBrief) extends AssetBase {

   override def key = if (this.brief == null ) null else this.brief.key

   // TODO do i need this?
   def setKey(ref:AssetKey ) =  brief match {
      case b : AssetBriefImpl => {
      b.setKey(ref)
      if (ref != null) b.name = ref.id
      }
   }
}
