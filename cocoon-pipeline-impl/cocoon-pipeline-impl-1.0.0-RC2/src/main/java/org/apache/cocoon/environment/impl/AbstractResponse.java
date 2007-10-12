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
package org.apache.cocoon.environment.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

import org.apache.cocoon.environment.Response;

/**
 * Base class for any response
 *
 * @version $Id$
 */
public abstract class AbstractResponse 
    implements Response {

    public String encodeRedirectURL(String url) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public String encodeRedirectUrl(String url) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public String encodeUrl(String url) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void sendError(int sc) throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void sendError(int sc, String msg) throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void sendRedirect(String location) throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setStatus(int sc) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setStatus(int sc, String sm) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void flushBuffer() throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public int getBufferSize() {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public String getContentType() {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public PrintWriter getWriter() throws IOException {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public boolean isCommitted() {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void reset() {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void resetBuffer() {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setBufferSize(int size) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String charset) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setContentLength(int len) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }

    public void setContentType(String type) {
        // TODO The method was added when Response was made extending HttpServletResponse, implement the method
        throw new UnsupportedOperationException();
    }
}
