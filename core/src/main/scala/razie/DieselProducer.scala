/**
 * ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie

class DieselProducerStatus
case object DSPS_OPEN   extends DieselProducerStatus
case object DSPS_CLOSED extends DieselProducerStatus

/** a stream consumer is a pair of engine/node where consumption occurs. the instance will maintain state info
  *  (connection, cursor, query etc)
  */
trait DieselProducer[T] {
  /** invoked by stream to produce data - will return data which will be put into stream
    * note - these are normally paginated, so some may in fact require that from is relative to last page
    */
  def produceData      (from:Long, size:Long) : List[T]
  /** open while producing, closed when done */
  def status           (): DieselProducerStatus
  /** invoked by stream to notify of an error */
  def onError          (): Unit = {}
  /** invoked by stream to notify stream was closed */
  def onComplete       (): Unit = {}
  /** add info at place of production
    *
    * if the consumer has issues it can be on different cluster node - this will give you oppt. to add info near
    * the production */
  def addInfo          (msg:String, details:String): Unit = {}
}

/** a rule in a specific engine is producing */
trait DieselArrayProducer[T] extends DieselProducer[T] {

  def l : List[T]

  var istatus:DieselProducerStatus = if(l.size > 0) DSPS_OPEN else DSPS_CLOSED
  var curidx = 0

  /** invoked by stream to produce data - will return data which will be put into stream */
  override def produceData (from: Long, size: Long): List[T] = {
    var res : List[T] = Nil
    if(from < l.size && istatus != DSPS_CLOSED) {
      val lastIdx = (from + size - 1).toInt
      res = if (size > 0) l.slice(from.toInt, lastIdx) else Nil
      if (res.isEmpty || lastIdx >= l.size) istatus = DSPS_CLOSED
    }
    res
  }

  /** open while producing, closed when done */
  override def status():DieselProducerStatus = istatus
}
