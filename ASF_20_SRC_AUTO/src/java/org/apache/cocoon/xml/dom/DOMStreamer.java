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
package org.apache.cocoon.xml.dom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.EmbeddedXMLPipe;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>DOMStreamer</code> is a utility class that will generate SAX
 * events from a W3C DOM Document.
 *
 * <p>The DOMStreamer uses a different strategy based on the value of the
 * normalizeNamespaces property:
 * <ul>
 * <li>if true (the default), the DOMStreamer will normalize namespace
 * declarations (i.e. add missing xmlns attributes or correct them). See
 * also {@link NamespaceNormalizingDOMStreamer}.
 * <li>if false, the standard JAXP identity transformer is used.
 * </ul>
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: DOMStreamer.java,v 1.14 2004/03/05 13:03:02 bdelacretaz Exp $
 */
public class DOMStreamer implements XMLProducer {

    /** Default value for normalizeNamespaces. */
    private final boolean DEFAULT_NORMALIZE_NAMESPACES = true;

    /** Indicates whether namespace normalization should happen. */
    protected boolean normalizeNamespaces = DEFAULT_NORMALIZE_NAMESPACES;

    /** DOMStreamer used in case of namespace normalization. */
    protected NamespaceNormalizingDOMStreamer namespaceNormalizingDOMStreamer = new NamespaceNormalizingDOMStreamer();

    /** DOMStreamer used when namespace normalization should not explicitely happen. */
    protected DefaultDOMStreamer defaultDOMStreamer = new DefaultDOMStreamer();

    /** The transformer factory shared by all instances (only used by DefaultDOMStreamer) */
    protected static TransformerFactory factory = TransformerFactory.newInstance();

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer() {
        super();
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(XMLConsumer consumer) {
        this(consumer, consumer);
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(ContentHandler content) {
        this(content, null);
        if (content instanceof LexicalHandler) {
            defaultDOMStreamer.setLexicalHandler((LexicalHandler) content);
            namespaceNormalizingDOMStreamer.setLexicalHandler((LexicalHandler) content);
        }
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(ContentHandler content, LexicalHandler lexical) {
        this();
        defaultDOMStreamer.setContentHandler(content);
        defaultDOMStreamer.setLexicalHandler(lexical);
        namespaceNormalizingDOMStreamer.setContentHandler(content);
        namespaceNormalizingDOMStreamer.setLexicalHandler(lexical);
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        defaultDOMStreamer.setContentHandler(consumer);
        defaultDOMStreamer.setLexicalHandler(consumer);
        namespaceNormalizingDOMStreamer.setContentHandler(consumer);
        namespaceNormalizingDOMStreamer.setLexicalHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     */
    public void setContentHandler(ContentHandler handler) {
        defaultDOMStreamer.setContentHandler(handler);
        namespaceNormalizingDOMStreamer.setContentHandler(handler);
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        defaultDOMStreamer.setLexicalHandler(handler);
        namespaceNormalizingDOMStreamer.setLexicalHandler(handler);
    }

    /**
     * Start the production of SAX events.
     */
    public void stream(Node node) throws SAXException {
        if (normalizeNamespaces) {
            namespaceNormalizingDOMStreamer.stream(node);
        } else {
            defaultDOMStreamer.stream(node);
        }
    }

    public boolean isNormalizeNamespaces() {
        return normalizeNamespaces;
    }

    public void setNormalizeNamespaces(boolean normalizeNamespaces) {
        this.normalizeNamespaces = normalizeNamespaces;
    }

    public void recycle() {
        defaultDOMStreamer.recycle();
        namespaceNormalizingDOMStreamer.recycle();
        normalizeNamespaces = DEFAULT_NORMALIZE_NAMESPACES;
    }

    /**
     * Streams a DOM tree to SAX events and normalizes namespace declarations on the way.
     *
     * <p>The code in this class is based on the org.apache.xml.utils.TreeWalker class from Xalan,
     * though it differs in some important ways.
     *
     * <p>This class will automatically fix up ("normalize") namespace declarations
     * while streaming to SAX. The original DOM-tree is not modified. The algorithm
     * used is described in
     * <a href="http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20021022/namespaces-algorithms.html#normalizeDocumentAlgo">an appendix of the DOM Level 3 spec</a>.
     *
     * <p>This class will NOT check the correctness of namespaces, e.g. it will not
     * check that the "xml" prefix is not misused etc.
     *
     * @author Bruno Dumon (bruno at outerthought dot org)
     * @author Xalan team
     */
    public class NamespaceNormalizingDOMStreamer extends AbstractXMLProducer {
        /**
         * Information about the current element. Used to remember the localName, qName
         * and namespaceURI for generating the endElement event, and holds the namespaces
         * declared on the element. This extra class is needed because we don't want to
         * modify the DOM-tree itself. The currentElementInfo has a pointer to its parent
         * elementInfo.
         */
        protected NamespaceNormalizingDOMStreamer.ElementInfo currentElementInfo = null;

        /** Counter used when generating new namespace prefixes. */
        protected int newPrefixCounter = 0;

        public void recycle() {
            super.recycle();
            currentElementInfo = null;
            newPrefixCounter = 0;
        }

        /**
         * Start the production of SAX events.
         *
         * <p>Perform a pre-order traversal non-recursive style.
         *
         * <p>Note that TreeWalker assumes that the subtree is intended to represent
         * a complete (though not necessarily well-formed) document and, during a
         * traversal, startDocument and endDocument will always be issued to the
         * SAX listener.
         *
         * @param pos Node in the tree where to start traversal
         *
         */
        protected void stream(Node pos) throws SAXException {

            // Start document only if we're streaming a document
            boolean isDoc = (pos.getNodeType() == Node.DOCUMENT_NODE);
            if (isDoc) {
              contentHandler.startDocument();
            }

            Node top = pos;
            while (null != pos) {
                startNode(pos);

                Node nextNode = pos.getFirstChild();
                while (null == nextNode) {
                    endNode(pos);

                    if (top.equals(pos)) {
                        break;
                    }

                    nextNode = pos.getNextSibling();
                    if (null == nextNode) {
                        pos = pos.getParentNode();

                        if ((null == pos) || (top.equals(pos))) {
                            if (null != pos)
                                endNode(pos);

                            nextNode = null;

                            break;
                        }
                    }
                }

                pos = nextNode;
            }

            if (isDoc) {
            	contentHandler.endDocument();
            }
        }

        private final void dispatchChars(Node node) throws SAXException {
            String data = ((Text) node).getData();
            contentHandler.characters(data.toCharArray(), 0, data.length());
        }

        /**
         * Start processing given node
         *
         * @param node Node to process
         */
        protected void startNode(Node node) throws SAXException {

            switch (node.getNodeType()) {
                case Node.COMMENT_NODE:
                    {
                        if (lexicalHandler != null) {
                            String data = ((Comment) node).getData();
                            lexicalHandler.comment(data.toCharArray(), 0, data.length());
                        }
                    }
                    break;
                case Node.DOCUMENT_FRAGMENT_NODE:

                    // ??;
                    break;
                case Node.DOCUMENT_NODE:

                    break;
                case Node.ELEMENT_NODE:
                    NamedNodeMap atts = node.getAttributes();
                    int nAttrs = atts.getLength();

                    // create a list of localy declared namespace prefixes
                    currentElementInfo = new NamespaceNormalizingDOMStreamer.ElementInfo(currentElementInfo);
                    for (int i = 0; i < nAttrs; i++) {
                        Node attr = atts.item(i);
                        String attrName = attr.getNodeName();

                        if (attrName.equals("xmlns") || attrName.startsWith("xmlns:")) {
                            int index;
                            String prefix = (index = attrName.indexOf(":")) < 0
                                    ? "" : attrName.substring(index + 1);

                            currentElementInfo.put(prefix, attr.getNodeValue());
                        }
                    }

                    String namespaceURI = node.getNamespaceURI();
                    String prefix = node.getPrefix();
                    String localName = node.getLocalName();

                    if (localName == null) {
                        // this is an element created with createElement instead of createElementNS
                        String[] prefixAndLocalName = getPrefixAndLocalName(node.getNodeName());
                        prefix = prefixAndLocalName[0];
                        localName = prefixAndLocalName[1];
                        // note: if prefix is null, there can still be a default namespace...
                        namespaceURI = getNamespaceForPrefix(prefix, (Element)node);
                    }

                    if (namespaceURI != null) {
                        // no prefix means: make this the default namespace
                        if (prefix == null)
                            prefix = "";

                        // check that is declared
                        String uri = currentElementInfo.findNamespaceURI(prefix);
                        if (uri != null && uri.equals(namespaceURI)) {
                            // System.out.println("namespace is declared");
                            // prefix is declared correctly, do nothing
                        } else if (uri != null) {
                            // System.out.println("prefix is declared with other namespace, overwriting it");
                            // prefix exists but is bound to another namespace, overwrite it
                            currentElementInfo.put(prefix, namespaceURI);
                        } else {
                            // System.out.println("prefix is not yet declared, declaring it now");
                            currentElementInfo.put(prefix, namespaceURI);
                        }
                    } else {
                        // element has no namespace
                        // check if there is a default namespace, if so undeclare it
                        String uri = currentElementInfo.findNamespaceURI("");
                        if (uri != null && !uri.equals("")) {
                            // System.out.println("undeclaring default namespace");
                            currentElementInfo.put("", "");
                        }
                    }

                    // SAX uses empty string to denote no namespace, while DOM uses null.
                    if (namespaceURI == null)
                        namespaceURI = "";

                    String qName;
                    if (prefix != null && prefix.length() > 0)
                        qName = prefix + ":" + localName;
                    else
                        qName = localName;

                    // make the attributes
                    AttributesImpl newAttrs = new AttributesImpl();
                    for (int i = 0; i < nAttrs; i++) {
                        Node attr = atts.item(i);
                        String attrName = attr.getNodeName();
                        String assignedAttrPrefix = null;

                        // only do non-namespace attributes
                        if (!(attrName.equals("xmlns") || attrName.startsWith("xmlns:"))) {
                            String attrPrefix;
                            String attrLocalName;
                            String attrNsURI;

                            if (attr.getLocalName() == null) {
                                // this is an attribute created with setAttribute instead of setAttributeNS
                                String[] prefixAndLocalName = getPrefixAndLocalName(attrName);
                                attrPrefix = prefixAndLocalName[0];
                                // the statement below causes the attribute to keep its prefix even if it is not
                                // bound to a namespace (to support pre-namespace XML).
                                assignedAttrPrefix = attrPrefix;
                                attrLocalName = prefixAndLocalName[1];
                                // note: if prefix is null, the attribute has no namespace (namespace defaulting
                                // does not apply to attributes)
                                if (attrPrefix != null)
                                    attrNsURI = getNamespaceForPrefix(attrPrefix, (Element)node);
                                else
                                    attrNsURI = null;
                            } else {
                                attrLocalName = attr.getLocalName();
                                attrPrefix = attr.getPrefix();
                                attrNsURI = attr.getNamespaceURI();
                            }

                            if (attrNsURI != null) {
                                String declaredUri = currentElementInfo.findNamespaceURI(attrPrefix);
                                // if the prefix is null, or the prefix has not been declared, or conflicts with an in-scope binding
                                if (declaredUri == null || !declaredUri.equals(attrNsURI)) {
                                    String availablePrefix = currentElementInfo.findPrefix(attrNsURI);
                                    if (availablePrefix != null)
                                        assignedAttrPrefix = availablePrefix;
                                    else {
                                        if (attrPrefix != null && declaredUri == null) {
                                            // prefix is not null and is not yet declared: declare it
                                            assignedAttrPrefix = attrPrefix;
                                            currentElementInfo.put(assignedAttrPrefix, attrNsURI);
                                        } else {
                                            // attribute has no prefix (which is not allowed for namespaced attributes) or
                                            // the prefix is already bound to something else: generate a new prefix
                                            newPrefixCounter++;
                                            assignedAttrPrefix = "NS" + newPrefixCounter;
                                            currentElementInfo.put(assignedAttrPrefix, attrNsURI);
                                        }
                                    }
                                } else {
                                    assignedAttrPrefix = attrPrefix;
                                }
                            }

                            String assignedAttrNsURI = attrNsURI != null ? attrNsURI : "";
                            String attrQName;
                            if (assignedAttrPrefix != null)
                                attrQName = assignedAttrPrefix + ":" + attrLocalName;
                            else
                                attrQName = attrLocalName;
                            newAttrs.addAttribute(assignedAttrNsURI, attrLocalName, attrQName, "CDATA", attr.getNodeValue());
                        }
                    }

                    // add local namespace declaration and fire startPrefixMapping events
                    if (currentElementInfo.namespaceDeclarations != null && currentElementInfo.namespaceDeclarations.size() > 0) {
                        Iterator localNsDeclIt = currentElementInfo.namespaceDeclarations.entrySet().iterator();
                        while (localNsDeclIt.hasNext()) {
                            Map.Entry entry = (Map.Entry) localNsDeclIt.next();
                            String pr = (String) entry.getKey();
                            String ns = (String) entry.getValue();
                            // the following lines enable the creation of explicit xmlns attributes
                            //String pr1 = pr.equals("") ? "xmlns" : pr;
                            //String qn = pr.equals("") ? "xmlns" : "xmlns:" + pr;
                            //newAttrs.addAttribute("", pr1, qn, "CDATA", ns);
                            // System.out.println("starting prefix mapping  for prefix " + pr + " for " + ns);
                            contentHandler.startPrefixMapping(pr, ns);
                        }
                    }

                    contentHandler.startElement(namespaceURI, localName, qName, newAttrs);

                    currentElementInfo.localName = localName;
                    currentElementInfo.namespaceURI = namespaceURI;
                    currentElementInfo.qName = qName;
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    {
                        ProcessingInstruction pi = (ProcessingInstruction) node;
                        contentHandler.processingInstruction(pi.getNodeName(), pi.getData());
                    }
                    break;
                case Node.CDATA_SECTION_NODE:
                    {
                        if (lexicalHandler != null)
                            lexicalHandler.startCDATA();

                        dispatchChars(node);

                        if (lexicalHandler != null)
                            lexicalHandler.endCDATA();
                    }
                    break;
                case Node.TEXT_NODE:
                    {
                        dispatchChars(node);
                    }
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    {
                        EntityReference eref = (EntityReference) node;

                        if (lexicalHandler != null) {
                            lexicalHandler.startEntity(eref.getNodeName());
                        } else {
                            // warning("Can not output entity to a pure SAX ContentHandler");
                        }
                    }
                    break;
                default :
            }
        }

        /**
         * Searches the namespace for a given namespace prefix starting from a
         * given Element.
         *
         * <p>Note that this resolves the prefix in the orginal DOM-tree, not in
         * the {@link ElementInfo} objects. This is used to resolve prefixes
         * of elements or attributes created with createElement or setAttribute
         * instead of createElementNS or setAttributeNS.
         *
         * <p>The code in this method is largely based on
         * org.apache.xml.utils.DOMHelper.getNamespaceForPrefix() (from Xalan).
         *
         * @param prefix the prefix to look for, can be empty or null to find the
         * default namespace
         *
         * @return the namespace, or null if not found.
         */
        public String getNamespaceForPrefix(String prefix, Element namespaceContext) {
            int type;
            Node parent = namespaceContext;
            String namespace = null;

            if (prefix == null)
                prefix = "";

            if (prefix.equals("xml")) {
                namespace = "http://www.w3.org/XML/1998/namespace";
            } else if(prefix.equals("xmlns")) {
                namespace = "http://www.w3.org/2000/xmlns/";
            } else {
                // Attribute name for this prefix's declaration
                String declname = (prefix == "") ? "xmlns" : "xmlns:" + prefix;

                // Scan until we run out of Elements or have resolved the namespace
                while ((null != parent) && (null == namespace)
                   && (((type = parent.getNodeType()) == Node.ELEMENT_NODE)
                       || (type == Node.ENTITY_REFERENCE_NODE))) {
                    if (type == Node.ELEMENT_NODE) {
                        Attr attr=((Element)parent).getAttributeNode(declname);
                        if(attr!=null) {
                            namespace = attr.getNodeValue();
                            break;
                        }
                    }

                    parent = parent.getParentNode();
                }
            }

            return namespace;
        }

        /**
         * Splits a nodeName into a prefix and a localName
         *
         * @return an array containing two elements, the first one is the prefix (can be null), the
         * second one is the localName
         */
        private String[] getPrefixAndLocalName(String nodeName) {
            String prefix, localName;
            int colonPos = nodeName.indexOf(":");
            if (colonPos != -1) {
                prefix = nodeName.substring(0, colonPos);
                localName = nodeName.substring(colonPos + 1, nodeName.length());
            } else {
                prefix = null;
                localName = nodeName;
            }
            return new String[] {prefix, localName};
        }


        /**
         * End processing of given node
         *
         * @param node Node we just finished processing
         */
        protected void endNode(Node node) throws org.xml.sax.SAXException {

            switch (node.getNodeType()) {
                case Node.DOCUMENT_NODE:
                    break;

                case Node.ELEMENT_NODE:
                    contentHandler.endElement(currentElementInfo.namespaceURI,
                            currentElementInfo.localName, currentElementInfo.qName);

                    // generate endPrefixMapping events if needed
                    if (currentElementInfo.namespaceDeclarations != null && currentElementInfo.namespaceDeclarations.size() > 0) {
                        Iterator namespaceIt = currentElementInfo.namespaceDeclarations.entrySet().iterator();
                        while (namespaceIt.hasNext()) {
                            Map.Entry entry = (Map.Entry) namespaceIt.next();
                            contentHandler.endPrefixMapping((String) entry.getKey());
                            //System.out.println("ending prefix mapping " + (String) entry.getKey());
                        }
                    }

                    currentElementInfo = currentElementInfo.parent;
                    break;
                case Node.CDATA_SECTION_NODE:
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    {
                        EntityReference eref = (EntityReference) node;

                        if (lexicalHandler != null) {
                            lexicalHandler.endEntity(eref.getNodeName());
                        }
                    }
                    break;
                default :
            }
        }

        public class ElementInfo {
            public String localName;
            public String namespaceURI;
            public String qName;
            public Map namespaceDeclarations = null;
            public ElementInfo parent;

            public ElementInfo(ElementInfo parent) {
                this.parent = parent;
            }

            /**
             * Declare a new namespace prefix on this element, possibly overriding
             * an existing one.
             */
            public void put(String prefix, String namespaceURI) {
                if (namespaceDeclarations == null)
                    namespaceDeclarations = new HashMap();
                namespaceDeclarations.put(prefix, namespaceURI);
            }

            /**
             * Finds a prefix declared on this element.
             */
            public String getPrefix(String namespaceURI) {
                if (namespaceDeclarations == null || namespaceDeclarations.size() == 0)
                    return null;
                // note: there could be more than one prefix for the same namespaceURI, but
                // we return the first found one.
                Iterator it = namespaceDeclarations.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (entry.getValue().equals(namespaceURI))
                        return (String) entry.getKey();
                }
                return null;
            }

            /**
             * Finds a namespace URI declared on this element.
             */
            public String getNamespaceURI(String prefix) {
                if (namespaceDeclarations == null || namespaceDeclarations.size() == 0)
                    return null;

                return (String) namespaceDeclarations.get(prefix);
            }

            /**
             * Finds a prefix declaration on this element or containing elements.
             */
            public String findPrefix(String namespaceURI) {
                if (namespaceDeclarations != null && namespaceDeclarations.size() != 0) {
                    String prefix = getPrefix(namespaceURI);
                    if (prefix != null)
                        return prefix;
                }
                if (parent != null)
                    return parent.findPrefix(namespaceURI);
                else
                    return null;
            }

            /**
             * Finds a namespace declaration on this element or containing elements.
             */
            public String findNamespaceURI(String prefix) {
                if (namespaceDeclarations != null && namespaceDeclarations.size() != 0) {
                    String uri = (String) namespaceDeclarations.get(prefix);
                    if (uri != null)
                        return uri;
                }
                if (parent != null)
                    return parent.findNamespaceURI(prefix);
                else
                    return null;
            }
        }
    }

    /**
     * The <code>DefaultDOMStreamer</code> is a utility class that will generate SAX
     * events from a W3C DOM Document.
     *
     * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
     * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
     *         (Apache Software Foundation)
     */
    public class DefaultDOMStreamer extends AbstractXMLProducer {

        /** The private transformer for this instance */
        protected Transformer transformer;

        /**
         * Start the production of SAX events.
         */
        public void stream(Node node)
        throws SAXException {
            if (this.transformer == null) {
                try {
                    this.transformer = factory.newTransformer();
                } catch (TransformerConfigurationException e) {
                    throw new SAXException(e);
                }
            }
            DOMSource source = new DOMSource(node);

            ContentHandler handler;
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                // Pass all SAX events
                handler = contentHandler;
            } else {
                // Strip start/endDocument
                handler = new EmbeddedXMLPipe(contentHandler);
            }

            SAXResult result = new SAXResult(handler);
            result.setLexicalHandler(lexicalHandler);

            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new SAXException(e);
            }
        }
    }
}
