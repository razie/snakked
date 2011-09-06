package razie.study2

import razie.base.AttrSpec

//--------------- foundation 

class Key(val meta: String, val id: String)
case class QKey(val q: Query) extends Key(q.meta, "")
case class Query(val meta: String, val name: String, val args: String*)
object Unknown extends Key ("Unknown", "")

object Entity { def apply(k: Key) = new Entity(k) }
class Entity(val key: Key) extends razie.AA {
  override def toString = "Entity: " + key + " - " + super.toString
}

trait IsSpec { me: Entity =>
  def make: Entity

  var actions: List[Action]
  var parms: List[Parm]

  override def toString: String = me.key.meta + ":" + parms
}

/** auto specs are based on reflection of member values */
trait AutoSpec {
  var parms: List[Parm]
  
  def doParms = {
    val fa = this.getClass.getFields.filter (_.getAnnotation (classOf[parm]) != null)
    parms = (fa map (f => Parm (f.getName))).toList // TODO identify type as well
  }
}

trait HasSpec {
  def specKey: Key
}

// defn is "name:type=defaultvalue"
abstract class ParmBase {
  val spec : AttrSpec 
}

// defn is "name:type=defaultvalue"
case class Parm (val parmDefn: String) {
  lazy val spec = AttrSpec apply parmDefn
}

/* lazy reflection based parm spec */
case class VParm {
  var spec : AttrSpec = null
}

//--------------- entities

object Service {
  def apply(name: String): Service = ServiceSpec (name).make
  //   def CF (name:String) : CFService = ServiceSpec (name).make
  //   def NF (name:String) : NFService = ServiceSpec (name).make
}

object Product {
  def apply(name: String): Product = ProductSpec (name).make
}

class Service(id: String, val specKey: Key) extends Entity(new Key("Service", id)) with HasSpec
case class CFService(id: String, override val specKey: Key) extends Service(id, specKey) with HasSpec
case class RFService(id: String, override val specKey: Key) extends Service(id, specKey) with HasSpec
case class Product(id: String, val spec: ProductSpec) extends Entity(new Key("Product", id)) with HasSpec {
  var decomposeTo: Seq[CFService] = Nil
  def specKey: Key = spec.key
}

//case class CFService (override specKey:Key) extends Service (specKey) with HasSpec {
//  def this (specKey : Key, parms:razie.AA) = { this (specKey); this.setAttr (parms) }}
//case class RFService (override specKey:Key) extends Service (specKey) with HasSpec {
//  def this (specKey : Key, parms:razie.AA) = { this (specKey); this.setAttr (parms) }}
//case class Product (val specKey:Key) extends Entity (Key("Product")) with HasSpec {
//        def decompose : Seq[CFService] = Nil
//     }

//--------------- specs

case class ServiceSpec(name: String) extends Entity(new Key("SSpec", name)) with IsSpec {
  override def make = new Service("", this.key)
  var actions: List[Action] = Nil
  var parms: List[Parm] = Nil
}

case class ProductSpec(name: String) extends Entity(new Key("PSpec", name)) with IsSpec {
  override def make() = new Product("", this)

  var actions: List[Action] = Nil
  var parms: List[Parm] = Nil

  var decomposeTo: List[ServiceSpec] = Nil

  //	override def toString = 
}

object Action { def apply(name: String) = new Action(name) }
class Action(val name: String) {
  def apply[T <: Entity](e: T, attr: String*) = new Item[T](e, this)
  def apply[T <: Entity](e: Key, attr: String*) = new Item(e, this)
  def apply[T <: Entity](e: Query, attr: String*) = new Item(QKey (e), this)
  def apply[T <: Entity](attr: String*) = new Item(Unknown, this)

  def | [T <: Entity] (e: T) = new Item[T](e, this)
  def a [T <: Entity] (e: T) = new Item[T](e, this)
}

case object Add extends Action("add")
case object Update extends Action("update")
case object Delete extends Action("delete")
case object Nop extends Action("nop")

object Implicits {
  implicit def P(s: String): Parm = Parm (s)
  implicit def Parms(s: String*): Seq[Parm] = s map P _ toList
}

import Implicits._

//---------------------------- orders

case class Item[T <: Entity](val entityKey: Key, val action: Action, val attr: String*)
  extends Entity(new Key("Item", "")) {
  var entity: Option[T] = None

  def this(e: T, a: Action) = {
    this (e.key, a)
    this.entity = Some(e)
  }

  override def toString = "Item: " + action.name + " " + entity
}

class Request[T <: Entity](val items: Seq[Item[_ <: T]])
  extends Entity(new Key("Request", ""))

case class Order[T <: Entity](override val items: Seq[Item[T]]) extends Request(items) {
  override def toString = "Order: " + items.mkString
}

//------------------------ gremlins

class Wf extends Entity(new Key("Wf", ""))
class BigWf

//------------------------ decomposing

abstract class Decomp(val who: Entity with IsSpec) {
  def decompose: List[Entity]
}
abstract class ProdDecomp(override val who: ProductSpec) extends Decomp(who) {
  def decompose: List[Entity]
}

object InternetProdDecomp extends ProdDecomp(Internet) {
  override def decompose() = Email :: Webspace :: Nil
}

//-------------------------- apis

object OM {
  def start: BigWf = new BigWf
  def executerso(o: Order[RFService]): Wf = null
  def executecso(o: Order[CFService]): Order[RFService] = null
  def executepo(o: Order[_ <: Product]): Order[CFService] = Order[CFService] (o.items.flatMap { i =>
    i.entity match {
      case Some(p) => p.decomposeTo.map (new Item(_, i.action))
      case None => Nil
    }
  })
}

//
