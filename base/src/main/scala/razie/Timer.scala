/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

object Timer {
   def apply[A] (f : => A) : (Long, A) = {
           val start = System.currentTimeMillis()
           val x = f
           val stop = System.currentTimeMillis()
           val dur= stop - start
       (dur, x)
   }
}
