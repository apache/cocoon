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
package org.apache.cocoon.xml;

import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Abstract implementation of {@link XMLFragment} for objects that are more
 * easily represented as a DOM.
 *
 * <p>The {@link #toSAX} method is implemented by streaming (using a
 * {@link DOMStreamer}) the results of the {@link #toDOM(Node)} that must be
 * implemented by concrete subclasses.</p>
 *
 * @version $Id$
 */
public abstract class AbstractDOMFragment implements XMLFragment {

    /**
     * Generates SAX events representing the object's state by serializing the
     * result of <code>toDOM()</code>.
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        // The ServiceManager is unknown here : use JAXP to create a document
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SAXException("Couldn't get a DocumentBuilder", e);
        }

        Document doc = builder.newDocument();

        // Create a DocumentFragment that will hold the results of toDOM()
        // (which can create several top-level elements)
        Node df = doc.createDocumentFragment();

        // Build the DOM representation of this object
        try {
            toDOM(df);
        } catch(Exception e) {
            throw new SAXException("Exception while converting object to DOM", e);
        }

        // Stream the document fragment
        handler.startDocument();
        new DOMStreamer(handler).stream(df);
        handler.endDocument();
    }
}
