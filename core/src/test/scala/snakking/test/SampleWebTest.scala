package snakking.test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import razie.Snakk

/** sample url tests */
class SampleTestWiki extends FlatSpec with ShouldMatchers with razie.UrlTester {
  // needs a host/port to target in this test
  implicit val hostport = "http://localhost:9000"
  val (u,p) = ("joe", "password")

  // home page visible - also contains the text "home"
  "/" sok "home"

  // admin not reacheable
  "/administration".s400

  //  "special admin topics" should "not be listed" in {
  "/wiki/list/Special" snok "urlmap"

  // anyone can see a blog but not edit it
  "/wiki/Enduro_Blog" sok "dirt bike"
  "/wikie/edit/Enduro_Blog".s400

  // joe can edit his note
  ("/wikie/edit/Joe's_private_note", u, p) sok "edit"

  // note how you use the 'e' instead of 's' inside a test
  "basic auth" should "fail on wrong user/pass" in {
    ("/wikie/edit/Joe's_private_note", u+u, p).e400
    ("/wikie/edit/Joe's_private_note", u, p+p).e400
  }
}

/** sample perf test - many threads hit the site, each does a sequence of calls */
class SampleTestPerf extends FlatSpec with ShouldMatchers with razie.UrlTester {
  implicit val hostport = "http://localhost:9000"

  "site" should "be fast" in {
    razie.Threads.forkjoin(0 to 100) { i =>
      ((0 to 10) map { x => "/".wget contains "home" }).exists(identity)
    }.exists(p => !p.isDefined || !p.get) === true
  }
}

/** test a form via POST */
class TestEditForm extends FlatSpec with ShouldMatchers with razie.UrlTester {
  implicit val hostport = "http://localhost:9000"
  val (u,p) = ("joe", "password")

  val form = Map (
      "label" -> ("Joe Private Note "+System.currentTimeMillis),
      "markup" -> "md",
      "content" -> "hehe",
      "visibility" -> "Public",
      "wvis" -> "Private",
      "tags" -> "note")
  
  "/wikie/save/Note:Joe_Private_Note_3".url.basic(u,p).form(form) sok "Private"
}

object SampleTestLocalhost extends App {
  org.scalatest.tools.Runner.run("-s snakking.test.SampleTestWiki".split(" "))
  //  org.scalatest.tools.Runner.run("-s snakking.test.SampleTestPerf".split(" "))
  //  org.scalatest.tools.Runner.run("-s snakking.test.SampleTestForm".split(" "))
}
