package snakking.test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.razie.pub.comms.CommRtException

/** make up unique IDs for scalatest's test names */
object TestHelper {
  var thou = 1000
  def uniq = System.currentTimeMillis().toString + "-" + { thou = (thou + 1) % 1000; thou }.toString
}

/** helper trait - include this to get the url constructs */
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
      uniq + s should "not be visible" in {
        evaluating { this.w } should produce[CommRtException]
      }
    }

    /** should case - url access ok, contains string */
    def sok(incl: String)(implicit hostport: String) = {
      uniq + s should "be visible" in {
        this.w should include(incl)
      }
    }

    /** should case - url access ok, NOT contains string */
    def snok(incl: String)(implicit hostport: String) = {
      uniq + s should "be visible but exclude " + incl in {
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

class SampleTestWiki extends FlatSpec with ShouldMatchers with UrlTester {
  implicit val hostport = "localhost:9000"

  // home page visible
  "/" sok "home"

  // admin not reacheable
  "/admin".s400

  //  "special admin topics" should "not be listed" in {
  "/wiki/list/Admin" snok "urlmap"

  // anyone can see a blog but not edit it
  "/wiki/Enduro_Blog" sok "dirt bike"
  "/wikie/edit/Enduro_Blog".s400

  // joe can edit his note
  ("/wikie/edit/Joe's_private_note", "john@doe.com", "pass") sok "edit"

  "basic auth" should "fail sometimes" in {
    ("/wikie/edit/Joe's_private_note", "Xjohn@doe.com", "pass").e400
    ("/wikie/edit/Joe's_private_note", "john@doe.com", "Xpass").e400
  }
}

/** sample perf test */
class SampleTestPerf extends FlatSpec with ShouldMatchers with UrlTester {
  implicit val hostport = "localhost:9000"

  "site" should "be fast" in {
    razie.Threads.forkjoin(0 to 100) { i =>
      ((0 to 10) map { x => "/".w contains "home" }).exists(identity)
    }.exists(p => !p.isDefined || !p.get) === true
  }
}

object SampleTestLocalhost extends App {
  org.scalatest.tools.Runner.run("-s snakking.test.SampleTestWiki".split(" "))
  //  org.scalatest.tools.Runner.run("-s snakking.test.SampleTestPerf".split(" "))
}
