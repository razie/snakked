/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting

import razie.base._

/**
 * actions execute in a context of objects available at that time in that environment.
 * 
 * a context is a set of objects in certain roles, like the current_movie or current_player etc. 
 * Contexts are a central concept in programming. 
 * 
 * This class may go away and be replaced with the jdk1.6 scriptables.
 * 
 * it's used to run activities and scripts $
 * 
 * You can define functions, which are evaluated every time
 * 
 * @author razvanc99
 * deprecated move code to ActContext
 */
trait ScriptContext extends AttrAccess {
  /**
   * define a new function - these are evaluated every time they are invoked. these also overwrite
   * another symbol, so you can redefine a symbol to do something else.
   * 
   * DO NOT forget to seal a context before passing it to untrusted plugins
   */
  def define(fun:String , expr:String )    /** remove a function */
  def undefine(m:String )

  /** TODO 3 FUNC use guards, document */
  def guard(name:String , condition:String , expr:String )

  /** TODO 3 FUNC use guards, document */
  def unguard(name:String , condition:String , expr:String )

  /** make execution verbose or not */
  def verbose(v:Boolean )

 /** content assist options
  * @param script - the line that needs assist
  * @param pos - the position it needs assist - normally script.length-1
  */
  def options (script:String, pos:Int) : java.util.List[String]
  
  /** 
   * Reset this context. Normally a context would cache a parser/interpreter instance. 
   * Reset will make sure this is in pristine condition .
   * 
   * NOTE that any state MAY be erased. Any variable/values defined in previous scripts may be erased. 
   * It is often that the individual interpreter instances cache local values, which would disapear.
   */
  def reset : Unit
}
