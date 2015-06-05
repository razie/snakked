package razie.xp.test;

class ScalaLeaf (val value:String) {
}

class ScalaB (value:String)extends ScalaLeaf (value){
   val s_ = "s";
   val t = "t";
   val u_ = "u";
  
   val c = new C1()
   
   val a = new ScalaLeaf("a");
   val b = new ScalaLeaf("b");
   val j = new JavaB("j");
   val l = List (new JavaB("a"), new JavaB("b"));
   
   def getS () = { s_; }
   def getA () = { a; }
   def u () = { u_; }

   override def toString = value
}

class C1 { val c = new C2() }
class C2 { val c = new C3() }
class C3 { val c = new C4() }
class C4 { val c = "c" }
