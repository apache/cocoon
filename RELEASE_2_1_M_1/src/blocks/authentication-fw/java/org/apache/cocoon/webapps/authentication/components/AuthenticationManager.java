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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.context.SessionContextImpl;
import org.apache.cocoon.webapps.authentication.context.SessionContextProviderImpl;
import org.apache.cocoon.webapps.session.components.AbstractSessionComponent;
import org.apache.cocoon.webapps.session.components.SessionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SimpleSessionContext;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceUtil;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This is the basis authentication component.
 * This is not a true Avalon component as for example this component is interface
 * and implementation at the same time.
 * But using Avalon allows offers some required features that are used here.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AuthenticationManager.java,v 1.4 2003/03/20 15:27:05 cziegeler Exp $
*/
public final class AuthenticationManager
extends AbstractSessionComponent
implements Configurable, SitemapConfigurable, RequestLifecycleComponent {

    /** The Avalon Role */
    public static final String ROLE = AuthenticationManager.class.getName();

    /** The handler manager */
    private HandlerManager handlerManager = new HandlerManager();

    /** The application name of the current pipeline */
    private String applicationName;

    /** The application */
    private ApplicationHandler application;

    /** The handler name of the current pipeline */
    private String handlerName;

    /** The handler of the current pipeline */
    private Handler handler;

    /** The media Types */
    private MediaType[] allMediaTypes;
    private String      defaultMediaType;
    private String[]    mediaTypeNames;

    /** media type */
    private String mediaType;

    /** The context provider */
    private static SessionContextProviderImpl contextProvider;

    /** Init the class,
     *  add the provider for the authentication context
     */
    static {
        // add the provider for the authentication context
        contextProvider = new SessionContextProviderImpl();
        try {
            SessionManager.addSessionContextProvider(contextProvider, AuthenticationConstants.SESSION_CONTEXT_NAME);
        } catch (ProcessingException local) {
            throw new CascadingRuntimeException("Unable to register provider for authentication context.", local);
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        super.recycle();
        this.applicationName = null;
        this.application = null;
        this.handler = null;
        this.handlerName = null;

        // clear handlers
        this.handlerManager.clearAvailableHandlers();
    }

    /**
     * Set the <code>Configuration</code>.
     */
    public void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException {
        this.handlerManager.addConfiguration( holder.getConfiguration(), this.resolver, this.request );
        this.handlerManager.addAvailableHandlers( holder.getConfiguration() );
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * used to process the request.
     *  This method is automatically called for each request. Do not invoke
     *  this method by hand.
     */
    public void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel);

        // get the media of the current request
        String useragent = request.getHeader("User-Agent");
        MediaType media = null;
        if (useragent != null) {
            int i, l;
            i = 0;
            l = this.allMediaTypes.length;
            while (i < l && media == null) {
                if (useragent.indexOf(this.allMediaTypes[i].useragent) == -1) {
                    i++;
                } else {
                    media = this.allMediaTypes[i];
                }
            }
        }
        this.mediaType = (media == null ? this.defaultMediaType : media.name);
    }

    /**
     * Configurable interface.
     */
    public void configure(Configuration myConfiguration)
    throws ConfigurationException {
        // no sync required
        Configuration mediaConf = myConfiguration.getChild("mediatypes", false);
        if (mediaConf == null) {
            // default configuration
            this.defaultMediaType = "html";
        } else {
            this.defaultMediaType = mediaConf.getAttribute("default", "html");
        }
        this.mediaTypeNames = new String[1];
        this.mediaTypeNames[0] = this.defaultMediaType;
        boolean found;
        int     i;
        String  name;

        Configuration[] childs = mediaConf.getChildren("media");
        MediaType[] array = new MediaType[0];
        MediaType[] copy;
        Configuration current;
        if (childs != null) {
            for(int x = 0; x < childs.length; x++) {
                current = childs[x];
                copy = new MediaType[array.length + 1];
                System.arraycopy(array, 0, copy, 0, array.length);
                array = copy;
                name = current.getAttribute("name");
                array[array.length-1] = new MediaType(name, current.getAttribute("useragent"));
                found = false;
                i = 0;
                while ( i < this.mediaTypeNames.length && found == false) {
                    found = this.mediaTypeNames[i].equals(name);
                    i++;
                }
                if (found == false) {
                    String[] newStrings = new String[this.mediaTypeNames.length + 1];
                    System.arraycopy(this.mediaTypeNames, 0, newStrings, 0, this.mediaTypeNames.length);
                    newStrings[newStrings.length-1] = name;
                    this.mediaTypeNames = newStrings;
                }
            }
        }
        this.allMediaTypes = array;
    }

    /**
     * Test if the media of the current request is the given value
     */
    public boolean testMedia(Map objectModel, String value) {
        // synchronized
        Request req = ObjectModelHelper.getRequest( objectModel );
        boolean result = false;

        if (req != null) {
            String useragent = request.getHeader("User-Agent");
            MediaType theMedia = null;
            int i, l;
            i = 0;
            l = this.allMediaTypes.length;
            while (i < l && theMedia == null) {
                if (useragent.indexOf(this.allMediaTypes[i].useragent) == -1) {
                    i++;
                } else {
                    theMedia = this.allMediaTypes[i];
                }
            }
            if (theMedia != null) {
                result = theMedia.name.equals(value);
            } else {
                result = this.defaultMediaType.equals(value);
            }
        }
        return result;
    }

    public String[] getMediaTypes() {
        // synchronized
        return this.mediaTypeNames;
    }

    /**
     * Return the current media type
     */
    public String getMediaType() {
        // synchronized
        return this.mediaType;
    }

    /**
     * Return the current handler
     */
    public Handler getHandler() {
        return this.handler;
    }

    /**
     * Return the current handler name
     */
    public String getHandlerName() {
        return this.handlerName;
    }

    /**
     * Return the name of the current application
     */
    public String getApplicationName() {
        return this.applicationName;
    }
    
    /**
     * Is the current user authenticated for the given handler?
     */
    public boolean isAuthenticated()
    throws IOException, ProcessingException {
        return this.isAuthenticated(this.handlerName);
    }

    /**
     * Is the current user authenticated for the given handler?
     */
    public boolean isAuthenticated(String name)
    throws IOException, ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN isAuthenticated handler=" + name);
        }
        boolean isAuthenticated = true;

        // if no handler: authenticated
        if (name != null) {
            isAuthenticated = this.handlerManager.hasUserHandler( name, this.request );
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END isAuthenticated authenticated=" + isAuthenticated);
        }
        return isAuthenticated;
    }

    /**
     * Checks authentication and generates a redirect, if not authenticated
     */
    public boolean checkAuthentication(Redirector redirector, 
                                         final String newHandlerName,
                                         final String newAppName)
    throws IOException, ProcessingException {
        // synchronized not needed
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN checkAuthentication");
        }
        boolean isAuthenticated = true;

        // set the configuration for the handler
        if (this.handlerName == null) this.handlerName = "";
        if (this.applicationName == null) this.applicationName = "";
        if (this.handlerName.equals(newHandlerName) == false
            || this.applicationName.equals(newAppName) == false) {
            this.handlerName = newHandlerName;
            this.applicationName = newAppName;
            this.handler = null;
            this.application = null;

            if (this.handlerName != null) {
                this.handler = this.getHandler(this.handlerName);

                if (this.handler == null) {
                    throw new ProcessingException("Handler not found: " + this.handlerName);
                }
                if (this.applicationName != null) {
                    this.application = (ApplicationHandler)this.handler.getApplications().get(this.applicationName);
                    if (this.application == null) {
                        throw new ProcessingException("Application not found: " + this.applicationName);
                    }
                }
            } else {
                throw new ProcessingException("Handler information not found.");
            }
        } else {
            if (this.handlerName.equals("")) this.handlerName = null;
            if (this.applicationName.equals("")) this.applicationName = null;
        }

        if (this.handler != null) {
            isAuthenticated = this.isAuthenticated(this.handlerName);

            if (isAuthenticated == false) {
                // create parameters
                SourceParameters parameters = handler.getRedirectParameters();
                if (parameters == null) parameters = new SourceParameters();
                String resource = this.request.getRequestURI();
                if (this.request.getQueryString() != null) {
                    resource += '?' + this.request.getQueryString();
                }

                parameters.setSingleParameterValue("resource", resource);
                final String redirectURI = handler.getRedirectURI();
                redirector.globalRedirect(false, SourceUtil.appendParameters(redirectURI, parameters));
            } else {
                // load application data if we are not inside a resource loading of authentication
                this.checkLoaded((SessionContextImpl)this.getSessionManager().getContext(AuthenticationConstants.SESSION_CONTEXT_NAME),
                                     "/");
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END checkAuthentication authenticated=" + isAuthenticated);
        }
        return isAuthenticated;
    }

    /**
     * Get the handler
     */
    private Handler getHandler(String name) {
        // synchronized
        return this.handlerManager.getHandler( name, this.request);
    }

    /**
     * Authenticate
     * If the authentication is successful, <code>null</code> is returned.
     * If not an element "failed" is return. If handler specific error
     * information is available this is also returned.
     */
    public DocumentFragment authenticate(String              loginHandlerName,
                                         SourceParameters    parameters)
    throws ProcessingException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN authenticate handler=" + loginHandlerName +
                                   ", parameters="+parameters);
        }

        DocumentFragment authenticationFragment = null;
        boolean         isValid                = false;

        Handler myHandler = this.getHandler(loginHandlerName);
        if (this.getLogger().isInfoEnabled() == true) {
            this.getLogger().info("AuthenticationManager: Trying to authenticate using handler '" + loginHandlerName +"'");
        }
        if (myHandler != null) {
            String           exceptionMsg     = null;

            if (this.getLogger().isDebugEnabled() == true) {
                this.getLogger().debug("start authentication");
            }

            final String   authenticationResourceName = myHandler.getAuthenticationResource();
            final SourceParameters authenticationParameters = myHandler.getAuthenticationResourceParameters();
            if (parameters != null) {
                parameters.add(authenticationParameters);
            } else {
                parameters = authenticationParameters;
            }

            try {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("start invoking auth resource");
                }
                Source source = null;
                try {
                    source = org.apache.cocoon.components.source.SourceUtil.getSource(authenticationResourceName, 
                                                                                      null, 
                                                                                      parameters, 
                                                                                      this.resolver);
                    
                    Document doc = org.apache.cocoon.components.source.SourceUtil.toDOM(source);
                    authenticationFragment = doc.createDocumentFragment();
                    authenticationFragment.appendChild(doc.getDocumentElement());
                } catch (SAXException se) {
                    throw new ProcessingException(se);
                } catch (SourceException se) {
                    throw org.apache.cocoon.components.source.SourceUtil.handle(se);
                } finally {
                    this.resolver.release(source);
                }

                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("end invoking auth resource");
                }
            } catch (ProcessingException local) {
                this.getLogger().error("authenticate", local);
                exceptionMsg = local.getMessage();
            }

            // test if authentication was successful
            if (authenticationFragment != null) {
                isValid = this.isValidAuthenticationFragment(authenticationFragment);

                if (isValid == true) {
                    if (this.getLogger().isInfoEnabled() == true) {
                        this.getLogger().info("AuthenticationManager: User authenticated using handler '" + myHandler.getName()+"'");
                    }
                    // create session object if necessary, context etc and get it
                    if (this.getLogger().isDebugEnabled() == true) {
                        this.getLogger().debug("creating session");
                    }
                    SessionContext context = this.getAuthenticationSessionContext(true);
                    if (this.getLogger().isDebugEnabled() == true) {
                        this.getLogger().debug("session created");
                    }

                    myHandler = this.handlerManager.storeUserHandler(myHandler,
                                                                     this.request);

                    synchronized(context) {
                        // add special nodes to the authentication block:
                        // useragent, type and media
                        Element specialElement;
                        Text    specialValue;
                        Element authNode;

                        authNode = (Element)authenticationFragment.getFirstChild();
                        specialElement = authenticationFragment.getOwnerDocument().createElementNS(null, "useragent");
                        specialValue = authenticationFragment.getOwnerDocument().createTextNode(request.getHeader("User-Agent"));
                        specialElement.appendChild(specialValue);
                        authNode.appendChild(specialElement);

                        specialElement = authenticationFragment.getOwnerDocument().createElementNS(null, "type");
                        specialValue = authenticationFragment.getOwnerDocument().createTextNode("cocoon.authentication");
                        specialElement.appendChild(specialValue);
                        authNode.appendChild(specialElement);

                        specialElement = authenticationFragment.getOwnerDocument().createElementNS(null, "media");
                        specialValue = authenticationFragment.getOwnerDocument().createTextNode(this.mediaType);
                        specialElement.appendChild(specialValue);
                        authNode.appendChild(specialElement);

                        // store the authentication data in the context
                        context.setXML("/" + myHandler.getName(), authenticationFragment);

                        // Now create the return value for this method:
                        // <code>null</code>
                        authenticationFragment = null;

                        // And now load applications
                        boolean loaded = true;
                        Iterator applications = myHandler.getApplications().values().iterator();
                        ApplicationHandler appHandler;

                        while (applications.hasNext() == true) {
                            appHandler = (ApplicationHandler)applications.next();
                            if (appHandler.getLoadOnDemand() == false) {
                                this.loadApplicationXML((SessionContextImpl)this.getSessionManager().getContext(AuthenticationConstants.SESSION_CONTEXT_NAME),
                                                        appHandler, "/");
                            } else {
                                loaded = appHandler.getIsLoaded();
                            }
                        }
                        myHandler.setApplicationsLoaded(loaded);

                    } // end sync
                }
            }
            if (isValid == false) {
                if (this.getLogger().isInfoEnabled() == true) {
                    this.getLogger().info("AuthenticationManager: Failed authentication using handler '" +  myHandler.getName()+"'");
                }
                // get the /authentication/data Node if available
                Node data = null;

                if (authenticationFragment != null) {
                    data = DOMUtil.getFirstNodeFromPath(authenticationFragment, new String[] {"authentication","data"}, false);
                }

                // now create the following xml:
                // <failed/>
                // if data is available data is included, otherwise:
                // <data>No information</data>
                // If exception message contains info, it is included into failed
                Document       doc = DOMUtil.createDocument();
                authenticationFragment = doc.createDocumentFragment();

                Element      element = doc.createElementNS(null, "failed");
                authenticationFragment.appendChild(element);

                if (exceptionMsg != null) {
                    Text text = doc.createTextNode(exceptionMsg);
                    element.appendChild(text);
                }

                if (data == null) {
                    element = doc.createElementNS(null, "data");
                    authenticationFragment.appendChild(element);
                    Text text = doc.createTextNode("No information");
                    element.appendChild(text);
                } else {
                    authenticationFragment.appendChild(doc.importNode(data, true));
                }

            }
            if (this.getLogger().isDebugEnabled() == true) {
                this.getLogger().debug("end authentication");
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END authenticate fragment="+authenticationFragment);
        }
        return authenticationFragment;
    }

    /**
     * Check the fragment if it is valid
     */
    private boolean isValidAuthenticationFragment(DocumentFragment authenticationFragment) 
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN isValidAuthenticationFragment fragment=" + XMLUtils.serializeNodeToXML(authenticationFragment));
        }
        boolean isValid = false;

        // authenticationFragment must only have exactly one child with
        // the name authentication
        if (authenticationFragment.hasChildNodes() == true
            && authenticationFragment.getChildNodes().getLength() == 1) {
            Node child = authenticationFragment.getFirstChild();

            if (child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals("authentication") == true) {

                // now authentication must have one child ID
                if (child.hasChildNodes() == true) {
                    NodeList children = child.getChildNodes();
                    boolean  found = false;
                    int      i = 0;
                    int      l = children.getLength();

                    while (found == false && i < l) {
                        child = children.item(i);
                        if (child.getNodeType() == Node.ELEMENT_NODE
                            && child.getNodeName().equals("ID") == true) {
                            found = true;
                        } else {
                            i++;
                        }
                    }

                    // now the last check: ID must have a TEXT child
                    if (found == true) {
                        child.normalize(); // join text nodes
                        if (child.hasChildNodes() == true &&
                            child.getChildNodes().getLength() == 1 &&
                            child.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                            String value = child.getChildNodes().item(0).getNodeValue().trim();
                            if (value.length() > 0) isValid = true;
                        }
                    }
                }

            }
        }
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END isValidAuthenticationFragment valid="+isValid);
        }
        return isValid;
    }

    /**
     * Get the private SessionContext
     */
    private SessionContext getAuthenticationSessionContext(boolean create)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getAuthenticationSessionContext create=" + create);
        }
        SessionContext context = null;

        Session session = this.getSessionManager().getSession(create);
        if (session != null) {
            synchronized(session) {
                context = (SessionContext)session.getAttribute(AuthenticationConstants.SESSION_ATTRIBUTE_CONTEXT_NAME);
                if (context == null && create == true) {
                    context = new SimpleSessionContext();
                    context.setup(AuthenticationConstants.SESSION_CONTEXT_NAME, null, null);
                    session.setAttribute(AuthenticationConstants.SESSION_ATTRIBUTE_CONTEXT_NAME, context);
                }
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END getAuthenticationSessionContext context=" + context);
        }
        return context;
    }

    /**
     * Logout from the given handler and eventually terminate session.
     * @param logoutHandlerName The authentication handler
     * @param mode              This mode defines how the termination of
     *                           the session is handled.
     */
    public void logout(String  logoutHandlerName,
                        int     mode)
    throws ProcessingException {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN logout handler=" + logoutHandlerName +
                                   ", mode="+mode);
        }
        SessionContext context = this.getAuthenticationSessionContext(false);

        if (context != null && logoutHandlerName != null) {

            // remove context
            context.removeXML(logoutHandlerName);
            // FIXME (CZ): The sessionContextImpl should not be null, but
            //             it is sometimes. Why?
            SessionContextImpl sessionContextImpl = (SessionContextImpl)
                        (this.getSessionManager().getContext(AuthenticationConstants.SESSION_CONTEXT_NAME));
            if (sessionContextImpl != null) {
                sessionContextImpl.cleanParametersCache(logoutHandlerName);
            } else if (this.getLogger().isWarnEnabled()) {
                this.getLogger().warn("AuthenticationManager:logout() - sessionContextImpl is null");
            }
            Handler logoutHandler = (Handler)this.getHandler(logoutHandlerName);

            final List handlerContexts = logoutHandler.getHandlerContexts();
            final Iterator iter = handlerContexts.iterator();
            while ( iter.hasNext() ) {
                final SessionContext deleteContext = (SessionContext) iter.next();
                this.getSessionManager().deleteContext( deleteContext.getName() );
            }
            logoutHandler.clearHandlerContexts();
            this.handlerManager.removeUserHandler( logoutHandler, this.request );
            if (logoutHandlerName.equals(this.handlerName)) {
                this.handlerName = null;
                this.handler = null;
                this.applicationName = null;
                this.application = null;
            }
        }

        if ( mode == AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY ) {
            this.getSessionManager().terminateSession(true);
        } else if (!this.handlerManager.hasUserHandler( this.request )) {
            if (mode == AuthenticationConstants.LOGOUT_MODE_IF_UNUSED) {
                this.getSessionManager().terminateSession(false);
            } else {
                this.getSessionManager().terminateSession(true);
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END logout");
        }
    }

    /**
     * Get the configuration if available
     */
    public Configuration getModuleConfiguration(String name)
    throws ProcessingException  {
        // synchronized not needed
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getModuleConfiguration module="+name);
        }
        Configuration conf = null;

        if (this.handler != null && this.application != null) {
            conf = this.application.getConfiguration(name);
        }
        if (this.handler != null && conf == null) {
            conf = this.handler.getConfiguration(name);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END getModuleConfiguration conf="+conf);
        }
        return conf;
    }

    /**
     * Create Application Context.
     * This context is destroyed when the user logs out of the handler
     */
    public SessionContext createHandlerContext(String name,
                                               String loadURI,
                                               String saveURI)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN createHandlerContext name="+name);
        }

        SessionContext context = null;

        if (this.handler != null) {

            final Session session = this.getSessionManager().getSession(false);
            synchronized(session) {

                try {
                    // create new context
                    context = this.getSessionManager().createContext(name, loadURI, saveURI);
                    this.handler.addHandlerContext( context );
                } catch (IOException ioe) {
                    throw new ProcessingException("Unable to create session context.", ioe);
                } catch (SAXException saxe) {
                    throw new ProcessingException("Unable to create session context.", saxe);
                }

            } // end synchronized
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END createHandlerContext context="+context);
        }
        return context;
    }

    /**
     * Load XML of an application
     */
    private void loadApplicationXML(SessionContextImpl context,
                                    ApplicationHandler appHandler,
                                    String path)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN loadApplicationXML application=" + appHandler.getName() + ", path="+path);
        }
        Object o = this.getSessionManager().getSession(true);
        synchronized(o) {

            if (appHandler.getIsLoaded() == false) {

                final String   loadResourceName = appHandler.getLoadResource();
                SourceParameters parameters = appHandler.getLoadResourceParameters();
                if (parameters != null) parameters = (SourceParameters)parameters.clone();
                parameters = this.createParameters(parameters,
                                                   appHandler.getHandler().getName(),
                                                   path,
                                                   appHandler.getName());
                DocumentFragment fragment;

                Source source = null;
                try {
                    source = org.apache.cocoon.components.source.SourceUtil.getSource(loadResourceName, 
                                                                                      null, 
                                                                                      parameters, 
                                                                                      this.resolver);
                    Document doc = org.apache.cocoon.components.source.SourceUtil.toDOM(source);
                    fragment = doc.createDocumentFragment();
                    fragment.appendChild(doc.getDocumentElement());
                } catch (SourceException se) {
                    throw org.apache.cocoon.components.source.SourceUtil.handle(se);
                } catch (IOException se) {
                    throw new ProcessingException(se);
                } catch (SAXException se) {
                    throw new ProcessingException(se);
                } finally {
                    this.resolver.release(source);
                }

                appHandler.setIsLoaded(true);

                context.setApplicationXML(appHandler.getHandler().getName(),
                                          appHandler.getName(),
                                          path,
                                          fragment);

                // now test handler if all applications are loaded
                Iterator applications = appHandler.getHandler().getApplications().values().iterator();
                boolean     allLoaded = true;
                while (allLoaded == true && applications.hasNext() == true) {
                    allLoaded = ((ApplicationHandler)applications.next()).getIsLoaded();
                }
                appHandler.getHandler().setApplicationsLoaded(allLoaded);
            }

        } // end synchronized

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END loadApplicationXML");
        }
    }

    /**
     * Check if application for path is loaded
     */
    private void checkLoaded(SessionContextImpl context,
                             String             path)
    throws ProcessingException {
        // synchronized as loadApplicationXML is synced
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN checkLoaded path="+path);
        }
        if (path.equals("/") == true || path.startsWith("/application") == true) {
            if (this.application != null) {
                this.loadApplicationXML(context, this.application, "/");
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END checkLoaded");
        }
    }

    /**
     * Build parameters for loading and saving of application data
     */
    public SourceParameters createParameters(String path)
    throws ProcessingException {
        // synchronized
        if (this.handler == null) {
            return new SourceParameters();
        }
        if (path == null) {
            SessionContext context = this.getAuthenticationSessionContext(false);
            SourceParameters pars = (SourceParameters)context.getAttribute("cachedparameters_" + this.handler.getName());
            if (pars == null) {
                 pars = this.createParameters(null, this.handlerName, path, this.applicationName);
                 context.setAttribute("cachedparameters_" + this.handler.getName(), pars);
            }
            return pars;
        }
        return this.createParameters(null, this.handlerName, path, this.applicationName);
    }

    protected static final Map EMPTY_MAP = Collections.unmodifiableMap(new TreeMap());

    /**
     * Create a map for the actions
     * The result is cached!
     */
    public Map createMap()
    throws ProcessingException {
        if (this.handler == null) {
            // this is only a fallback
            return EMPTY_MAP;
        }
        SessionContext context = this.getAuthenticationSessionContext(false);
        Map map = (Map)context.getAttribute("cachedmap_" + this.handler.getName());
        if (map == null) {
            map = new HashMap();
            Parameters pars = this.createParameters(null).getFirstParameters();
            String[] names = pars.getNames();
            if (names != null) {
                String key;
                String value;
                for(int i=0;i<names.length;i++) {
                    key = names[i];
                    value = pars.getParameter(key, null);
                    if (value != null) map.put(key, value);
                }
            }
            context.setAttribute("cachedmap_" + this.handler.getName(), map);
        }
        return map;
    }

    /**
     * Build parameters for loading and saving of application data
     */
    private SourceParameters createParameters(SourceParameters parameters,
                                                String             myHandler,
                                                String             path,
                                                String             appName)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN createParameters handler=" + myHandler +
                              ", path="+path+ ", application=" + appName);
        }

        SessionContextImpl context;
        context = (SessionContextImpl)contextProvider.getSessionContext(AuthenticationConstants.SESSION_CONTEXT_NAME,
                                                      this.objectModel,
                                                      this.resolver,
                                                      this.manager);
        parameters = context.createParameters(parameters, myHandler, path, appName);

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END createParameters parameters="+parameters);
        }
        return parameters;
    }

}


/**
 * This class stores the media type configuration
 */
final class MediaType {

    String name;
    String useragent;

    MediaType(String name, String useragent) {
        this.name = name;
        this.useragent = useragent;
    }
}
