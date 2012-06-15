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
package org.apache.cocoon.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * XML utility methods.
 *
 * @version $Id$
 */
public class SAXUtils {

    /**
     * Empty attributes immutable object.
     */
    public static final Attributes EMPTY_ATTRIBUTES = new ImmutableAttributesImpl();

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
     * Create a start and endElement with a empty Namespace and without Attributes
     *
     * @param localName The local name (without prefix)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement(ContentHandler, String)
     */
    public static void createElement(ContentHandler contentHandler,
                                     String localName)
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
    public static void createElement(ContentHandler contentHandler,
                                     String localName,
                                     String stringValue)
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
    public static void createElement(ContentHandler contentHandler,
                                     String localName,
                                     Attributes atts)
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
    public static void createElement(ContentHandler contentHandler,
                                     String localName,
                                     Attributes atts,
                                     String stringValue)
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
    public static void createElementNS(ContentHandler contentHandler,
                                       String namespaceURI,
                                       String localName)
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
    public static void createElementNS(ContentHandler contentHandler,
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
    public static void createElementNS(ContentHandler contentHandler,
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
    public static void createElementNS(ContentHandler contentHandler,
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
    public static void endElement(ContentHandler contentHandler,
                                  String localName)
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
    public static void endElement(ContentHandler contentHandler,
                                  String namespaceURI,
                                  String localName)
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
    public static void startElement(ContentHandler contentHandler,
                                    String localName)
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
    public static void startElement(ContentHandler contentHandler,
                                    String namespaceURI,
                                    String localName)
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
    public static void startElement(ContentHandler contentHandler,
                                    String localName,
                                    Attributes atts)
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
    public static void startElement(ContentHandler contentHandler,
                                    String namespaceURI,
                                    String localName,
                                    Attributes atts)
    throws SAXException {

        contentHandler.startElement(namespaceURI, localName, localName, atts);
    }
}
