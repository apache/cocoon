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
package org.apache.cocoon.webapps.session.context;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.apache.excalibur.source.SourceParameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.SourceResolver;

/**
 *  Interface for a SessionContext.
 *  This interface describes a SessionContext. The SessionContext is a data
 *  container containing structured XML which can be retrieved/set by the
 *  session transformer.
 *  This interface does not specify how the session context stores the data.
 *  This is left to the implementation itself, but actually this interface
 *  is build in the DOM model.
 *  As this context is used in a web context, all methods must be synchronized.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContext.java,v 1.3 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface SessionContext
extends Serializable {

    /** Set the name of the context.
     *  This method must be invoked in the init phase.
     *  In addition a load and a save resource can be provided.
     */
    void setup(String value, String loadResource, String saveResource);

    /**
     * Get the name of the context
     */
    String getName();

    /**
     *  Get a document fragment.
     *  If the node specified by the path exist, its content is returned
     *  as a DocumentFragment.
     *  If the node does not exists, <CODE>null</CODE> is returned.
     */
    DocumentFragment getXML(String path)
    throws ProcessingException ;

    /**
     *  Set a document fragment at the given path.
     *  The implementation of this method is context specific.
     *  Usually all children of the node specified by the path are removed
     *  and the children of the fragment are inserted as new children.
     *  If the path is not existent it is created.
     */
    void setXML(String path, DocumentFragment fragment)
    throws ProcessingException;

    /**
     * Append a document fragment at the given path.
     * The implementation of this method is context specific.
     * Usually the children of the fragment are appended as new children of the
     * node specified by the path.
     * If the path is not existent it is created and this method should work
     * in the same way as setXML.
     */
    void appendXML(String path, DocumentFragment fragment)
    throws ProcessingException;

    /**
     * Remove some content from the context.
     * The implementation of this method is context specific.
     * Usually this method should remove all children of the node specified
     * by the path.
     */
    void removeXML(String path)
    throws ProcessingException;

    /**
     * Set a context attribute.
     * Attributes over a means to store any data (object) in a session
     * context. If <CODE>value</CODE> is <CODE>null</CODE> the attribute is
     * removed. If already an attribute exists with the same key, the value
     * is overwritten with the new one.
     */
    void setAttribute(String key, Object value)
    throws ProcessingException;

    /**
     * Get the value of a context attribute.
     * If the attribute is not available return <CODE>null</CODE>.
     */
    Object getAttribute(String key)
    throws ProcessingException;

    /**
     * Get the value of a context attribute.
     * If the attribute is not available the return the
     * <CODE>defaultObject</CODE>.
     */
    Object getAttribute(String key, Object defaultObject)
    throws ProcessingException;

    /**
     * Get a copy of the first node specified by the path.
     * If the node does not exist, <CODE>null</CODE> is returned.
     */
    Node getSingleNode(String path)
    throws ProcessingException;

    /**
     * Get a copy of all nodes specified by the path.
     */
    NodeList getNodeList(String path)
    throws ProcessingException;

    /**
     * Set the value of a node. The node is copied before insertion.
     */
    void setNode(String path, Node node)
    throws ProcessingException;

    /**
     * Get the value of this node.
     * This is similiar to the xsl:value-of function.
     * If the node does not exist, <code>null</code> is returned.
     */
    String getValueOfNode(String path)
    throws ProcessingException;

    /**
     * Set the value of a node.
     * All children of the node are removed beforehand and one single text
     * node with the given value is appended to the node.
     */
    void setValueOfNode(String path, String value)
    throws ProcessingException;

    /**
     * Stream the XML directly to the handler.
     * This streams the contents of getXML() to the given handler without
     * creating a DocumentFragment containing a copy of the data.
     * If no data is available (if the path does not exist) <code>false</code> is
     * returned, otherwise <code>true</code>.
     */
    boolean streamXML(String path,
                      ContentHandler contentHandler,
                      LexicalHandler lexicalHandler)
    throws SAXException, ProcessingException;

    /**
     * Try to load XML into the context.
     * If the context does not provide the ability of loading,
     * an exception is thrown.
     */
    void loadXML(String path,
                 SourceParameters parameters,
                 Map              objectModel,
                 SourceResolver   resolver,
                 ServiceManager   manager)
    throws SAXException, ProcessingException, IOException;

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    void saveXML(String path,
                 SourceParameters parameters,
                 Map              objectModel,
                 SourceResolver   resolver,
                 ServiceManager   manager)
    throws SAXException, ProcessingException, IOException;
}
