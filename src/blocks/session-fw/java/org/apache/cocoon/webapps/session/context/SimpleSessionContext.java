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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.NodeListImpl;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *  This is a simple implementation of the session context.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SimpleSessionContext.java,v 1.7 2003/12/18 14:29:03 cziegeler Exp $
*/
public final class SimpleSessionContext
implements SessionContext {

    /** Context name */
    private String   name;

    /** The content of the context */
    private Document data;

    /** The attributes */
    private Map attributes = new HashMap();

    /** load resource */
    private String loadResource;
    
    /** save resource */
    private String saveResource;

    /** The XPath Processor */
    private XPathProcessor xpathProcessor;

    /**
     * Constructor
     */
    public SimpleSessionContext(XPathProcessor xPathProcessor)
    throws ProcessingException {
        data = DOMUtil.createDocument();
        data.appendChild(data.createElementNS(null, "context"));
        this.xpathProcessor = xPathProcessor;
    }

    /**
     * Get the name of the context
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.session.context.SessionContext#setup(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setup(String value, String loadResource, String saveResource) {
        this.name = value;
        this.loadResource = loadResource;
        this.saveResource = saveResource;
    }

    public synchronized DocumentFragment getXML(String path)
    throws ProcessingException {
        DocumentFragment result = null;
        NodeList list;
        path = this.createPath(path);

        String[] pathComponents = DOMUtil.buildPathArray(path);
        if (pathComponents == null) {
            list = this.xpathProcessor.selectNodeList(this.data, path);
        } else {
            list = DOMUtil.getNodeListFromPath(data, pathComponents);
        }

        if (list != null && list.getLength() > 0) {
            Document doc = DOMUtil.createDocument();
            result = doc.createDocumentFragment();

            for(int i = 0; i < list.getLength(); i++) {

                // the found node is either an attribute or an element
                if (list.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                    // if it is an attribute simple create a new text node with the value of the attribute
                    result.appendChild(doc.createTextNode(list.item(i).getNodeValue()));
                } else {
                    // now we have an element
                    // copy all children of this element in the resulting tree
                    NodeList childs = list.item(i).getChildNodes();
                    if (childs != null) {
                        for(int m = 0; m < childs.getLength(); m++) {
                            result.appendChild(doc.importNode(childs.item(m), true));
                        }
                    }
                }
            }
        }

        return result;
    }


    public synchronized void setXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        path = this.createPath(path);
        Node node = DOMUtil.selectSingleNode(data, path, this.xpathProcessor);
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            // now we have to serialize the fragment to a string and insert this
            Attr attr = (Attr)node;
            attr.setNodeValue(DOMUtil.getValueOfNode(fragment));

        } else {

            // remove old childs
            while (node.hasChildNodes() == true) {
                node.removeChild(node.getFirstChild());
            }

            // Insert new childs
            NodeList childs = fragment.getChildNodes();
            if (childs != null && childs.getLength() > 0) {
                for(int i = 0; i < childs.getLength(); i++) {
                    Node n = data.importNode(childs.item(i), true);
                    node.appendChild(n);
                }
            }
        }
    }

    /**
     * Append a document fragment at the given path. The implementation of this
     * method is context specific.
     * Usually the children of the fragment are appended as new children of the
     * node specified by the path.
     * If the path is not existent it is created.
     */
    public synchronized void appendXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        path = this.createPath(path);
        Node node = DOMUtil.selectSingleNode(data, path, this.xpathProcessor);
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr attr;

            if (node.getNodeValue() != null || node.getNodeValue().trim().length() > 0) {
                // this is an existing attr, create a new one
                attr = node.getOwnerDocument().createAttributeNS(null, node.getNodeName());
                node.getParentNode().appendChild(attr);
            } else {
                attr = (Attr)node;
            }

            // now we have to serialize the fragment to a string and insert this
            attr.setNodeValue(DOMUtil.getValueOfNode(fragment));
        } else {

            // Insert new childs
            NodeList childs = fragment.getChildNodes();
            if (childs != null && childs.getLength() > 0) {
                for(int i = 0; i < childs.getLength(); i++) {
                    Node n = data.importNode(childs.item(i), true);
                    node.appendChild(n);
                }
            }
        }
    }

    /**
     * Build path
     */
    private String createPath(String path) {
        if (path == null) path ="/";
        if (!path.startsWith("/") ) path = "/" + path;
        path = "context" + path;
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    /**
     * Remove nodes
     */
    public synchronized void removeXML(String path)
    throws ProcessingException {
        NodeList list;
        path = this.createPath(path);

        String[] pathComponents = DOMUtil.buildPathArray(path);
        if (pathComponents == null) {
            list = this.xpathProcessor.selectNodeList(this.data, path);
        } else {
            list = DOMUtil.getNodeListFromPath(data, pathComponents);
        }
        if (list != null && list.getLength() > 0) {
            int  len = list.getLength();
            Node child;
            for(int i = 0; i < len; i++) {
                child = list.item(len - 1 -i);
                child.getParentNode().removeChild(child);
            }
        }
    }

    /**
     * Get a copy the first node specified by the path.
     */
    public synchronized Node getSingleNode(String path)
    throws ProcessingException {
        Node result = null;

        path = this.createPath(path);

        try {
            result = DOMUtil.getSingleNode(data, path, this.xpathProcessor);
            if (result != null) result = result.cloneNode(true);
        } catch (javax.xml.transform.TransformerException localException) {
            throw new ProcessingException("TransformerException: " + localException, localException);
        }

        return result;
    }

    /**
     * Get a copy all the nodes specified by the path.
     */
    public synchronized NodeList getNodeList(String path)
    throws ProcessingException {
        NodeList result = null;

        path = this.createPath(path);

        String[] pathComponents = DOMUtil.buildPathArray(path);
        if (pathComponents == null) {
            result = this.xpathProcessor.selectNodeList(this.data, path);
        } else {
            result = DOMUtil.getNodeListFromPath(data, pathComponents);
        }
        // clone list
        if (result != null) {
            result = new NodeListImpl(result);
        }

        return result;
    }

    /**
     * Set the value of a node. The node is copied before insertion.
     */
    public synchronized void setNode(String path, Node node)
    throws ProcessingException {
        if ( path == null || path.equals("/")) {
            data = DOMUtil.createDocument();
            data.appendChild(data.createElementNS(null, "context"));
            data.getFirstChild().appendChild(data.importNode(node, true));
        } else {
            path = this.createPath(path);        
            Node removeNode = DOMUtil.selectSingleNode(data, path, this.xpathProcessor);
            removeNode.getParentNode().replaceChild(data.importNode(node, true), removeNode);
        }
    }


    /**
     * Set a context attribute. If value is null the attribute is removed.
     */
    public synchronized void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * Get a context attribute. If the attribute is not available return null
     */
    public synchronized Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get a context attribute. If the attribute is not available the defaultObject is returned
     */
    public synchronized Object getAttribute(String key, Object defaultObject) {
        Object value = attributes.get(key);
        if (value == null) value = defaultObject;
        return value;
    }

    /**
     * Get the value of this node. This is similiar to the xsl:value-of
     * function. If the node does not exist, <code>null</code> is returned.
     */
    public synchronized String getValueOfNode(String path)
    throws ProcessingException {
        String value = null;

        path = this.createPath(path); // correct path
        value = DOMUtil.getValueOf(data, path, this.xpathProcessor);

        return value;
    }

    /**
     * Set the value of a node.
     */
    public synchronized void setValueOfNode(String path, String value)
    throws ProcessingException {
        path = this.createPath(path); // correct path

        Node node = DOMUtil.selectSingleNode(data, path, this.xpathProcessor);
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr attr = (Attr)node;
            attr.setNodeValue(value);

        } else {

            // remove old childs
            while (node.hasChildNodes() == true) {
                node.removeChild(node.getFirstChild());
            }

            node.appendChild(node.getOwnerDocument().createTextNode(value));
        }
    }

    /**
     * Stream the XML directly to the handler. This streams the contents of getXML()
     * to the given handler without creating a DocumentFragment containing a copy
     * of the data
     */
    public synchronized boolean streamXML(String path, ContentHandler contentHandler,
                           LexicalHandler lexicalHandler)
    throws SAXException, ProcessingException {
        NodeList list;
        boolean  streamed = false;
        path = this.createPath(path);

        String[] pathComponents = DOMUtil.buildPathArray(path);
        if (pathComponents == null) {
            list = this.xpathProcessor.selectNodeList(this.data, path);
        } else {
            list = DOMUtil.getNodeListFromPath(data, pathComponents);
        }
        if (list != null && list.getLength() > 0) {
            streamed = true;
            for(int i = 0; i < list.getLength(); i++) {

                // the found node is either an attribute or an element
                if (list.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                    // if it is an attribute simple create a new text node with the value of the attribute
                    String value = list.item(i).getNodeValue();
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                } else {
                    // now we have an element
                    // stream all children of this element to the resulting tree
                    NodeList childs = list.item(i).getChildNodes();
                    if (childs != null) {
                        for(int m = 0; m < childs.getLength(); m++) {
                            IncludeXMLConsumer.includeNode(childs.item(m), contentHandler, lexicalHandler);
                        }
                    }
                }
            }
         }

        return streamed;
    }

    /**
     * Try to load XML into the context.
     * If the context does not provide the ability of loading,
     * an exception is thrown.
     */
    public void loadXML(String            path,
                        SourceParameters  parameters,
                        Map               objectModel,
                        SourceResolver    resolver,
                        ServiceManager    manager)
    throws SAXException, ProcessingException, IOException {
        if (this.loadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }
        Source source = null;
        try {
            source = SourceUtil.getSource(this.loadResource, null, parameters, resolver);
            Document doc = SourceUtil.toDOM(source);
            DocumentFragment df = doc.createDocumentFragment();
            df.appendChild(doc.getDocumentElement());
            this.setXML(path, df);
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    public void saveXML(String path,
                        SourceParameters parameters,
                        Map              objectModel,
                        SourceResolver   resolver,
                        ServiceManager   manager)
    throws SAXException, ProcessingException, IOException {
        if (this.saveResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support saving.");
        }
        DocumentFragment frag = this.getXML(path);
        if (frag == null) {
            // create empty fake document
            frag = DOMUtil.createDocument().createDocumentFragment();
        }

        SourceUtil.writeDOM(this.saveResource,
                            null,
                            parameters,
                            frag,
                            resolver,
                            "xml");
    }

}

