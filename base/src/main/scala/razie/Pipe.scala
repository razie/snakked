/**
 *   ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

/** just some fun operator to keep the code looking nice */
object |> {
  implicit class PipedObject[T] private[|>] (value: T) {
    def |> [R](f: T => R) = f(this.value)
  }

  implicit class PipedObjectL[T] private[|>] (value: List[T]) {
    def |>  [R](f: T => R) = this.value map f
    def ||> [R](f: T => List[R]) = this.value flatMap f
  }

  implicit class PipedObjectS[T] private[|>] (value: Seq[T]) {
    def |>  [R](f: T => R) = this.value map f
    def ||> [R](f: T => Seq[R]) = this.value flatMap f
  }

  implicit class PipedObjectO[T] private[|>] (value: Option[T]) {
    def |>  [R](f: T => R) = this.value map f
    def ||> [R](f: T => Option[R]) = this.value flatMap f
    def ||  [R](f: => Option[R]) = this.value orElse f
  }
}

/** replaces getOrElse, orElse AND if(String.length>1) x else y */
object OR {
  implicit class OROb[T <: {def isEmpty() : Boolean}] (value: T) {
    def OR (f: => T) = if(! this.value.isEmpty) this.value else f
  }

  implicit class Oroo[T] (value: Option[T]) {
    def OR (f: => Option[T]) = this.value orElse f
    def OR (f: => T) = this.value getOrElse f
  }
}
