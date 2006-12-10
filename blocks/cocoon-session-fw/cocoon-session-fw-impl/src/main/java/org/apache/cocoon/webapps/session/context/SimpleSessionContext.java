/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.webapps.session.xml.XMLUtil;
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
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
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

    /** The source resolver */
    private SourceResolver resolver;
    
    /**
     * Constructor
     */
    public SimpleSessionContext(XPathProcessor xPathProcessor, SourceResolver resolver)
    throws ProcessingException {
        this.data = DOMUtil.createDocument();
        this.data.appendChild(data.createElementNS(null, "context"));
        this.xpathProcessor = xPathProcessor;
        this.resolver = resolver;
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
                        SourceParameters  parameters)
    throws SAXException, ProcessingException, IOException {
        if (this.loadResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support loading.");
        }
        Source source = null;
        try {
            source = SourceUtil.getSource(this.loadResource, null, parameters, this.resolver);
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
                        SourceParameters parameters)
    throws SAXException, ProcessingException, IOException {
        if (this.saveResource == null) {
            throw new ProcessingException("The context " + this.name + " does not support saving.");
        }
        DocumentFragment frag = this.getXML(path);
        if (frag == null) {
            // create empty fake document
            frag = DOMUtil.createDocument().createDocumentFragment();
        }

        XMLUtil.writeDOM(this.saveResource,
                            null,
                            parameters,
                            frag,
                            this.resolver,
                            "xml");
    }

}

