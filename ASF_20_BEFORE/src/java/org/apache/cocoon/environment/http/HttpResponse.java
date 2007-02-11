/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.environment.http;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Implements the {@link org.apache.cocoon.environment.Response} interface
 * to provide response functionality in the HTTP servlets environment.
 * 
 * @author <a href="mailto:dev@cocoon.apache.org">Apache Cocoon Team</a>
 * @version CVS $Id: HttpResponse.java,v 1.3 2003/10/31 21:38:36 vgritsenko Exp $
 */

public final class HttpResponse implements Response {

    /** The real HttpServletResponse object */
    private final HttpServletResponse res;

    /**
     * Creates a HttpServletResponse based on a real HttpServletResponse object
     */
    protected HttpResponse (HttpServletResponse res) {
        this.res = res;
    }

    /**
     * Create a new cookie which is not added to the response
     */
    public Cookie createCookie(String name, String value) {
        return new HttpCookie(name, value);
    }

    public void addCookie(Cookie cookie) {
        if (cookie instanceof HttpCookie) {
            this.res.addCookie(((HttpCookie)cookie).getServletCookie());
        } else {
            javax.servlet.http.Cookie newCookie;
            newCookie = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
            newCookie.setComment(cookie.getComment());
            newCookie.setDomain(cookie.getDomain());
            newCookie.setMaxAge(cookie.getMaxAge());
            newCookie.setPath(cookie.getPath());
            newCookie.setSecure(cookie.getSecure());
            newCookie.setVersion(cookie.getVersion());
            this.res.addCookie(newCookie);
        }
    }

    public boolean containsHeader(String name) {
        return this.res.containsHeader(name);
    }

    public String encodeURL(String url) {
        if (url != null && url.indexOf(";jsessionid=") != -1)
            return url;
        return this.res.encodeURL(url);
    }

    public String encodeRedirectURL(String url) {
        if (url != null && url.indexOf(";jsessionid=") != -1) {
            return url;
        }

        return this.res.encodeRedirectURL(url);
    }

    public void sendError(int sc, String msg) throws IOException {
        this.res.sendError(sc, msg);
    }

    public void sendError(int sc) throws IOException {
        this.res.sendError(sc);
    }

    public void sendRedirect(String location) throws IOException {
        this.res.sendRedirect(location);
    }

    public void sendPermanentRedirect(String location) throws IOException {
        this.res.setHeader("location", location);
        this.res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    }
    
    public void setDateHeader(String name, long date) {
        this.res.setDateHeader(name, date);
    }

    public void addDateHeader(String name, long date) {
        this.res.addDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
        this.res.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
        this.res.addHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        this.res.setIntHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        this.res.addIntHeader(name, value);
    }

    public void setStatus(int sc) {
        this.res.setStatus(sc);
    }

    /**
     * @deprecated        As of version 2.1, use encodeURL(String url) instead
     */
    public String encodeUrl(String url) {
        return this.res.encodeUrl(url);
    }

    /**
     * @deprecated        As of version 2.1, use
     *              encodeRedirectURL(String url) instead
     */
    public String encodeRedirectUrl(String url) {
        return this.res.encodeRedirectUrl(url);
    }

    /**
     * @deprecated As of version 2.1, due to ambiguous meaning of the
     * message parameter. To set a status code
     * use <code>setStatus(int)</code>, to send an error with a description
     * use <code>sendError(int, String)</code>.
     */
    public void setStatus(int sc, String sm) {
        this.res.setStatus(sc, sm);
    }

    /* The ServletResponse interface methods */

    public String getCharacterEncoding() {
        return this.res.getCharacterEncoding();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        //throw new IllegalStateException ("you are not a serializer or reader");
        return this.res.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        //throw new IllegalStateException ("you are not a serializer or reader");
        return this.res.getWriter();
    }

    public void setContentLength(int len) {
        this.res.setContentLength(len);
    }

    public void setContentType(String type) {
        this.res.setContentType(type);
    }

    public void setBufferSize(int size) {
        this.res.setBufferSize(size);
    }

    public int getBufferSize() {
        return this.res.getBufferSize();
    }

    public void flushBuffer() throws IOException {
        this.res.flushBuffer();
    }

    public boolean isCommitted() {
        return this.res.isCommitted();
    }

    public void reset() {
        this.res.reset();
    }

    public void setLocale(Locale loc) {
        this.res.setLocale(loc);
    }

    public Locale getLocale() {
        return this.res.getLocale();
    }
}

