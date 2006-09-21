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
package org.apache.cocoon.xml.dom;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xml.xpath.NodeListImpl;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.excalibur.xml.xpath.XPathUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class is a utility class for miscellaneous DOM functions, like getting
 * and setting values of nodes.
 * 
 * @version $Id$
 */
public final class DOMUtil {

    private static final String XPATH_IS_REQUIRED = "XPath is required.";

    /**
     * Get the owner of the DOM document belonging to the node. This works even
     * if the node is the document itself.
     * 
     * @param node
     *            The node.
     * @return The corresponding document.
     */
    public static Document getOwnerDocument(Node node) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }

    /**
     * Get the value of the node specified by the XPath. This works similar to
     * &lt;xsl:value-of&gt;. If the node does not exist <CODE>null</CODE> is
     * returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @return The value of the node or <CODE>null</CODE>
     */
    public static String getValueOfNode(XPathProcessor processor, Node root, String path) throws ProcessingException {
        if (path == null) {
            throw new ProcessingException(XPATH_IS_REQUIRED);
        }
        if (root != null) {
            path = StringUtils.strip(path, "/");
            Node node = XPathUtil.searchSingleNode(processor, root, path);
            if (node != null) {
                return getValueOfNode(node);
            }
        }
        return null;
    }

    /**
     * Get the value of the node specified by the XPath. This works similar to
     * &lt;xsl:value-of&gt;. If the node is not found the <CODE>defaultValue</CODE>
     * is returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param defaultValue
     *            The default value if the node does not exist.
     * @return The value of the node or <CODE>defaultValue</CODE>
     */
    public static String getValueOfNode(XPathProcessor processor, Node root, String path, String defaultValue)
            throws ProcessingException {
        String value = getValueOfNode(processor, root, path);
        if (value == null)
            value = defaultValue;

        return value;
    }

    /**
     * Get the boolean value of the node specified by the XPath. This works
     * similar to &lt;xsl:value-of&gt;. If the node exists and has a value this
     * value is converted to a boolean, e.g. "true" or "false" as value will
     * result into the corresponding boolean values.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @return The boolean value of the node.
     * @throws ProcessingException
     *             If the node is not found.
     */
    public static boolean getValueOfNodeAsBoolean(XPathProcessor processor, Node root, String path)
            throws ProcessingException {
        String value = getValueOfNode(processor, root, path);
        if (value == null) {
            throw new ProcessingException("No such node: " + path);
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * Get the boolean value of the node specified by the XPath. This works
     * similar to &lt;xsl:value-of&gt;. If the node exists and has a value this
     * value is converted to a boolean, e.g. "true" or "false" as value will
     * result into the corresponding boolean values. If the node does not exist,
     * the <CODE>defaultValue</CODE> is returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param defaultValue
     *            Default boolean value.
     * @return The value of the node or <CODE>defaultValue</CODE>
     */
    public static boolean getValueOfNodeAsBoolean(XPathProcessor processor, Node root, String path, boolean defaultValue)
            throws ProcessingException {
        String value = getValueOfNode(processor, root, path);
        if (value != null) {
            return BooleanUtils.toBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Get the value of the DOM node. The value of a node is the content of the
     * first text node. If the node has no text nodes, <code>null</code> is
     * returned.
     */
    public static String getValueOfNode(Node node) {
        if (node != null) {
            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                return node.getNodeValue();
            } else {
                node.normalize();
                NodeList childs = node.getChildNodes();
                int i = 0;
                int length = childs.getLength();
                while (i < length) {
                    if (childs.item(i).getNodeType() == Node.TEXT_NODE) {
                        return childs.item(i).getNodeValue().trim();
                    } else {
                        i++;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the value of the node. The value of the node is the content of the
     * first text node. If the node has no text nodes the <CODE>defaultValue</CODE>
     * is returned.
     */
    public static String getValueOfNode(Node node, String defaultValue) {
        return StringUtils.defaultString(getValueOfNode(node), defaultValue);
    }

    /**
     * Set the value of the DOM node. All current children of the node are
     * removed and a new text node with the value is appended.
     */
    public static void setValueOfNode(Node node, String value) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            node.setNodeValue(value);
        } else {
            while (node.hasChildNodes()) {
                node.removeChild(node.getFirstChild());
            }
            node.appendChild(node.getOwnerDocument().createTextNode(value));
        }
    }

    /** XML definition for a document */
    private static final String XML_DEFINITION = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    private static final String XML_ROOT_DEFINITION = XML_DEFINITION + "<root>";

    /**
     * Get a document fragment from a <code>Reader</code>. The reader must
     * provide valid XML, but it is allowed that the XML has more than one root
     * node. This xml is parsed by the specified parser instance and a DOM
     * DocumentFragment is created.
     */
    public static DocumentFragment getDocumentFragment(SAXParser parser, Reader stream) throws ProcessingException {
        DocumentFragment frag = null;

        Writer writer;
        Reader reader;
        boolean removeRoot = true;

        try {
            // create a writer,
            // write the root element, then the input from the
            // reader
            writer = new StringWriter();

            writer.write(XML_ROOT_DEFINITION);
            char[] cbuf = new char[16384];
            int len;
            do {
                len = stream.read(cbuf, 0, 16384);
                if (len != -1) {
                    writer.write(cbuf, 0, len);
                }
            } while (len != -1);
            writer.write("</root>");

            // now test if xml input start with <?xml
            String xml = writer.toString();
            String searchString = XML_ROOT_DEFINITION + "<?xml ";
            if (xml.startsWith(searchString)) {
                // now remove the surrounding root element
                xml = xml.substring(XML_ROOT_DEFINITION.length(), xml.length() - 7);
                removeRoot = false;
            }

            reader = new StringReader(xml);

            InputSource input = new InputSource(reader);

            DOMBuilder builder = new DOMBuilder();
            builder.startDocument();
            builder.startElement("", "root", "root", XMLUtils.EMPTY_ATTRIBUTES);

            IncludeXMLConsumer filter = new IncludeXMLConsumer(builder, builder);
            parser.parse(input, filter);

            builder.endElement("", "root", "root");
            builder.endDocument();

            // Create Document Fragment, remove <root>
            final Document doc = builder.getDocument();
            frag = doc.createDocumentFragment();
            final Node root = doc.getDocumentElement().getFirstChild();
            root.normalize();
            if (removeRoot == false) {
                root.getParentNode().removeChild(root);
                frag.appendChild(root);
            } else {
                Node child;
                while (root.hasChildNodes()) {
                    child = root.getFirstChild();
                    root.removeChild(child);
                    frag.appendChild(child);
                }
            }
        } catch (SAXException sax) {
            throw new ProcessingException("SAXException: " + sax, sax);
        } catch (IOException ioe) {
            throw new ProcessingException("IOException: " + ioe, ioe);
        }
        return frag;
    }

    /**
     * Create a parameter object from xml. The xml is flat and consists of
     * elements which all have exactly one text node: <parone>value_one<parone>
     * <partwo>value_two<partwo> A parameter can occur more than once with
     * different values. If <CODE>source</CODE> is not specified a new
     * parameter object is created otherwise the parameters are added to source.
     */
    public static SourceParameters createParameters(Node fragment, SourceParameters source) {
        SourceParameters par = (source == null ? new SourceParameters() : source);
        if (fragment != null) {
            NodeList childs = fragment.getChildNodes();
            if (childs != null) {
                Node current;
                for (int i = 0; i < childs.getLength(); i++) {
                    current = childs.item(i);

                    // only element nodes
                    if (current.getNodeType() == Node.ELEMENT_NODE) {
                        current.normalize();
                        NodeList valueChilds = current.getChildNodes();
                        String key;
                        StringBuffer valueBuffer;
                        String value;

                        key = current.getNodeName();
                        valueBuffer = new StringBuffer();
                        for (int m = 0; m < valueChilds.getLength(); m++) {
                            current = valueChilds.item(m); // attention: current is reused here!
                            if (current.getNodeType() == Node.TEXT_NODE) { // only text nodes
                                if (valueBuffer.length() > 0)
                                    valueBuffer.append(' ');
                                valueBuffer.append(current.getNodeValue());
                            }
                        }
                        value = valueBuffer.toString().trim();
                        if (key != null && value.length() > 0) {
                            par.setParameter(key, value);
                        }
                    }
                }
            }
        }
        return par;
    }

    /**
     * Create a string from a DOM document fragment. Only the top level text
     * nodes are chained together to build the text.
     */
    public static String createText(DocumentFragment fragment) {
        StringBuffer value = new StringBuffer();
        if (fragment != null) {
            NodeList childs = fragment.getChildNodes();
            if (childs != null) {
                Node current;

                for (int i = 0; i < childs.getLength(); i++) {
                    current = childs.item(i);

                    // only text nodes
                    if (current.getNodeType() == Node.TEXT_NODE) {
                        if (value.length() > 0)
                            value.append(' ');
                        value.append(current.getNodeValue());
                    }
                }
            }
        }
        return value.toString().trim();
    }

    /**
     * Compare all attributes of two elements. This method returns true only if
     * both nodes have the same number of attributes and the same attributes
     * with equal values. Namespace definition nodes are ignored
     */
    public static boolean compareAttributes(Element first, Element second) {
        NamedNodeMap attr1 = first.getAttributes();
        NamedNodeMap attr2 = second.getAttributes();
        String value;

        if (attr1 == null && attr2 == null)
            return true;
        int attr1Len = (attr1 == null ? 0 : attr1.getLength());
        int attr2Len = (attr2 == null ? 0 : attr2.getLength());
        if (attr1Len > 0) {
            int l = attr1.getLength();
            for (int i = 0; i < l; i++) {
                if (attr1.item(i).getNodeName().startsWith("xmlns:"))
                    attr1Len--;
            }
        }
        if (attr2Len > 0) {
            int l = attr2.getLength();
            for (int i = 0; i < l; i++) {
                if (attr2.item(i).getNodeName().startsWith("xmlns:"))
                    attr2Len--;
            }
        }
        if (attr1Len != attr2Len)
            return false;
        int i, l;
        int m, l2;
        i = 0;
        l = attr1.getLength();
        l2 = attr2.getLength();
        boolean ok = true;
        // each attribute of first must be in second with the same value
        while (i < l && ok) {
            value = attr1.item(i).getNodeName();
            if (value.startsWith("xmlns:") == false) {
                ok = false;
                m = 0;
                while (m < l2 && ok == false) {
                    if (attr2.item(m).getNodeName().equals(value)) {
                        // same name, same value?
                        ok = attr1.item(i).getNodeValue().equals(attr2.item(m).getNodeValue());
                    }
                    m++;
                }
            }

            i++;
        }
        return ok;
    }

    /**
     * Implementation for <code>String</code> : outputs characters
     * representing the value.
     * 
     * @param parent
     *            The node getting the value
     * @param text
     *            the value
     */
    public static void valueOf(Node parent, String text) throws ProcessingException {
        if (text != null) {
            parent.appendChild(parent.getOwnerDocument().createTextNode(text));
        }
    }

    /**
     * Implementation for <code>XMLizable</code> : outputs the value by
     * calling <code>v.toSax(contentHandler)</code>.
     * 
     * @param parent
     *            The node getting the value
     * @param v
     *            the XML fragment
     */
    public static void valueOf(Node parent, XMLizable v) throws ProcessingException {
        if (v != null) {
            DOMBuilder builder = new DOMBuilder(parent);
            try {
                v.toSAX(builder);
            } catch (SAXException e) {
                throw new ProcessingException(e);
            }
        }
    }

    /**
     * Implementation for <code>org.w3c.dom.Node</code> : converts the Node to
     * a SAX event stream.
     * 
     * @param parent
     *            The node getting the value
     * @param v
     *            the value
     */
    public static void valueOf(Node parent, Node v) throws ProcessingException {
        if (v != null) {
            parent.appendChild(parent.getOwnerDocument().importNode(v, true));
        }
    }

    /**
     * Implementation for <code>java.util.Collection</code> : outputs the
     * value by calling {@link #valueOf(Node, Object)} on each element of the
     * collection.
     * 
     * @param parent
     *            The node getting the value
     * @param v
     *            the XML fragment
     */
    public static void valueOf(Node parent, Collection v) throws ProcessingException {
        if (v != null) {
            Iterator iterator = v.iterator();
            while (iterator.hasNext()) {
                valueOf(parent, iterator.next());
            }
        }
    }

    /**
     * Implementation for <code>java.util.Map</code> : For each entry an
     * element is created with the childs key and value Outputs the value and
     * the key by calling {@link #valueOf(Node, Object)} on each value and key
     * of the Map.
     * 
     * @param parent
     *            The node getting the value
     * @param v
     *            the Map
     */
    public static void valueOf(Node parent, Map v) throws ProcessingException {
        if (v != null) {
            Node mapNode = parent.getOwnerDocument().createElementNS(null, "java.util.map");
            parent.appendChild(mapNode);
            for (Iterator iter = v.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry me = (Map.Entry) iter.next();

                Node entryNode = mapNode.getOwnerDocument().createElementNS(null, "entry");
                mapNode.appendChild(entryNode);

                Node keyNode = entryNode.getOwnerDocument().createElementNS(null, "key");
                entryNode.appendChild(keyNode);
                valueOf(keyNode, me.getKey());

                Node valueNode = entryNode.getOwnerDocument().createElementNS(null, "value");
                entryNode.appendChild(valueNode);
                valueOf(valueNode, me.getValue());
            }
        }
    }

    /**
     * Implementation for <code>Object</code> depending on its class :
     * <ul>
     * <li>if it's an array, call {@link #valueOf(Node, Object)} on all its
     * elements,</li>
     * <li>if it's class has a specific {@link #valueOf(Node, Object)}
     * implementation, use it,</li>
     * <li>else, output it's string representation.</li>
     * </ul>
     * 
     * @param parent
     *            The node getting the value
     * @param v
     *            the value
     */
    public static void valueOf(Node parent, Object v) throws ProcessingException {
        if (v == null) {
            return;
        }

        // Array: recurse over each element
        if (v.getClass().isArray()) {
            Object[] elements = (Object[]) v;

            for (int i = 0; i < elements.length; i++) {
                valueOf(parent, elements[i]);
            }
            return;
        }

        // Check handled object types in case they were not typed in the XSP

        // XMLizable
        if (v instanceof XMLizable) {
            valueOf(parent, (XMLizable) v);
            return;
        }

        // Node
        if (v instanceof Node) {
            valueOf(parent, (Node) v);
            return;
        }

        // Collection
        if (v instanceof Collection) {
            valueOf(parent, (Collection) v);
            return;
        }

        // Map
        if (v instanceof Map) {
            valueOf(parent, (Map) v);
            return;
        }

        // Give up: hope it's a string or has a meaningful string representation
        valueOf(parent, String.valueOf(v));
    }

    /**
     * Use an XPath string to select a single node. XPath namespace prefixes are
     * resolved from the context node, which may not be what you want (see the
     * next method).
     * 
     * @param contextNode
     *            The node to start searching from.
     * @param str
     *            A valid XPath string.
     * @param processor
     *            The XPath processor to use
     * @return The first node found that matches the XPath, or null.
     * 
     * @throws TransformerException
     */
    public static Node getSingleNode(Node contextNode, String str, XPathProcessor processor)
            throws TransformerException {
        String[] pathComponents = buildPathArray(str);
        if (pathComponents == null) {
            return processor.selectSingleNode(contextNode, str);
        } else {
            return getFirstNodeFromPath(contextNode, pathComponents, false);
        }
    }

    /**
     * Return the <CODE>Node</CODE> from the DOM Node <CODE>rootNode</CODE>
     * using the XPath expression <CODE>path</CODE>. If the node does not
     * exist, it is created and then returned. This is a very simple method for
     * creating new nodes. If the XPath contains selectors ([,,,]) or "*" it is
     * of course not possible to create the new node. So if you use such XPaths
     * the node must exist beforehand. An simple exception is if the expression
     * contains attribute test to values (e.g. [@id = 'du' and
     * 
     * @number = 'you'], the attributes with the given values are added. The
     *         attributes must be separated with 'and'. Another problem are
     *         namespaces: XPath requires sometimes selectors for namespaces,
     *         e.g. : /*[namespace-uri()="uri" and local-name()="name"] Creating
     *         such a node with a namespace is not possible right now as we use
     *         a very simple XPath parser which is not able to parse all kinds
     *         of selectors correctly.
     * 
     * @param rootNode
     *            The node to start the search.
     * @param path
     *            XPath expression for searching the node.
     * @param processor
     *            The XPath processor to use
     * @return The node specified by the path.
     * @throws ProcessingException
     *             If no path is specified or the XPath engine fails.
     */
    public static Node selectSingleNode(Node rootNode, String path, XPathProcessor processor)
            throws ProcessingException {
        // Now we have to parse the string
        // First test: path? rootNode?
        if (path == null) {
            throw new ProcessingException(XPATH_IS_REQUIRED);
        }
        if (rootNode == null)
            return rootNode;

        if (path.length() == 0 || path.equals("/"))
            return rootNode;

        // now the first "quick" test is if the node exists using the
        // full XPathAPI
        try {
            Node testNode = getSingleNode(rootNode, path, processor);
            if (testNode != null)
                return testNode;
        } catch (TransformerException local) {
            throw new ProcessingException("Transforming exception during selectSingleNode with path: '" + path
                    + "'. Exception: " + local, local);
        }

        // remove leading "/" oon both ends
        path = StringUtils.strip(path, "/");

        // now step through the nodes!
        Node parent = rootNode;
        int pos;
        int posSelector;
        do {
            pos = path.indexOf("/"); // get next separator
            posSelector = path.indexOf("[");
            if (posSelector != -1 && posSelector < pos) {
                posSelector = path.indexOf("]");
                pos = path.indexOf("/", posSelector);
            }

            String nodeName;
            boolean isAttribute = false;
            if (pos != -1) { // found separator
                nodeName = path.substring(0, pos); // string until "/"
                path = path.substring(pos + 1); // rest of string after "/"
            } else {
                nodeName = path;
            }

            // test for attribute spec
            if (nodeName.startsWith("@")) {
                isAttribute = true;
            }

            Node singleNode;
            try {
                singleNode = getSingleNode(parent, nodeName, processor);
            } catch (TransformerException localException) {
                throw new ProcessingException("XPathUtil.selectSingleNode: " + localException.getMessage(),
                        localException);
            }

            // create node if necessary
            if (singleNode == null) {
                Node newNode;
                // delete XPath selectors
                int posSelect = nodeName.indexOf("[");
                String XPathExp = null;
                if (posSelect != -1) {
                    XPathExp = nodeName.substring(posSelect + 1, nodeName.length() - 1);
                    nodeName = nodeName.substring(0, posSelect);
                }
                if (isAttribute) {
                    try {
                        newNode = getOwnerDocument(rootNode).createAttributeNS(null, nodeName.substring(1));
                        ((Element) parent).setAttributeNodeNS((org.w3c.dom.Attr) newNode);
                        parent = newNode;
                    } catch (DOMException local) {
                        throw new ProcessingException("Unable to create new DOM node: '" + nodeName + "'.", local);
                    }
                } else {
                    try {
                        newNode = getOwnerDocument(rootNode).createElementNS(null, nodeName);
                    } catch (DOMException local) {
                        throw new ProcessingException("Unable to create new DOM node: '" + nodeName + "'.", local);
                    }
                    if (XPathExp != null) {
                        java.util.List attrValuePairs = new java.util.ArrayList(4);
                        boolean noError = true;

                        String attr;
                        String value;
                        // scan for attributes
                        StringTokenizer tokenizer = new StringTokenizer(XPathExp, "= ");
                        while (tokenizer.hasMoreTokens()) {
                            attr = tokenizer.nextToken();
                            if (attr.startsWith("@")) {
                                if (tokenizer.hasMoreTokens()) {
                                    value = tokenizer.nextToken();
                                    if (value.startsWith("'") && value.endsWith("'"))
                                        value = value.substring(1, value.length() - 1);
                                    if (value.startsWith("\"") && value.endsWith("\""))
                                        value = value.substring(1, value.length() - 1);
                                    attrValuePairs.add(attr.substring(1));
                                    attrValuePairs.add(value);
                                } else {
                                    noError = false;
                                }
                            } else if (attr.trim().equals("and") == false) {
                                noError = false;
                            }
                        }
                        if (noError) {
                            for (int l = 0; l < attrValuePairs.size(); l = l + 2) {
                                ((Element) newNode).setAttributeNS(null, (String) attrValuePairs.get(l),
                                        (String) attrValuePairs.get(l + 1));
                            }
                        }
                    }
                    parent.appendChild(newNode);
                    parent = newNode;
                }
            } else {
                parent = singleNode;
            }
        }
        while (pos != -1);
        return parent;
    }

    /**
     * Get the value of the node specified by the XPath. This works similar to
     * &lt;xsl:value-of&gt;. If the node does not exist <CODE>null</CODE> is
     * returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param processor
     *            The XPath processor to use
     * @return The value of the node or <CODE>null</CODE>
     */
    public static String getValueOf(Node root, String path, XPathProcessor processor) throws ProcessingException {
        if (path == null) {
            throw new ProcessingException(XPATH_IS_REQUIRED);
        }
        if (root == null)
            return null;
        path = StringUtils.strip(path, "/");

        try {
            Node node = getSingleNode(root, path, processor);
            if (node != null) {
                return getValueOfNode(node);
            }
        } catch (TransformerException localException) {
            throw new ProcessingException("XPathUtil.selectSingleNode: " + localException.getMessage(), localException);
        }
        return null;
    }

    /**
     * Get the value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node is not found
     * the <CODE>defaultValue</CODE> is returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param defaultValue
     *            The default value if the node does not exist.
     * @param processor
     *            The XPath Processor
     * @return The value of the node or <CODE>defaultValue</CODE>
     */
    public static String getValueOf(Node root, String path, String defaultValue, XPathProcessor processor)
            throws ProcessingException {
        String value = getValueOf(root, path, processor);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Get the boolean value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node exists and has a value
     * this value is converted to a boolean, e.g. "true" or "false" as value
     * will result into the corresponding boolean values.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param processor
     *            The XPath Processor
     * @return The boolean value of the node.
     * @throws ProcessingException
     *             If the node is not found.
     */
    public static boolean getValueAsBooleanOf(Node root, String path, XPathProcessor processor)
            throws ProcessingException {
        String value = getValueOf(root, path, processor);
        if (value == null) {
            throw new ProcessingException("No such node: " + path);
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * Get the boolean value of the node specified by the XPath.
     * This works similar to xsl:value-of. If the node exists and has a value
     * this value is converted to a boolean, e.g. "true" or "false" as value
     * will result into the corresponding boolean values.
     * If the node does not exist, the <CODE>defaultValue</CODE> is returned.
     * 
     * @param root
     *            The node to start the search.
     * @param path
     *            XPath search expression.
     * @param defaultValue
     *            Default boolean value.
     * @param processor
     *            The XPath Processor
     * @return The value of the node or <CODE>defaultValue</CODE>
     */
    public static boolean getValueAsBooleanOf(Node root, String path, boolean defaultValue, XPathProcessor processor)
            throws ProcessingException {
        String value = getValueOf(root, path, processor);
        if (value != null) {
            return Boolean.valueOf(value).booleanValue();
        }
        return defaultValue;
    }

    /**
     * Create a new empty DOM document.
     */
    public static Document createDocument() throws ProcessingException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            documentFactory.setNamespaceAware(true);
            documentFactory.setValidating(false);
            DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch (ParserConfigurationException pce) {
            throw new ProcessingException("Creating document failed.", pce);
        }
    }

    /**
     *  Use an XPath string to select a nodelist.
     *  XPath namespace prefixes are resolved from the contextNode.
     * 
     * @param contextNode
     *            The node to start searching from.
     * @param str
     *            A valid XPath string.
     * @param processor
     *            The XPath Processor
     * @return A NodeIterator, should never be null.
     * 
     * @throws TransformerException
     */
    public static NodeList selectNodeList(Node contextNode, String str, XPathProcessor processor)
            throws TransformerException {
        String[] pathComponents = buildPathArray(str);
        if (pathComponents != null) {
            return getNodeListFromPath(contextNode, pathComponents);
        }
        return processor.selectNodeList(contextNode, str);
    }

    /**
     * Build the input for the get...FromPath methods. If the XPath expression
     * cannot be handled by the methods, <code>null</code> is returned.
     */
    public static String[] buildPathArray(String xpath) {
        String[] result = null;
        if (xpath != null && xpath.charAt(0) != '/') {
            // test
            int components = 1;
            int i, l;
            l = xpath.length();
            boolean found = false;
            i = 0;
            while (i < l && found == false) {
                switch (xpath.charAt(i)) {
                case '[':
                    found = true;
                    break;
                case '(':
                    found = true;
                    break;
                case '*':
                    found = true;
                    break;
                case '@':
                    found = true;
                    break;
                case ':':
                    found = true;
                    break;
                case '/':
                    components++;
                default:
                    i++;
                }
            }
            if (found == false) {
                result = new String[components];
                if (components == 1) {
                    result[components - 1] = xpath;
                } else {
                    i = 0;
                    int start = 0;
                    components = 0;
                    while (i < l) {
                        if (xpath.charAt(i) == '/') {
                            result[components] = xpath.substring(start, i);
                            start = i + 1;
                            components++;
                        }
                        i++;
                    }
                    result[components] = xpath.substring(start);
                }
            }
        }
        return result;
    }

    /**
     * Use a path to select the first occurence of a node. The namespace of a
     * node is ignored!
     * 
     * @param contextNode
     *            The node starting the search.
     * @param path
     *            The path to search the node. The contextNode is searched for a
     *            child named path[0], this node is searched for a child named
     *            path[1]...
     * @param create
     *            If a child with the corresponding name is not found and create
     *            is set, this node will be created.
     */
    public static Node getFirstNodeFromPath(Node contextNode, final String[] path, final boolean create) {
        if (contextNode == null || path == null || path.length == 0)
            return contextNode;
        // first test if the node exists
        Node item = getFirstNodeFromPath(contextNode, path, 0);
        if (item == null && create) {
            int i = 0;
            NodeList childs;
            boolean found;
            int m, l;
            while (contextNode != null && i < path.length) {
                childs = contextNode.getChildNodes();
                found = false;
                if (childs != null) {
                    m = 0;
                    l = childs.getLength();
                    while (found == false && m < l) {
                        item = childs.item(m);
                        if (item.getNodeType() == Node.ELEMENT_NODE && item.getLocalName().equals(path[i])) {
                            found = true;
                            contextNode = item;
                        }
                        m++;
                    }
                }
                if (found == false) {
                    Element e = contextNode.getOwnerDocument().createElementNS(null, path[i]);
                    contextNode.appendChild(e);
                    contextNode = e;
                }
                i++;
            }
            item = contextNode;
        }
        return item;
    }

    /**
     * Private helper method for getFirstNodeFromPath()
     */
    private static Node getFirstNodeFromPath(final Node contextNode, final String[] path, final int startIndex) {
        int i = 0;
        NodeList childs;
        boolean found;
        int l;
        Node item = null;

        childs = contextNode.getChildNodes();
        found = false;
        if (childs != null) {
            i = 0;
            l = childs.getLength();
            while (found == false && i < l) {
                item = childs.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE
                        && path[startIndex].equals(item.getLocalName() != null ? item.getLocalName() : item
                                .getNodeName())) {
                    if (startIndex == path.length - 1) {
                        found = true;
                    } else {
                        item = getFirstNodeFromPath(item, path, startIndex + 1);
                        if (item != null)
                            found = true;
                    }
                }
                if (found == false) {
                    i++;
                }
            }
            if (found == false) {
                item = null;
            }
        }
        return item;
    }

    /**
     * Use a path to select all occurences of a node. The namespace of a node is
     * ignored!
     * 
     * @param contextNode
     *            The node starting the search.
     * @param path
     *            The path to search the node. The contextNode is searched for a
     *            child named path[0], this node is searched for a child named
     *            path[1]...
     */
    public static NodeList getNodeListFromPath(Node contextNode, String[] path) {
        if (contextNode == null)
            return new NodeListImpl();
        if (path == null || path.length == 0) {
            return new NodeListImpl(new Node[] { contextNode });
        }
        NodeListImpl result = new NodeListImpl();
        try {
            getNodesFromPath(result, contextNode, path, 0);
        } catch (NullPointerException npe) {
            // this NPE is thrown because the parser is not configured
            // to use DOM Level 2
            throw new NullPointerException("XMLUtil.getNodeListFromPath() did catch a NullPointerException."
                    + "This might be due to a missconfigured XML parser which does not use DOM Level 2."
                    + "Make sure that you use the XML parser shipped with Cocoon.");
        }
        return result;
    }

    /**
     * Helper method for getNodeListFromPath()
     */
    private static void getNodesFromPath(final NodeListImpl result, final Node contextNode, final String[] path,
            final int startIndex) {
        final NodeList childs = contextNode.getChildNodes();
        int m, l;
        Node item;
        if (startIndex == (path.length - 1)) {
            if (childs != null) {
                m = 0;
                l = childs.getLength();
                while (m < l) {
                    item = childs.item(m);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        // Work around: org.apache.xerces.dom.ElementImpl
                        // doesn't handle getLocalName() correct
                        if (path[startIndex].equals(item.getLocalName() != null ? item.getLocalName() : item
                                .getNodeName())) {
                            result.addNode(item);
                        }
                    }
                    m++;
                }
            }
        } else {
            if (childs != null) {
                m = 0;
                l = childs.getLength();
                while (m < l) {
                    item = childs.item(m);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        // Work around: org.apache.xerces.dom.ElementImpl
                        // doesn't handle getLocalName() correct
                        if (path[startIndex].equals(item.getLocalName() != null ? item.getLocalName() : item
                                .getNodeName())) {
                            getNodesFromPath(result, item, path, startIndex + 1);
                        }
                    }
                    m++;
                }
            }
        }
    }

}
