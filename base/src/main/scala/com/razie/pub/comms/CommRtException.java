/**
 * Razvan's public code. 
 * Copyright 2008 based on Apache license (share alike) see LICENSE.txt for details.
 */
package com.razie.pub.comms;

import java.net.URLConnection;

/**
 * Communications-related exception
 * 
 * @author razvanc99
 */
@SuppressWarnings("serial")
public class CommRtException extends RuntimeException {
    public URLConnection uc = null;
    public int httpCode = -1;
    public String details = "";
    public String location302 = "";

    public CommRtException(String message, URLConnection uc) {
        super(message);
        this.uc = uc;
    }

    public CommRtException(String message, URLConnection uc, int code) {
        super(message);
        this.uc = uc;
        this.httpCode = code;
    }

    public CommRtException withDetails(String s) {
        this.details = s;
        return this;
    }

    public CommRtException withLocation302(String s) {
        this.location302 = s;
        return this;
    }

    public CommRtException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommRtException(Throwable cause) {
        super(cause);
    }

}
