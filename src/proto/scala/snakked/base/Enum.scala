package snakked.base

/** @viktorklang 's enum */
trait Enum { //DIY enum type
  import java.util.concurrent.atomic.AtomicReference //Concurrency paranoia

  type EnumVal <: Value //This is a type that needs to be found in the implementing class

  private val _values = new AtomicReference(Vector[EnumVal]()) //Stores our enum values

  //Adds an EnumVal to our storage, uses CCAS to make sure it's thread safe, returns the ordinal
  private final def addEnumVal(newVal: EnumVal): Int = { import _values.{get, compareAndSet => CAS}
    val oldVec = get
    val newVec = oldVec :+ newVal
    if((get eq oldVec) && CAS(oldVec, newVec)) newVec.indexWhere(_ eq newVal) else addEnumVal(newVal)
  }

  def values: Vector[EnumVal] = _values.get //Here you can get all the enums that exist for this type

  //This is the trait that we need to extend our EnumVal type with, it does the book-keeping for us
  protected trait Value { self: EnumVal => //Enforce that no one mixes in Value in a non-EnumVal type
    final val ordinal = addEnumVal(this) //Adds the EnumVal and returns the ordinal

    def name: String //All enum values should have a name

    override def toString = name //And that name is used for the toString operation
    override def equals(other: Any) = other.asInstanceOf[AnyRef] eq this
    override def hashCode = 31 * (this.getClass.## + name.## + ordinal)
  }
}

//And here's how to use it, if you want compiler exhaustiveness checking

/*
object Foos extends Enum {
  sealed trait AttrType extends Value /*{ you can define your own methods etc here }*/

  val F = new EnumVal { val name = "F" }
  val X = new EnumVal { val name = "X" }
}
*/

//But if you don't care about getting exhaustiveness warnings, you can do:

/*
object Foos extends Enum {
  case class EnumVal private[Foos](name: String) extends Value /* { you can define your own methods and stuff here } */

  val F = EnumVal("F")
  val X = EnumVal("X")
}
*/
