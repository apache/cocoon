/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: UserHandler.java,v 1.9 2003/07/01 19:26:40 cziegeler Exp $
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
    public boolean getApplicationsLoaded()
    throws ProcessingException {
        if ( this.handler.getApplications().isEmpty() ) {
            return true;
        } else {
            return this.appsLoaded;
        }
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
