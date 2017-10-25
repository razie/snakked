/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package com.razie.pub.base;

import java.util.HashMap;
import java.util.Map;

/**
 * thread local statics, to allow multiple instances of stuff/services/agents to
 * co-exist (for testing for instance) you need easy ways to avoid using
 * statics. As opposed to NoStatic, this is designed with one instance per
 * class.
 * 
 * <p>
 * The idea is that code should not have statics, which are bad in multithreaded
 * programming. This class should be used to hold these "statics" instead -
 * since it can hold difference instances of the same "static" in each thread.
 * 
 * <p>
 * The way this works is: the root "process/agent" will register a "context" in
 * each thread it owns or reuses. This class will find that and get the related
 * set of "statics" for that context.
 * 
 * <p>
 * Keeping instances: you either keep the instnace yourself and call
 * enter(instance) or register(name, instance) and then call enter(name).
 * 
 * <p>
 * Usage: call put() to create statics and get() to get them.
 * 
 * <p>
 * NOTE: this is not terribly efficient, but simplifies multithreaded
 * programming.
 * 
 * TODO i should use ThreadLocal to implement this
 * 
 * TODO sync the instances
 * 
 * @author razvanc99
 */
public class NoStatics {
	// these are the current "static" values for this context/thread
	private Map<Class<?>, Object> statics = new HashMap<Class<?>, Object>();

	// the contexts are basically one per agent instance
	private static Map<String, NoStatics> contexts = new HashMap<String, NoStatics>();

	// there are as many instances as there are current active threads,
	// registered here
	private static Map<Thread, NoStatics> instances = new HashMap<Thread, NoStatics>();
	private static NoStatics root = new NoStatics();

	public static NoStatics instance() {
		NoStatics s = instances.get(Thread.currentThread());
		return s == null ? root : s;
	}

   public static void reset() {
      instances.clear(); 
      root.statics.clear(); 
      // questionable: this deletes all statics - but that's a JVM reset, isn't it?
   }

   public static void resetJVM() {  ExecutionContext.resetJVM();  }

	/** the current thread uses this instance - normally from a ThreadContext */
	public static void enter(NoStatics instance) {
		instances.put(Thread.currentThread(), instance);
	}

	/** the current thread uses this instance - normally from a ThreadContext */
	public static void enter(String name) {
		instances.put(Thread.currentThread(), contexts.get(name));
	}

	/**
	 * register a set of statics - for one plugin/agent instance this is then
	 * referenced by its name
	 * 
	 * @param name
	 *            the name of this group of statics - agent instnace, plugin
	 *            instance etc
	 * @param instance
	 *            the set of "statics"
	 */
	public static void register(String name, NoStatics instance) {
		contexts.put(name, instance);
	}

	/**
	 * the current thread done with this instance - normally from a
	 * ThreadContext
	 */
	public static void exit() {
		instances.remove(Thread.currentThread());
	}

	/** this is the root context in the main thread and all threads that don't have a NoStatics */
	public static NoStatics root () { return root; };

	/**
	 * create a static for the current thread for the given class
	 * 
	 * @param c
	 *            the class of the static
	 * @param o
	 *            the instance to use in this and related threads
	 * @return the same object you put in
	 */
	public static Object put(Class<?> c, Object o) {
		instance().statics.put(c, o);
		return o;
	}

	/**
	 * remove a static for the current thread for the given class
	 * 
	 * @param c
	 *            the class of the static
	 * @param o
	 *            the instance to use in this and related threads
	 * @return the same object you put in
	 */
	public static void remove(Class<?> c) {
		instance().statics.remove(c);
	}

	/**
	 * get the instance/static for this thread of the given class on this thread
	 */
	public static Object get(Class<?> c) {
		return instance().statics.get(c);
	}
	
   /**
    * same as get, but works on instance - goot for root().getLocal()
    */
   public Object getLocal(Class<?> c) {
      return statics.get(c);
   }
}
