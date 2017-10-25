/**
 * Razvan's public code. 
 * Copyright 2008 based on Apache license (share alike) see LICENSE.txt for details.
 */
package com.razie.pub.comms;

/**
 * Communications-related exception
 * 
 * @author razvanc99
 */
@SuppressWarnings("serial")
public class CommRtException extends RuntimeException {
    public CommRtException(String message) {
        super(message);
    }

    public CommRtException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommRtException(Throwable cause) {
        super(cause);
    }

}
