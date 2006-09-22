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
package org.apache.cocoon.webapps.authentication.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.context.AuthenticationContext;

/**
 * The authentication Handler.
 *
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
 */
public final class UserHandler
implements java.io.Serializable {

    /** The corresponding handler */
    private HandlerConfiguration handler;
    
    /** Are all apps loaded? */
    private boolean appsLoaded = false;

    /** The context */
    private AuthenticationContext context;
    
    /** Loaded List */
    private List loadedApps = new ArrayList(3);
    
    /** Application contexts */
    private List applicationContexts;
     
    /** The unique user ID */
    private String userID;
     
    /**
     * Create a new handler object.
     */
    public UserHandler(HandlerConfiguration handler, AuthenticationContext context) {
        this.context = context;
        this.handler = handler;
        this.context.init(this);
    }

    /**
     * Are all application contexts already loaded?
     */
    public boolean getApplicationsLoaded() {
        if ( this.handler.getApplications().isEmpty() ) {
            return true;
        }
        return this.appsLoaded;
    }
    
    /**
     * Add a handler context
     */
    public AuthenticationContext getContext() {
        return this.context;
    }

    /**
     * Get the handler name
     */
    public String getHandlerName() {
        return this.handler.getName();
    }
    
    /**
     * Get the handler configuration
     */
    public HandlerConfiguration getHandlerConfiguration() {
        return this.handler;
    }
    
    /**
     * Is the named application context already loaded?
     */
    public boolean isApplicationLoaded(ApplicationConfiguration appConf) {
        return this.loadedApps.contains( appConf );
    }
    
    /**
     * Notify that the application context has been loaded
     */
    public void setApplicationIsLoaded(ApplicationConfiguration appConf) {
        this.loadedApps.add( appConf );
        this.appsLoaded = (this.loadedApps.size() == this.handler.getApplications().size());
    }
    
    /**
     * Get the unique user id
     */
    public String getUserId() {
        if ( null == this.userID) {
            try {
                this.userID = (String) this.context.getContextInfo().get("ID");
            } catch (ProcessingException ignore) {
                this.userID = "";
            }
        }
        return this.userID;
    }
    
    /**
     * Test if the user has a role
     * @since 2.1.6
     */
    public boolean isUserInRole(String role) {
        return this.context.isUserInRole(role);    
    }
    
    public void addApplicationContext(String name) {
        if ( this.applicationContexts == null) {
            this.applicationContexts = new ArrayList(3);
        }
        this.applicationContexts.add( name );
    }
    
    /**
     * Return the list or null.
     */
    public List getApplicationContexts() {
        return this.applicationContexts;
    }
}
