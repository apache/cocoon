/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.SAXException;

/**
 * Base class for XSP's object model manipulation logicsheets
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:36 $
 */
public class XSPObjectHelper implements Constants {
  /**
   * Empty attributes used for contentHandler.startElement()
   */
  protected static final AttributesImpl emptyAttr = new AttributesImpl();

  /**
   * Uri and prefix associated with object helper. Derived classes must assign
   * these variables to their proper values
   */
  protected static String URI;
  protected static String PREFIX;

  /**
   * Output an element containing text only and no attributes
   *
   * @param contentHandler The SAX content handler
   * @param name The element name
   * @param data The data contained by the element
   */
  protected static void elementData(
    ContentHandler contentHandler,
    String name,
    String data
  )
    throws SAXException
  {
    start(contentHandler, name);
    data(contentHandler, data);
    end(contentHandler, name);
  }

  /**
   * Output an element containing text only and attributes
   *
   * @param contentHandler The SAX content handler
   * @param name The element name
   * @param data The data contained by the element
   * @param attr The element attributes
   */
  protected static void elementData(
    ContentHandler contentHandler,
    String name,
    String data,
    AttributesImpl attr
  )
    throws SAXException
  {
    start(contentHandler, name, attr);
    data(contentHandler, data);
    end(contentHandler, name);
  }

  /**
   * Start an element with the proper object's uri and prefix and no
   * attributes
   *
   * @param contentHandler The SAX content handler
   * @param name The element name
   */
  protected static void start(
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    contentHandler.startElement(URI, name, PREFIX + ":" + name, emptyAttr);
  }

  /**
   * Start an element with the proper object's uri and prefix and with
   * attributes
   *
   * @param contentHandler The SAX content handler
   * @param name The element name
   * @param attr The element attributes
   */
  protected static void start(
    ContentHandler contentHandler,
    String name,
    AttributesImpl attr
  )
    throws SAXException
  {
    contentHandler.startElement(URI, name, PREFIX + ":" + name, attr);
  }

  /**
   * End an element with the proper object's uri and prefix
   *
   * @param contentHandler The SAX content handler
   * @param name The element name
   */
  protected static void end(
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    contentHandler.endElement(URI, name, PREFIX + ":" + name);
  }

  /**
   * Add an attribute
   *
   * @param attr The attribute list
   * @param name The attribute name
   * @param value The attribute value
   */
  protected static void addAttribute(
    AttributesImpl attr,
    String name,
    String value
  )
    throws SAXException
  {
    attr.addAttribute("", name, "", "CDATA", value);
  }

  /**
   * Add string data
   *
   * @param contentHandler The SAX content handler
   * @param data The string data
   */
  protected static void data(
    ContentHandler contentHandler,
    String data
  )
    throws SAXException
  {
    contentHandler.characters(data.toCharArray(), 0, data.length());
  }
}
