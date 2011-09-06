package razie.study2

import Implicits._

//----------------------------------------------- solution

object Entities extends Entities
trait Entities {
  case class Contact(email: String) {
    val parmList = Parms ("email")
  }
}

object HsdServices extends HsdServices
trait HsdServices {
  case class Webspace(val domain: String) extends CFService("?", WebspaceSpec.key)
  object WebspaceSpec extends ServiceSpec("Email") {
    override def actions = Add :: Update :: Delete :: Nil
    val parmList@List(domain) = Parms ("domain")
    override def parms = parmList
  }

  case class Email(val user: String, val domain: String, val pass: String) extends CFService("?", EmailSpec.key)
  object EmailSpec extends ServiceSpec("Email") {
    override def actions = Add :: Update :: Delete :: Nil
    val parmList@List(user, domain, pass) = Parms ("user:String", "domain:String=rogers.com", "pass:String")
    override def parms = parmList
  }
}

object VoiceServices extends VoiceServices
trait VoiceServices {
  case class Dialtone(val phone: String) extends CFService("?", DialtoneSpec.key)
  object DialtoneSpec extends ServiceSpec("Dialtone") {
    override def actions = Add :: Update :: Delete :: Nil
    val parmList@List(phone) = Parms ("phone:String")
    override def parms = parmList
  }
}

// combines HSD and voice
object Services extends HsdServices with VoiceServices

import razie.study1.{ Services => S }

object Products {
  import razie.study1.Services._

  object Internet extends ProductSpec("Internet") {
    override def actions = Add :: Update :: Delete :: Nil
    val parmList@List(user, domain, pass) = Parms ("user", "domain", "pass")
    parms = parmList

    override def services = S.EmailSpec :: S.WebspaceSpec :: Nil

    def apply(user: String, domain: String, pass: String) = new Product("?", this)
  }

//  object SimpleInternet extends ProductSpec("Internet") with AutoSpec {
//    override def actions = Add :: Update :: Delete :: Nil
//    
//    val user   = VParm 
//    val domain = VParm
//    val pass   = VParm
//
//    override def services = S.EmailSpec :: S.WebspaceSpec :: Nil
//
//    def apply(user: String, domain: String, pass: String) = new Product("?", this)
//  }
 
  object SimpleInternet extends ProductSpec("Internet") with AutoSpec {
    override def actions = Add :: Update :: Delete :: Nil
    
//    val parmList@List(user, domain, pass) = VParms(3)

    override def services = S.EmailSpec :: S.WebspaceSpec :: Nil

    def apply(user: String, domain: String, pass: String) = new Product("?", this)
  }
 
  class PInternet(val user: String, val domain: String, val pass: String) extends Product("?", Internet) {
    override def decompose() = Email(user, domain, pass) :: Webspace(domain) :: Nil
  }

  object GenProductSpec extends ProductSpec("GenSpec") {
    val parmList@List(user, domain, pass) = Parms ("user", "domain", "pass")
    override def actions = Add :: Update :: Delete :: Nil
    override def parms = parmList
    def apply(user: String, domain: String, pass: String) = new Product("?", this)
    def k(n: String) = new Key("ProductSpec", n)
  }

  //@Product
  class BizInternet(val user: String, val domain: String, val pass: String) extends Product("?", GenProductSpec) {
    val parmList = Parms ("user", "domain", "pass")
    override def decompose() =
      S.Email(user, domain, pass) :: S.Webspace(domain) :: Nil
  }
}

import razie.study1.{ Products => P }

import razie.study1.{ Services => S }

import scala.util.parsing.combinator._

object MyBss {
  // request format is create:Email(user=john,domain=rogers.com)
  object ACTF extends JavaTokenParsers {
    type TUP = (String, String, String)
    def ac: Parser[TUP] = ident ~ ":" ~ ident ~ opt(acoa) ^^ { case d ~ ":" ~ f ~ sa => (d, f, sa.getOrElse("")) }
    val acargs: Parser[String] = """[^)]*""".r
    def acoa: Parser[String] = "(" ~ acargs ~ ")" ^^ { case "(" ~ a ~ ")" => a }

    def parse(s: String) = parseAll(ac, s)
  }

//  def process(requests: String*) = Order[Service] (requests map line)

  def line(request: String) = ACTF parse request get match {
    case ("create", s, p) => Add(service(s, p))
    case ("delete", s, p) => Delete(service(s, p))
    case _ => Nop ()
  }

  def service(name: String, parms: String) = name match {
    case "Email" => CFService ("?", S.EmailSpec.key)
    case "Webspace" => CFService ("?", S.WebspaceSpec.key)
  }
}

object Samples2 {
  def prodOrder = Order[Product] (
    Add (P.Internet (user = "john", domain = "me.com", pass = "12345")) ::
//    Add (P.BizInternet (user = "john", domain = "me.com", pass = "12345")) :: 
    Nil)
}

object RunMe extends Application {
  val po = Samples2.prodOrder
  println ("po: " + po)
  val so = OM.executepo(po)
  println ("so: " + so)
}

