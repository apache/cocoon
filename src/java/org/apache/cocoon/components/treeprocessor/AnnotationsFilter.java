/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.treeprocessor;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/** Filter out annotations in the sitemap
 *  (bugzilla 25352)
 *  $Id$
 */
class AnnotationsFilter implements ContentHandler {
    public static final String ANNOTATIONS_NAMESPACE = "http://apache.org/cocoon/sitemap/annotations/1.0";

    private ContentHandler delegate;

    private int nestingLevel;

    private boolean isOutsideAnnotation()
    {
        return nestingLevel == 0;
    }

    public AnnotationsFilter(ContentHandler delegate) {
        this.delegate = delegate;
    }

    public void setDocumentLocator(Locator locator) {
        delegate.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    public void endDocument() throws SAXException {
        delegate.endDocument();
    }

    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.startPrefixMapping(prefix, namespaceURI);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.endPrefixMapping(prefix);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
        if (namespaceURI !=  null && namespaceURI.equals(ANNOTATIONS_NAMESPACE)) {
            nestingLevel++;
        }
        if (isOutsideAnnotation()) {
            delegate.startElement(namespaceURI, localName, qName, attributes);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.endElement(namespaceURI, localName, qName);
        }
        if (namespaceURI !=  null && namespaceURI.equals(ANNOTATIONS_NAMESPACE)) {
            nestingLevel--;
        }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.characters(ch, start, len);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int len) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.ignorableWhitespace(ch, start, len);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.processingInstruction(target, data);
        }
    }

    public void skippedEntity(String name) throws SAXException {
        if (isOutsideAnnotation()) {
            delegate.skippedEntity(name);
        }
    }
}
