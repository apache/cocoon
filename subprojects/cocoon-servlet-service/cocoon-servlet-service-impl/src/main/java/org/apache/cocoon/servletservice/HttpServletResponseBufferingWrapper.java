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
package org.apache.cocoon.servletservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>THIS IS INTERNAL CLASS OF SERVLET SERVICE FRAMEWORK AND SHOULDN'T BE USED ELSEWHERE!</p>
 * 
 * <p>This class works in two modes:</p>
 * <ol>
 * <li>If status code has been set to value different than <code>404</code> (<code>SC_NOT_FOUND</code>) then this class 
 *     acts completely transparently by forwarding all method calls to wrapped response object.</li>
 * <li>If status code has been set to <code>404</code> then this class acts like a buffer. It buffers all method calls 
 *     that would commit wrapped response. Buffering of such calls is being performed in order to assure that wrapped 
 *     response can be always reseted if needed. It's worth mentioning the fact that buffer for {@link OutputStream}
 *     returned by {@link #getOutputStream()} is limited to the size specified in
 *     {@link #BUFFER_LIMIT} field.</li>
 *
 *  <p>Additionally, this class lets the access to statusCode code that has been set through the 
 *     {@link #getStatusCode()} method.</p> 
 */
class HttpServletResponseBufferingWrapper extends HttpServletResponseWrapper {

    /**
     * Limit for a buffer for output stream returned by {@link #getOutputStream()} method.
     * This is a hard limit, if exceeded an exception is thrown.
     */
    static private int BUFFER_LIMIT = 1024 * 1024; //= 1MB
    static private String ALREADY_COMMITTED_EXCEPTION = "The response has been already committed.";
    
    private boolean bufferResponse;
    private boolean committed;
    private String message;
    private int statusCode;
    private boolean sendError;

    private ForwardingOrLimitingServletOutputStream outputStream;
    private PrintWriter printWriter;

    public HttpServletResponseBufferingWrapper(HttpServletResponse response) {
        super(response);
        resetBufferedResponse();
    }

    public void addCookie(Cookie cookie) {
        if (isCommitted())
            return;
        super.addCookie(cookie);
    }

    public void sendError(int sc) throws IOException {
        if (isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        if (sc != SC_NOT_FOUND)
            super.sendError(sc);
        else {
            bufferResponse = true;
            committed = true;
            sendError = true;
        }
        statusCode = sc;
    }

    public void sendError(int sc, String msg) throws IOException {
        if (isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        if (sc != SC_NOT_FOUND)
            super.sendError(sc, msg);
        else {
            bufferResponse = true;
            committed = true;
            message = msg;
            sendError = true;
        }
        statusCode = sc;
    }

    public void sendRedirect(String location) throws IOException {
        if (isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        super.sendRedirect(location);
        statusCode = HttpServletResponse.SC_TEMPORARY_REDIRECT;
    }

    public void setDateHeader(String name, long date) {
        if (isCommitted())
            return;
        super.setDateHeader(name, date);
    }

    public void addDateHeader(String name, long date) {
        if (isCommitted())
            return;
        super.addDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
        if (isCommitted())
            return;
        super.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
        if (isCommitted())
            return;
        super.addHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        if (isCommitted())
            return;
        super.setIntHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        if (isCommitted())
            return;
        super.addIntHeader(name, value);
    }

    public void setStatus(int sc) {
        if (isCommitted())
            return;
        if (sc != SC_NOT_FOUND)
            super.setStatus(sc);
        else {
            bufferResponse = true;
        }
        statusCode = sc;
    }

    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException(
                "This method has been deprecated.");
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null)
            this.outputStream = new ForwardingOrLimitingServletOutputStream(BUFFER_LIMIT, super.getOutputStream());
        return outputStream;
    }

    public PrintWriter getWriter() throws IOException {
        if (this.outputStream != null)
            throw new IllegalStateException(
                    "Output buffer has been already obtained. You can use either output buffer or print writer at one time.");
        if (this.printWriter == null)
            this.printWriter = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
        return printWriter;
    }

    public void flushBuffer() throws IOException {
        if (!bufferResponse)
            super.flushBuffer();
        else
            committed = true;
    }

    public boolean isCommitted() {
        return committed || super.isCommitted();
    }

    public void resetBuffer() {
        if (isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        if (!bufferResponse)
            super.resetBuffer();
        else if (outputStream != null)
            outputStream.reset();
    }

    public void reset() {
        if (isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        super.reset();
        bufferResponse = false;
        message = null;
    }

    public void flushBufferedResponse() throws IOException {
        if (bufferResponse) {
            if (sendError) {
                try {
                    if (message != null)
                        super.sendError(SC_NOT_FOUND, message);
                    else
                        super.setStatus(SC_NOT_FOUND);
                } catch (IOException e) {
                    // this should never occur
                    throw new IllegalStateException(
                            "FATAL ERROR! This situation should never occur because it's a job of "
                                    + getClass().getName() + " class to "
                                    + "prevent such situation.");
                }
            } else {
                if (message != null)
                    super.setStatus(SC_NOT_FOUND, message);
                else
                    super.setStatus(SC_NOT_FOUND);
            }
        }
        
        //since all data goes through ForwardingOrLimitingServletOutputStream then flushing
        //must be performed in *every* case now
        if (this.printWriter != null) {
            if (this.printWriter.checkError())
                throw new IOException(
                        "Error occured while writing to printWriter.");
            this.printWriter.close();
        } else if (outputStream != null)
            outputStream.writeTo(super.getOutputStream());
        
        super.flushBuffer();
    }

    public void resetBufferedResponse() {
        if (super.isCommitted())
            throw new IllegalStateException(ALREADY_COMMITTED_EXCEPTION);
        if (bufferResponse) {
            message = null;
            bufferResponse = false;
            committed = false;
            sendError = false;
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Simple class acting like a {@link ServletOutputStream} but limiting (if it does not forward) number of bytes that 
     * can be written to the stream.
     */
    private class ForwardingOrLimitingServletOutputStream extends ServletOutputStream {
        
        private Log log = LogFactory.getLog(getClass());

        private int writeLimit;
        private ByteArrayOutputStream outputStream;
        
        private OutputStream forwardTo;

        public ForwardingOrLimitingServletOutputStream(int writeLimit, OutputStream forwardTo) {
            this.writeLimit = writeLimit;
            this.forwardTo = forwardTo;
            reset();
        }

        public void write(int b) throws IOException {
            HttpServletResponseBufferingWrapper.this.committed = true;
            
            if (isForwarding())
                forwardTo.write(b);
            else {
                if (this.outputStream.size() < this.writeLimit)
                    this.outputStream.write(b);
                else {
                    RuntimeException e = new RuntimeException(
                            "The buffering limit (" + writeLimit+ ") has been reached. If you encounter this exception it means that you to "
                            + "write a big response body for response that has error code set as status code. This is always a bad "
                            + "idea and in such case you should reconsider your design.");
                    log.fatal("Fatal error occured in writing to response", e);
                    throw e;
                }
            }
        }

        public void reset() {
            this.outputStream = new ByteArrayOutputStream(writeLimit);
        }

        public void writeTo(OutputStream outputStream) throws IOException {
            if (this.outputStream.size() > 0)
                this.outputStream.writeTo(outputStream);
        }
        
        private boolean isForwarding() {
            return !HttpServletResponseBufferingWrapper.this.bufferResponse;
        }

    }

}
