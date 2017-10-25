/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

/** i can never find the stupid parse methods
 * Adapted by Alexandre Martins
 */
object Boolean {
  def apply (s:String) : Boolean = s.toUpperCase match {
     case "TRUE" | "ON" | "YES" => true
     case "FALSE" | "OFF" | "NO" => false
     case _ => throw new IllegalArgumentException("argument isn't an acceptable boolean value...");
 
  }
}