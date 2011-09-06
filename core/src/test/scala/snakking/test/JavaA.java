package snakking.test;

public class JavaA {
   /** NOTE the public modifier...*/
   public String value;
   
   public JavaA (String value) { this.value = value; }
   String s = "s";
   String t = "t";
   String u_ = "u";
   
   public String getS () { return s; }
   public String u () { return u_; }
 
   @Override 
   public String toString () { return value; }
   
   @Override
   public boolean equals (Object other) {
      return other instanceof JavaA && ((JavaA)other).value.equals(this.value);
   }
}
