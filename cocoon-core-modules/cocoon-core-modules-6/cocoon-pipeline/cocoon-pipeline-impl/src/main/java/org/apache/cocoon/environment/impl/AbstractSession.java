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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

import org.apache.cocoon.environment.Session;

/**
 * Base class for any session
 *
 * @version $Id$
 * @deprecated This class implements deprecated interface and will be removed in the future.
 *             See {@link Session} interface for details. 
 */
public abstract class AbstractSession 
    implements Session {

    public Map getAttributes() {
	return new SessionMap(this);
    }

    public ServletContext getServletContext() {
        // TODO The method was added when Session was made extending HttpSession, implement the method
        throw new UnsupportedOperationException();
    }

    public HttpSessionContext getSessionContext() {
        // TODO Deprecated method that was added when Session was made extending HttpSession, should it be implemented?
        throw new UnsupportedOperationException();
    }

    public Object getValue(String name) {
        // TODO Deprecated method that was added when Session was made extending HttpSession, should it be implemented?
        throw new UnsupportedOperationException();
    }

    public String[] getValueNames() {
        // TODO Deprecated method that was added when Session was made extending HttpSession, should it be implemented?
        throw new UnsupportedOperationException();
    }

    public void putValue(String name, Object value) {
        // TODO Deprecated method that was added when Session was made extending HttpSession, should it be implemented?
        throw new UnsupportedOperationException();
    }

    public void removeValue(String name) {
        // TODO Deprecated method that was added when Session was made extending HttpSession, should it be implemented?
        throw new UnsupportedOperationException();
    }
}
