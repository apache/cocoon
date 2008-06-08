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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.servletservice.util.ServletServiceRequest;
import org.apache.cocoon.servletservice.util.ServletServiceResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Id$
 * @since 1.0.0
 */
public abstract class AbstractServletConnection implements ServletConnection {

    /** By default we use the logger for this class. */
    protected final Log logger = LogFactory.getLog(this.getClass());

    /** Connection request */
    protected ServletServiceRequest request;

    /** Connection response */
    protected ServletServiceResponse response;

    /** The current block context */
    protected ServletContext context;

    /** If already connected */
    protected boolean connected;

    protected ByteArrayOutputStream requestBody;

    protected InputStream responseBody;

    protected URI uri;

    public void connect() throws IOException, ServletException {
        // if already connected, do nothing
        if (this.connected) {
            return;
        }

        this.request.setContext(this.context);

        if (this.requestBody != null) {
            this.request.setMethod("POST");
            this.request.setInputStream(new ByteArrayInputStream(this.requestBody.toByteArray()));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.response.setOutputStream(os);

        try {
            this.performConnect();
            this.response.flushBuffer();
            this.responseBody = new ByteArrayInputStream(os.toByteArray());
        } finally {
            os.close();
        }

        this.connected = true;
    }

    /**
     * Access the servlet and fill the response object.
     * 
     * @throws ServletException
     * @throws IOException
     */
    protected abstract void performConnect() throws ServletException, IOException;

    /**
     * Return an <code>InputStream</code> object to read from the source.
     * 
     * @throws IOException
     * @throws ServletException
     */
    public InputStream getInputStream() throws IOException, ServletException {
        this.connect();
        return this.responseBody;
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }

        this.request.setDateHeader("If-Modified-Since", ifmodifiedsince);
    }

    public long getLastModified() {
        if (!this.connected) {
            try {
                this.connect();
            } catch (Exception e) {
                this.logger.warn("Exception while reading the getLastModified data.");
                return 0;
            }
        }
        long headerFieldDate = this.getHeaderFieldDate("Last-Modified", 0);
        return headerFieldDate;
    }

    public String getContentType() {
        return this.getHeaderField("Content-Type");
    }

    public long getHeaderFieldDate(String name, long defaultValue) {
        try {
            return this.response.getDateHeader(name);
        } catch (Exception e) {
            this.logger.warn("Exception while reading the response header '" + name + "'.");
        }

        return defaultValue;
    }

    public String getHeaderField(String name) {
        try {
            this.connect();
        } catch (Exception e) {
            this.logger.warn("Exception while reading the response header '" + name + "'.");
            return null;
        }

        return this.response.getHeader(name);
    }

    public int getResponseCode() throws IOException {
        if (!this.connected) {
            try {
                this.connect();
            } catch (ServletException e) {
                throw new IOException("Could not get response status code");
            }
        }

        return this.response.getStatus();
    }

    /**
     * Returns an output stream that writes as POST to this connection.
     * 
     * @return an output stream that writes as POST to this connection.
     * @throws IllegalStateException - if already connected
     */
    public OutputStream getOutputStream() throws IllegalStateException {
        if (this.connected) {
            throw new IllegalStateException("You cannot write to the connection already connected.");
        }

        if (this.requestBody == null) {
            this.requestBody = new ByteArrayOutputStream();
        }
        return this.requestBody;

    }

    public URI getURI() {
        return this.uri;
    }

    /**
     * A special exception indicating that there is no servlet context available.
     */
    protected static class NoServletContextAvailableException extends RuntimeException {

        public NoServletContextAvailableException(String message) {
            super(message);
        }

    }

}
