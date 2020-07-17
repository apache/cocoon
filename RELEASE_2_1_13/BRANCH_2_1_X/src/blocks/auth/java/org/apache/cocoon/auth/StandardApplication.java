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
package org.apache.cocoon.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.auth.impl.AnonymousSecurityHandler;

/**
 * This is the default implementation for an {@link Application}.
 *
 * @version $Id$
*/
public class StandardApplication
    extends AbstractLogEnabled
    implements Application, Configurable, Serviceable, Disposable, ThreadSafe {

    /** This prefix is used to lookup security handlers. */
    protected static final String HANDLER_CONFIG_PREFIX =
                                        SecurityHandler.class.getName() + '/';
    /** This prefix is used to lookup application stores. */
    protected static final String STORE_CONFIG_PREFIX =
                                        ApplicationStore.class.getName() + '/';

    /** The service manager. */
    protected ServiceManager manager;

    /** The security handler. */
    protected SecurityHandler handler;

    /** Attributes. */
    protected final Map attributes = new HashMap();

    /** Application store. */
    protected ApplicationStore store;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration conf)
    throws ConfigurationException {
        String handlerName = conf.getAttribute("security-handler", null);
        String storeName = conf.getAttribute("store", null);
        try {
            if ( handlerName == null ) {
                this.handler = new AnonymousSecurityHandler();
            } else {
                if ( !handlerName.startsWith(HANDLER_CONFIG_PREFIX) ) {
                    handlerName = HANDLER_CONFIG_PREFIX + handlerName;
                }
                this.handler = (SecurityHandler)this.manager.lookup(handlerName);
            }
            if ( storeName != null ) {
                if ( !storeName.startsWith(STORE_CONFIG_PREFIX) ) {
                    storeName = STORE_CONFIG_PREFIX + storeName;
                }
                this.store = (ApplicationStore)this.manager.lookup(storeName);
            }
        } catch (ServiceException se) {
            throw new ConfigurationException("Unable to look up component.", se);
        }
        this.configureAttributes(conf);
    }

    /**
     * This method is invoked during configuration of the application. The
     * default behaviour is to add all children of the configuration object
     * as key value pairs. The name of the child is the key, and the value
     * of the tag is the value (as a string).
     * Subclasses can override this method, if a different/additional
     * behaviour is wanted.
     * @param conf The application configuration.
     */
    protected void configureAttributes(final Configuration conf) {
        Configuration[] children = conf.getChildren();
        for(int i=0; i<children.length; i++) {
            final String name = children[i].getName();
            final String value = children[i].getValue(null);
            if ( value != null && value.trim().length() > 0 ) {
                this.setAttribute(name, value.trim());
            }
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null) {
            this.manager.release(this.store);
            if ( !(this.handler instanceof AnonymousSecurityHandler) ) {
                this.manager.release(this.handler);
            }
            this.store = null;
            this.handler = null;
            this.manager = null;
        }
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
