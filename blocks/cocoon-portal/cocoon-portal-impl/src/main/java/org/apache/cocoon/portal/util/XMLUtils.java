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
package org.apache.cocoon.portal.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XML utility methods.
 *
 * @version $Id$
 */
public class XMLUtils {

    /**
     * Empty attributes.
     */
    public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    /**
     * Add string data
     *
     * @param contentHandler The SAX content handler
     * @param data The string data
     */
    public static void data(ContentHandler contentHandler,
                            String data)
    throws SAXException {
        final char[] c = data.toCharArray();
        contentHandler.characters(c, 0, c.length);
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
     * Add an attribute of type CDATA with empty Namespace to the end of the list.
     *
     * <p>For the sake of speed, this method does no checking
     * to see if the attribute is already in the list: that is
     * the responsibility of the application.</p>
     *
     * @param localName The local name.
     * @param value The attribute value.
     */
    public static void addCDATAAttribute(AttributesImpl ai, String localName, String value) {
        ai.addAttribute("", localName, localName, "CDATA", value);
    }

}
