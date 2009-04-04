/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.servletservice.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates a {@link HttpServletResponse} object that is usable for internal block calls.
 * 
 * @version $Id$
 * @since 1.0.0
 */
public class ServletServiceResponse implements HttpServletResponse {

    private OutputStream outputStream;
    private ServletOutputStream servletStream;
    private PrintWriter writer;
    private boolean committed;
    private Locale locale;
    private int statusCode;

    private Map<String, String> headers;

    /**
     * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
     */
    final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    public ServletServiceResponse() {
        this.headers = new HashMap<String, String>();
        this.statusCode = HttpServletResponse.SC_OK;
    }

    public void addCookie(Cookie cookie) {
        // Ignore
    }

    public void addDateHeader(String name, long date) {
        // this class does not support multivalue headers
        this.setDateHeader(name, date);
    }

    public void addHeader(String name, String value) {
        // this class does not support multivalue headers
        this.setHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        // this class does not support multivalue headers
        this.setIntHeader(name, value);
    }

    public boolean containsHeader(String name) {
        return this.headers.containsKey(name);
    }

    public String encodeRedirectUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public void flushBuffer() throws IOException {
        this.committed = true;
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        // TODO Let it depend on the actual response body
        return "ISO-8859-1";
    }

    public Locale getLocale() {
        return this.locale;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writer != null) {
            throw new IllegalStateException("Tried to create output stream; writer already exists");
        }

        if (this.servletStream == null) {
            this.servletStream = new ServletOutputStream() {

                @Override
                public void flush() throws IOException {
                    ServletServiceResponse.this.outputStream.flush();
                }

                @Override
                public void write(int b) throws IOException {
                    ServletServiceResponse.this.outputStream.write(b);
                    ServletServiceResponse.this.committed = true;
                }

                /*
                 * This method is probably never called, the close will be initiated directly on
                 * this.outputStream by the one who set it via
                 * BlockCallHttpServletResponse.setOutputStream()
                 */
                @Override
                public void close() throws IOException {
                    ServletServiceResponse.this.outputStream.close();
                }

            };
        }

        return this.servletStream;
    }

    public PrintWriter getWriter() throws IOException {
        if (this.servletStream != null) {
            throw new IllegalStateException("Tried to create writer; output stream already exists");
        }

        if (this.writer == null) {
            this.writer = new PrintWriter(new OutputStreamWriter(this.outputStream, this.getCharacterEncoding()));
        }

        return this.writer;
    }

    public boolean isCommitted() {
        return this.committed;
    }

    public void reset() {
        this.resetBuffer();
    }

    public void resetBuffer() {
        // this class does not buffer anything so if first byte is written to output stream
        // there is no way to reset anything
        // Servlet Service Framework uses for buffering a wrapper called
        // HttpServletResponseBufferingWrapper
        if (this.committed) {
            throw new IllegalStateException("May not resetBuffer after response is committed");
        }
    }

    public void sendError(int sc) throws IOException {
        // TODO Auto-generated method stub
    }

    public void sendError(int sc, String msg) throws IOException {
        // TODO Auto-generated method stub
    }

    public void sendRedirect(String location) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setBufferSize(int size) {
        // TODO Implement buffering, for the moment ignore.
    }

    public void setContentLength(int len) {
        // Ignore
    }

    public void setContentType(String type) {
        this.setHeader("Content-Type", type);
    }

    public void setDateHeader(String name, long date) {
        this.setHeader(name, this.dateFormat.format(new Date(date)));
    }

    public long getDateHeader(String name) {
        String header = this.getHeader(name);
        if (header == null) {
            return -1;
        }

        try {
            return this.dateFormat.parse(header).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setIntHeader(String name, int value) {
        this.setHeader(name, String.valueOf(value));
    }

    public int getIntHeader(String name) {
        String header = this.getHeader(name);
        if (header == null) {
            return -1;
        }

        return Integer.parseInt(header);
    }

    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setStatus(int sc) {
        this.statusCode = sc;
    }

    public int getStatus() {
        return this.statusCode;
    }

    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException("This method has been deprecated");
    }

    public String getContentType() {
        return this.getHeader("Content-Type");
    }

    public void setCharacterEncoding(String arg0) {
        // TODO Auto-generated method stub
    }
}
