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
package org.apache.cocoon.webapps.authentication.context;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.webapps.authentication.components.ApplicationHandler;
import org.apache.cocoon.webapps.authentication.components.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.components.Handler;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContextImpl.java,v 1.1 2003/03/09 00:02:20 pier Exp $
*/
public final class SessionContextImpl
implements SessionContext {

    private String          name;
    private SessionContext  authContext;
    private String          loadResource;
    private String          saveResource;
    private String          authLoadResource;
    private String          authSaveResource;
    private SourceParameters loadResourceParameters;
    private SourceParameters saveResourceParameters;
    private SourceParameters authLoadResourceParameters;
    private SourceParameters authSaveResourceParameters;
    private String          handlerName;
    private String          applicationName;

    public SessionContextImpl(SessionContext context,
                              String         contextName,
                              String          handlerName,
                              String          applicationName,
                              SourceResolver  resolver,
                              ComponentManager manager)
    throws ProcessingException {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)manager.lookup(AuthenticationManager.ROLE);
            Handler handler = authManager.getHandler();
            this.name = contextName;
            this.authContext = context;
            this.handlerName = handlerName;
            this.applicationName = applicationName;
            this.authLoadResource = handler.getLoadResource();
            this.authSaveResource = handler.getSaveResource();
            this.authLoadResourceParameters = handler.getLoadResourceParameters();
            this.authSaveResourceParameters = handler.getSaveResourceParameters();
            if (this.applicationName != null) {
                ApplicationHandler appHandler = (ApplicationHandler)handler.getApplications().get(this.applicationName);
                this.loadResource = appHandler.getLoadResource();
                this.saveResource = appHandler.getSaveResource();
                this.loadResourceParameters = appHandler.getLoadResourceParameters();
                this.saveResourceParameters = appHandler.getSaveResourceParameters();
            }

        } catch (ComponentException ce) {
            throw new ProcessingException("Unable to lookup the resource connector.", ce);
        } finally {
            manager.release(authManager);
        }
    }

    /** Set the name of the context.
     *  This method must be invoked in the init phase.
     *  In addition a load and a save resource can be provided.
     */
    public void setup(String value, String load, String save) {
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
        if (path.startsWith("/") == false) path = '/' + path;

        DocumentFragment frag = null;

        if (path.equals("/") == true) {
            // get all: first authentication then application
            frag = this.authContext.getXML("/" + this.handlerName + "/authentication");

            if (frag != null) {
                // now add root node authentication
                Node root = frag.getOwnerDocument().createElementNS(null, "authentication");
                Node child;
                while (frag.hasChildNodes() == true) {
                    child = frag.getFirstChild();
                    frag.removeChild(child);
                    root.appendChild(child);
                }
                frag.appendChild(root);
            }

            if (this.applicationName != null) {
                // join
                DocumentFragment appFrag = this.authContext.getXML("/" + this.handlerName + "/applications/" + this.applicationName);
                if (appFrag != null) {
                    // now add root node application
                    Node root = appFrag.getOwnerDocument().createElementNS(null, "application");
                    Node child;
                    while (appFrag.hasChildNodes() == true) {
                        child = appFrag.getFirstChild();
                        appFrag.removeChild(child);
                        root.appendChild(child);
                    }
                    appFrag.appendChild(root);

                    if (frag == null) {
                        frag = appFrag;
                    } else {
                        while (appFrag.hasChildNodes() == true) {
                            child = appFrag.getFirstChild();
                            appFrag.removeChild(child);
                            child = frag.getOwnerDocument().importNode(child, true);
                            frag.appendChild(child);
                        }
                    }
                }
            }

        } else if (path.startsWith("/authentication") == true) {
            frag = this.authContext.getXML("/" + this.handlerName + path);

        } else if (path.equals("/application") == true || path.startsWith("/application/") == true) {
            if (this.applicationName != null) {
                String appPath;
                if (path.equals("/application") == true) {
                    appPath ="/";
                } else {
                    appPath = path.substring("/application".length());
                }
                frag = this.authContext.getXML("/" + this.handlerName + "/applications/" + this.applicationName + appPath);
            }
        } else {
            frag = this.authContext.getXML("/" + this.handlerName + path);
        }

        return frag;
    }

    /**
     * Convert the authentication XML of a handler to parameters.
     * The XML is flat and consists of elements which all have exactly one text node:
     * <parone>value_one<parone>
     * <partwo>value_two<partwo>
     * A parameter can occur more than once with different values.
     */
    public void addParametersFromAuthenticationXML(String handlerName,
                                                   String path,
                                                   SourceParameters parameters)
    throws ProcessingException {
        final DocumentFragment fragment = this.authContext.getXML("/" + handlerName + "/authentication" + path);
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
        if (path.startsWith("/") == false) path = '/' + path;

        if ( path.equals("/") ) {
            // set all is not allowed with "/"
            throw new ProcessingException("Path '/' is not allowed");

        } else if ( path.startsWith("/authentication") ) {

            this.cleanParametersCache();
            this.authContext.setXML('/' + this.handlerName + path, fragment);

        } else if (path.equals("/application") == true
                   || path.startsWith("/application/") == true) {

            if (this.applicationName == null) {
                throw new ProcessingException("Application is required");
            }
            String appPath;
            if (path.equals("/application") == true) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.setXML("/" + this.handlerName + "/applications/" + this.applicationName + appPath, fragment);

        } else {
            this.authContext.setXML("/" + this.handlerName + path, fragment);
        }
    }

    /**
     * Set the XML for an application
     */
    public void setApplicationXML(String setHandlerName,
                                  String setApplicationName,
                                  String path,
                                  DocumentFragment fragment)
    throws ProcessingException {
        path = "/" + setHandlerName + "/applications/" + setApplicationName + path;
        this.authContext.setXML(path, fragment);
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
        if (path.startsWith("/") == false) path = '/' + path;

        if ( path.equals("/") ) {
            // set all is not allowed with "/"
            throw new ProcessingException("Path '/' is not allowed");

        } else if ( path.startsWith("/authentication") ) {

            this.cleanParametersCache();
            this.authContext.appendXML('/' + this.handlerName + path, fragment);

        } else if (path.equals("/application") == true
                   || path.startsWith("/application/") == true) {

            if (this.applicationName == null) {
                throw new ProcessingException("Application is required");
            }
            String appPath;
            if (path.equals("/application") == true) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.appendXML("/" + this.handlerName + "/applications/" + this.applicationName + appPath, fragment);

        } else {
            this.authContext.appendXML("/" + this.handlerName + path, fragment);
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
        if (path.startsWith("/") == false) path = '/' + path;

        if (path.equals("/") == true) {
            this.cleanParametersCache();
            this.authContext.removeXML("/" + this.handlerName);

        } else if (path.startsWith("/authentication") == true) {

            this.cleanParametersCache();
            this.authContext.removeXML("/" + this.handlerName + path);

        } else if (path.equals("/application") == true
                   || path.startsWith("/application/") == true) {
            if (this.applicationName == null) {
                throw new ProcessingException("removeXML: Application is required for path " + path);
            }
            String appPath;
            if (path.equals("/application") == true) {
                appPath = "/";
            } else {
                appPath = path.substring("/application".length());
            }
            this.authContext.removeXML("/" + this.handlerName + "/applications/" + this.applicationName + appPath);
        } else {
            this.authContext.removeXML("/" + this.handlerName + path);
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
        if (path.startsWith("/") == false) path = '/' + path;

        if (path.equals("/") == true) {
            // get all: first authentication then application
            contentHandler.startElement(null, "authentication", "authentication", new AttributesImpl());
            this.authContext.streamXML('/' + this.handlerName + "/authentication", contentHandler, lexicalHandler);
            contentHandler.endElement(null, "authentication", "authentication");

            if (this.applicationName != null) {
                contentHandler.startElement(null, "application", "application", new AttributesImpl());
                this.authContext.streamXML('/' + this.handlerName + "/applications/" + this.applicationName, contentHandler, lexicalHandler);
                contentHandler.endElement(null, "application", "application");
            }
            return true;

        } else if (path.startsWith("/authentication") == true) {
            return this.authContext.streamXML('/' + this.handlerName + path, contentHandler, lexicalHandler);

        } else if (path.equals("/application") == true || path.startsWith("/application/") == true) {
            if (this.applicationName != null) {
                String appPath;
                if (path.equals("/application") == true) {
                    appPath ="/";
                } else {
                    appPath = path.substring("/application".length());
                }
                return this.authContext.streamXML('/' + this.handlerName + "/applications/" + this.applicationName + appPath, contentHandler, lexicalHandler);
            }
        } else {
            return this.authContext.streamXML('/' + this.handlerName + path, contentHandler, lexicalHandler);
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
                        Map                objectModel,
                        SourceResolver     resolver,
                        ComponentManager   manager)
    throws SAXException, ProcessingException, IOException {
        if (path.startsWith("/") == false) path = '/' + path;

        if (path.equals("/") == true) {
            // load all: first authentication then application
            this.loadAuthenticationXML("/authentication",
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);
            if (this.applicationName != null) {
                this.loadApplicationXML("/",
                                        parameters,
                                        objectModel,
                                        resolver,
                                        manager);
            }

        } else if (path.startsWith("/authentication") == true) {
            this.loadAuthenticationXML(path,
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);

        } else if (path.equals("/application") == true && this.applicationName != null) {
            this.loadApplicationXML("/",
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else if (path.startsWith("/application/") == true && this.applicationName != null) {
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
                        ComponentManager   manager)
    throws SAXException, ProcessingException, IOException {
        if (path.startsWith("/") == false) path = '/' + path;

        if (path.equals("/") == true) {
            // save all: first authentication then application
            this.saveAuthenticationXML("/authentication",
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);
            if (this.applicationName != null) {
                this.saveApplicationXML("/",
                                        parameters,
                                        objectModel,
                                        resolver,
                                        manager);
            }

        } else if (path.startsWith("/authentication") == true) {
            this.saveAuthenticationXML(path,
                                       parameters,
                                       objectModel,
                                       resolver,
                                       manager);

        } else if (path.equals("/application") == true && this.applicationName != null) {
            this.saveApplicationXML("/",
                                    parameters,
                                    objectModel,
                                    resolver,
                                    manager);
        } else if (path.startsWith("/application/") == true && this.applicationName != null) {
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
        this.authContext.setAttribute("cachedparameters_" + this.handlerName, null);
        this.authContext.setAttribute("cachedmap_" + this.handlerName, null);
    }

    /**
     * Clean the parameters cache
     */
    public void cleanParametersCache(String handlerName)
    throws ProcessingException {
        this.authContext.setAttribute("cachedparameters_" + handlerName, null);
        this.authContext.setAttribute("cachedmap_" + handlerName, null);
    }

    /**
     * Save Authentication
     */
    private void saveAuthenticationXML(String             path,
                                       SourceParameters parameters,
                                       Map                objectModel,
                                       SourceResolver     resolver,
                                       ComponentManager   manager)
    throws ProcessingException {
        if (this.authSaveResource == null) {
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
                parameters.add(this.authSaveResourceParameters);
            } else if (this.authSaveResourceParameters != null) {
                parameters = (SourceParameters)this.authSaveResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               this.handlerName,
                                               path,
                                               null);
            SourceUtil.writeDOM(this.authSaveResource,
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
                                       ComponentManager   manager)
    throws ProcessingException {
        if (this.authLoadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }

        synchronized(this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(this.authLoadResourceParameters);
            } else if (this.authLoadResourceParameters != null) {
                parameters = (SourceParameters)this.authLoadResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               this.handlerName,
                                               path,
                                               null);
            DocumentFragment frag;
            
            frag = SourceUtil.readDOM(this.authLoadResource, 
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
                                    ComponentManager   manager)
    throws ProcessingException {
        if (this.loadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }
        // synchronized
        synchronized (this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(this.loadResourceParameters);
            } else if (this.loadResourceParameters != null) {
                parameters = (SourceParameters)this.loadResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               this.handlerName,
                                               path,
                                               this.applicationName);
            DocumentFragment fragment;
            fragment = SourceUtil.readDOM(this.loadResource, 
                                          null, 
                                          parameters, 
                                          resolver);
            this.setXML(path, fragment);

        } // end synchronized

    }

    /**
     * Save XML of an application
     */
    private void saveApplicationXML(String path,
                                    SourceParameters parameters,
                                    Map                objectModel,
                                    SourceResolver     resolver,
                                    ComponentManager   manager)
    throws ProcessingException {
        if (this.saveResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support saving.");
        }
        // synchronized
        synchronized (this.authContext) {

            if (parameters != null) {
                parameters = (SourceParameters)parameters.clone();
                parameters.add(this.saveResourceParameters);
            } else if (this.saveResourceParameters != null) {
                parameters = (SourceParameters)this.saveResourceParameters.clone();
            }
            parameters = this.createParameters(parameters,
                                               this.handlerName,
                                               path,
                                               this.applicationName);
            DocumentFragment fragment = this.getXML("/application" + path);
            if (fragment == null) {
                // create empty fake fragment
                fragment = DOMUtil.createDocument().createDocumentFragment();
            }

            SourceUtil.writeDOM(this.saveResource,
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
    public SourceParameters createParameters(SourceParameters parameters,
                                               String             myHandler,
                                               String             path,
                                               String             appName)
    throws ProcessingException {
        if (parameters == null) parameters = new SourceParameters();

        // add all elements from inside the handler data
        this.addParametersFromAuthenticationXML(myHandler,
                                                "/data",
                                                parameters);

        // add all top level elements from authentication
        this.addParametersFromAuthenticationXML(myHandler,
                                                "",
                                                parameters);

        // add application and path
        parameters.setSingleParameterValue("handler", myHandler);
        if (appName != null) parameters.setSingleParameterValue("application", appName);
        if (path != null) parameters.setSingleParameterValue("path", path);

        return parameters;
    }

}
