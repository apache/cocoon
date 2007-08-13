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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import org.apache.cocoon.environment.Request;

/**
 * Base class for any request
 *
 * @version $Id$
 */
public abstract class AbstractRequest 
    implements Request {

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributes()
     */
    public Map getAttributes() {
        return new RequestMap(this);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getParameters()
     */
    public Map getParameters() {
        return new RequestParameterMap(this);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getHeaders()
     */
    public Map getHeaders() {
        return new RequestHeaderMap(this);
    }
    
    public int getIntHeader(String name) {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO The method was added when Request was made extending HttpServletRequest, implement the method
        throw new UnsupportedOperationException();
    }
}
