/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import java.io.File;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import org.apache.cocoon.util.DOMUtils;
import org.apache.cocoon.components.language.markup.AbstractMarkupLanguage;

import org.apache.cocoon.components.language.programming.ProgrammingLanguage;


import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * This class implements <code>MarkupLanguage</code> for Cocoon's
 * <a href="http://xml.apache.org/cocoon/sitemap.html">Sitemap</a>.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-02 20:03:44 $
 */
public class SitemapMarkupLanguage extends AbstractMarkupLanguage {
  /**
   * The default constructor.
   */
  public SitemapMarkupLanguage() throws SAXException, IOException {
    super();
  }

  /**
   * Return the Sitemap language name: <i>map</i> :-)
   *
   * @return The <i>map</i> constant
   */
  public String getName() {
    return "map";
  }

  /**
   * Return the document-declared encoding or <code>null</code> if it's the
   * platform's default encoding
   *
   * @param document The input document
   * @return The document-declared encoding
   */
  public String getEncoding(Document document) {
    String encoding = document.getDocumentElement().getAttribute("encoding");

    if (encoding.length() > 0) {
      return encoding;
    }

    return null;
  }

  /**
   * Prepare the document for logicsheet processing and code generation. This
   * method sets the base filename, file path and creation date as root element
   * attibutes and encodes text nodes as strings.
   *
   * @param document The input document
   * @param filename The input source filename
   * @param language The target programming language
   * @return The augmented document
   */
  protected Document preprocessDocument(
    Document document, String filename, ProgrammingLanguage language
  )
  {
    // Store path and file name
    int pos = filename.lastIndexOf(File.separatorChar);
    String name = filename.substring(pos + 1);
    String path = filename.substring(0, pos).replace(File.separatorChar, '/');

    Element root = document.getDocumentElement();

    root.setAttribute("file-name", name);
    root.setAttribute("file-path", path);
    root.setAttribute("creation-date", String.valueOf(new Date().getTime()));

    this.quoteStrings(document, language);

    return document;
  }

  /**
   * Encode text nodes as strings according to the target programming languages
   * string constant escaping rules.
   *
   * @param node The node to be escaped
   * @param language The target programming language
   */
  protected void quoteStrings(Node node, ProgrammingLanguage language) {
    switch (node.getNodeType()) {
      case Node.PROCESSING_INSTRUCTION_NODE:
        ProcessingInstruction pi = (ProcessingInstruction) node;
	if (!pi.getTarget().equals("xml-logicsheet")) {
          pi.setData(language.quoteString(pi.getData()));
	}
        break;
      case Node.TEXT_NODE:
        if (true) break; // the sitemap shouldn't have any text node
        Element parent = (Element) node.getParentNode();

        String tagName = parent.getTagName();

        if (
          tagName.equals("xsp:expr") ||
          tagName.equals("xsp:logic") ||
          tagName.equals("xsp:structure") ||
          tagName.equals("xsp:include")
        ) {
          return;
        }

        String value = language.quoteString(node.getNodeValue());
        Text textNode = node.getOwnerDocument().createTextNode(value);

        Element textElement = node.getOwnerDocument().createElement("xsp:text");

        textElement.appendChild(textNode);
        parent.replaceChild(textElement, node);

        break;
      case Node.ELEMENT_NODE:
        ((Element) node).normalize();
        // Fall through
      default:
        NodeList childList = node.getChildNodes();
        int childCount = childList.getLength();

        for (int i = 0; i < childCount; i++) {
          this.quoteStrings(childList.item(i), language);
        }

        break;
    }
  }

  /**
   * Returns a list of logicsheets to be applied to this document for source
   * code generation. This method scans the input document for
   * &lt;?xml-logicsheet?&gt; processing instructions and top-level
   * &lt;xsp:logicsheet&gt; elements. Logicsheet declarations are removed from
   * the input document.
   *
   * @param document The input document
   * @return An array of logicsheet <i>names</i>
   */
  protected String[] getLogicsheets(Document document) {
    Vector removedNodes = new Vector();
    Vector logicsheetList = new Vector();
    Element root = document.getDocumentElement();

    // Retrieve logicsheets declared by processing-instruction
    NodeList nodeList = document.getChildNodes();
    int count = nodeList.getLength();
    for (int i = 0; i < count; i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
	ProcessingInstruction pi = (ProcessingInstruction) node;

	if (pi.getTarget().equals("xml-logicsheet")) {
          Hashtable attrs = DOMUtils.getPIPseudoAttributes(pi);
	  logicsheetList.addElement(attrs.get("href"));

	  removedNodes.addElement(pi);
	}
      }
    }

    // Retrieve logicsheets declared by top-level elements
    nodeList = root.getElementsByTagName("map:logicsheet");
    count = nodeList.getLength();

    for (int i = 0; i < count; i++) {
      Element logicsheetElement = (Element) nodeList.item(i);
      removedNodes.addElement(logicsheetElement);
      logicsheetList.addElement(logicsheetElement.getAttribute("location"));
    }

    String[] logicsheetLocations = new String[logicsheetList.size()];
    logicsheetList.copyInto(logicsheetLocations);


    // Remove logicsheet directives
    count = removedNodes.size();
    for (int i = 0; i < count; i++) {
      Node node = (Node) removedNodes.elementAt(i); 
      Node parent = node.getParentNode();
      parent.removeChild(node);
    }

    return logicsheetLocations;
  }

  /**
   * Add a dependency on an external file to the document for inclusion in
   * generated code. This is used by <code>AbstractServerPagesGenerator</code>
   * to populate a list of <code>File</code>'s tested for change on each
   * invocation; this information, in turn, is used by
   * <code>ServerPagesLoaderImpl</code> to assert whether regeneration is
   * necessary. XSP uses &lt;xsp:dependency&gt; elements for this purpose
   *
   * @param PARAM_NAME Param description
   * @return the value
   * @exception EXCEPTION_NAME If an error occurs
   * @see ServerPages <code>AbstractServerPagesGenerator</code>
   *      and <code>ServerPagesLoaderImpl</code>
   */
/** Sitemaps don't (yet) have dependencies */
  protected void addDependency(Document document, String location) {
    Element root = document.getDocumentElement();
    Element dependency = document.createElement("xsp:dependency");
    dependency.appendChild(document.createTextNode(location));
    root.appendChild(dependency);
  }
/* */

  /**
   * Scan top-level document elements for non-xsp tag names returning the first
   * (and hopefully <i>only</i>) user-defined element
   *
   * @param document The input document
   * @return The first non-xsp element
   */
/** Sitemaps don't have a user root
  protected Element getUserRoot(Document document) {
    Element root = document.getDocumentElement();
    NodeList elements = root.getElementsByTagName("*");
    int elementCount = elements.getLength();
    for (int i = 0; i < elementCount; i++) {
      Element userRoot = (Element) elements.item(i);
      if (!userRoot.getTagName().startsWith("map:")) {
        return userRoot;
      }
    }

    return null;
  }
*/
}
