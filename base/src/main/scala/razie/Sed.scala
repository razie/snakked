package razie

import scala.util.matching.Regex.Match

/** sed like filter using Java regexp
 *
 *  example: from US to normal: Sed ("""(\d\d)/(\d\d)/(\d\d)""", """\2/\1/\3""", "01/31/12")
 *  
 *  Essentially useless since plain "sss".replaceAll(..., "$1 $2...") works almost the same way..
 */
object Sed {
  def apply(pat: String, rep: String, input: String): String = apply(pat, rep, identity, input)

  def apply(pat: String, rep: String, repf: (String => String), input: String): String = {

    pat.r replaceAllIn (input, (m: Match) => 
        patRep replaceAllIn (rep, (m1: Match) => 
          repf(m group (m1 group 1).toInt))
      )
  }

  val patRep = """\\([0-9])""".r
}

object SedTest extends App {
  println ("US:     01/31/12")
  println ("Normal: " + Sed ("""(\d\d)/(\d\d)/(\d\d)""", """\2/\1/\3""", "01/31/12"))
}