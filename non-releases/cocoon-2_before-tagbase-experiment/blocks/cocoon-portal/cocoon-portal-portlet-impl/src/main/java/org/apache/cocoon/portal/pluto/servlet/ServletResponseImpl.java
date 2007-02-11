/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Our response wrapper.
 *
 * @version $Id$
 */
public class ServletResponseImpl extends HttpServletResponseWrapper {

    protected MyOutputStream stream;
    protected PrintWriter writer;

    protected boolean committed = false;
    protected int bufferSize = 1024;

    protected String redirectURL;

    protected String encoding = "ISO-8859-1";

    public ServletResponseImpl(HttpServletResponse response) {
        super(response);
        this.stream = new MyOutputStream();
    }

    /**
     * Return the content of the portlet
     */
    public String getContent() {
        if ( this.writer != null ) {
            this.writer.flush();
        }
        try {
            this.stream.flush();
        } catch (IOException ignore) {
            // just ignore it
        }
        String value;
        try {
            value = this.stream.stream.toString(this.encoding);
        } catch (UnsupportedEncodingException uee) {
            value = new String(this.stream.stream.toByteArray());
        }
        this.stream = new MyOutputStream();
        this.writer = null;
        return value;
    }

    /**
     * Get redirect url
     */
    public String getRedirectURL() {
        return this.redirectURL;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getContent();
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int, String)
     */
    public void sendError(int arg0, String arg1) throws IOException {
        //this.response.sendError(arg0, arg1);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError(int arg0) throws IOException {
        //this.response.sendError(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    public void sendRedirect(String arg0) throws IOException {
        this.redirectURL = arg0;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, String)
     */
    public void setStatus(int arg0, String arg1) {
        //this.response.setStatus(arg0, arg1);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int arg0) {
        //this.response.setStatus(arg0);
    }

    /**
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        this.committed = true;
    }

    /**
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    public int getBufferSize() {
        return this.bufferSize = 1024;
    }

    /**
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        return this.stream;
    }

    /**
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        if ( this.writer == null ) {
            this.writer = new PrintWriter(new OutputStreamWriter(this.stream, this.encoding));
        }
        return this.writer;
    }

    /**
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    public boolean isCommitted() {
        return this.committed;
    }

    /**
     * @see javax.servlet.ServletResponse#reset()
     */
    public void reset() {
        if (!this.committed) {
            this.stream = new MyOutputStream();
        }
    }

    /**
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    public void setBufferSize(int arg0) {
        this.bufferSize = arg0;
    }

    /**
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    public void setContentLength(int arg0) {
        // nothing to do 
    }

    /**
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String typeInfo) {
        if ( typeInfo != null ) {
            int pos = typeInfo.indexOf("charset=");
            if ( pos != -1 ) {
                this.encoding = typeInfo.substring(pos + 8);
            }
        }
    }

    public void setCharacterEncoding(String enc) {
        this.encoding = enc;
    }

    /**
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        // nothing to do 
    }

    /**
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    public void resetBuffer() {
        // nothing to do 
    }

    protected final static class MyOutputStream extends ServletOutputStream {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        public MyOutputStream() {
            // nothing to do 
        }

        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) throws IOException {
            this.stream.write(b);
        }

        /**
         * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException {
            super.flush();
            this.stream.flush();
        }
    }
}
