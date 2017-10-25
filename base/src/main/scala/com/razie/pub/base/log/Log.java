/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package com.razie.pub.base.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple log proxy - log4j is dominant but then there's the JDK's log... this gives you the freedom
 * to use one or the other...or simply recode to use your own - if you hapen to use another
 * one...doesn't it suck when you use a library which writes to stdout?
 * 
 * largely deprecated, should use the slf4j directly instead
 * 
 * @author razvanc99
 */
public class Log {

  private String         category;
  private String         component;

  public static int      MAXLOGS    = 1000;
  public static String[] lastLogs   = new String[MAXLOGS];
  public static int      curLogLine = 0;

  public static boolean  DEBUGGING  = true;
  public static boolean  SILENT     = false;

  /** use this to access the underlying slf4j directly and use their formatting etc */
  public Logger          slf4j;

  @SuppressWarnings("rawtypes")
  public Log(Class c) {
    this.category = "?";
    this.component = c.getName();
    slf4j = LoggerFactory.getLogger(c);
  }

  /** @deprecated use by class */
  public Log(String componentNm, String categoryNm) {
    this.category = categoryNm;
    this.component = componentNm;
    slf4j = LoggerFactory.getLogger(componentNm + "-" + categoryNm);
  }

  public static void addLogLine(String line) {
    synchronized (lastLogs) {
      lastLogs[curLogLine] = line;
      curLogLine = (curLogLine + 1) % MAXLOGS;
    }
  }

  public static String[] getLastLogs(int howMany) {
    synchronized (lastLogs) {
      int theseMany = howMany;
      String[] ret;

      // find out how many we have
      if (lastLogs[MAXLOGS - 1] == null) {
        theseMany = howMany > curLogLine ? curLogLine : howMany;
        ret = new String[theseMany];
        int k = 0;
        for (int i = curLogLine - theseMany; k < theseMany; i++) {
          ret[k++] = lastLogs[i];
        }
      } else {
        // bounced
        theseMany = howMany > MAXLOGS ? MAXLOGS : howMany;
        ret = new String[theseMany];
        int k = 0;
        for (int i = theseMany - curLogLine; i >= 0 && i < MAXLOGS && k < theseMany; i++) {
          ret[k++] = lastLogs[i];
        }
        for (int i = curLogLine - (theseMany - k); i < curLogLine && k < theseMany; i++) {
          ret[k++] = lastLogs[i];
        }
      }
      return ret;
    }
  }

  /** from http://www.javapractices.com/topic/TopicAction.do?Id=78 */
  public static String getStackTraceAsString(Throwable aThrowable) {
    if (aThrowable != null) {
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      aThrowable.printStackTrace(printWriter);
      return result.toString();
    } else
      return "";
  }

  public void log(String m, Throwable t) {
    log(m + (t != null ? " Exception: " + getStackTraceAsString(t) : ""));
  }

  public void log(Object... o) {
    ilog("LOG", o);
  }

  protected void ilog(String cat, Object... o) {
    String m = "";
    for (int i = 0; i < o.length; i++) {
      m += o[i].toString();
    }

    if (cat == "LOG")
      slf4j.info(m);
    else if (cat == "ALARM")
      slf4j.error(m);
    else if (cat == "DEBUG")
      slf4j.debug(m);
    else if (cat == "WARN")
      slf4j.warn(m);
    else
      slf4j.warn(m);

    addLogLine(m);
  }

  public void warn(String m, Throwable... e) {
    ilog("WARN", m + (e.length <= 0 ? "" : getStackTraceAsString(e[0])));
  }

  public void alarm(String m, Throwable... e) {
    ilog("ALARM", m + (e.length <= 0 ? "" : getStackTraceAsString(e[0])));
  }

  public void debug(int l, Object... o) {
    trace(l, o);
  }

  /**
   * trace by concatenating the sequence of objects to String - this is the most efficient trace
   * since the strings will only be evaluated and concatenated if the trace is actually turned on
   */
  public void trace(int l, Object... o) {
    if (isTraceLevel(l)) {
      String m = "";
      for (int i = 0; i < o.length; i++) {
        m += (o[i] == null ? "null" : o[i].toString());
      }
      ilog("DEBUG", m);
    }
  }

  public boolean isTraceLevel(int l) {
    if (l == 3) return DEBUGGING && !SILENT && slf4j.isTraceEnabled();
    else return DEBUGGING && !SILENT && slf4j.isDebugEnabled();
  }

  public boolean isTraceOn() {
    return DEBUGGING && !SILENT && slf4j.isDebugEnabled();
  }

  public static void logThis(String m) {
    logger.log(m);
  }

  // TODO 2-1 implement the separate audit facility
  public static void audit(String m) {
    logger.ilog("AUDIT", m);
  }

  public static void audit(String m, Throwable t) {
    logger.log("AUDIT", m, t);
  }

  public static void traceThis(String m) {
    logger.trace(1, m);
  }

  public static void traceThis(String m, Throwable t) {
    logger.trace(1, m, t);
  }

  public static void traceThis(int level, String m) {
    logger.trace(level, m);
  }

  public static void traceThis(int level, String m, Throwable t) {
    logger.trace(level, m, t);
  }

  public static void logThis(String m, Throwable t) {
    logger.log(m, t);
  }

  /** alarm this only once in this run... */
  public static void alarmOnce(String errorcode, String m, Throwable... e) {
    if (!alarmedOnce.containsKey(errorcode)) {
      logger.alarm(m, e);
      alarmedOnce.put(errorcode, errorcode);
    }
  }

  protected static Map<String, String> alarmedOnce = Collections
                                                       .synchronizedMap(new HashMap<String, String>());

  /** alarm this */
  public static void alarmThis(String m, Throwable... e) {
    logger.alarm(m, e);
  }

  public static void warnThis(String m, Throwable... e) {
    logger.warn(m, e);
  }

  /** alarm this and throw a new RT exception with the message and the cause */
  public static void alarmThisAndThrow(String m, Throwable... e) {
    // TODO i don't think this should log again...since it throws it, eh?
    logger.alarm(m, e);
    if (e.length > 0 && e[0] != null)
      throw new RuntimeException(m, e[0]);
    else
      throw new RuntimeException(m);
  }

  /**
   * helper to turn lists/arrays/maps into strings for nice logging
   * 
   * @param ret object to toString
   * @return either the new String or the original object if not recognized
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object tryToString(String indent, Object ret) {
    if (ret != null && ret instanceof Collection) {
      return toString("", (Collection) ret);
    } else if (ret != null && ret instanceof Map) {
      return "\n" + (ret).toString();
    } else if (ret != null && ret instanceof Object[]) {
      return toString("", (Object[]) ret);
    } else {
      return ret;
    }
  }

  /**
   * simple helper to log collections, each element toString()
   * 
   * @param indent is a prefix to be added to each line, useful if this is inside a structure. Don't
   *        send null, but "".
   * @param col is the collection to be logged
   */
  public static String toString(String indent, Collection<? extends Object> col) {
    String msg = indent + "Collection is null!";
    if (col != null) {
      msg = indent + "Collection: {\n";
      for (Object k : col) {
        msg += indent + "   " + (k == null ? "null" : k.toString()) + "\n";
      }
      msg += indent + "}";
    }
    return msg;
  }

  /**
   * simple helper to log collections, each element toString()
   * 
   * @param indent is a prefix to be added to each line, useful if this is inside a structure. Don't
   *        send null, but "".
   * @param col is the collection to be logged
   */
  public static String toString(String indent, Object[] map) {
    String msg = indent + "Object[] is null!";
    if (map != null) {
      msg = indent + "Object[]: {\n";
      for (int i = 0; i < map.length; i++) {
        Object k = map[i];
        msg += indent + "   " + (k == null ? "null" : k.toString()) + "\n";
      }

      msg += indent + "}";
    }
    return msg;
  }

  public static Factory factory = new Factory();
  public static Log     logger  = factory.create("?", "?");
  public static Log     dflt    = logger;                  // alias
}
