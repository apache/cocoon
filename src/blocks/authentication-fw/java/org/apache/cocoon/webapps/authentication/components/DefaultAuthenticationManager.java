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
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
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
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This is the basis authentication component.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultAuthenticationManager.java,v 1.1 2003/03/20 15:27:05 cziegeler Exp $
*/
public final class DefaultAuthenticationManager
extends AbstractSessionComponent
implements Manager, Configurable, SitemapConfigurable, RequestLifecycleComponent {

    /** The media Types */
    private PreparedMediaType[] allMediaTypes;
    
    /** The default media type (usually this is html) */
    private String      defaultMediaType;
    
    /** All media type names */
    private String[]    mediaTypeNames;

    /** The manager for the authentication handlers */
    private DefaultHandlerManager handlerManager;
    
    /** The context provider */
    private static SessionContextProviderImpl contextProvider;

    /** media type */
    private String mediaType;

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
        PreparedMediaType[] array = new PreparedMediaType[0];
        PreparedMediaType[] copy;
        Configuration current;
        if (childs != null) {
            for(int x = 0; x < childs.length; x++) {
                current = childs[x];
                copy = new PreparedMediaType[array.length + 1];
                System.arraycopy(array, 0, copy, 0, array.length);
                array = copy;
                name = current.getAttribute("name");
                array[array.length-1] = new PreparedMediaType(name, current.getAttribute("useragent"));
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
     * Set the sitemap configuration containing the handlers
     */
    public void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException {
        if ( null == this.handlerManager ) {
            this.handlerManager = new DefaultHandlerManager( holder );
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        super.recycle();
        this.handlerManager.recycle();
    }
    
    /**
     * @see org.apache.cocoon.components.RequestLifecycleComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map)
     */
    public void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel);
        this.handlerManager.setup( resolver, objectModel );
        // get the media of the current request
        String useragent = request.getHeader("User-Agent");
        PreparedMediaType media = null;
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
     * Test if the media of the current request is the given value
     */
    public boolean testMedia(String value) {
        // synchronized
        boolean result = false;

        String useragent = this.request.getHeader("User-Agent");
        PreparedMediaType theMedia = null;
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

        return result;
    }

    /**
     * Get all media type names
     */
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
     * Get the handler
     */
    private Handler getHandler(String name) 
    throws ProcessingException {
        // synchronized
        try {
            return this.handlerManager.getHandler( name );
        } catch (ConfigurationException ce) {
            throw new ProcessingException("Unable to get handler " + name, ce);
        }
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
            isAuthenticated = this.handlerManager.hasUserHandler( name );
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END isAuthenticated authenticated=" + isAuthenticated);
        }
        return isAuthenticated;
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

                    myHandler = this.handlerManager.storeUserHandler( myHandler );

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
final class PreparedMediaType {

    String name;
    String useragent;

    PreparedMediaType(String name, String useragent) {
        this.name = name;
        this.useragent = useragent;
    }
}
