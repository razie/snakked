/**
 * Razvan's public code. 
 * Copyright 2008 based on Apache license (share alike) see LICENSE.txt for details.
 */
package razie.assets

/** Referenceable instances have a unique key 
 * 
 * @deprecated use Referenceable instead?
 */
trait IReferenceable {
    def key : AssetKey
    def getKey() : AssetKey // TODO inline
}

/** Referenceable instances have a unique key 
 * 
 * @deprecated use Referenceable instead?
 */
trait Referenceable extends IReferenceable {
    def getKey() : AssetKey = key // TODO inline
}

/** simple Referenceable concrete/implementation */
class ReferenceableImpl (val key : AssetKey) extends Referenceable {
}
