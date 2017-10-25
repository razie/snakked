/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package razie.base.data

import scala.collection.mutable

/**
 * a simple triple index: each A has a set of (B,C)*, so that a pair (A,B) identifies a unique C.
 * This is often useful in applications to index data/configuration
 *
 * @author razie
 */
class TripleIdx[A, B, C] {

  val idx = mutable.Map[A, mutable.Map[B, C]] ()

  private def initMap(a: A): mutable.Map[B, C] = {
    val m = idx.get(a)

    m match {
      case None => {
        val m: mutable.Map[B, C] = mutable.Map[B, C] ()
        idx.put (a, m)
        m
      }
      case Some(x) => x
    }
  }

  /** new type inject */
  def put(a: A, b: B, c: C): Unit = {
    initMap(a).put (b, c)
  }

  /** get the C for (a,b)*/
  def get2(a: A, b: B): Option[C] = {
    if (idx.contains(a))
      idx.get(a).get.get(b)
    else
      None
  }

  /** get the C for (a,b)*/
  def remove2(a: A, b: B): Option[C] = {
    if (idx.contains(a)) {
      val ret = idx.get(a).get.remove(b)
      if (idx.get(a).get.isEmpty)
        idx.remove(a)
      ret
    } else
      None
  }

  /** get the list of B's for a*/
  def get1k(a: A): List[B] = {
    if (idx.contains(a))
      Nil ++ idx.get(a).get.keySet
    else
      Nil
  }

  /** get the list of C's for a*/
  def get1v(a: A): List[C] = {
    if (idx.contains(a)) {
      var ret: List[C] = List[C]()

      for (x <- idx.get(a).get.values)
        ret = x :: ret
      // TODO reverse may be useless since map doesn't preserve order?
      ret.reverse
    } else
      Nil
  }

  /** find an entry */
  def find(f: (A, B, C) => Boolean): Option[(A, B, C)] = {
    for (a <- idx; x <- a._2)
      if (f(a._1, x._1, x._2))
        return Some((a._1, x._1, x._2))
    None
  }

  /** for each */
  def foreach(f: (A, B, C) => Unit): Unit = {
    for (a <- idx; x <- a._2)
      f(a._1, x._1, x._2)
  }

  /** map the entire filter contents */
  def map[R](f: (A, B, C) => R): Seq[R] = {
    (for (a <- idx; x <- a._2)
      yield f(a._1, x._1, x._2)).toList
  }
}
