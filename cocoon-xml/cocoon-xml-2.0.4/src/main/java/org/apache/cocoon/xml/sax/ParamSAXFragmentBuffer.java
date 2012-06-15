/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX buffer which passes only the content of the document element to the content handler. An element with an arbitrary
 * local name can be used the wrapper element.
 */
public class ParamSAXFragmentBuffer extends ParamSAXBuffer {

    private static final long serialVersionUID = -9153292487513611344L;

    private int depth = 0;

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName,
            final Attributes atts) throws SAXException {

        if (this.depth > 0) {
            super.startElement(namespaceURI, localName, qName, atts);
        }
        this.depth++;
    }

    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName)
            throws SAXException {

        this.depth--;
        if (this.depth > 0) {
            super.endElement(namespaceURI, localName, qName);
        }
    }
}
