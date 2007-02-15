/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cocoon.auth.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.auth.Application;
import org.apache.cocoon.auth.ApplicationStore;
import org.apache.cocoon.auth.SecurityHandler;
import org.apache.cocoon.auth.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the default implementation for an {@link Application}.
 *
 * @version $Id$
*/
public class StandardApplication
    implements Application {

    /** This prefix is used to lookup security handlers. */
    protected static final String HANDLER_CONFIG_PREFIX =
                                        SecurityHandler.class.getName() + '/';
    /** This prefix is used to lookup application stores. */
    protected static final String STORE_CONFIG_PREFIX =
                                        ApplicationStore.class.getName() + '/';

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /** The security handler. */
    protected SecurityHandler handler;

    /** Attributes. */
    protected Map attributes = Collections.synchronizedMap(new HashMap());

    /** Application store. */
    protected ApplicationStore store;

    public void setSecurityHandler(SecurityHandler h) {
        this.handler = h;
    }

    public void setApplicationStore(ApplicationStore s) {
        this.store = s;
    }

    public void setAttributes(Map map) {
        this.attributes = map;
    }

    /**
     * @see org.apache.cocoon.auth.Application#getSecurityHandler()
     */
    public SecurityHandler getSecurityHandler() {
        return this.handler;
    }

    /**
     * @see org.apache.cocoon.auth.Application#getApplicationStore()
     */
    public ApplicationStore getApplicationStore() {
        return this.store;
    }

    /**
     * @see org.apache.cocoon.auth.Application#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(final String key, final Object value) {
        this.attributes.put(key, value);
    }

    /**
     * @see org.apache.cocoon.auth.Application#removeAttribute(java.lang.String)
     */
    public void removeAttribute(final String key) {
        this.attributes.remove(key);
    }

    /**
     * @see org.apache.cocoon.auth.Application#getAttribute(java.lang.String)
     */
    public Object getAttribute(final String key) {
        return this.attributes.get(key);
    }

    /**
     * @see org.apache.cocoon.auth.Application#userDidLogin(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userDidLogin(final User user, final Map context) {
        // nothing to do here
    }

    /**
     * @see org.apache.cocoon.auth.Application#userWillLogout(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userWillLogout(final User user, final Map context) {
        // nothing to do here
    }

    /**
     * @see org.apache.cocoon.auth.Application#userIsAccessing(org.apache.cocoon.auth.User)
     */
    public void userIsAccessing(final User user) {
        // nothing to do here
    }
}
