/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

/** test that results are near some expeted results */
case class Near (l:List[Double], percent:Int = 20) {
   override def equals (o:Any) : Boolean = o match {
      case s:Seq[Double] => cmp (s)
      case n:Near => cmp (n.l)
      case _ => false
   }
   
   def cmp (s:Seq[Double]) = razie.M.equals(l, s.toList) (
         (s1,l1)=>{
            (s1 > l1*(100-percent)/100 && s1 < l1*(100+percent)/100 ||
            l1 > s1*(100-percent)/100 && l1 < s1*(100+percent)/100) 
      })
}

/** MT performance testing tools */
object Perf {      
      
   /** run a series of tests and return the response time/throughput plot per no of threads
    * 
    * @return List(Tuple(response, throughput, errorString))
    */
  def perf (noThreads:Seq[Int], noLoops:Int) (tfunc :(Int,Int) => Unit) = {

    var errors = List[Exception]()

    val graph = (for (i <- noThreads.toList) yield (i, try {
        runmt(i, noLoops) (tfunc) match {
            // don't print OK in the results
            case (count, resp, thr, "OK") => (resp, thr, "")
            case (count, resp, thr, s:String) => (resp, thr, "ERR "+s)
        }
    } catch {
        case e:Exception => { errors ::= e; "ERROR: " + e }
    })).toList

    println ("REPORT: ")

    val asmap = {
       val m = new java.util.TreeMap[java.lang.Integer, String]
       graph.foreach (x => m.put(new java.lang.Integer(x._1), x._2.toString))
       m
       }
    
//    val report = new com.sigma.hframe.jbt.mvc.LoggedTable.MapTable("", asmap)
//    report.setHeader (Array("no.threads", "ms/sub"))
//    simpletons.JSStuff.header(report, Array("no.threads", "ms/sub"))
//    println (report.toString)

    println (asmap)
    println ("---------")
    graph
  }

  /** run a test with the given no of threads and loops 
   * 
   * @return (throughput, avg resp time, "OK"/error 
   */
  def runmt (threads:Int, howmanyloops:Int) (tfunc :(Int,Int) => Unit) : (Int, Double, Double, String) = {
    val outerTime = razie.Timer {
    val results = razie.Threads.repeatAndWait[(Int, Double)](threads) ( 
       (thread:Int) => {
      var c = 0:Int
      val innerTime = razie.Timer {
         for (i <- 0 until howmanyloops/threads) {
             c += 1
             tfunc(thread, i)
         }
      }
     (c, innerTime._1 *1.0 / c)
     }
       )
    results
    }
    
    val results = outerTime._2
    var count      = results.foldRight(0) (_._1+_)
    var response   = results.foldRight(0.0) (_._2+_) / results.size
    var throughput = count * 1.0 / (outerTime._1 *1.0)

    while (count < howmanyloops) {
          count += 1
        tfunc (0, count)
    }

    if (count != howmanyloops) {
       println ("ran only "+count+" loops on "+threads+" threads")
       (count, response, throughput, count.toString)
    } else {
       (count, response, throughput, "OK")
    }
  }
}
