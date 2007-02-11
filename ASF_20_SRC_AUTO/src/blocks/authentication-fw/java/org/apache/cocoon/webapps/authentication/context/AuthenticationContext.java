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
package org.apache.cocoon.webapps.authentication.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.components.DefaultAuthenticationManager;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SimpleSessionContext;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is the implementation for the authentication context
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AuthenticationContext.java,v 1.15 2004/03/05 13:01:41 bdelacretaz Exp $
*/
public class AuthenticationContext
implements SessionContext {

    protected String          name;
    protected UserHandler     handler;
    protected SessionContext  authContext;
    protected String          handlerName;
    protected boolean         initialized;
    protected Context         context;
    protected XPathProcessor  xpathProcessor;
    
    /** Constructor */
    public AuthenticationContext(Context context, XPathProcessor processor) {
        this.context = context;
        this.xpathProcessor = processor;
    }
    
    /**
     * Initialize the context. This method has to be called right after
     * the constructor.
     */
    public void init(UserHandler handler) {
        this.name = AuthenticationConstants.SESSION_CONTEXT_NAME;

        this.handler = handler;
        this.handlerName = this.handler.getHandlerName();
        try {
            this.authContext = new SimpleSessionContext(this.xpathProcessor);
        } catch (ProcessingException pe) {
            throw new CascadingRuntimeException("Unable to create simple context.", pe);
        }
    }
    
    protected RequestState getState() {
        return DefaultAuthenticationManager.getRequestState( this.context );
    }
    
    public void init(Document doc) 
    throws ProcessingException {
        if ( initialized ) {
            throw new ProcessingException("The context can only be initialized once.");
        }
        this.authContext.setNode("/", doc.getFirstChild());
    }
    
    /** Set the name of the context.
     *  This method must be invoked in the init phase.
     *  In addition a load and a save resource can be provided.
     */
    public void setup(String value, String load, String save) {
        // this is not used, everything is set in the constructor
    }

    /**
     * Get the name of the context
     */
    public String getName() {
        return this.name;
    }

    /**
     *  Get a document fragment.
     *  If the node specified by the path exist, its content is returned
     *  as a DocumentFragment.
     *  If the node does not exists, <CODE>null</CODE> is returned.
     */
    public DocumentFragment getXML(String path)
    throws ProcessingException {
        if (path == null) {
            throw new ProcessingException("getXML: Path is required");
        }
        if (!path.startsWith("/")) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();
        
        DocumentFragment frag = null;

        if ( path.equals("/") ) {
            // get all: first authentication then application
            frag = this.authContext.getXML("/authentication");

            if (frag != null) {
                // now add root node authentication
                Node root = frag.getOwnerDocument().createElementNS(null, "authentication");
                Node child;
                while (frag.hasChildNodes()) {
                    child = frag.getFirstChild();
                    frag.removeChild(child);
                    root.appendChild(child);
                }
                frag.appendChild(root);
            }

            if (applicationName != null) {
                // join
                DocumentFragment appFrag = this.authContext.getXML("/applications/" + applicationName);
                if (appFrag != null) {
                    // now add root node application
                    Node root = appFrag.getOwnerDocument().createElementNS(null, "application");
                    Node child;
                    while (appFrag.hasChildNodes() ) {
                        child = appFrag.getFirstChild();
                        appFrag.removeChild(child);
                        root.appendChild(child);
                    }
                    appFrag.appendChild(root);

                    if (frag == null) {
                        frag = appFrag;
                    } else {
                        while (appFrag.hasChildNodes() ) {
                            child = appFrag.getFirstChild();
                            appFrag.removeChild(child);
                            child = frag.getOwnerDocument().importNode(child, true);
                            frag.appendChild(child);
                        }
                    }
                }
            }

        } else if (path.startsWith("/authentication") ) {
            frag = this.authContext.getXML(path);

        } else if (path.equals("/application") || path.startsWith("/application/") ) {
            if (applicationName != null) {
                String appPath;
                if (path.equals("/application")) {
                    appPath ="/";
                } else {
                    appPath = path.substring("/application".length());
                }
                frag = this.authContext.getXML("/applications/" + applicationName + appPath);
            }
        } else {
            frag = this.authContext.getXML(path);
        }

        return frag;
    }

    /**
     *  Set a document fragment at the given path.
     *  The implementation of this method is context specific.
     *  Usually all children of the node specified by the path are removed
     *  and the children of the fragment are inserted as new children.
     *  If the path is not existent it is created.
     */
    public void setXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        if (path == null) {
            throw new ProcessingException("setXML: Path is required");
        }
        if (!path.startsWith("/")) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();

        if ( path.equals("/") ) {
            // set all is not allowed with "/"
            throw new ProcessingException("Path '/' is not allowed");

        } else if ( path.startsWith("/authentication") ) {

            this.cleanParametersCache();
            this.authContext.setXML(path, fragment);

        } else if (path.equals("/application") 
                   || path.startsWith("/application/") ) {

            if (applicationName == null) {
                throw new ProcessingException("Application is required");
            }
            String appPath;
            if (path.equals("/application")) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.setXML("/applications/" + applicationName + appPath, fragment);

        } else {
            this.authContext.setXML(path, fragment);
        }
    }

    /**
     * Append a document fragment at the given path.
     * The implementation of this method is context specific.
     * Usually the children of the fragment are appended as new children of the
     * node specified by the path.
     * If the path is not existent it is created and this method should work
     * in the same way as setXML.
     */
    public void appendXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        if (path == null) {
            throw new ProcessingException("appendXML: Path is required");
        }
        if (!path.startsWith("/") ) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();

        if ( path.equals("/") ) {
            // set all is not allowed with "/"
            throw new ProcessingException("Path '/' is not allowed");

        } else if ( path.startsWith("/authentication") ) {

            this.cleanParametersCache();
            this.authContext.appendXML(path, fragment);

        } else if (path.equals("/application")
                   || path.startsWith("/application/") ) {

            if (applicationName == null) {
                throw new ProcessingException("Application is required");
            }
            String appPath;
            if (path.equals("/application") ) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.appendXML("/applications/" + applicationName + appPath, fragment);

        } else {
            this.authContext.appendXML(path, fragment);
        }
    }

    /**
     * Remove some content from the context.
     * The implementation of this method is context specific.
     * Usually this method should remove all children of the node specified
     * by the path.
     */
    public void removeXML(String path)
    throws ProcessingException {
        if (path == null) {
            throw new ProcessingException("removeXML: Path is required");
        }
        if (!path.startsWith("/") ) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();

        if (path.equals("/") ) {
            this.cleanParametersCache();
            this.authContext.removeXML("/");

        } else if (path.startsWith("/authentication") ) {

            this.cleanParametersCache();
            this.authContext.removeXML(path);

        } else if (path.equals("/application") 
                   || path.startsWith("/application/") ) {
            if (applicationName == null) {
                throw new ProcessingException("removeXML: Application is required for path " + path);
            }
            String appPath;
            if (path.equals("/application") ) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.removeXML("/applications/" + applicationName + appPath);
        } else {
            this.authContext.removeXML(path);
        }
    }

    /**
     * Set a context attribute.
     * Attributes over a means to store any data (object) in a session
     * context. If <CODE>value</CODE> is <CODE>null</CODE> the attribute is
     * removed. If already an attribute exists with the same key, the value
     * is overwritten with the new one.
     */
    public void setAttribute(String key, Object value)
    throws ProcessingException {
        this.authContext.setAttribute(key, value);
    }

    /**
     * Get the value of a context attribute.
     * If the attribute is not available return <CODE>null</CODE>.
     */
    public Object getAttribute(String key)
    throws ProcessingException {
        return this.authContext.getAttribute(key);
    }

    /**
     * Get the value of a context attribute.
     * If the attribute is not available the return the
     * <CODE>defaultObject</CODE>.
     */
    public Object getAttribute(String key, Object defaultObject)
    throws ProcessingException {
        return this.authContext.getAttribute(key, defaultObject);
    }

    /**
     * Get a copy of the first node specified by the path.
     * If the node does not exist, <CODE>null</CODE> is returned.
     */
    public Node getSingleNode(String path)
    throws ProcessingException {
        throw new ProcessingException("This method is not supported by the authenticaton session context.");
    }

    /**
     * Get a copy of all nodes specified by the path.
     */
    public NodeList getNodeList(String path)
    throws ProcessingException {
        throw new ProcessingException("This method is not supported by the authenticaton session context.");
    }

    /**
     * Set the value of a node. The node is copied before insertion.
     */
    public void setNode(String path, Node node)
    throws ProcessingException {
        throw new ProcessingException("This method is not supported by the authenticaton session context.");
    }

    /**
     * Get the value of this node.
     * This is similiar to the xsl:value-of function.
     * If the node does not exist, <code>null</code> is returned.
     */
    public String getValueOfNode(String path)
    throws ProcessingException {
        throw new ProcessingException("This method is not supported by the authenticaton session context.");
    }

    /**
     * Set the value of a node.
     * All children of the node are removed beforehand and one single text
     * node with the given value is appended to the node.
     */
    public void setValueOfNode(String path, String value)
    throws ProcessingException {
        throw new ProcessingException("This method is not supported by the authenticaton session context.");
    }

    /**
     * Stream the XML directly to the handler.
     * This streams the contents of getXML() to the given handler without
     * creating a DocumentFragment containing a copy of the data.
     * If no data is available (if the path does not exist) <code>false</code> is
     * returned, otherwise <code>true</code>.
     */
    public boolean streamXML(String path, ContentHandler contentHandler,
                             LexicalHandler lexicalHandler)
    throws SAXException, ProcessingException {
        if (path == null) {
            throw new ProcessingException("streamXML: Path is required");
        }
        if (!path.startsWith("/") ) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();

        if (path.equals("/") ) {
            // get all: first authentication then application
            contentHandler.startElement("", "authentication", "authentication", new AttributesImpl());
            this.authContext.streamXML("/authentication", contentHandler, lexicalHandler);
            contentHandler.endElement("", "authentication", "authentication");

            if (applicationName != null) {
                contentHandler.startElement("", "application", "application", new AttributesImpl());
                this.authContext.streamXML("/applications/" + applicationName, contentHandler, lexicalHandler);
                contentHandler.endElement("", "application", "application");
            }
            return true;

        } else if (path.startsWith("/authentication") ) {
            return this.authContext.streamXML(path, contentHandler, lexicalHandler);

        } else if (path.equals("/application") || path.startsWith("/application/") ) {
            if (applicationName != null) {
                String appPath;
                if (path.equals("/application") ) {
                    appPath ="/";
                } else {
                    appPath = path.substring("/application".length());
                }
                return this.authContext.streamXML("/applications/" + applicationName + appPath, contentHandler, lexicalHandler);
            }
        } else {
            return this.authContext.streamXML(path, contentHandler, lexicalHandler);
        }
        return false;
    }

    /**
     * Try to load XML into the context.
     * If the context does not provide the ability of loading,
     * an exception is thrown.
     */
    public void loadXML(String path,
                        SourceParameters parameters,
                        Map              objectModel,
                        SourceResolver   resolver,
                        ServiceManager   manager)
    throws SAXException, ProcessingException, IOException {
        if (!path.startsWith("/") ) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();
        if (path.equals("/") ) {
            // load all: first authentication then application
            this.loadAuthenticationXML("/authentication",
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);
            if (applicationName != null) {
                this.loadApplicationXML("/",
                                        parameters,
                                        objectModel,
                                        resolver,
                                        manager);
            }

        } else if (path.startsWith("/authentication") ) {
            this.loadAuthenticationXML(path,
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);

        } else if (path.equals("/application") && applicationName != null) {
            this.loadApplicationXML("/",
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else if (path.startsWith("/application/") && applicationName != null) {
            this.loadApplicationXML(path.substring(12), // start path with '/'
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else {
            throw new ProcessingException("loadXML: Path is not valid: " + path);
        }
    }

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    public void saveXML(String             path,
                        SourceParameters parameters,
                        Map                objectModel,
                        SourceResolver     resolver,
                        ServiceManager   manager)
    throws SAXException, ProcessingException, IOException {
        if (!path.startsWith("/") ) path = '/' + path;

        final String applicationName = this.getState().getApplicationName();

        if (path.equals("/") ) {
            // save all: first authentication then application
            this.saveAuthenticationXML("/authentication",
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);
            if (applicationName != null) {
                this.saveApplicationXML("/",
                                        parameters,
                                        objectModel,
                                        resolver,
                                        manager);
            }

        } else if (path.startsWith("/authentication") ) {
            this.saveAuthenticationXML(path,
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);

        } else if (path.equals("/application") && applicationName != null) {
            this.saveApplicationXML("/",
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else if (path.startsWith("/application/") && applicationName != null) {
            this.saveApplicationXML(path.substring(12), // start path with '/'
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else {
            throw new ProcessingException("saveXML: Path is not valid: " + path);
        }
    }

    /**
     * Clean the parameters cache
     */
    private void cleanParametersCache()
    throws ProcessingException {
        this.authContext.setAttribute("cachedmap" , null);
        this.authContext.setAttribute("cachedpar" , null);
    }

    /**
     * Save Authentication
     */
    private void saveAuthenticationXML(String             path,
                                       SourceParameters parameters,
                                       Map                objectModel,
                                       SourceResolver     resolver,
                                       ServiceManager   manager)
    throws ProcessingException {
        String authSaveResource = this.handler.getHandlerConfiguration().getSaveResource();
        SourceParameters authSaveResourceParameters = this.handler.getHandlerConfiguration().getSaveResourceParameters();

        if (authSaveResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support saving.");
        }

        synchronized(this.authContext) {
            DocumentFragment fragment = this.getXML(path);
            if (fragment == null) {
                // create empty fake fragment
                fragment = DOMUtil.createDocument().createDocumentFragment();
            }
            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(authSaveResourceParameters);
            } else if (authSaveResourceParameters != null) {
                parameters = (SourceParameters)authSaveResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               path,
                                               false);
            SourceUtil.writeDOM(authSaveResource,
                                null,
                                parameters,
                                fragment,
                                resolver,
                                "xml");
        } // end synchronized
    }

    /**
     * Save Authentication
     */
    private void loadAuthenticationXML(String             path,
                                       SourceParameters parameters,
                                       Map                objectModel,
                                       SourceResolver     resolver,
                                       ServiceManager   manager)
    throws ProcessingException {
        String authLoadResource = this.handler.getHandlerConfiguration().getLoadResource();
        SourceParameters authLoadResourceParameters = this.handler.getHandlerConfiguration().getLoadResourceParameters();
        
        if (authLoadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }

        synchronized(this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(authLoadResourceParameters);
            } else if (authLoadResourceParameters != null) {
                parameters = (SourceParameters)authLoadResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               path,
                                               false); 
            DocumentFragment frag;
            
            frag = SourceUtil.readDOM(authLoadResource, 
                                      null, 
                                      parameters, 
                                      resolver);
            
            this.setXML(path, frag);

        } // end synchronized
    }

    /**
     * Load XML of an application
     */
    private void loadApplicationXML(String             path,
                                    SourceParameters parameters,
                                    Map                objectModel,
                                    SourceResolver     resolver,
                                    ServiceManager   manager)
    throws ProcessingException {
        final String applicationName = this.getState().getApplicationName();

        final ApplicationConfiguration conf = (ApplicationConfiguration)this.handler.getHandlerConfiguration().getApplications().get( applicationName );
        String loadResource = conf.getLoadResource();
        SourceParameters loadResourceParameters = conf.getLoadResourceParameters();
        if (loadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }
        // synchronized
        synchronized (this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(loadResourceParameters);
            } else if (loadResourceParameters != null) {
                parameters = (SourceParameters)loadResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               path,
                                               true);
            DocumentFragment fragment;
            fragment = SourceUtil.readDOM(loadResource, 
                                          null, 
                                          parameters, 
                                          resolver);
            this.authContext.setXML("/applications/" + applicationName + '/', fragment);

        } // end synchronized

    }

    /**
     * Save XML of an application
     */
    private void saveApplicationXML(String path,
                                    SourceParameters parameters,
                                    Map                objectModel,
                                    SourceResolver     resolver,
                                    ServiceManager   manager)
    throws ProcessingException {
        final String applicationName = this.getState().getApplicationName();
        final ApplicationConfiguration conf = (ApplicationConfiguration)this.handler.getHandlerConfiguration().getApplications().get( applicationName );
        String saveResource = conf.getSaveResource();
        SourceParameters saveResourceParameters = conf.getSaveResourceParameters();

        if (saveResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support saving.");
        }
        // synchronized
        synchronized (this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(saveResourceParameters);
            } else if (saveResourceParameters != null) {
                parameters = (SourceParameters)saveResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               path,
                                               true);
            DocumentFragment fragment = this.getXML("/application" + path);
            if (fragment == null) {
                // create empty fake fragment
                fragment = DOMUtil.createDocument().createDocumentFragment();
            }

            SourceUtil.writeDOM(saveResource,
                                null,
                                parameters,
                                fragment,
                                resolver,
                                "xml");

        } // end synchronized

    }

    /**
     * Build parameters for loading and saving of application data
     */
    private SourceParameters createParameters(SourceParameters parameters,
                                               String           path,
                                               boolean         appendAppInfo)
    throws ProcessingException {
        if (parameters == null) parameters = new SourceParameters();

        final String applicationName = this.getState().getApplicationName();

        // add all elements from inside the handler data
        this.addParametersFromAuthenticationXML("/data",
                                                parameters);

        // add all top level elements from authentication
        this.addParametersFromAuthenticationXML("",
                                                parameters);

        // add application and path
        parameters.setSingleParameterValue("handler", this.handlerName);
        if ( appendAppInfo ) {
            if (applicationName != null) parameters.setSingleParameterValue("application", applicationName);
        }
        if (path != null) parameters.setSingleParameterValue("path", path);

        return parameters;
    }

    /**
     * Convert the authentication XML of a handler to parameters.
     * The XML is flat and consists of elements which all have exactly one text node:
     * <parone>value_one<parone>
     * <partwo>value_two<partwo>
     * A parameter can occur more than once with different values.
     */
    private void addParametersFromAuthenticationXML(String path,
                                                     SourceParameters parameters)
    throws ProcessingException {
        final DocumentFragment fragment = this.authContext.getXML("/authentication" + path);
        if (fragment != null) {
            NodeList   childs = fragment.getChildNodes();
            if (childs != null) {
                Node current;
                for(int i = 0; i < childs.getLength(); i++) {
                    current = childs.item(i);

                    // only element nodes
                    if (current.getNodeType() == Node.ELEMENT_NODE) {
                        current.normalize();
                        NodeList valueChilds = current.getChildNodes();
                        String   key;
                        StringBuffer   valueBuffer;
                        String         value;

                        key = current.getNodeName();
                        valueBuffer = new StringBuffer();
                        for(int m = 0; m < valueChilds.getLength(); m++) {
                            current = valueChilds.item(m); // attention: current is reused here!
                            if (current.getNodeType() == Node.TEXT_NODE) { // only text nodes
                                if (valueBuffer.length() > 0) valueBuffer.append(' ');
                                valueBuffer.append(current.getNodeValue());
                            }
                        }
                        value = valueBuffer.toString().trim();
                        if (key != null && value != null && value.length() > 0) {
                            parameters.setParameter(key, value);
                        }
                    }
                }
            }
        }
    }

    public Map getContextInfo() 
    throws ProcessingException {
        Map map = (Map)this.authContext.getAttribute( "cachedmap" );
        if (map == null) {
            map = new HashMap(20);
            Parameters pars = this.createParameters(null, null, false).getFirstParameters();
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
            this.authContext.setAttribute("cachedmap", map);
        }
        return map;
    }
    
    public SourceParameters getContextInfoAsParameters() 
    throws ProcessingException {
        SourceParameters pars = (SourceParameters)this.authContext.getAttribute( "cachedpar" );
        if (pars == null) {
            pars = this.createParameters(null, null, false);
            this.authContext.setAttribute("cachedpar", pars);
        }
        return pars;
    }

    /**
     * Load XML of an application
     */
    public void loadApplicationXML(ApplicationConfiguration appConf, 
                                    SourceResolver resolver)
    throws ProcessingException {
        String loadResource = appConf.getLoadResource();
        SourceParameters loadResourceParameters = appConf.getLoadResourceParameters();
        if ( !this.handler.isApplicationLoaded(appConf) && loadResource != null ) {
            synchronized (this.authContext) {
        
                SourceParameters parameters;
                if (loadResourceParameters != null) {
                    parameters = (SourceParameters)loadResourceParameters.clone();
                } else {
                    parameters = new SourceParameters();
                }
                parameters = this.createParameters(parameters,
                                                   null,
                                                   true);
                DocumentFragment fragment;
                fragment = SourceUtil.readDOM(loadResource, 
                                              null, 
                                              parameters, 
                                              resolver);
                this.authContext.setXML("/applications/" + appConf.getName() + '/', fragment);
        
            } // end synchronized
        }
        this.handler.setApplicationIsLoaded(appConf);
    }

}
