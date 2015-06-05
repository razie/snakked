/** ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.g

//-------------------------- simple (set) graphs, with structure induced on an existing set

/** a graph is a set of nodes and links between these nodes. It's better though to forget this
 *  and see each node as a (sub) graph. Most of the code here will do that
 */
trait SGraph[N <: GSNode[_], L <: GLink[N]] {
  def gnodes: Seq[N]
  def glinks: Seq[L]
}

/** this is a generic container node - use it to inject a graph structure onto something else */
trait GSNode[A] extends GNode[GSNode[A], GLink[GSNode[A]]] {
  def content: A
}

/** a link between two nodes */
trait GLink[N <: GNode[_, _]] {
  def a: N
  def z: N
}

//---------------------------- smart graphs of nodes, each node is a sub-graph with embedded structure

/** smart graph, each node is a sub-graph of "child" nodes and links */
trait GNode[N <: GNode[N, L], L <: GLink[N]] { this: N =>
  def glinks: Seq[L] // by convention, all links with a == this

  def mkString = Graphs.mkString[N, L](this)
}

/** a modifyable graph - use the defined operators to change it */
trait WRGraph[N <: GNode[N, L], L <: GLink[N]] extends GNode[N, L] { this: N =>

  type LFactory = (N, N) => L
  type LVFactory[C <: Any] = (N, N, C) => L
  type W = WRGraph[N, L]

  //  def gnodes_=(s: Seq[N])
  def glinks_=(s: Seq[L]) // by convention, all links with a == this

  /** reroute all links to z - x --> y --> z doens't mean what you may think, see --: */
  def -->[T <: N](z: T)(implicit linkFactory: LFactory): N = {
    glinks = linkFactory(this, z) :: Nil
    this
  }

  /** reroute - this allows x --: y --: z to mean what you thinbk it should */
  def --:[T <: W](z: T)(implicit linkFactory: LFactory): T = {
    z.glinks = linkFactory(z.asInstanceOf[N], this) :: Nil
    z
  }

  /** reroute */
  //  def <-- [T<:WRGraph[N,L]] (z:T)(implicit linkFactory: LFactory) : N =  z --> (this)
  //  def <-+ [T<:WRGraph[N,L]] (z:T)(implicit linkFactory: LFactory) : N =  z +-> (this)

  /** add link to z - x +-> y --> z doens't mean what you may think, see +-: */
  /** add a new dependency */
  def +->[T <: N](z: T)(implicit linkFactory: LFactory): N = {
    glinks = glinks.toList.asInstanceOf[List[L]] ::: List(linkFactory(this, z))
    this
  }

  /** add a new dependency - this allows x +- y -- z to mean what you thinbk it should */
  def +-:[T <: W](z: T)(implicit linkFactory: LFactory): T = {
    z.glinks = z.glinks.toList.asInstanceOf[List[L]] ::: List(linkFactory(z.asInstanceOf[N], this))
    z
  }

  /** par depy a -> (b,c) */
  def -->[T <: N](z: Seq[T])(implicit linkFactory: LFactory): N = {
    glinks = z.map(linkFactory(this, _)).toList
    this
  }

  /** add a new par depy a -> (b,c) */
  def +->[T <: N](z: Seq[T])(implicit linkFactory: LFactory): N = {
    glinks = glinks.toList.asInstanceOf[List[L]] ::: z.map(linkFactory(this, _)).toList
    this
  }

}

/** this is a filtered graph - a subgraph, see Graphs.colored */
trait GraphLike[N <: GNode[N, L], L <: GLink[N]] {
  def root: N
  def colored: N => Boolean

  protected def notin(l: Any, i: Int) {}

  def foreachNode(fn: (N, Int) => Unit) { foreach (fn, notin) }

  def foreach(fn: (N, Int) => Unit, fl: (L, Int) => Unit) =
    iforeach (root, None, fn, fl, 0)

  protected def iforeach(n: N, from: Option[L], fn: (N, Int) => Unit, fl: (L, Int) => Unit, level: Int) {
    if (Graphs.maxDebugDepth > 0 && level >= Graphs.maxDebugDepth)
      throw new IllegalStateException("Maximum depth reached in graph: " + Graphs.maxDebugDepth + " reset Graphs.maxDebugDepth...")
    if (colored (n)) {
      fn(n, level)
      n.glinks.foreach(l => { fl(l, level); iforeach(l.z, Option(l), fn, fl, level + 1) })
    }
  }

  /** paint a graph old school - tree style: you better give me a DAG :) */
  def mkString: String = {
    var s = new StringBuffer()
    s.append("Graph: \n")
    s.append(pt(" ", 0) + root.toString + "\n")
    foreach(
      (x: N, v: Int) => {}, // nothing - I print nodes as part of the assocs pointing to them
      (l: L, v: Int) => { s.append(pt(" ", v) + "->" + l.z.toString + "\n") })
    s.toString
  }

  /** paint a graph old school - tree style: you better give me a DAG :) */
  def mkString2: String = {
    var s = new StringBuffer()
    s.append("Graph: \n")
    foreach(
      (x: N, v: Int) => { s.append(pt(" ", v) + x.toString + "\n") },
      (l: L, v: Int) => { s.append(pt(" ", v) + "->" + l.z.toString + "\n") })
    s.toString
  }

  def filterNodes(f: N => Boolean): Seq[N] = {
    val ret = razie.Listi[N]
    foreach ((x: N, v: Int) => { if (f(x)) ret append x }, (l: L, v: Int) => {})
    ret
  }

  // TODO this is just mean...
  def pt(s: String, i: Int) = (Range(0, i).map(k => s)).mkString

  def indexed = new IndexedGraphLike[N, L](this)
  def dag = new NoCyclesGraphLike[N, L](this)
  def depth(maxDepth: Int) = new DepthGraphLike[N, L](maxDepth)(this)
}

/** DAG - avoids cycles, if any...then I can traverse each node AND LINK just once */
class IndexedGraphLike[N <: GNode[N, L], L <: GLink[N]](target: GraphLike[N, L])
  extends NoCyclesGraphLike[N, L](target) {

  // index it
  // TODO should warn if the graph is too big...how the heck do i find that out?
  var collecting = true
  foreach (notin, { (l: L, i: Int) => collect (l.z, Option(l)) })
  collecting = false

  override protected def iforeach(n: N, from: Option[L], fn: (N, Int) => Unit, fl: (L, Int) => Unit, level: Int) {
    if (colored (n) && (from.isDefined && index.get(n).isDefined &&
      index(n).headOption.map(ll => ll == from.get).getOrElse(false)) ||
      !from.isDefined ||
      collecting) {
      fn(n, level)
      n.glinks.foreach(l => { fl(l, level); iforeach(l.z, Option(l), fn, fl, level + 1) })
    }
  }
}

/** Wraps a graph so then I can traverse each node just once, even if the original graph had cycles
 *
 *  it will index the nodes as it traverses them, to avoid walking the same path twice.
 *
 *  This is excellent for example for printing graphs or doing any operation where you just want to visit the nodes and not worry about cycles and stack overflows...
 */
class NoCyclesGraphLike[N <: GNode[N, L], L <: GLink[N]](target: GraphLike[N, L]) extends GraphLike[N, L] {
  import scala.collection.mutable

  def root: N = target.root
  def colored: N => Boolean = target.colored

  val index = new mutable.HashMap[Any, mutable.ListBuffer[L]]()

  protected def collect(n: N, from: Option[L]) =
    if (index contains n)
      index(n) += from.get
    else
      index put (n,
        from.map (mutable.ListBuffer[L](_)) getOrElse mutable.ListBuffer[L]())

  protected def isNodeCollected(n: N) = index.get(n).isDefined

  protected def isCollected(n: N, from: Option[L]) =
    from.isDefined &&
      index.get(n).isDefined &&
      index(n).filter(ll => ll == from.get).headOption.isDefined

  override protected def iforeach(n: N, from: Option[L], fn: (N, Int) => Unit, fl: (L, Int) => Unit, level: Int) {
    if (Graphs.maxDebugDepth > 0 && level >= Graphs.maxDebugDepth)
      throw new IllegalStateException("Maximum depth reached in graph: " + Graphs.maxDebugDepth + " reset Graphs.maxDebugDepth...")
    if (colored (n) && !isCollected (n, from)) {
      if (!isNodeCollected(n))
        fn(n, level)
      collect (n, from)
      n.glinks.filter(x => !isCollected(n, Option(x))).foreach(l => {
        fl(l, level)
        iforeach(l.z, Option(l), fn, fl, level + 1)
      })
    }
  }

}

/** depth-limited graph - use for debugging cyclical dependencies - will throw up if more than X levelx */
class DepthGraphLike[N <: GNode[N, L], L <: GLink[N]](max: Int)(target: GraphLike[N, L]) extends GraphLike[N, L] {
  import scala.collection.mutable

  def root: N = target.root
  def colored: N => Boolean = target.colored

  override protected def iforeach(n: N, from: Option[L], fn: (N, Int) => Unit, fl: (L, Int) => Unit, level: Int) {
    if (level >= max)
      throw new IllegalStateException("Maximum depth reached in graph: " + max)
    super.iforeach (n, from, fn, fl, level)
  }
}

/** traversal helpers */
object Graphs {
  /** set this to > 0 to limit all recursion for debug purposes */
  var maxDebugDepth = -1

  // TODO make this work - the types don't work right now...
  implicit def toGraphLike[N <: GNode[N, L], L <: GLink[N]](root: N)(implicit mn: Manifest[N], ml: Manifest[L]) = entire[N, L] (root)

  /** @deprecated - use entire(n).mkString */
  def mkString[N <: GNode[N, L], L <: GLink[N]](n: N): String = entire[N, L] (n).mkString

  /** @deprecated - use entire(n).mkString */
  def mkString2[N <: GNode[N, L], L <: GLink[N]](n: N): String = entire[N, L] (n).mkString

  /** @deprecated use Graphs.entire (root) foreach ...*/
  def foreach[N <: GNode[N, L], L <: GLink[N]](n: N, fn: (N, Int) => Unit, fl: (L, Int) => Unit, level: Int = 0) {
    entire[N, L] (n) foreach (fn, fl)
  }

  def filterNodes[N <: GNode[N, L], L <: GLink[N]](n: N)(f: N => Boolean): Seq[N] =
    entire[N, L] (n) filterNodes (f)

  /** colored graph where you inject the color as a function of node colored: N => Boolean */
  class CGL[N <: GNode[N, L], L <: GLink[N]](val root: N, val colored: N => Boolean)
    extends GraphLike[N, L]

  /** wrap a node and turn it into a colored graph with the given coloring function */
  def colored[N <: GNode[N, L], L <: GLink[N]](n: N)(color: N => Boolean) =
    new CGL[N, L](n, color)

  /** wrap a node and turn it into a graph with no color (all nodes traversed) */
  def entire[N <: GNode[N, L], L <: GLink[N]](n: N) = new CGL[N, L](n, { x: N => true })
}

