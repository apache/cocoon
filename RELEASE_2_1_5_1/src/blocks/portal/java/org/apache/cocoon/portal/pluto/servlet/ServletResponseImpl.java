/*
 * Copyright 2004,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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