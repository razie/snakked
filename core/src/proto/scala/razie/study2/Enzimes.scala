package razie.study2

//------------------------ specs

import Implicits._

object Email extends ServiceSpec("Email") {
  actions = Add :: Update :: Delete :: Nil
  parms = parmList
  val parmList@List(user, domain, pass) = Parms ("user", "domain", "pass")

  def apply(user: String, domain: String, pass: String) = CFService ("?", this.key)
}

object Webspace extends ServiceSpec("Email") {
  actions = Add :: Update :: Delete :: Nil
  parms = parmList
  val parmList@List(domain) = Parms ("domain")

  def apply(domain: String) = CFService ("?", this.key)
}

object Internet extends ProductSpec("Internet") {
  actions = Add :: Update :: Delete :: Nil
  parms = parmList
  val parmList@List(user, domain, pass) = Parms ("user", "domain", "pass")

  //   override def parms = parmList
  //   override def parms = P("user") :: P("domain") :: P("pass") :: Nil

  //   def apply (user:String, domain:String, pass:String) = new Product ("?", this) 
  def apply(user: String, domain: String, pass: String): Product = new PInternet(user, domain, pass)

  decomposeTo = Email :: Webspace :: Nil
}

class PInternet(val user: String, val domain: String, val pass: String) extends Product("Internet", Internet) {
  //   override def actions = Add :: Update :: Delete :: Nil
  //   val parmList @ List(user,domain,pass) = Parms ("user", "domain", "pass")
  //   override def parms = parmList
  decomposeTo =
    Email (user, domain, pass) ::
    Webspace (domain) ::
    Nil

  //   override def services = Email :: Nil
}

//--------------------------- samples

object aBss {
  //  def process (request:String) : ProductOrder =  prodOrder1
  //    
  def prodOrder1 = Order[Product] (
    Add (Internet(user = "john", domain = "me.com", pass = "12345")) :: Nil)

  //	def prodOrder2 = Order[Product] (
  //			Add (Product ("?", Internet.key, (user="john", domain="me.com", pass="12345")) :: Nil
  //			)
}

//--------------------------- samples

object UseCases {
  def Add_Sub (user:String, domain :String, pass :String) = {
    
  }

  //	def prodOrder2 = Order[Product] (
  //			Add (Product ("?", Internet.key, (user="john", domain="me.com", pass="12345")) :: Nil
  //			)
}

//--------------------------- samples

object Samples {

  def serviceOrder1 = Order[Service] (
    Add (Service("email")) ::
    Update (Service("phone"), "number", "(905)713-3503") ::
    Delete (Service("voicemail")) :: Nil)

  def prodOrder1 = Order[Product] (
    Add (Internet(user = "john", domain = "me.com", pass = "12345")) :: Nil)
}

object Enzo extends Application {
  val po = Samples.prodOrder1
  println ("po: " + po)
  val so = OM.executepo(po)
  println ("so: " + so)
}


