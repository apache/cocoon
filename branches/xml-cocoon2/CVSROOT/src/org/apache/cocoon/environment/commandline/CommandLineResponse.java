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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

/**
 * Creates a specific servlet response simulation from command line usage.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-02 11:07:29 $
 */

public class CommandLineResponse implements HttpServletResponse {
    
    public PrintWriter getWriter() throws IOException {
        throw new IllegalStateException ("you are not a serializer or reader");
    }
    public ServletOutputStream getOutputStream() throws IOException { 
        throw new IllegalStateException ("you are not a serializer or reader");
    }

    public void setContentLength(int len) {}
    public void setContentType(String type) {}
    public String getCharacterEncoding() { return null; }
    public void addCookie(Cookie cookie) {}
    public boolean containsHeader(String name) { return false; }
    public void setStatus(int sc) {}
    public void setHeader(String name, String value) {}
    public void setIntHeader(String name, int value) {}
    public void setDateHeader(String name, long date) {}
    public void sendError(int sc, String msg) throws IOException {}
    public void sendError(int sc) throws IOException {}
    public void sendRedirect(String location) throws IOException {}
    public String encodeURL (String url) { return url; }
    public String encodeRedirectURL (String url) { return url; }
    public void setBufferSize(int size) { }
    public int getBufferSize() { return 0; }
    public void flushBuffer() { }
    public boolean isCommitted() { return false; }
    public void reset() { }
    public void setLocale(Locale locale) { }
    public Locale getLocale() { return null; }
    public void addDateHeader(String name, long date) { }
    public void addHeader(String name, String value) { }
    public void addIntHeader(String name, int value) { }
    
    /** @deprecated */
    public void setStatus(int sc, String sm) {}
    /** @deprecated */
    public String encodeUrl(String url) { return url; }
    /** @deprecated */
    public String encodeRedirectUrl(String url) { return url; }
}
