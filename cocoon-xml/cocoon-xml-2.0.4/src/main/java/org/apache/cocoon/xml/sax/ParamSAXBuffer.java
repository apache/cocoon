/*
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
package org.apache.cocoon.xml.sax;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * Any <code>{name}</code> expression inside of the character events can be
 * replaced by the content of another SAXBuffer if it is present in the map
 * passed to the {@link #toSAX(ContentHandler, Map&lt;String, SAXBuffer&gt;)}
 * method.
 */
public class ParamSAXBuffer extends SAXBuffer {

    /**
     * If ch (in characters()) contains an unmatched '{' then
     * we save the chars from '{' onward in previous_ch.
     * Next call to characters() prepends the saved chars to ch before processing
     * (and sets prevChar to null).
     */
    private char[] prevChar = null;

    public ParamSAXBuffer() {
        super();
    }

    public ParamSAXBuffer(final SAXBuffer saxBuffer) {
        super(saxBuffer);
    }

    /**
     * Parses text and extracts <code>{name}</code> parameters for later
     * substitution.
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (prevChar != null) {
            // prepend char's from previous_ch to ch
            final char[] buf = new char[length + prevChar.length];
            System.arraycopy(prevChar, 0, buf, 0, prevChar.length);
            System.arraycopy(ch, start, buf, prevChar.length, length);
            ch = buf;
            start = 0;
            length += prevChar.length;
            prevChar = null;
        }

        final int end = start + length;
        StringBuilder name;
        int j;
        for (int i = start; i < end; i++) {
            if (ch[i] == '{') {
                // Send any collected characters so far
                if (i > start) {
                    addBit(new Characters(ch, start, i - start));
                }

                // Find closing brace, and construct parameter name
                name = new StringBuilder();
                j = i + 1;
                for (; j < end; j++) {
                    if (ch[j] == '}') {
                        break;
                    }
                    name.append(ch[j]);
                }
                if (j == end) {
                    // '{' without a closing '}'
                    // save char's from '{' in previous_ch in case the following
                    // call to characters() provides the '}'
                    prevChar = new char[end - i];
                    System.arraycopy(ch, i, prevChar, 0, end - i);
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

    @Override
    public void endElement(final String namespaceURI, final String localName,
            final String qName)
            throws SAXException {

        flushChars();
        super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start,
            final int length)
            throws SAXException {

        flushChars();
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {

        flushChars();
        super.processingInstruction(target, data);
    }

    @Override
    public void startDocument()
            throws SAXException {

        flushChars();
        super.startDocument();
    }

    @Override
    public void startElement(final String namespaceURI, final String localName,
            final String qName, final Attributes atts)
            throws SAXException {

        flushChars();
        super.startElement(namespaceURI, localName, qName, atts);
    }

    @Override
    public void endDocument()
            throws SAXException {

        flushChars();
        super.endDocument();
    }

    @Override
    public void comment(final char[] ch, final int start, final int length)
            throws SAXException {

        flushChars();
        super.comment(ch, start, length);
    }

    @Override
    public void endDTD()
            throws SAXException {

        flushChars();
        super.endDTD();
    }

    @Override
    public void startDTD(final String name, final String publicId,
            final String systemId)
            throws SAXException {

        flushChars();
        super.startDTD(name, publicId, systemId);
    }

    private void flushChars() {
        // Handle saved chars (in case we had a '{' with no matching '}').
        if (prevChar != null) {
            addBit(new Characters(prevChar, 0, prevChar.length));
            prevChar = null;
        }
    }

    /**
     * @param parameters map containing SaxBuffers
     */
    public void toSAX(final ContentHandler contentHandler,
            final Map<String, SAXBuffer> parameters)
            throws SAXException {
        
        for (SaxBit saxbit : saxbits) {
            if (saxbit instanceof Parameter) {
                ((Parameter) saxbit).send(contentHandler, parameters);
            } else {
                saxbit.send(contentHandler);
            }
        }
    }

    final static class Parameter implements SaxBit {

        private final String name;

        public Parameter(final String name) {
            this.name = name;
        }

        @Override
        public void send(final ContentHandler contentHandler) {
        }

        public void send(final ContentHandler contentHandler,
                final Map<String, SAXBuffer> parameters)
                throws SAXException {

            final SAXBuffer value = parameters.get(name);
            if (value != null) {
                value.toSAX(contentHandler);
            }
        }

        public void toString(final StringBuilder result,
                final Map<String, String> parameters)
                throws SAXException {

            final String value = parameters.get(name);
            if (value != null) {
                result.append(value);
            }
        }

        @Override
        public void dump(final Writer writer)
                throws IOException {

            writer.write("[Parameter] name=" + name);
        }
    }
}
