/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.util;

import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.ProcessingInstruction;

import org.xml.sax.InputSource;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;

/* Start Xalan/Xerces kludge */
import org.w3c.dom.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xpath.xml.XMLParserLiaisonDefault;
/* End Xalan/Xerces kludge */

/**
 * Collection of utility methods for DOM manipulation.
 * This class contains ugly dependencies on Xerces and Xalan
 * classes. These should be removed as the Cocoon2 core interfaces
 * add (needed!) DOM support.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:10:13 $
 */
public class DOMUtils {
  // *** Start Xalan/Xerces kludge ***

  /**
   * Static stylesheet processor with an augmented default liaison capable
   * of creating empty documents
   */
  private static XSLTProcessor xsltProcessor =
      XSLTProcessorFactory.getProcessor(
        new XMLParserLiaisonDefault() {
          public Document createDocument() {
            return new DocumentImpl();
          }
	}
      );

  /**
   * Parse an input source returning a DOM. <code>Document</code>
   *
   * @param inputSource The input source
   * @return A DOM <code>Document</code>
   * @exception IOException IO Error
   * @exception SAXException Parse Error
   */
  public static Document DOMParse(InputSource inputSource)
    throws IOException, SAXException
  {
    DOMParser parser = new DOMParser();
    parser.parse(inputSource);

    return parser.getDocument();
  }

  /**
   * Apply a stylesheet to a given document
   *
   * @param input The input document
   * @param stylehseet The stylesheet document
   * @return The transformed document
   * @exception SAXException SAX Error
   */
  public static Document transformDocument(Document input, Document stylesheet)
    throws SAXException
  {
    Document output = new DocumentImpl();

    xsltProcessor.process(
      new XSLTInputSource(input),
      new XSLTInputSource(stylesheet),
      new XSLTResultTarget(output)
    );

    return output;
  }

  // *** End Xalan/Xerces kludge ***

  /**
   * This method returns an hashtable of pseudo attributes found in the first
   * occurrence of the PI with the given name in the given document.
   * No validation is performed on the PI pseudo syntax
   *
   * @param pi The processing instruction
   * @return A hashtable containing key/value pairs in the pi pseudo-attributes
   */
  public static Hashtable getPIPseudoAttributes(ProcessingInstruction pi)
  {
    Hashtable attributes = new Hashtable();
    addPIPseudoAttributes(pi, attributes);
    return attributes;
  }

  /**
   * This method adds pseudo attributes from a pi to an existing attribute list.
   * All attributes are all put in the same hashtable.
   * If there are collisions, the last attribute is inserted.
   * No validation is performed on the PI pseudo syntax.
   *
   * @param pi The processing instruction
   * @param attributes The receiving hashtable
   */
  public static void addPIPseudoAttributes(
    ProcessingInstruction pi, Hashtable attributes
  )
  {
    String data = pi.getData();

    Tokenizer st = new Tokenizer(data, "\" \t\r\n");
    try {
      while (st.hasMoreTokens()) {
        String key   = st.nextToken();   // attribute name and '='
        String token = st.nextToken();   // exact attribute value
        key = key.replace('=',' ').trim(); // remove whitespace and '='
        attributes.put(key, token);  
      }
    } catch (NoSuchElementException nsee) {
      // ignore white-space at the end of pseudo-list
    }
  }


  /**
   * Return a list of all namespace-declaring attributes in the element.
   * This method returns both prefix-mapped namespaces and the optional
   * "unnamed" uri.
   *
   * @param element An element containing namespace declarations
   * @return A (possibly empty) vector containing all namespace declarations
   *         as a <code>String</code> array of 2 elements (attribue name, uri)
   */
  public static Vector namespaces(Element element) {
   Vector vector = new Vector();
   NamedNodeMap map = element.getAttributes();
   int attrCount = map.getLength();
   for (int i = 0; i < attrCount; i++) {
     Attr attr = (Attr) map.item(i);
     String attrName = attr.getName();
     if (
	  !attrName.equals("xmlns:xsl") &&
          (attrName.equals("xmlns") || attrName.startsWith("xmlns:"))
     ) {
       String[] pair = new String[2];
       pair[0] = attrName;
       pair[1] = attr.getValue();
       vector.addElement(pair);
     }
   } 

   return vector;
  }

  /**
   * Escape reserved markup characters as their corresponding entity
   * representation.
   *
   * @param string The string to be encoded
   * @return A markup-encoded string
   */
  public static String encodeMarkup(String string) {
    char[] array = string.toCharArray();
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < array.length; i++) {
      switch (array[i]) {
        case '<':
          buffer.append("&lt;");
          break;
        case'>':
          buffer.append("&gt;");
          break;
        case '&':
          buffer.append("&amp;");
          break;
        default:
          buffer.append(array[i]);
          break;
      }
    }

    return buffer.toString();
  }

  /**
   * Return the string representation of a DOM node
   *
   * @param node The node to be represented as a <code>String</code>
   * @return The string representation of the node
   */
  public static String toMarkup(Node node) {
    StringBuffer buffer = new StringBuffer();
    doMarkup(node, buffer);
    return buffer.toString();
  }

  /**
   * Actually perform the (recursive) <code>Node</code> to
   * <code>String</code> conversion
   *
   * @param node The <code>Node</code> to be converted
   * @param buffer The <code>StringBuffer</code> to append to
   */
  protected static void doMarkup(Node node, StringBuffer buffer) {
    switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE:
        buffer.append("<![CDATA[\n");
        buffer.append(node.getNodeValue());
        buffer.append("]]>\n");
        break;
      case Node.DOCUMENT_NODE:
      case Node.DOCUMENT_FRAGMENT_NODE: {
        NodeList nodeList = node.getChildNodes();
        int childCount = nodeList.getLength();

        for (int i = 0; i < childCount; i++) {
          doMarkup(nodeList.item(i), buffer);
        }

        break;
      }
      case Node.ELEMENT_NODE: {
        Element element = (Element) node;

        buffer.append("<" + element.getTagName());

        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for (int i = 0; i < attributeCount; i++) {
          Attr attribute = (Attr) attributes.item(i);

          buffer.append(" ");
          buffer.append(attribute.getName());
          buffer.append("=\"");
          buffer.append(attribute.getValue());
          buffer.append("\"");
        }

        NodeList nodeList = element.getChildNodes();
        int childCount = nodeList.getLength();

        if (childCount == 0) {
          buffer.append("/>\n");
        } else {
          buffer.append(">");
          for (int i = 0; i < childCount; i++) {
            doMarkup(nodeList.item(i), buffer);
          }

          buffer.append("</");
          buffer.append(element.getTagName());
          buffer.append(">");
        }

        break;
      }
      case Node.COMMENT_NODE:
        buffer.append("<!-- ");
        buffer.append(node.getNodeValue());
        buffer.append(" -->\n");
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        ProcessingInstruction pi = (ProcessingInstruction) node;

        buffer.append("<?");
        buffer.append(pi.getTarget());
        buffer.append(" ");
        buffer.append(pi.getData());
        buffer.append("?>\n");
        break;
      case Node.TEXT_NODE:
        buffer.append(encodeMarkup(node.getNodeValue()));
        // buffer.append("\n");
        break;
      default:
        break;
    }
  }
}
