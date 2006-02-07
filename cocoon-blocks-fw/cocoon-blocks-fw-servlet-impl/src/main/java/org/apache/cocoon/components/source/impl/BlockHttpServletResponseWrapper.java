/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wraps the response. Modification of headers and status is ignored. The output
 * stream and the methods for it are overridden.
 * 
 * @version $Id$
 */
class BlockHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private OutputStream outputStream;
    private ServletOutputStream servletStream;
    private OutputStreamWriter writer;
    private boolean committed;
    
    public BlockHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addDateHeader(java.lang.String, long)
     */
    public void addDateHeader(String name, long date) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String name, String value) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
     */
    public void addIntHeader(String name, int value) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setDateHeader(java.lang.String, long)
     */
    public void setDateHeader(String name, long date) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String value) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
     */
    public void setIntHeader(String name, int value) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int, java.lang.String)
     */
    public void setStatus(int sc, String sm) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    public void setStatus(int sc) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    public void flushBuffer() throws IOException {
        this.committed = true;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getBufferSize()
     */
    public int getBufferSize() {
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writer != null)
            throw new IllegalStateException( "Tried to create output stream; writer already exists" );

        return new ServletOutputStream() {

            /* (non-Javadoc)
             * @see java.io.OutputStream#write(int)
             */
            public void write(int b) throws IOException {
                BlockHttpServletResponseWrapper.this.outputStream.write(b);
            }
        };
    }
    
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;        }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        if (this.servletStream != null)
            throw new IllegalStateException( "Tried to create writer; output stream already exists" );

        return new PrintWriter(new OutputStreamWriter(this.outputStream, this.getCharacterEncoding()));
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#isCommitted()
     */
    public boolean isCommitted() {
        return this.committed;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#reset()
     */
    public void reset() {
        this.resetBuffer();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#resetBuffer()
     */
    public void resetBuffer() {
        if (this.committed)
            throw new IllegalStateException( "May not resetBuffer after response is committed" );
        this.outputStream = null;
        this.servletStream = null;
        this.writer = null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setBufferSize(int)
     */
    public void setBufferSize(int size) {
        // TODO Implement buffering, for the moment ignore.
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
     */
    public void setContentLength(int len) {
        // Ignore
    }
}
