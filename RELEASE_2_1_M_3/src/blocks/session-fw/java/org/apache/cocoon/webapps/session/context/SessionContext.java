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
import org.apache.avalon.framework.component.ComponentManager;
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
 * @version CVS $Id: SessionContext.java,v 1.1 2003/03/09 00:06:10 pier Exp $
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
                 Map                objectModel,
                 SourceResolver     resolver,
                 ComponentManager   manager)
    throws SAXException, ProcessingException, IOException;

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    void saveXML(String path,
                 SourceParameters parameters,
                 Map                objectModel,
                 SourceResolver     resolver,
                 ComponentManager   manager)
    throws SAXException, ProcessingException, IOException;
}
