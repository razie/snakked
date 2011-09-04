package snakked.test

object SnakkTest extends App {
  val x = scala.xml.XML.load("http://feeds.razie.com/Razblog?format=xml")
  val posts = for (n <- (x \ "channel" \ "item" \ "title")) yield n.text

  import _root_.razie.Snakk._

  val xnames =
    for (n <- xml(url("http://feeds.razie.com/Razblog?format=xml")) \ "channel" \ "item" \ "title")
      yield (n.text)

  val jnames =
    for (n <- json(url("http://blog.razie.com/feeds/posts/default?alt=json")) \ "feed" \ "entry" \ "title" \@ "$t")
      yield (n)

  println ((xnames.sort(_>_) zip jnames.sort(_>_)).mkString("\n"))
  
  if (razie.M.equals(xnames, jnames) (_ == _)) println ("OK")
  else println ("OOPS")
}

