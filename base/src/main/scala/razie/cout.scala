/**
 *   ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

/** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
object cout {
  def <(x: Any) = { println("< " + x); this }
  def <<(x: Any) = { println("<<  " + x); this }
  def <<<(x: Any) = { println("<<<   " + x); this }

  def |(x: Any) = this < x
  def ||(x: Any) = this << x
  def |||(x: Any) = this <<< x
  
  def eol = {println(); this}
}

// TODO reorg code nicely
class CSTimer (val what:String, val id:String) {
  val beg = System.currentTimeMillis()
  var last = beg
  
  def start (msg:String="") { csys start mks+":"+msg }
  def snap (msg:String="snap") { 
    val cur = System.currentTimeMillis()
    csys < "SNAP."+mks+":"+msg+" "+(cur-last)
    last=cur
    }
  def log  (msg:String) { csys < "SNAP."+mks+":"+msg }
  def stop (msg:String="") { 
    val cur = System.currentTimeMillis()
    csys < "STOP."+mks+":"+msg+" "+(cur-last)+" "+(cur-beg)
    last=cur
    }
  
  lazy val mks = id+":"+what
}

/** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
object csys extends Logging {
  def <(x: Any) = { log("< CSYS:" + x); this }
  def <<(x: Any) = { log("<<  CSYS:" + x); this }
  def <<<(x: Any) = { log("<<<   CSYS:" + x); this }

  def start (x: Any) = this < "START."+x
  def stop  (x: Any) = this < "STOP."+x
  
  def |(x: Any) = this < x
  def ||(x: Any) = this << x
  def |||(x: Any) = this <<< x
  
  def eol = {this}
}

/** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
object clog extends Logging {
  def <(x: Any) = { log("< " + x); this }
  def <<(x: Any) = { log("<<  " + x); this }
  def <<<(x: Any) = { log("<<<   " + x); this }

  def |(x: Any) = this < x
  def ||(x: Any) = this << x
  def |||(x: Any) = this <<< x
  
  def eol = {this}
}

/** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
object cdebug extends Logging {
  def <(x: Any) = { debug("< " + x); this }
  def <<(x: Any) = { debug("<<  " + x); this }
  def <<<(x: Any) = { debug("<<<   " + x); this }

  def |(x: Any) = this < x
  def ||(x: Any) = this << x
  def |||(x: Any) = this <<< x

  def eol = {this}
}
/** c++ memories, anyone... i do like to use the cout << x instead of println(x) */
object ctrace extends Logging {
  def <(x: Any) = { trace("< " + x); this }
  def <<(x: Any) = { trace("<<  " + x); this }
  def <<<(x: Any) = { trace("<<<   " + x); this }

  def |(x: Any) = this < x
  def ||(x: Any) = this << x
  def |||(x: Any) = this <<< x
  
  def eol = {this}
}

