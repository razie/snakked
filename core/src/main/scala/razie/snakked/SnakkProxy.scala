package razie

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props, _}
import org.joda.time.DateTime

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/** controller for server side fiddles / services */
object SnakkProxy extends scala.App {

  val dest : String = System.getProperty("snakk.proxy.to")

  while (true) {
    try {
      check(dest)
    } catch {
      case t => log (t.toString)
    }
  }


  def check (url:String) = {
    val resp = Snakk.body(url)
  }

  def log (s:String) : Unit = {
    System.out.println(DateTime.now.toString + " " +  s);
  }

}


