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
package org.apache.cocoon.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * XML utility methods.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: XMLUtils.java,v 1.7 2004/03/17 11:50:21 cziegeler Exp $
 */
public class XMLUtils {

    public static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    //using parent because some dom implementations like jtidy are bugged,
    //cannot get parent or delete child
    public static void stripDuplicateAttributes(Node node, Node parent) {
        // The output depends on the type of the node
        switch(node.getNodeType()) {
        case Node.DOCUMENT_NODE: {
            Document doc = (Document)node;
            Node child = doc.getFirstChild();
            while(child != null) {
                stripDuplicateAttributes(child, node);
                child = child.getNextSibling();
            }
            break;
        }
        case Node.ELEMENT_NODE: {
            Element elt = (Element) node;
            NamedNodeMap attrs = elt.getAttributes();

            ArrayList nodesToRemove = new ArrayList();
            int nodesToRemoveNum = 0;

            for(int i = 0; i < attrs.getLength(); i++) {
                Node a = attrs.item(i);

              for(int j = 0; j < attrs.getLength(); j++) {
                  Node b = attrs.item(j);

                  //if there are two attributes with same name
                  if(i!=j&&(a.getNodeName().equals(b.getNodeName())))
                  {
                    nodesToRemove.add(b);
                    nodesToRemoveNum++;
                  }

              }

            }

            for(int i=0;i<nodesToRemoveNum;i++)
            {
              org.w3c.dom.Attr nodeToDelete = (org.w3c.dom.Attr) nodesToRemove.get(i);
              org.w3c.dom.Element nodeToDeleteParent =  (org.w3c.dom.Element)node; //nodeToDelete.getParentNode();
              nodeToDeleteParent.removeAttributeNode(nodeToDelete);
            }

            nodesToRemove.clear();

            Node child = elt.getFirstChild();
            while(child != null) {
                stripDuplicateAttributes(child, node);
                child = child.getNextSibling();
            }

            break;
        }
        default:
            //do nothing
            break;
        }
    }

    /**
     * Get an <code>XMLConsumer</code> from a <code>ContentHandler</code> and
     * a <code>LexicalHandler</code>. If the content handler is already an
     * <code>XMLConsumer</code>, it is returned as is, otherwise it is wrapped
     * in an <code>XMLConsumer</code> with the lexical handler.
     *
     * @param ch the content handler, which should not be <code>null</code>
     * @param lh the lexical handler, which can be <code>null</code>
     * @return an <code>XMLConsumer</code> for <code>ch</code> an <code>lh</code>
     */
    public static XMLConsumer getConsumer(ContentHandler ch, LexicalHandler lh) {
        if (ch instanceof XMLConsumer) {
            return (XMLConsumer)ch;
        } else {
            if ( lh == null && ch instanceof LexicalHandler ) {
                lh = (LexicalHandler)ch;
            }
            return new ContentHandlerWrapper(ch, lh);
        }
    }

    /**
     * Get an <code>XMLConsumer</code> from <code>ContentHandler</code>. If the
     * content handler is already an <code>XMLConsumer</code>, it is returned as
     * is, otherwise it is wrapped in an <code>XMLConsumer</code>.
     *
     * @param ch the content handler, which should not be <code>null</code>
     * @return an <code>XMLConsumer</code> for <code>ch</code>
     */
    public static XMLConsumer getConsumer(ContentHandler ch) {
        return getConsumer(ch, null);
    }

    /**
     * Serialize a DOM node to a String.
     * The defaultSerializeToXMLFormat() is used to format the serialized xml.
     * @deprecated use serializeNode(Node, Properties) instead
     */
    public static String serializeNodeToXML(Node node)
    throws ProcessingException {
        return serializeNode(node, XMLUtils.defaultSerializeToXMLFormat());
    }

    /**
     * This is the default properties set used to serialize xml.
     * It is used by the serializeNodeToXML() method.
     * The format is as follows:
     * Method: xml
     * Encoding: ISO-8859-1
     * Omit xml declaration: no
     * Indent: yes
     * @deprecated Use createPropertiesForXML(false) instead and add the encoding
     */
    public static Properties defaultSerializeToXMLFormat() {
        return defaultSerializeToXMLFormat(false);
    }

    /**
     * This is the default properties set used to serialize xml.
     * It is used by the serializeNodeToXML() method.
     * The omit xml declaration property can be controlled by the flag.
     * Method: xml
     * Encoding: ISO-8859-1
     * Omit xml declaration: according to the flag
     * Indent: yes
     * @deprecated Use createPropertiesForXML(boolean) instead and add the encoding
     */
    public static Properties defaultSerializeToXMLFormat(boolean omitXMLDeclaration) {
        final Properties format = createPropertiesForXML(omitXMLDeclaration);
        format.put(OutputKeys.ENCODING, "ISO-8859-1");
        return format;
    }

    /**
     * Create a new properties set for serializing xml
     * The omit xml declaration property can be controlled by the flag.
     * Method: xml
     * Omit xml declaration: according to the flag
     * Indent: yes
     */
    public static Properties createPropertiesForXML(boolean omitXMLDeclaration) {
        final Properties format = new Properties();
        format.put(OutputKeys.METHOD, "xml");
        format.put(OutputKeys.OMIT_XML_DECLARATION, (omitXMLDeclaration ? "yes" : "no"));
        format.put(OutputKeys.INDENT, "yes");
        return format;        
    }
    
    /**
     * Serialize a DOM node to a String.
     * The format of the output can be specified with the properties.
     * If the node is null the empty string is returned.
     */
    public static String serializeNode(Node node, Properties format)
    throws ProcessingException {
        try {
            if (node == null) {
                return "";
            }
            StringWriter writer = new StringWriter();
            TransformerHandler transformerHandler;
            transformerHandler = ((SAXTransformerFactory)TransformerFactory.newInstance()).newTransformerHandler();
            transformerHandler.getTransformer().setOutputProperties(format);
            transformerHandler.setResult(new StreamResult(writer));
            if (node.getNodeType() != Node.DOCUMENT_NODE) {
                transformerHandler.startDocument();
            } 
            DOMStreamer domStreamer = new DOMStreamer(transformerHandler, transformerHandler);
            domStreamer.stream(node);
            if (node.getNodeType() != Node.DOCUMENT_NODE) {
                transformerHandler.endDocument();
            } 
            return writer.toString();
        } catch (javax.xml.transform.TransformerException local) {
            throw new ProcessingException("TransformerException: " + local, local);
        } catch (SAXException local) {
            throw new ProcessingException("SAXException while streaming DOM node to SAX: " + local, local);
        }        
    }

    /**
     * Add string data
     *
     * @param contentHandler The SAX content handler
     * @param data The string data
     */
    public static void data(ContentHandler contentHandler,
                            String data)
    throws SAXException {
        contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>String</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param text the value
     */
    public static void valueOf(ContentHandler contentHandler, String text)
    throws SAXException {
        if (text != null) {
            data(contentHandler, text);
        }
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>XMLizable</code> :
     * outputs the value by calling <code>v.toSax(contentHandler)</code>.
     *
     * @param contentHandler the SAX content handler
     * @param v the XML fragment
     */
    public static void valueOf(ContentHandler contentHandler,
                               org.apache.excalibur.xml.sax.XMLizable v)
    throws SAXException {
        if (v != null) {
            v.toSAX(contentHandler);
        }
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>org.w3c.dom.Node</code> :
     * converts the Node to a SAX event stream.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void valueOf(ContentHandler contentHandler, Node v)
    throws SAXException {
        if (v != null) {
            DOMStreamer streamer = new DOMStreamer(contentHandler);
            if (contentHandler instanceof LexicalHandler) {
                streamer.setLexicalHandler((LexicalHandler)contentHandler);
            }
            streamer.stream(v);
        }
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>java.util.Collection</code> :
     * outputs the value by calling <code>xspExpr()</code> on each element of the
     * collection.
     *
     * @param contentHandler the SAX content handler
     * @param v the XML fragment
     */
    public static void valueOf(ContentHandler contentHandler,
                               Collection v)
    throws SAXException {
        if (v != null) {
            Iterator iterator = v.iterator();
            while (iterator.hasNext()) {
                valueOf(contentHandler, iterator.next());
            }
        }
     }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>Object</code> depending on its class :
     * <ul>
     * <li>if it's an array, call <code>xspExpr()</code> on all its elements,</li>
     * <li>if it's class has a specific <code>xspExpr()</code>implementation, use it,</li>
     * <li>else, output it's string representation.</li>
     * </ul>
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void valueOf(ContentHandler contentHandler, Object v)
    throws SAXException {
        if (v == null) {
            return;
        }

        // Array: recurse over each element
        if (v.getClass().isArray()) {
            Object[] elements = (Object[]) v;

            for (int i = 0; i < elements.length; i++) {
                valueOf(contentHandler, elements[i]);
            }
            return;
         }

         // Check handled object types in case they were not typed in the XSP

         // XMLizable
         if (v instanceof org.apache.excalibur.xml.sax.XMLizable) {
             valueOf(contentHandler, (org.apache.excalibur.xml.sax.XMLizable)v);
             return;
         }

         // Node
         if (v instanceof Node) {
             valueOf(contentHandler, (Node)v);
             return;
         }

         // Collection
         if (v instanceof Collection) {
             valueOf(contentHandler, (Collection)v);
             return;
         }

         // Give up: hope it's a string or has a meaningful string representation
         data(contentHandler, String.valueOf(v));
    }

    /**
     * Create a start and endElement with a empty Namespace and without Attributes
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void createElement(ContentHandler contentHandler, String localName) 
    throws SAXException {
        startElement(contentHandler, localName);
        endElement(contentHandler, localName);
    }

    /**
     * Create a start and endElement with a empty Namespace and without Attributes
     * The content of the Element is set to the stringValue parameter
     *
     * @param localName The local name (without prefix)
     * @param stringValue The content of the Element
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void createElement(ContentHandler contentHandler, String localName, String stringValue)
        throws SAXException {
        startElement(contentHandler, localName);
        data(contentHandler, stringValue);
        endElement(contentHandler, localName);
    }

    /**
     * Create a start and endElement with a empty Namespace
     *
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void createElement(ContentHandler contentHandler, String localName, Attributes atts) 
    throws SAXException {
        startElement(contentHandler, localName, atts);
        endElement(contentHandler, localName);
    }

    /**
     * Create a start and endElement with a empty Namespace
     * The content of the Element is set to the stringValue parameter
     *
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @param stringValue The content of the Element
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void createElement(ContentHandler contentHandler, String localName, Attributes atts, String stringValue)
        throws SAXException {
        startElement(contentHandler, localName, atts);
        data(contentHandler, stringValue);
        endElement(contentHandler, localName);
    }

    /**
     * Create a start and endElement without Attributes
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void createElementNS(ContentHandler contentHandler, String namespaceURI, String localName)
        throws SAXException {
        startElement(contentHandler, namespaceURI, localName);
        endElement(contentHandler, namespaceURI, localName);
    }

    /**
     * Create a start and endElement without Attributes
     * The content of the Element is set to the stringValue parameter
     *
     * @param localName The local name (without prefix)
     * @param stringValue The content of the Element
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void createElementNS(
        ContentHandler contentHandler,
        String namespaceURI,
        String localName,
        String stringValue)
    throws SAXException {
        startElement(contentHandler, namespaceURI, localName);
        data(contentHandler, stringValue);
        endElement(contentHandler, namespaceURI, localName);
    }
    
    /**
     * Create a start and endElement
     *
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void createElementNS(
        ContentHandler contentHandler,
        String namespaceURI,
        String localName,
        Attributes atts)
    throws SAXException {
        startElement(contentHandler, namespaceURI, localName, atts);
        endElement(contentHandler, namespaceURI, localName);
    }
    
    /**
     * Create a start and endElement with a empty Namespace
     * The content of the Element is set to the stringValue parameter
     *
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @param stringValue The content of the Element
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void createElementNS(
        ContentHandler contentHandler,
        String namespaceURI,
        String localName,
        Attributes atts,
        String stringValue)
    throws SAXException {
        startElement(contentHandler, namespaceURI, localName, atts);
        data(contentHandler, stringValue);
        endElement(contentHandler, namespaceURI, localName);
    }
    
   
    /**
     * Create endElement with empty Namespace
     *
     * <p>For information on the names, see startElement.</p>
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public static void endElement(ContentHandler contentHandler, String localName) 
    throws SAXException {
        contentHandler.endElement("", localName, localName);
    }
    
    /**
     * Create endElement
     * Prefix must be mapped to empty String
     *
     * <p>For information on the names, see startElement.</p>
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public static void endElement(ContentHandler contentHandler, String namespaceURI, String localName) 
    throws SAXException {
        contentHandler.endElement(namespaceURI, localName, localName);
    }
    
    /**
     * Create a startElement with a empty Namespace and without Attributes
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void startElement(ContentHandler contentHandler, String localName) 
    throws SAXException {
        contentHandler.startElement("", localName, localName, EMPTY_ATTRIBUTES);
    }

    /**
     * Create a startElement without Attributes
     * Prefix must be mapped to empty String
     *
     * @param namespaceURI The Namespace URI
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void startElement(ContentHandler contentHandler, String namespaceURI, String localName)
    throws SAXException {
        contentHandler.startElement(namespaceURI, localName, localName, EMPTY_ATTRIBUTES);
    }

    /**
     * Create a startElement with a empty Namespace
     *
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void startElement(ContentHandler contentHandler, String localName, Attributes atts) 
    throws SAXException {
        contentHandler.startElement("", localName, localName, atts);
    }

    /**
     * Create a startElement with a empty Namespace
     * Prefix must be mapped to empty String
     *
     * @param namespaceURI The Namespace URI
     * @param localName The local name (without prefix)
     * @param atts The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     * @see org.xml.sax.Attributes
     */
    public static void startElement(ContentHandler contentHandler, String namespaceURI, String localName, Attributes atts)
    throws SAXException {
        contentHandler.startElement(namespaceURI, localName, localName, atts);
    }
}
