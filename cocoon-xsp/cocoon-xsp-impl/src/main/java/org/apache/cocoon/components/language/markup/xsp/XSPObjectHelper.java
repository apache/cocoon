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
package org.apache.cocoon.components.language.markup.xsp;

import java.util.Collection;
import java.util.Iterator;

import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Base class for XSP's object model manipulation logicsheets
 *
 * @version $Id$
 */
public class XSPObjectHelper {

    /**
     * Output an element containing text only and no attributes
     *
     * @param contentHandler The SAX content handler
     * @param name The element name
     * @param data The data contained by the element
     */
    protected static void elementData(String uri,
                                      String prefix,
                                      ContentHandler contentHandler,
                                      String name,
                                      String data)
    throws SAXException {
        start(uri, prefix, contentHandler, name);
        data(contentHandler, data);
        end(uri, prefix, contentHandler, name);
    }

    /**
     * Output an element containing text only and attributes
     *
     * @param contentHandler The SAX content handler
     * @param name The element name
     * @param data The data contained by the element
     * @param attr The element attributes
     */
    protected static void elementData(String uri,
                                      String prefix,
                                      ContentHandler contentHandler,
                                      String name,
                                      String data,
                                      AttributesImpl attr)
    throws SAXException {
        start(uri, prefix, contentHandler, name, attr);
        data(contentHandler, data);
        end(uri, prefix, contentHandler, name);
    }

    /**
     * Start an element with the proper object's uri and prefix and no
     * attributes
     *
     * @param contentHandler The SAX content handler
     * @param name The element name
     */
    protected static void start(String uri,
                                String prefix,
                                ContentHandler contentHandler,
                                String name)
    throws SAXException {
        contentHandler.startElement(uri, name, prefix + ":" + name, XMLUtils.EMPTY_ATTRIBUTES);
    }

    /**
     * Start an element with the proper object's uri and prefix and with
     * attributes
     *
     * @param contentHandler The SAX content handler
     * @param name The element name
     * @param attr The element attributes
     */
    protected static void start(String uri,
                                String prefix,
                                ContentHandler contentHandler,
                                String name,
                                AttributesImpl attr)
    throws SAXException {
        contentHandler.startElement(uri, name, prefix + ":" + name, attr);
    }

    /**
     * End an element with the proper object's uri and prefix
     *
     * @param contentHandler The SAX content handler
     * @param name The element name
     */
    protected static void end(String uri,
                              String prefix,
                              ContentHandler contentHandler,
                              String name)
    throws SAXException {
        contentHandler.endElement(uri, name, prefix + ":" + name);
    }

    /**
     * Add an attribute
     *
     * @param attr The attribute list
     * @param name The attribute name
     * @param value The attribute value
     */
    protected static void addAttribute(AttributesImpl attr,
                                       String name,
                                       String value)
    throws SAXException {
        attr.addAttribute("", name, name, "CDATA", value);
    }

    /**
     * Add string data
     *
     * @param contentHandler The SAX content handler
     * @param data The string data
     */
    protected static void data(ContentHandler contentHandler, String data)
    throws SAXException {
        contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    // <xsp:expr> methods

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>char</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, char v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>byte</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, byte v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>boolean</code> :
     * outputs characters representing the value (true / false).
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, boolean v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>int</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, int v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>long</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, long v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>long</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, float v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>double</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param v the value
     */
    public static void xspExpr(ContentHandler contentHandler, double v)
    throws SAXException {
        data(contentHandler, String.valueOf(v));
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>String</code> :
     * outputs characters representing the value.
     *
     * @param contentHandler the SAX content handler
     * @param text the value
     */
    public static void xspExpr(ContentHandler contentHandler, String text)
    throws SAXException {
        if (text != null) {
            data(contentHandler, text);
        }
    }

    // Now handled by XMLizable
    //  /**
    //   * Implementation of &lt;xsp:expr&gt; for <code>XMLFragment</code> :
    //   * outputs the value by calling <code>v.toSax(contentHandler)</code>.
    //   *
    //   * @param contentHandler the SAX content handler
    //   * @param v the XML fragment
    //   */
    //  public static void xspExpr(ContentHandler contentHandler, XMLFragment v) throws SAXException
    //  {
    //    if (v != null)
    //    {
    //      v.toSAX(contentHandler);
    //    }
    //  }

    /**
     * Implementation of &lt;xsp:expr&gt; for <code>XMLizable</code> :
     * outputs the value by calling <code>v.toSax(contentHandler)</code>.
     *
     * @param contentHandler the SAX content handler
     * @param v the XML fragment
     */
    public static void xspExpr(ContentHandler contentHandler, XMLizable v)
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
    public static void xspExpr(ContentHandler contentHandler, Node v)
    throws SAXException {
        if (v != null) {
            DOMStreamer streamer = new DOMStreamer(contentHandler);
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
    public static void xspExpr(ContentHandler contentHandler, Collection v)
    throws SAXException {
        if (v != null) {
            Iterator iterator = v.iterator();
            while (iterator.hasNext()) {
                xspExpr(contentHandler, iterator.next());
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
    public static void xspExpr(ContentHandler contentHandler, Object v)
    throws SAXException {
        if (v == null) {
            return;
        }

        // Array: recurse over each element
        if (v.getClass().isArray()) {
            Object[] elements = (Object[]) v;

            for (int i = 0; i < elements.length; i++) {
                xspExpr(contentHandler, elements[i]);
            }
            return;
        }

        // Check handled object types in case they were not typed in the XSP

        // XMLizable
        if (v instanceof XMLizable) {
            xspExpr(contentHandler, (XMLizable) v);
            return;
        }

        // Now handled by XMLizable
        //    // XMLFragment
        //    if (v instanceof XMLFragment)
        //    {
        //      xspExpr(contentHandler, (XMLFragment)v);
        //      return;
        //    }

        // Node
        if (v instanceof Node) {
            xspExpr(contentHandler, (Node) v);
            return;
        }

        // Collection
        if (v instanceof Collection) {
            xspExpr(contentHandler, (Collection) v);
            return;
        }

        // Give up: hope it's a string or has a meaningful string representation
        data(contentHandler, String.valueOf(v));
    }
}
