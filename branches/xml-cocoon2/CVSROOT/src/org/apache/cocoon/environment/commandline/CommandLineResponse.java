/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;


import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

/**
 * Creates a specific servlet response simulation from command line usage.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-04-18 12:05:56 $
 */

public class CommandLineResponse implements Response {

    public String getCharacterEncoding() { return null; }
    public Cookie createCookie(String name, String value) { return null; }
    public void addCookie(Cookie cookie) {}
    public boolean containsHeader(String name) { return false; }
    public void setHeader(String name, String value) {}
    public void setIntHeader(String name, int value) {}
    public void setDateHeader(String name, long date) {}
    public String encodeURL (String url) { return url; }
    public void setLocale(Locale locale) { }
    public Locale getLocale() { return null; }
    public void addDateHeader(String name, long date) { }
    public void addHeader(String name, String value) { }
    public void addIntHeader(String name, int value) { }
}
