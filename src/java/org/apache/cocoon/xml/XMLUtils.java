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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * XML utility methods.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: XMLUtils.java,v 1.2 2003/03/12 15:11:15 cziegeler Exp $
 */
public class XMLUtils{

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
        if (ch instanceof XMLConsumer)
          return (XMLConsumer)ch;
        else
          return new ContentHandlerWrapper(ch, lh);
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
     */
    public static Properties defaultSerializeToXMLFormat(boolean omitXMLDeclaration) {
        Properties format = new Properties();
        format.put(OutputKeys.METHOD, "xml");
        format.put(OutputKeys.ENCODING, "ISO-8859-1");
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
            if (node == null) return "";
            StringWriter writer = new StringWriter();
            TransformerHandler transformerHandler;
            transformerHandler = ((SAXTransformerFactory)TransformerFactory.newInstance()).newTransformerHandler();
            transformerHandler.getTransformer().setOutputProperties(format);
            transformerHandler.setResult(new StreamResult(writer));
            if ( node.getNodeType() != Node.DOCUMENT_NODE ) {
                transformerHandler.startDocument();
            } 
            DOMStreamer domStreamer = new DOMStreamer(transformerHandler, transformerHandler);
            domStreamer.stream(node);
            if ( node.getNodeType() != Node.DOCUMENT_NODE ) {
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

}
