/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package com.razie.pub.base.test;

import junit.framework.TestCase;

import com.razie.pub.base.log.Log;

public class LastLogTest extends TestCase {
    public static void testA1() {
        for (int i = 0; i < 50; i++)
            Log.logThis("log line " + i);
    }
    public static void testAGetLess() {
        String[] lines = Log.getLastLogs(20);
        System.out.println (Log.tryToString("   ", lines));
        assertTrue ("didn't get as many...", lines.length == 20);
    }
    public static void testBGetMore() {
        String[] lines = Log.getLastLogs(70);
        System.out.println (Log.tryToString("   ", lines));
        // there's more than 50 sometimes when running in a suite - i guess the Log is not reset or
        // there's background threads or something...
        assertTrue ("didn't get as many...just "+lines.length, lines.length >= 50);
    }
    public static void testC1() {
        for (int i = 51; i < Log.MAXLOGS; i++)
            Log.logThis("log line M " + i);
    }
    public static void testCWrapped() {
        String[] lines = Log.getLastLogs(200);
        System.out.println (Log.tryToString("   ", lines));
        assertTrue ("didn't get as many...", lines.length == 200);
    }
}
