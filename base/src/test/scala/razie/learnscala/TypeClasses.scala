package razie.learnscala

object TypeClasses extends App {

  {
    //============== the OO version

    trait Eq[A] { // the type trait
      def same(b: A): Boolean
    }

    case class Student(name: String) extends Eq[Student] { // classic OO implementation
      override def same(b: Student) = this.name == b.name
    }

    // someone actually using the Eq: A must be a subtype of EqOO
    def isIn[A <: Eq[A]](a: A, container: List[A]) =
      container.exists(_.same(a)) 

    isIn(Student("John"), List(Student("John")))
  }

  {
    //============== the Type Class version

    trait Eq[A] { // the type class
      def same(a: A, b: A): Boolean
    }

    case class Student(name: String) // better decoupling - extends nothing

    object EqImplementations { // the implementations for different classes
      implicit object EqStudent extends Eq[Student] {
        override def same(a: Student, b: Student) = a.name == b.name
      }
    }

    // all it says is that there must be an Eq implementation for A, somewhere in context
    def isIn[A: Eq](a: A, container: List[A]) =
      container.exists(implicitly[Eq[A]].same(_, a))

    import EqImplementations._ // bring it in context

    isIn(Student("John"), List(Student("John")))
  }
}