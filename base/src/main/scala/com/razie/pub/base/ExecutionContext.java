/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package com.razie.pub.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import razie.base.AttrAccessImpl;

/**
 * An execution context is used by entities (agents) that EACH can span a few threads. You can think of
 * it as a MiniVM as well, with its own statics etc - see NoStatics.
 * 
 * <p>
 * Basically, when you want to share the same execution context in a thread pool, just use 
 * EC.enter() and EC.exit().
 * 
 * <p>
 * It is similar to a session in a
 * servlet call or an EJB context during an EJB call, SOMEBODY is responsible for setting the right
 * context with enter() when the thread is started / reused from a pool AND also erasing it at the
 * end, with exit(), to be nice.
 * 
 * <p>
 * NOTE: This is a logical context. While you can certainly create one for each thread, you can also
 * share one across multiple threads and keep it between invocations, much like a servlet session.
 * 
 * <p>
 * In the mutant-agent, this is used per instance of agent, such that multiple agents can run inside
 * the same JVM.
 * 
 * <p>
 * Some other classes, most noticeably NoStatics, can use the context later to figure out who
 * they're working for...
 * 
 * <p>USAGE: if you need one and don't know what you should do, just do .instance()
 * 
 * <p>
 * NOTE: this is not terribly efficient, but simplifies multithreaded programming.
 * 
 * TODO 1-1 check that it is mtsafe
 * 
 * TODO 2-2 CODE i should use ThreadLocal to implement this: the current context for this thread
 * should be in the ThreadLocal and don't keep syncing on the map by thread, eh...?
 * 
 * @author razvanc
 */
public class ExecutionContext extends AttrAccessImpl {
   // TODO 1-1 actually use weak refrences in this map- this probably keeps Threads from being
   // collected if one forgets to exit()

   // TODO 2-1 implement nice debugging: remember stack trace when enter() and print
   // when collected without exit()

   // all active instances, indexed by active thread
   private static Map<Thread, ExecutionContext> instances = Collections
                                                                .synchronizedMap(new HashMap<Thread, ExecutionContext>());

   public static ExecutionContext               DFLT_CTX  = new ExecutionContext(null);
   public static ExecutionContext               DEFAULT  = DFLT_CTX;

   // these locals are cleaned upon current thread exiting the context
   List<String>                                 locals    = new ArrayList<String>();
   NoStatics                                    statics   = null;

   /**
    * thread contexts are intricately tied to the no-statics concept
    * 
    * @param myStatics
    */
   public ExecutionContext(NoStatics myStatics) {
      this.statics = myStatics;
   }

   /** return the instance to use for the curent thread */
   public static ExecutionContext instance() {
      ExecutionContext s = instances.get(Thread.currentThread());
      return s == null ? DFLT_CTX : s;
   }

   /**
    * used in unit tests: perform a complete cleaniup of all statics in the current jvm - call this
    * before setting up a new test
    */
   public static void resetJVM() {
      instances.clear();
      NoStatics.reset();
      DFLT_CTX.clear();
   }

   /*
    * this attr is erased when exit() - it is only accessible on this session/thread/call
    */
   public synchronized void setLocalAttr(String name, Object value) {
      this.setAttr(name, value);
      locals.add(name);
   }

   /**
    * current thread ENTERs this context and return the old one - you can use the old one on exit
    */
   public synchronized ExecutionContext enter() {
      // no further locking since the entry for this thread can't be modified
      // inbetween
      ExecutionContext old = instances.get(Thread.currentThread());
      instances.put(Thread.currentThread(), this);

      if (statics != null)
         NoStatics.enter(statics);

      return old;
   }

   /** current thread EXITs this context */
   public static void exit(ExecutionContext... old) {
      ExecutionContext exited = instances.remove(Thread.currentThread());

      if (old.length > 0 && old[0] != null)
         instances.put(Thread.currentThread(), old[0]);

      if (exited != null)
         synchronized (exited) {
            if (exited.locals != null)
               for (String n : exited.locals)
                  exited.setAttr(n, null);
         }
   }
}
