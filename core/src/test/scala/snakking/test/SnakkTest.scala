package snakking.test

import org.junit._
import org.scalatest.junit.MustMatchersForJUnit
import _root_.razie.Snakk._

class SnakkTest extends MustMatchersForJUnit {
  val x = scala.xml.XML.load("http://feeds.razie.com/Razblog?format=xml")
  val posts = for (n <- (x \ "channel" \ "item" \ "title")) yield n.text

  /*@Test*/ def test1 = expect(true) {
    val xnames =
      for (n <- xml(url("http://feeds.razie.com/Razblog?format=xml")) \ "channel" \ "item" \ "title")
        yield (n.text)

    val jnames =
      for (n <- json(url("http://blog.razie.com/feeds/posts/default?alt=json")) \ "feed" \ "entry" \ "title" \@ "$t")
        yield (n)

    println((xnames.sort(_ > _) zip jnames.sort(_ > _)).mkString("\n"))

    if (razie.M.equals(xnames, jnames)(_ == _)) { println("OK"); true }
    else { println("OOPS"); false }
  }

  /*@Test*/ def testhttpbasicauth = expect(true) {
    // if the username and password don't match, another exception should be thrown
    val u = url("http://test.webdav.org/auth-basic/").basic("user1", "user1")
    try {
      body(u)
      false
    } catch {
      case e: com.razie.pub.comms.CommRtException => e.getCause match {
        case _: java.io.FileNotFoundException => true
        case _ => false
      }
      case _ => false
    }
  }
}

