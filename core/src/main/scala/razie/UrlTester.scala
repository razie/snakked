package razie

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.razie.pub.comms.CommRtException
import java.net.URL

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
  case class MyUrl(url:SnakkUrl) {
    def s = url.url.getPath()
    
    def wget(implicit hostport: String): String = {
      import razie.Snakk

      val u = new SnakkUrl(new URL(hostport + url.url.getPath()), url.httpAttr, url.method, url.formData)
      val bod = Snakk.body(u)
      bod
    }

    /** should matcher - url access fails */
    def e400(implicit hostport: String) = {
      evaluating { this.wget } should produce[CommRtException]
    }

    /** should case - url access fails */
    def s400(implicit hostport: String) = {
      (s+uniq) should "not be visible" in {
        evaluating { this.wget } should produce[CommRtException]
      }
    }

    /** should case - url access ok, contains string */
    def sok(incl: String)(implicit hostport: String) = {
      (s+uniq) should "be visible" in {
        this.wget should include(incl)
      }
    }

    /** should case - url access ok, NOT contains string */
    def snok(incl: String)(implicit hostport: String) = {
      (s+uniq) should "be visible but exclude " + incl in {
        this.wget should not include (incl)
      }
    }

    /** should matcher - url access ok, contains string */
    def eok(incl: String)(implicit hostport: String) = {
      this.wget should include(incl)
    }

    /** should matcher - url access ok, NOT contains string */
    def enok(incl: String)(implicit hostport: String) = {
      this.wget should not include (incl)
    }
  }

  implicit def toMyString(s: String)(implicit hostport:String) = MyUrl(Snakk.url(hostport+s))
  implicit def toMyString2(s: (String, String, String))(implicit hostport:String) = MyUrl(Snakk.url(hostport+s._1).basic(s._2, s._3))
  implicit def toMyUrl(u:SnakkUrl)(implicit hostport:String) = MyUrl(u)
}
