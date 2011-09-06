/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import com.razie.pub.base._
import com.razie.pub.base.data._
import com.razie.pub.assets._
import scala.collection._
import com.razie.pub.base.data._
import razie.base._
import razie.base.scripting._

/** these inject actions on some entity types: they enumerate the entity types supported and the actions injected and then can execute the actions.
 * 
 * this is abstract - can be implemented by Java code. In Scala you'd normally inject functions, no need to create new classes/objects
 */
trait AssetCmdInjection {
   def entityTypes : Array[String]
   def actions : Array[ActionItem]
  
   def doAction (entityKey:AssetKey, entity:Referenceable, action:String, ctx:ActionContext) : AnyRef
}

/** 
 * This adds scala niceties to the java AssetMgr, especially injections. You can inject new actions on assets as well as override existing actions
 * 
 * TODO NEED mechanism to call "super.action" when overriding...
 */
trait AssetMgrInjector {

   
   // You can inject these on-the-fly functions or AssetCmdInjector instances
   type InjectedFun = (AssetKey, Referenceable, String, ActionContext) => AnyRef

   // tiny little helper
   private[this] implicit def str2ActionItem (s:String) : ActionItem = new ActionItem (s)

   // don't you just love it how we created a new Tuple type on the fly? I do!!!

   // these are Java-type injected objects
   val injectedActions = new TripleIdx[String,String, (ActionItem,AssetCmdInjection)]
   // these are Scala-type injected functions
   val injectedFun = new TripleIdx[String,ActionItem,InjectedFun]

   
   /** classic Java type injection: create subclass with definition */
   def inject (injected:AssetCmdInjection) : Unit = {
      for (meta <- injected.entityTypes; action <- injected.actions)
    	  injectedActions.put(meta, action.name, (action, injected))
   }
  
   /** scala type inject: function */
   def inject (meta:String, action:ActionItem, injected:InjectedFun) : Unit = {
      injectedFun.put(meta, action, injected)
   }

   /** find an injection */
   def injection (meta:String, cmd:String) : Option[InjectedFun] = {
      //injectedActions.get2 (meta,new ActionItem(cmd)) match {
      injectedActions.get2 (meta, cmd) match {
         case Some(x) => Some(x._2.doAction _)
         case None => injectedFun.get2(meta,cmd)
      }
      //None
   }

   /** list all injections for a meta - for display i guess */
   def injections (meta:String) : Array[ActionItem] = {
      //var ret : Array[String] = Array()
      var ret : List[ActionItem] = List()
    
      val aa = injectedActions.get1v(meta)
        
      for (x <- injectedActions.get1v(meta))
    	  ret = x._1 :: ret

      ret = ret ++ injectedFun.get1k(meta)
    
      ret.reverse.toArray
   }
   
}
