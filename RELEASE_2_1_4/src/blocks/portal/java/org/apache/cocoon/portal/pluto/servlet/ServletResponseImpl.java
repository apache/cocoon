/*
 * 
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 2004 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself, if and
 * wherever such third-party acknowledgments normally appear.
 *  4. The names "Apache Cocoon" and "Apache Software Foundation" must not be
 * used to endorse or promote products derived from this software without prior
 * written permission. For written permission, please contact
 * apache@apache.org.
 *  5. Products derived from this software may not be called "Apache", nor may
 * "Apache" appear in their name, without prior written permission of the
 * Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package org.apache.cocoon.portal.pluto.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Our response wrapper
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: URICopletAdapter.java,v 1.11 2003/10/20 13:36:41 cziegeler
 *          Exp $
 */
public class ServletResponseImpl implements HttpServletResponse {

    protected final HttpServletResponse response;
    protected MyOutputStream stream;
    protected PrintWriter writer;

    protected boolean committed = false;
    protected int bufferSize = 1024;

    protected String redirectURL;
    
    public ServletResponseImpl(HttpServletResponse response) {
        this.response = response;
        this.stream = new MyOutputStream();
        this.writer = new PrintWriter(this.stream);
    }

    /**
     * Return the content of the portlet
     */
    public String getContent() {
        this.writer.flush();
        try {
            this.stream.flush();
        } catch (IOException ignore) {
        }
        final String value = new String(this.stream.stream.toByteArray());
        this.stream = new MyOutputStream();
        this.writer = new PrintWriter(this.stream);
        return value;
    }

    /**
     * Get redirect url
     */
    public String getRedirectURL() {
        return this.redirectURL;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    public void addCookie(Cookie arg0) {
        this.response.addCookie(null);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
     *      long)
     */
    public void addDateHeader(String arg0, long arg1) {
        this.response.addDateHeader(arg0, arg1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
     *      java.lang.String)
     */
    public void addHeader(String arg0, String arg1) {
        this.response.addHeader(arg0, arg1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
     *      int)
     */
    public void addIntHeader(String arg0, int arg1) {
        this.response.addIntHeader(arg0, arg1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    public boolean containsHeader(String arg0) {
        return this.response.containsHeader(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     */
    public String encodeRedirectUrl(String arg0) {
        return this.response.encodeRedirectURL(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    public String encodeRedirectURL(String arg0) {
        return this.response.encodeRedirectURL(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    public String encodeUrl(String arg0) {
        return this.response.encodeURL(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    public String encodeURL(String arg0) {
        return this.response.encodeURL(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#sendError(int,
     *      java.lang.String)
     */
    public void sendError(int arg0, String arg1) throws IOException {
        //this.response.sendError(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError(int arg0) throws IOException {
        //this.response.sendError(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#redirect(java.lang.String)
     */
    public void sendRedirect(String arg0) throws IOException {
        this.redirectURL = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
     *      long)
     */
    public void setDateHeader(String arg0, long arg1) {
        this.response.setDateHeader(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
     *      java.lang.String)
     */
    public void setHeader(String arg0, String arg1) {
        this.response.setHeader(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
     *      int)
     */
    public void setIntHeader(String arg0, int arg1) {
        this.response.setIntHeader(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setStatus(int,
     *      java.lang.String)
     */
    public void setStatus(int arg0, String arg1) {
        //this.response.setStatus(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int arg0) {
        //this.response.setStatus(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        this.committed = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    public int getBufferSize() {
        return this.bufferSize = 1024;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.response.getCharacterEncoding();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getLocale()
     */
    public Locale getLocale() {
        return this.response.getLocale();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        return this.stream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    public boolean isCommitted() {
        return this.committed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#reset()
     */
    public void reset() {
        if (!this.committed) {
            this.stream = new MyOutputStream();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    public void setBufferSize(int arg0) {
        this.bufferSize = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    public void setContentLength(int arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    public void setLocale(Locale arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    public void resetBuffer() {
    }

}

class MyOutputStream extends ServletOutputStream {

    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public MyOutputStream() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        this.stream.write(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        super.flush();
        this.stream.flush();
    }

}