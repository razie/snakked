/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package snakked.base

sealed trait AttrType extends AttrType.EnumVal /*{ you can define your own methods etc here }*/
  
/** these types MUST be supported by forms for capture, not necessarily by displays */
object AttrType extends Enum {
  trait EnumVal extends Value /*{ you can define your own methods etc here }*/

  val STRING = new AttrType { val name = "STRING" }
  val MEMO = new AttrType { val name = "MEMO" }
  val SCRIPT = new AttrType { val name = "SCRIPT" }
  val INT = new AttrType { val name = "INT" }
  val FLOAT = new AttrType { val name = "FLOAT" }
  val DATE = new AttrType { val name = "DATE" }
  val DEFAULT = new AttrType { val name = "DEFAULT" }
  val ENUM = new AttrType { val name = "ENUM" }
  /** ENUM not supported yet */
}
