/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.webapps.session.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;
import org.apache.cocoon.webapps.session.context.SimpleSessionContext;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.xml.sax.SAXException;

/**
 * Context manager
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultContextManager.java,v 1.6 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public final class DefaultContextManager
extends AbstractLogEnabled
implements Serviceable, ContextManager, ThreadSafe, Component, Contextualizable, Disposable {

    /** The <code>ServiceManager</code> */
    private ServiceManager manager;

    /** The context */
    private Context context;
    
    /** selector for context provider */
    private ServiceSelector contextSelector;
    
    /** The xpath processor */
    private XPathProcessor xpathProcessor;
    
    /* The list of reserved contexts */
    static private final String[] reservedContextNames = {"session",
                                                            "context"};
    /**
     * Avalon Serviceable Interface
     */
    public void service(ServiceManager manager) 
    throws ServiceException {
        this.manager = manager;
        this.contextSelector = (ServiceSelector)this.manager.lookup(SessionContextProvider.ROLE+"Selector");
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /**
     * Get the session
     */
    private Session getSession(boolean create) {
        final Request request = ContextHelper.getRequest( this.context );
        return request.getSession( create );
    }
    
    /**
     * Get the list of contexts
     */
    private Map getSessionContexts(Session session) {
        Map contexts;
        contexts = (Map)session.getAttribute(SessionContext.class.getName());
        if (contexts == null) {
            contexts = new HashMap(5, 3);
            session.setAttribute(SessionContext.class.getName(), contexts);
        }
        return contexts;
    }

    /**
     * Checks if the context name is a reserved context.
     */
    private boolean isReservedContextName(String name) {
        // synchronized (not needed)
        int     i, l;
        boolean found;
        found = false;
        i = 0;
        l = reservedContextNames.length;
        while (i < l && found == false) {
            found = reservedContextNames[i].equals(name);
            i++;
        }
        if (!found ) {
            found = false;
            SessionContextProvider provider = null;
            try {
                provider = (SessionContextProvider)this.contextSelector.select( name );
                found = true;
            } catch (ServiceException ignore) {
            } finally {
                this.contextSelector.release(provider);
            }
        }
        return found;
    }

    /**
     * Get a reserved context
     */
    private boolean existsReservedContext(String name) 
    throws ProcessingException {
        // synchronized (not needed)
        boolean exists = false;
        SessionContextProvider provider = null;
        try {
            provider = (SessionContextProvider)this.contextSelector.select( name );
            exists = provider.existsSessionContext( name );
        } catch (ServiceException ignore) {
        } finally {
            this.contextSelector.release(provider);
        }

        return exists;
    }

    /**
     * Get a reserved context
     */
    private SessionContext getReservedContext(String name)
    throws ProcessingException {
        // synchronized 
        SessionContext context = null;
        SessionContextProvider provider = null;
        try {
            provider = (SessionContextProvider)this.contextSelector.select( name );
            synchronized (provider) {
                context = provider.getSessionContext(name);
            }
        } catch (ServiceException ignore) {
        } finally {
            this.contextSelector.release(provider);
        }

        return context;
    }

    /**
     *  Create a new public context in the session.
     *  Create a new public session context for this user. If this context
     *  already exists no new context is created and the old one will be used
     *  instead.
     */
    public SessionContext createContext(String name, String loadURI, String saveURI)
    throws IOException, SAXException, ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN createContext name=" + name +
                                   "load=" + loadURI +
                                   "save=" + saveURI);
        }
        // test arguments
        if (name == null) {
            throw new ProcessingException("CreateContext: Name is required");
        }
        Session session = this.getSession(true);
        if (session == null) {
            throw new ProcessingException("CreateContext: Session is required");
        }

        SessionContext context;
        synchronized(session) {
            // test for reserved context
            if (this.isReservedContextName(name)) {
                throw new ProcessingException("SessionContext with name " + name + " is reserved and cannot be created manually.");
            }

            if (this.existsContext(name)) {
                context = this.getContext(name);
            } else {
                Map contexts = this.getSessionContexts(session);
                context = new SimpleSessionContext(this.xpathProcessor);
                context.setup(name, loadURI, saveURI);
                contexts.put(name, context);
            }
        }
        
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END createContext context="+context);
        }

        return context;
    }

    /**
     *  Delete a public context in the session.
     *  If the context exists for this user, it and all of its information
     *  is deleted.
     */
    public void deleteContext(String name)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN deleteContext name=" + name);
        }

        // test arguments
        if (name == null) {
            throw new ProcessingException("SessionManager.deleteContext: Name is required");
        }
        if (this.isReservedContextName(name)) {
            throw new ProcessingException("SessionContext with name " + name + " is reserved and cannot be deleted manually.");
        }
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.deleteContext: Session is required");
        }

        synchronized(session) {
            final Map contexts = this.getSessionContexts(session);
            if (contexts.containsKey(name)) {
                contexts.remove(name);
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END deleteContext");
        }
    }

    /**
     *  Get a public context.
     *  The session context with the given name is returned. If the context does
     *  not exist <CODE>null</CODE> is returned.
     */
    public SessionContext getContext(String name)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN getContext name=" + name);
        }

        SessionContext context;
        if (this.isReservedContextName(name) ) {
            context = this.getReservedContext(name);
        } else {
            Session session = this.getSession(false);
            if ( session != null) {
                synchronized (session) {
                    final Map contexts = this.getSessionContexts( session );
                    context = (SessionContext)contexts.get(name);
                }
            } else {
                context = null;
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END getContext context=" + context);
        }

        return context;
    }

    /**
     * Check if a context exists
     */
    public boolean hasSessionContext() 
    throws ProcessingException {
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.hasSessionContext: Session is required.");
        }
        synchronized (session) {
            final Map contexts = this.getSessionContexts(session);
            return !(contexts.isEmpty());
        }
    }

    /**
     *  Check if a public context exists.
     *  If the session context with the given name exists, <CODE>true</CODE> is
     *  returned.
     */
    public boolean existsContext(String name) 
    throws ProcessingException {
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.existsContext: Session is required.");
        }
        synchronized (session) {
            final Map contexts = this.getSessionContexts(session);
            boolean result = contexts.containsKey(name);
            if (!result && this.isReservedContextName(name) ) {
                result = this.existsReservedContext(name);
            }
            return result;
        }
    }


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null) {
            this.manager.release( this.contextSelector );
            this.manager.release( this.xpathProcessor );
            this.contextSelector = null;
            this.xpathProcessor = null;
            this.manager = null;            
        }
    }

}
