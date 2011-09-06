package snakking.test;

class ScalaLeaf (val value:String) {
}

class ScalaB (value:String)extends ScalaLeaf (value){
   val s_ = "s";
   val t = "t";
   val u_ = "u";
   
   val a = new ScalaLeaf("a");
   val b = new ScalaLeaf("b");
   val j = new JavaB("j");
   val l = List (new JavaB("a"), new JavaB("b"));
   
   def getS () = { s_; }
   def getA () = { a; }
   def u () = { u_; }

   override def toString = value
}