/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.net.URL;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;

import org.apache.cocoon.util.DOMUtils;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet though this will change shortly: a new markup
 * language will be used for logicsheet authoring; logicsheets written in this
 * language will be transformed into an equivalent XSLT stylesheet anyway...
 * This class should probably be based on an interface...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-29 18:30:27 $
 */
public class Logicsheet {
  /**
   * The wrapped XSLT stylesheet
   */
  protected Document stylesheet;

  /**
   * The list of preserved namespaces
   */
  protected Vector namespaces;
  /**
   * A time-saving trick: this is actually the namespace's vector size()
   */
  protected int namespaceCount;

  /**
   * The constructor. This method scans the logicsheet collecting namespaces to
   * be preserved on output; this ensures that no namespaces will be dropped
   * from the original document
   *
   * @param inputSource The stylesheet's input source
   * @exception IOException IOError processing input source
   * @exception SAXException Input source parse error
   */
  public void setInputSource(InputSource inputSource)
    throws SAXException, IOException
  {
    this.stylesheet = DOMUtils.DOMParse(inputSource);
    this.namespaces = DOMUtils.namespaces(this.stylesheet.getDocumentElement());
    this.namespaceCount = this.namespaces.size();
  }

  /**
   * Apply this logicsheet to an input document. This method does additional
   * namesapace preserving as stylsheet processing may drop namespaces required
   * by further code-generation steps
   *
   * @param input Param The input document
   * @return The transformed document
   * @exception SAXException If a stylesheet processing error occurs
   */
  public Document apply(Document input) throws SAXException {
    // Save original namespaces
    Vector inputNamespaces = DOMUtils.namespaces(input.getDocumentElement());
    int inputCount = inputNamespaces.size();

    // Transform input document
    Document output = DOMUtils.transformDocument(input, this.stylesheet);

    // Restore original namespaces
    Element root = output.getDocumentElement();

    for (int i = 0; i < inputCount; i++) {
      String[] pair = (String[]) inputNamespaces.elementAt(i);
      root.setAttribute(pair[0], pair[1]);
    }

    // Restore stylesheet namespaces
    for (int i = 0; i < this.namespaceCount; i++) {
      String[] pair = (String[]) this.namespaces.elementAt(i);
      root.setAttribute(pair[0], pair[1]);
    }

    return output;
  }
}
