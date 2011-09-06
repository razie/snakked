package snakking.test;

public class JavaB extends JavaA {
   
   public JavaA a = new JavaA("a");
   public JavaA b = new JavaA("b");
   
   public JavaA getA () { return a; }
  
   public JavaB (String value) { super(value); }
   
}