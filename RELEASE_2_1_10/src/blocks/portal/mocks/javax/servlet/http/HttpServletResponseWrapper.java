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
package javax.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version $Id$
 */
public class HttpServletResponseWrapper implements HttpServletResponse {

    public HttpServletResponseWrapper(HttpServletResponse response) {
        
    }

    public ServletResponse getResponse() {
        return null;
    }

    public void addCookie(Cookie arg0) {
    }

    public void addDateHeader(String arg0, long arg1) {
    }

    public void addHeader(String arg0, String arg1) {
    }

    public void addIntHeader(String arg0, int arg1) {
    }

    public boolean containsHeader(String arg0) {
        return false;
    }

    public String encodeRedirectUrl(String arg0) {
        return null;
    }

    public String encodeRedirectURL(String arg0) {
        return null;
    }

    public String encodeUrl(String arg0) {
        return null;
    }

    public String encodeURL(String arg0) {
        return null;
    }

    public void sendError(int arg0, String arg1) throws IOException {
    }

    public void sendError(int arg0) throws IOException {
    }

    public void sendRedirect(String arg0) throws IOException {
    }

    public void setDateHeader(String arg0, long arg1) {
    }

    public void setHeader(String arg0, String arg1) {
    }

    public void setIntHeader(String arg0, int arg1) {
    }

    public void setStatus(int arg0, String arg1) {
    }

    public void setStatus(int arg0) {
    }

    public void flushBuffer() throws IOException {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void setBufferSize(int arg0) {
    }

    public void setContentLength(int arg0) {
    }

    public void setContentType(String arg0) {
    }

    public void setLocale(Locale arg0) {
    }
}