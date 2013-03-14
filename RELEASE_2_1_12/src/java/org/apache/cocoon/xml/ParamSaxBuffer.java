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

import java.util.Iterator;
import java.util.Map;
import java.io.Writer;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * Any <code>{name}</code> expression inside of the character events can be
 * replaced by the content of another SaxBuffer if it is present in the map
 * passed to the {@link #toSAX(ContentHandler, Map)} method.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class ParamSaxBuffer extends SaxBuffer {

   /**
    * If ch (in characters()) contains an unmatched '{' then
    * we save the chars from '{' onward in previous_ch.
    * Next call to characters() prepends the saved chars to ch before processing
    * (and sets previous_ch to null).
    */
    private char[] previous_ch = null;

    /**
     * Creates empty SaxBuffer
     */
    public ParamSaxBuffer() {
    }

    /**
     * Creates copy of another SaxBuffer
     */
    public ParamSaxBuffer(SaxBuffer saxBuffer) {
        super(saxBuffer);
    }

    /**
     * Parses text and extracts <code>{name}</code> parameters for later
     * substitution.
     */
    public void characters(char ch[], int start, int length) throws SAXException {

        if (previous_ch != null) {
            // prepend char's from previous_ch to ch
            char[] buf = new char[length + previous_ch.length];
            System.arraycopy(previous_ch, 0, buf, 0, previous_ch.length);
            System.arraycopy(ch, start, buf, previous_ch.length, length);
            ch = buf;
            start = 0;
            length += previous_ch.length;
            previous_ch = null;
        }

        final int end = start + length;
        for (int i = start; i < end; i++) {
            if (ch[i] == '{') {
                // Send any collected characters so far
                if (i > start) {
                    addBit(new Characters(ch, start, i - start));
                }

                // Find closing brace, and construct parameter name
                StringBuffer name = new StringBuffer();
                int j = i + 1;
                for (; j < end; j++) {
                    if (ch[j] == '}') {
                        break;
                    }
                    name.append(ch[j]);
                }
                if (j == end) {
                    // '{' without a closing '}'
                    // save char's from '{' in previous_ch in case the following call to characters()
                    // provides the '}'
                    previous_ch = new char[end - i];
                    System.arraycopy(ch, i, previous_ch, 0, end - i);
                    return;
                }
                addBit(new Parameter(name.toString()));

                // Continue processing
                i = j;
                start = j + 1;
                continue;
            }
        }

        // Send any tailing characters
        if (start < end) {
            addBit(new Characters(ch, start, end - start));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        flushChars();
        super.endElement(namespaceURI, localName, qName);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        flushChars();
        super.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flushChars();
        super.processingInstruction(target, data);
    }

    public void startDocument() throws SAXException {
        flushChars();
        super.startDocument();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        flushChars();
        super.startElement(namespaceURI, localName, qName, atts);
    }

    public void endDocument() throws SAXException {
        flushChars();
        super.endDocument();
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        flushChars();
        super.comment(ch, start, length);
    }

    public void endDTD() throws SAXException {
        flushChars();
        super.endDTD();
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        flushChars();
        super.startDTD(name, publicId, systemId);
    }

    private void flushChars() {
        // Handle saved chars (in case we had a '{' with no matching '}').
        if (previous_ch != null) {
            addBit(new Characters(previous_ch, 0, previous_ch.length));
            previous_ch = null;
        }
    }

    /**
     * @param parameters map containing SaxBuffers
     */
    public void toSAX(ContentHandler contentHandler, Map parameters) throws SAXException {
        for (Iterator i = bits(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            if (saxbit instanceof Parameter) {
                ((Parameter)saxbit).send(contentHandler, parameters);
            } else {
                saxbit.send(contentHandler);
            }
        }
    }

    /**
     * @param parameters map containing SaxBuffers
     */
    public String toString(Map parameters) throws SAXException {
        final StringBuffer buffer = new StringBuffer();
        for (Iterator i = bits(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            if (saxbit instanceof Parameter) {
                ((Parameter)saxbit).toString(buffer, parameters);
            } else if (saxbit instanceof Characters) {
                ((Characters) saxbit).toString(buffer);
            }
        }
        return buffer.toString();
    }


    final static class Parameter implements SaxBit {
        private final String name;

        public Parameter(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) {
        }

        public void send(ContentHandler contentHandler, Map parameters) throws SAXException {
            SaxBuffer value = (SaxBuffer)parameters.get(name);
            if (value != null) {
                value.toSAX(contentHandler);
            }
        }

        public void toString(StringBuffer result, Map parameters) throws SAXException {
            String value = (String)parameters.get(name);
            if (value != null) {
                result.append(value);
            }
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[Parameter] name=" + name);
        }
    }
}
