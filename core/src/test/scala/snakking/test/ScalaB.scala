package snakking.test

class ScalaLeaf (val value:String) {
}

class ScalaB (value:String)extends ScalaLeaf (value){
   val s_ = "s";
   val t = "t";
   val u_ = "u";
  
   val c = C1()
   
   val a = new ScalaLeaf("a");
   val b = new ScalaLeaf("b");
   val j = new JavaB("j");
   val l = List (new JavaB("a"), new JavaB("b"));
   
   def getS () = { s_; }
   def getA () = { a; }
   def u () = { u_; }

   override def toString = value
}

case class C1 { val c = C2() }
case class C2 { val c = C3() }
case class C3 { val c = "c" }
