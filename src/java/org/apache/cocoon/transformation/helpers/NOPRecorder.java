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
package org.apache.cocoon.transformation.helpers;

import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * The base class for all recorders. Simply does nothing
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: NOPRecorder.java,v 1.2 2004/03/08 14:03:31 cziegeler Exp $
*/
public abstract class NOPRecorder
implements ContentHandler, LexicalHandler, XMLConsumer {

    public NOPRecorder() {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument()
    throws SAXException {
    }

    public void endDocument()
    throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
    }

    public void endPrefixMapping(String prefix)
    throws SAXException {
    }

    public void startElement(String namespace, String name, String raw,
                         Attributes attr)
    throws SAXException {
    }

    public void endElement(String namespace, String name, String raw)
    throws SAXException {
    }

    public void characters(char ary[], int start, int length)
    throws SAXException {
    }

    public void ignorableWhitespace(char ary[], int start, int length)
    throws SAXException {
    }

    public void processingInstruction(String target, String data)
    throws SAXException {
    }

    public void skippedEntity(String name)
    throws SAXException {
    }

    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(char ary[], int start, int length)
    throws SAXException {
    }
}
