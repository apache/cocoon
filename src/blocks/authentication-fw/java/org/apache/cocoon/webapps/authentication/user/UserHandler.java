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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.context.AuthenticationContext;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.xml.sax.SAXException;

/**
 * The authentication Handler.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: UserHandler.java,v 1.6 2003/05/04 20:43:23 cziegeler Exp $
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
    public UserHandler(HandlerConfiguration handler) {
        this.handler = handler;
        this.context = new AuthenticationContext(this);
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
     * Create Application Context.
     * This context is destroyed when the user logs out of the handler
     */
    public synchronized SessionContext createApplicationContext(String name,
                                                                  String loadURI,
                                                                 String saveURI)
    throws ProcessingException {

        SessionContext context = null;

        ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();
        ContextManager contextManager = null;
        try {
            contextManager = (ContextManager)manager.lookup(ContextManager.ROLE);
            // create new context
            context = contextManager.createContext(name, loadURI, saveURI);
            if ( this.applicationContexts == null) {
                this.applicationContexts = new ArrayList(3);
            }
            this.applicationContexts.add( name );

        } catch (ComponentException ce) {
            throw new ProcessingException("Unable to create session context.", ce);
        } catch (IOException ioe) {
            throw new ProcessingException("Unable to create session context.", ioe);
        } catch (SAXException saxe) {
            throw new ProcessingException("Unable to create session context.", saxe);
        } finally {
            manager.release( (Component)contextManager);
        }

        return context;
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
     * Terminate the handler
     */
    public void terminate() 
    throws ProcessingException {
        ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();

        if ( this.applicationContexts != null ) {
            ContextManager contextManager = null;

            try {
                contextManager = (ContextManager)manager.lookup(ContextManager.ROLE);

                Iterator i = this.applicationContexts.iterator();
                while ( i.hasNext() ) {
                    final String current = (String)i.next();
                    contextManager.deleteContext( current );
                }
            } catch (ComponentException ce) {
                throw new ProcessingException("Unable to create session context.", ce);
            } finally {
                manager.release( (Component)contextManager);
            }
        }
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
}
