package razie.learnscala

object TempMain {

}

class Test {
   def createMultiArray(componentType: Class[_], dimensions: Int) {
//     java.lang.reflect.Array.newInstance(componentType, new Array[Int](dimensions))
     java.lang.reflect.Array.newInstance(componentType, Array[Int](dimensions):_*)
   }
} 