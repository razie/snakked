/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base;


/**
 * the simplest interface for an actionable...similar to f: ActionContext => Any in scala
 * 
 * @author razvanc99
 */
trait IActionable {
  /**
   * execute this action in a given context. The context must include me as well?
   * 
   * default implementation assumes i need to call an url and get the first line of response
   */
  def act(ctx : ActionContext) : AnyRef
}
