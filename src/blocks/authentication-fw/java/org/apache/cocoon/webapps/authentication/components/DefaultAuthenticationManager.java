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

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceUtil;
import org.w3c.dom.DocumentFragment;

/**
 * This is the basis authentication component.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultAuthenticationManager.java,v 1.4 2003/04/21 19:26:14 cziegeler Exp $
*/
public final class DefaultAuthenticationManager
extends AbstractLogEnabled
implements Manager, SitemapConfigurable, Serviceable, ThreadSafe {

    /** The name of the session attribute storing the user status */
    public final static String SESSION_ATTRIBUTE_USER_STATUS = DefaultAuthenticationManager.class.getName() + "/UserStatus";

    /** The manager for the authentication handlers */
    private SitemapConfigurationHolder holder;
    
    /** The Service Manager */
    private ServiceManager manager;
    
    /**
     * Set the sitemap configuration containing the handlers
     */
    public void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException {
        this.holder = holder;
    }

    /**
     * Get the handler configuration for the current sitemap
     */
    private Map getHandlerConfigurations() 
    throws ProcessingException {
        Map configs = (Map) this.holder.getPreparedConfiguration();
        if ( null == configs ) {
            // prepare the configs
            SourceResolver resolver = null;
            try {       
                resolver = (SourceResolver) this.manager.lookup( SourceResolver.ROLE );
                configs = DefaultHandlerManager.prepareHandlerConfiguration(resolver, 
                                                                            CocoonComponentManager.getCurrentEnvironment().getObjectModel(), 
                                                                            this.holder);
            } catch (ServiceException se) {
                throw new ProcessingException("Unable to lookup source resolver.", se);
            } catch (ConfigurationException ce) {
                throw new ProcessingException("Configuration error.", ce);
            } finally {
                this.manager.release( resolver );
            }
        }
        return configs;
    }

    /**
     * Get the handler configuration
     * @param name
     * @return
     */
    private HandlerConfiguration getHandlerConfiguration(String name) 
    throws ProcessingException {   
        final Map configs = this.getHandlerConfigurations();
        HandlerConfiguration c = null;
        if ( configs != null) {
            c = (HandlerConfiguration)configs.get( name ); 
        }
        return c;
    }
    
    private Request getRequest() {
        final Map objectModel = CocoonComponentManager.getCurrentEnvironment().getObjectModel();
        return ObjectModelHelper.getRequest( objectModel );
    }
    
    private Session getSession(boolean create) {        
        return this.getRequest().getSession(create);
    }
    
    private UserStatus getUserStatus() {
        final Session session = this.getSession( false );
        UserStatus status = null;
        if ( session != null) {
            status = (UserStatus) session.getAttribute(SESSION_ATTRIBUTE_USER_STATUS);
        }
        return status;
    }

    private UserStatus createUserStatus() {
        UserStatus status = this.getUserStatus();
        if ( status == null ) {
            final Session session = this.getSession(true);
            status = new UserStatus();
            session.setAttribute(SESSION_ATTRIBUTE_USER_STATUS, status);
        }
        return status;
    }
    
    private UserHandler getUserHandler(String name) {
        final UserStatus status = this.getUserStatus();
        if ( status != null ) {
            return status.getUserHandler( name );
        }
        return null;
    }
    
    private void updateUserStatus() {
        final Session session = this.getSession(true);
        Object status = session.getAttribute(SESSION_ATTRIBUTE_USER_STATUS);
        session.setAttribute(SESSION_ATTRIBUTE_USER_STATUS, status);
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#authenticate(java.lang.String, java.lang.String, org.apache.excalibur.source.SourceParameters)
	 */
	public DocumentFragment authenticate(String handlerName,
                                          String applicationName,
                                          SourceParameters parameters)
    throws ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to authenticate: " + handlerName);
        }
        // are we already logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        if ( handler != null ) {
            throw new ProcessingException("User is already authenticated using handler: " + handlerName);
        }
        
        // TODO Authentication
        
        // create UserStatus
        final UserStatus status = this.createUserStatus();
        handler = new UserHandler( config );
        
        status.addHandler( handler );        
        this.updateUserStatus();
        
 		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#checkAuthentication(org.apache.cocoon.environment.Redirector, java.lang.String, java.lang.String)
	 */
	public boolean checkAuthentication(Redirector redirector,
                                        String handlerName,
                                        String applicationName)
	throws IOException, ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to check: " + handlerName);
        }
        // are we already logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        final boolean authenticated = ( handler != null );
        if ( !authenticated ) {
            // create parameters
            SourceParameters parameters = config.getRedirectParameters();
            if (parameters == null) parameters = new SourceParameters();
            final Request request = this.getRequest();
            String resource = request.getRequestURI();
            if (request.getQueryString() != null) {
                resource += '?' + request.getQueryString();
            }

            parameters.setSingleParameterValue("resource", resource);
            final String redirectURI = config.getRedirectURI();
            redirector.globalRedirect(false, SourceUtil.appendParameters(redirectURI, parameters));
        }
        
		return authenticated;
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#isAuthenticated(java.lang.String)
	 */
	public boolean isAuthenticated(String handlerName)
    throws ProcessingException {
        return ( this.getUserHandler( handlerName ) != null);
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.webapps.authentication.components.Manager#logout(java.lang.String, java.lang.String)
	 */
	public void logout(String handlerName, int mode) 
    throws ProcessingException {
        HandlerConfiguration config = this.getHandlerConfiguration( handlerName );
        if ( config == null ) {
            throw new ProcessingException("Unknown handler to logout: " + handlerName);
        }
        // are we logged in?
        UserHandler handler = this.getUserHandler( handlerName );
        // we don't throw an exception if we are already logged out!
        if ( handler != null ) {
            UserStatus status = this.getUserStatus();
            status.removeHandler( handlerName );
            this.updateUserStatus();
        }
        
        /*
        if ( mode == AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY ) {
            this.getSessionManager().terminateSession(true);
        } else if (!this.handlerManager.hasUserHandler( this.request )) {
            if (mode == AuthenticationConstants.LOGOUT_MODE_IF_UNUSED) {
                this.getSessionManager().terminateSession(false);
            } else {
                this.getSessionManager().terminateSession(true);
            }
        }
        */
	}

	/**
     * Serviceable
	 */
	public void service(ServiceManager manager) 
    throws ServiceException {
        this.manager = manager;
	}

}


