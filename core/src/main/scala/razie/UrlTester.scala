package razie

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.razie.pub.comms.CommRtException

/** simple helpers for this tester */
private object TestHelper {
  var thou = 1000
  def uniq = "-"+System.currentTimeMillis().toString + "-" + { thou = (thou + 1) % 1000; thou }.toString+"-"
}

/** 
 *  helper trait - include this to get the url constructs 
 *  
 *  See class snakking.test.SampleWebTest for examples
 */
trait UrlTester { self: FlatSpec with ShouldMatchers =>
  import TestHelper._

  /** helper class, will add the test methods */
  case class MyString(s: String, basic: Option[(String, String)] = None) {
    def w(implicit hostport: String): String = {
      import razie.Snakk

      val u = Snakk.url("http://" + hostport + s)
      val uu = basic.map(x => u.basic(x._1, x._2)).getOrElse(u)
      val bod = Snakk.body(uu)
      bod
    }

    /** should matcher - url access fails */
    def e400(implicit hostport: String) = {
      evaluating { this.w } should produce[CommRtException]
    }

    /** should case - url access fails */
    def s400(implicit hostport: String) = {
      (s+uniq) should "not be visible" in {
        evaluating { this.w } should produce[CommRtException]
      }
    }

    /** should case - url access ok, contains string */
    def sok(incl: String)(implicit hostport: String) = {
      (s+uniq) should "be visible" in {
        this.w should include(incl)
      }
    }

    /** should case - url access ok, NOT contains string */
    def snok(incl: String)(implicit hostport: String) = {
      (s+uniq) should "be visible but exclude " + incl in {
        this.w should not include (incl)
      }
    }

    /** should matcher - url access ok, contains string */
    def eok(incl: String)(implicit hostport: String) = {
      this.w should include(incl)
    }

    /** should matcher - url access ok, NOT contains string */
    def enok(incl: String)(implicit hostport: String) = {
      this.w should not include (incl)
    }
  }

  implicit def toMyString(s: String) = MyString(s)
  implicit def toMyString2(s: (String, String, String)) = MyString(s._1, Some(s._2, s._3))
}