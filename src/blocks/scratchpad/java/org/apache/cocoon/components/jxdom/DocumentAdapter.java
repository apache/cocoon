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
package org.apache.cocoon.components.jxdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * DOM Wrapper for Java Beans and JavaScript objects utilizing Apache JXPath's Introspector.
 *
 * @version CVS $ID$
 */

public class DocumentAdapter implements Document {
    
    private static final CompiledExpression
        GET_SELF = JXPathContext.compile(".");
    private static final CompiledExpression 
        GET_ATTRS = JXPathContext.compile("@*");
    protected static final CompiledExpression 
        GET_CHILD_NODES = JXPathContext.compile("*");

    protected static NamedNodeMap EMPTY_NODE_MAP = new NamedNodeMap() {
            public Node getNamedItem(String name) {
                return null;
            }
            public Node setNamedItem(Node arg) 
                            throws DOMException {
                notSupported();
                return null;
            }
            public Node removeNamedItem(String name) 
                throws DOMException {
                notSupported();
                return null;
            }
            public Node item(int index) {
                return null;
            }
            public int getLength() {
                return 0;
            }
            public Node getNamedItemNS(String namespaceURI,
                                       String localName) {
                return null;
            }
            public Node setNamedItemNS(Node arg) 
                throws DOMException {
                notSupported();
                return null;
            }
            public Node removeNamedItemNS(String namespaceURI,
                                          String localName)
                throws DOMException {
                notSupported();
                return null;
            }
        };

    protected static final NodeList EMPTY_NODE_LIST = new NodeList() {
            public int getLength() {
                return 0;
            }
            public Node item(int i) {
                return null;
            }
        };

    private static final JXPathContextFactory jxpathContextFactory = 
        JXPathContextFactory.newInstance();

    protected static void notSupported() throws DOMException {
        throw new UnsupportedOperationException("Not Supported");
    }

    protected static JXPathContext newContext(Object obj) {
        return jxpathContextFactory.newContext(null, obj);
    }

    private ElementAdapter root;

    public DocumentAdapter(Object obj, String tagName) {
        root = new ElementAdapter(this, 
                                  GET_SELF.getPointer(newContext(obj), "."),
                                  -10,
                                  tagName,
                                  obj);
    }

    public abstract class NodeAdapter implements Node {
        Node parent;
        Pointer ptr;
        JXPathContext context;

        public Object unwrap() {
            if (ptr == null) {
                return null;
            }
            return ptr.getNode();
        }

        NodeAdapter(Node parent, Pointer ptr) {
            this.parent = parent;
            this.ptr = ptr;
        }

        JXPathContext getContext() {
            if (this.context == null) {
                this.context = newContext(ptr.getNode());
            }
            return this.context;
        }

        JXPathContext getParentContext() {
            if (this.parent instanceof NodeAdapter) {
                NodeAdapter par = (NodeAdapter)parent;
                return par.getContext();
            }
            return null;
        }

        Pointer getPointer(CompiledExpression expr) {
            return expr.getPointer(getParentContext(), "???");
        }

        Object getValue(CompiledExpression expr) {
            return expr.getValue(getContext());
        }

        abstract public String getNodeName();

        public String getNodeValue() {
            return "";
        }

        public void setNodeValue(String nodeValue)
            throws DOMException {
            notSupported();
        }

        abstract public short getNodeType();

        public Node getParentNode() {
            return null;
        }

        public NodeList getChildNodes() {
            return EMPTY_NODE_LIST;
        }

        public Node getFirstChild() {
            return null;
        }

        public Node getLastChild() {
            return null;
        }

        public Node getPreviousSibling() {
            return null;
        }

        public Node getNextSibling() {
            return null;
        }

        public NamedNodeMap getAttributes() {
            return EMPTY_NODE_MAP;
        }

        public Document getOwnerDocument() {
            return DocumentAdapter.this;
        }

        public Node insertBefore(Node newChild, 
                                 Node refChild)
            throws DOMException {
            notSupported();
            return null;
        }

        public Node replaceChild(Node newChild, 
                                 Node oldChild)
            throws DOMException {
            notSupported();
            return null;
        }

        public Node removeChild(Node oldChild)
            throws DOMException {
            notSupported();
            return null;
        }

        public Node appendChild(Node newChild)
            throws DOMException {
            notSupported();
            return null;
        }

        public boolean hasChildNodes() {
            return false;
        }

        public Node cloneNode(boolean deep) {
            notSupported();
            return null;
        }        

        public void normalize() {
        }

        public boolean isSupported(String feature, 
                                   String version) {
            return false;
        }

        public String getNamespaceURI() {
            return null;
        }

        public String getPrefix() {
            return null;
        }

        public void setPrefix(String prefix)
            throws DOMException {
            notSupported();
        }

        public String getLocalName() {
            return getNodeName();
        }

        public boolean hasAttributes() {
            return false;
        }

    }

    public class TextAdapter extends NodeAdapter implements Text {

        Object data;
        String strValue;

        TextAdapter(Node parent, Pointer ptr, Object data) {
            super(parent, ptr);
            this.data = data;
        }

        public Object unwrap() {
            return data;
        }

        public Node getParentNode() {
            return parent;
        }

        public short getNodeType() {
            return TEXT_NODE;
        }

        public String getNodeName() {
            return "#text";
        }

        public String getNodeValue() {
            if (strValue == null) {
                if (data instanceof Boolean) {
                    if (((Boolean)data).booleanValue()) {
                        strValue = "true";
                    } else {
                        strValue = ""; // in XPath false is the empty string
                    }
                } else {
                    strValue = String.valueOf(data);
                }
            }
            return strValue;
        }

        public String getData()
            throws DOMException {
            return getNodeValue();
        }

        public void setData(String data)
            throws DOMException {
            notSupported();
        }

        public int getLength() {
            return getData().length();
        }

        public String substringData(int offset, 
                                    int count)
            throws DOMException {
            return getData().substring(0, count);
        }

        public void appendData(String arg)
            throws DOMException {
            notSupported();
        }

        public void insertData(int offset, 
                               String arg)
            throws DOMException {
            notSupported();
        }

        public void deleteData(int offset, 
                               int count)
            throws DOMException {
            notSupported();
        }

        public void replaceData(int offset, 
                                int count, 
                                String arg)
            throws DOMException {
            notSupported();
        }

        public Text splitText(int offset)
            throws DOMException {
            notSupported();
            return null;
        }

    }

    public class ElementAdapter extends NodeAdapter implements Element {

        int myIndex;
        String tagName;
        Object nodeValue;
        NodeList childNodes;
        NamedNodeMap attributes;
        Node nextSibling, prevSibling;

        public Object unwrap() {
            return nodeValue;
        }

        JXPathContext getContext() {
            if (context == null) {
                context = newContext(nodeValue);
            }
            return context;
        }

        ElementAdapter(Node parent, Pointer ptr, int index, String tagName,
                       Object nodeValue) {
            super(parent, ptr);
            myIndex = index;
            this.tagName = tagName;
            if (nodeValue == null) {
                nodeValue = "";
            }
            this.nodeValue = nodeValue;
            if (nodeValue instanceof String ||
                nodeValue instanceof Boolean ||
                nodeValue instanceof Number) {
                final TextAdapter text = new TextAdapter(this, ptr,
                                                         nodeValue);
                childNodes = new NodeList() {
                        public int getLength() {
                            return 1;
                        }
                        public Node item(int i) {
                            return text;
                        }
                        
                    };
            }
        }

        public short getNodeType() {
            return ELEMENT_NODE;
        }

        public Node getParentNode() {
            return parent;
        }

        public String getTagName() {
            return tagName;
        }

        public String getNodeName() {
            return getTagName();
        }

        public String getLocalName() {
            return tagName;
        }
        
        public String getNodeValue() {
            if (ptr == null) {
                return "";
            }
            return String.valueOf(nodeValue);
        }

        public boolean hasChildNodes() {
            return getChildNodes().getLength() > 0;
        }

        public NodeList getChildNodes() {
            if (childNodes == null) {
                final List nodeList = new ArrayList();
                childNodes = new NodeList() {
                        public int getLength() {
                            return nodeList.size();
                       }
                        public Node item(int i) {
                            return (Node)nodeList.get(i);
                        }
                    };
                Iterator iter = GET_CHILD_NODES.iteratePointers(getContext());
                for (int i = 0; iter.hasNext(); i++) {
                    NodePointer p = (NodePointer)iter.next();
                    Object value = p.getNode();
                    if (value instanceof NodeAdapter) {
                        p = (NodePointer) ((NodeAdapter)value).ptr;
                        value = p.getNode();
                    } else if (value instanceof DocumentAdapter) {
                        value = ((DocumentAdapter)value).unwrap();
                    }
                    if (value instanceof Node) {
                        nodeList.add(value);
                    } else {
                        QName q = p.getName();
                        nodeList.add(new ElementAdapter(this, p, i, 
                                                        q.getName(),
                                                        value));
                    }
                }
            }
            return childNodes;
        }

        public Node getFirstChild() {
            getChildNodes();
            if (childNodes.getLength() > 0) {
                return childNodes.item(0);
            }
            return null;
        }
        
        public Node getLastChild() {
            getChildNodes();
            if (childNodes.getLength() > 0) {
                return childNodes.item(childNodes.getLength()-1);
            }
            return null;
        }
        
        public Node getPreviousSibling() {
            if (prevSibling == null) {
                if (parent instanceof ElementAdapter) {
                    prevSibling = ((ElementAdapter)parent).getPreviousSibling(myIndex);
                }
            }
            return prevSibling;
        }

        Node getPreviousSibling(int index) {
            int siblingIndex = index -1;
            getChildNodes();
            if (siblingIndex < 0 || 
                siblingIndex >= childNodes.getLength()) return null;
            return childNodes.item(siblingIndex);
        }

        public Node getNextSibling() {
            if (nextSibling == null) {
                if (parent instanceof ElementAdapter) {
                    nextSibling = ((ElementAdapter)parent).getNextSibling(myIndex);
                }
            }
            return nextSibling;
        }

        Node getNextSibling(int index) {
            int siblingIndex = index +1;
            getChildNodes();
            if (siblingIndex < 0 || 
                siblingIndex >= childNodes.getLength()) return null;
            return childNodes.item(siblingIndex);
        }
        
        public class AttrAdapter extends NodeAdapter implements Attr {

            final NodePointer np;

            AttrAdapter(Node par, NodePointer np) {
                super(par, np);
                this.np = np;
            }
            public short getNodeType() {
                return ATTRIBUTE_NODE;
            }
            public String getNodeName() {
                return getName();
            }
            public String getNodeValue() {
                return getValue();
            }
            public String getName() {
                return np.getName().getName();
            }
            public boolean getSpecified() {
                return true;
            }
            public String getValue() {
                Object val = np.getValue();
                if (val == null) val = "";
                return String.valueOf(val);
            }
            public void setValue(String value)
                throws DOMException {
                notSupported();
            }
            public Element getOwnerElement() {
                return ElementAdapter.this;
            }
        }

        
        public NamedNodeMap getAttributes() {
            if (true) return EMPTY_NODE_MAP;
            if (attributes == null) {
                Iterator iter = GET_ATTRS.iteratePointers(getContext());
                final List attrList = new ArrayList();
                final Map nameMap = new HashMap();
                final Map qnameMap = new HashMap();
                while (iter.hasNext()) {
                    final NodePointer np = (NodePointer)iter.next();
                    Attr attr = new AttrAdapter(this, np);
                    attrList.add(attr);
                    String localName = np.getName().getName();
                    nameMap.put(localName, attr);
                    qnameMap.put("{"+np.getNamespaceURI() + "}" +
                                 localName, attr);
                }
                attributes = new NamedNodeMap() {
                        public Node getNamedItem(String name) {
                            return (Node)nameMap.get(name);
                        }
                        public Node setNamedItem(Node arg) 
                            throws DOMException {
                            notSupported();
                            return null;
                        }
                        public Node removeNamedItem(String name) 
                            throws DOMException {
                            notSupported();
                            return null;
                        }
                        public Node item(int index) {
                                return (Node)attrList.get(index);
                        }
                        public int getLength() {
                            return attrList.size();
                        }
                        public Node getNamedItemNS(String namespaceURI,
                                                   String localName) {
                            return (Node)
                                qnameMap.get("{"+ namespaceURI + "}"+
                                             localName);
                            
                        }
                        public Node setNamedItemNS(Node arg) 
                            throws DOMException {
                            notSupported();
                            return null;
                        }
                        public Node removeNamedItemNS(String namespaceURI,
                                                      String localName)
                            throws DOMException {
                            notSupported();
                            return null;
                        }
                    };
            }
            return attributes;
        }

        public String getAttribute(String name) {
            Attr a = getAttributeNode(name);
            if (a == null) return null;
            return a.getValue();
        }

        public void setAttribute(String name, 
                                 String value)
            throws DOMException {
            notSupported();
        }

        public void removeAttribute(String name)
            throws DOMException {
            notSupported();
        }

        public Attr getAttributeNode(String name) {
            NamedNodeMap map = getAttributes();
            if (map == null) return null;
            return (Attr)map.getNamedItem(name);
        }

        public Attr setAttributeNode(Attr newAttr)
            throws DOMException {
            notSupported();
            return null;
        }

        public Attr removeAttributeNode(Attr oldAttr)
            throws DOMException {
            notSupported();
            return null;
        }

        public NodeList getElementsByTagName(String name) {
            return EMPTY_NODE_LIST;
        }

        public String getAttributeNS(String namespaceURI, 
                                     String localName) {
            Attr a = getAttributeNodeNS(namespaceURI, localName);
            if (a == null) return null;
            return a.getValue();
        }

        public void setAttributeNS(String namespaceURI, 
                                   String qualifiedName, 
                                   String value)
            throws DOMException {
            notSupported();
        }

        public void removeAttributeNS(String namespaceURI, 
                                      String localName)
            throws DOMException {
            notSupported();
        }

        public Attr getAttributeNodeNS(String namespaceURI, 
                                       String localName) {
            NamedNodeMap map = getAttributes();
            if (map == null) return null;
            return (Attr)map.getNamedItemNS(namespaceURI, localName);
        }

        public Attr setAttributeNodeNS(Attr newAttr)
            throws DOMException {
            notSupported();
            return null;
        }

        public NodeList getElementsByTagNameNS(String namespaceURI, 
                                               String localName) {
            return EMPTY_NODE_LIST;
        }

        public boolean hasAttribute(String name) {
            return getAttributeNode(name) != null;
        }

        public boolean hasAttributeNS(String namespaceURI, 
                                      String localName) {
            return getAttributeNodeNS(namespaceURI, localName) != null;
        }

        public boolean hasAttributes() {
            return getAttributes().getLength() > 0;
        }
        
    }

    public DocumentType getDoctype() {
        return null;
    }

    public DOMImplementation getImplementation() {
        return null;
    }

    public Element getDocumentElement() {
        return root;
    }

    public Element createElement(String tagName)
        throws DOMException {
        notSupported();
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        return null;
    }

    public Text createTextNode(String data) {
        return null;
    }

    public Comment createComment(String data) {
        return null;
    }

    public CDATASection createCDATASection(String data)
        throws DOMException {
        return null;
    }

    public ProcessingInstruction 
        createProcessingInstruction(String target, 
                                    String data)
        throws DOMException {
        return null;
    }

    public Attr createAttribute(String name)
        throws DOMException {
        return null;
    }

    public EntityReference createEntityReference(String name)
        throws DOMException {
        return null;
    }

    public NodeList getElementsByTagName(String tagname) {
        return null;
    }

    public Node importNode(Node importedNode, 
                           boolean deep)
        throws DOMException {
        return null;
    }

    public Element createElementNS(String namespaceURI, 
                                   String qualifiedName)
        throws DOMException {
        return null;
    }

    public Attr createAttributeNS(String namespaceURI, 
                                  String qualifiedName)
        throws DOMException {
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, 
                                           String localName) {
        return null;
    }

    public Element getElementById(String elementId) {
        return null;
    }

    public String getNodeName() {
        return root.getNodeName();
    }

    public String getNodeValue()
        throws DOMException {
        return root.getNodeValue();
    }

    public void setNodeValue(String nodeValue)
        throws DOMException {
        notSupported();
    }

    public short getNodeType() {
        return DOCUMENT_NODE;
    }

    public Node getParentNode() {
        return null;
    }

    public NodeList getChildNodes() {
        return root.getChildNodes();
    }

    public Node getFirstChild() {
        return root.getFirstChild();
    }

    public Node getLastChild() {
        return root.getLastChild();
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return root.getAttributes();
    }

    public Document getOwnerDocument() {
        return this;
    }

    public Node insertBefore(Node newChild, 
                             Node refChild)
        throws DOMException {
        notSupported();
        return null;
    }

    public Node replaceChild(Node newChild, 
                             Node oldChild)
        throws DOMException {
        notSupported();
        return null;
    }

    public Node removeChild(Node oldChild)
        throws DOMException {
        notSupported();
        return null;
    }

    public Node appendChild(Node newChild)
        throws DOMException {
        notSupported();
        return null;
    }

    public boolean hasChildNodes() {
        return root.hasChildNodes();
    }

    public Node cloneNode(boolean deep) {
        notSupported();
        return null;
    }        

    public void normalize() {
        root.normalize();
    }

    public boolean isSupported(String feature, 
                               String version) {
        return false;
    }

    public String getNamespaceURI() {
        return root.getNamespaceURI();
    }

    public String getPrefix() {
        return root.getPrefix();
    }

    public void setPrefix(String prefix)
        throws DOMException {
        notSupported();
    }

    public String getLocalName() {
        return root.getLocalName();
    }

    public boolean hasAttributes() {
        return root.hasAttributes();
    }


    public Object unwrap() {
        return root.unwrap();
    }
}
