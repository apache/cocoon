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
package org.apache.cocoon.webapps.authentication.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.excalibur.source.SourceResolver;


/**
 *  This is a utility class managing the authentication handlers
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultHandlerManager.java,v 1.1 2003/03/20 15:27:05 cziegeler Exp $
*/
public final class DefaultHandlerManager {

    /** The name of the session attribute storing the handler configuration */
    private final static String SESSION_ATTRIBUTE_HANDLERS = "org.apache.cocoon.webapps.authentication.Handlers";

    /** Sitemap configuration holder */
    private SitemapConfigurationHolder holder;
    
    /** SourceResolver */
    private SourceResolver resolver;
    
    /** Request */
    private Request request;
    
    /** The handlers of the current user */
    private Map userHandlers;

    /**
     * Constructor
     */
    public DefaultHandlerManager(SitemapConfigurationHolder holder) {
        this.holder = holder;
    }

    public void setup(SourceResolver resolver,
                        Map           objectModel) {
        this.resolver = resolver;
        this.request = ObjectModelHelper.getRequest( objectModel );
    }

    /**
     * Return a list of handlers
     */
    private Map currentHandlers() 
    throws ConfigurationException {
        Map handlers = (Map)this.holder.getPreparedConfiguration();
        if ( null == handlers ) {
            ChainedConfiguration conf = this.holder.getConfiguration();
            if ( null != conf ) {
                handlers = new HashMap(5);
                this.prepare(conf, handlers);
                this.holder.setPreparedConfiguration( conf, handlers );
            }
        }
        return handlers;
    }
    
    /**
     * Prepare the handler configuration
     */
    private void prepare(ChainedConfiguration conf, 
                           Map values) 
    throws ConfigurationException {
        final ChainedConfiguration parent = conf.getParent();
        if ( null != parent ) {
            this.prepare( parent, values );
        }
        // test for handlers
        Configuration handlersWrapper = conf.getChild("handlers", false);
        if ( null != handlersWrapper ) {
            Configuration[] handlers = handlersWrapper.getChildren("handler");
            if ( null != handlers ) {

                for(int i=0; i<handlers.length;i++) {
                    // check unique name
                    final String name = handlers[i].getAttribute("name");
                    if ( null != values.get(name) ) {
                        throw new ConfigurationException("Handler names must be unique: " + name);
                    }

                    this.addHandler( handlers[i], values );
                }
            }
        }
    }

    /**
     * Add one handler configuration
     */
    private void addHandler(Configuration configuration,
                              Map            values)
    throws ConfigurationException {
        // get handler name
        final String name = configuration.getAttribute("name");

        // create handler
        Handler currentHandler = new Handler(name);

        try {
            currentHandler.configure(this.resolver, this.request, configuration);
        } catch (ProcessingException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        } catch (org.xml.sax.SAXException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        } catch (java.io.IOException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        }
        values.put( name, currentHandler );
    }

    /**
     * Clear available handlers
     */
    public void recycle() {
        this.userHandlers = null;
        this.resolver = null;
        this.request = null;
    }

    /**
     * Get the handler of the current user
     */
    public Handler getHandler(String handlerName) 
    throws ConfigurationException {
        if ( null == handlerName) return null;
        if ( null == this.userHandlers) {
            final Session session = this.request.getSession(false);
            if ( null != session) {
                this.userHandlers = (Map)session.getAttribute(SESSION_ATTRIBUTE_HANDLERS);
            }
        }
        Handler handler = null;
        if ( null != this.userHandlers) {
            handler = (Handler)this.userHandlers.get(handlerName);
        }
        if ( null == handler ) {
            handler = (Handler)this.currentHandlers().get(handlerName);
        }
        return handler;
    }

    /**
     * Create a handler copy for the user and return it!
     */
    public Handler storeUserHandler(Handler handler) {
        final Session session = this.request.getSession();
        if ( null == this.userHandlers) {
            this.userHandlers = (Map)session.getAttribute(SESSION_ATTRIBUTE_HANDLERS);
        }
        if ( null == this.userHandlers ) {
            this.userHandlers = new HashMap(3);
        }
        handler = handler.copy();
        this.userHandlers.put(handler.getName(), handler);
        // value did change, update attributes
        session.setAttribute(SESSION_ATTRIBUTE_HANDLERS, this.userHandlers);

        return handler;
    }

    /**
     * Remove from user handler
     */
    public void removeUserHandler(Handler handler) {
        final Session session = this.request.getSession();
        if ( null == this.userHandlers) {
            this.userHandlers = (Map)session.getAttribute(SESSION_ATTRIBUTE_HANDLERS);
        }
        if ( null != this.userHandlers) {
            this.userHandlers.remove( handler.getName() );
            // value did change, update attributes
            session.setAttribute(SESSION_ATTRIBUTE_HANDLERS, this.userHandlers);
        }
    }

    /**
     * Check, if a user handler is available (= is authenticated)
     */
    public boolean hasUserHandler(String name) {
        if ( null == this.userHandlers) {
            final Session session = this.request.getSession(false);
            if ( null != session) {
                this.userHandlers = (Map)session.getAttribute(SESSION_ATTRIBUTE_HANDLERS);
            }
        }
        if ( null != this.userHandlers) {
            return this.userHandlers.containsKey( name );
        }
        return false;
    }
    
    /**
     * Check, if any handler is available
     */
    public boolean hasAnyUserHandler() {
        if ( null == this.userHandlers) {
            final Session session = this.request.getSession(false);
            if ( null != session) {
                this.userHandlers = (Map)session.getAttribute(SESSION_ATTRIBUTE_HANDLERS);
            }
        }
        if ( null != this.userHandlers) {
            return !this.userHandlers.isEmpty();
        }
        return false;
    }
}
