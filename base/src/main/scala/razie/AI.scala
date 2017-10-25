/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie

import razie.base.ActionItem

/** simplified ActionItem 
 * 
 * i really should drop the stupid java base classes and move it all to scala 
 */
object AI {
   implicit def stoai (s:String) = new AI (s, s, s)
   
   def apply (name:String, label:String=null, tooltip:String=null) = new AI (name,label, tooltip)
   def cmdicon (name:String, icon:String=null) = new AI (name,name, name, icon)
}

/** simplified ActionItem 
 * 
 *  i really should drop the stupid java base classes and move it all to scala 
 */
//class AI (val name:String, val label:String, val tooltip:String, iconP:String=razie.Icons.UNKNOWN.name) 
class AI (name:String, label:String, tooltip:String, iconP:String=razie.Icons.UNKNOWN.name) 
   extends ActionItem (name, iconP, (if (label==null)name else label), (if(tooltip==null)name else tooltip)) {
   def this (name:String) = this(name, null, null)
}
