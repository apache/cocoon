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
package org.apache.cocoon.xml;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.Writer;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * <p>Any <code>{name}</code> expression inside of the character events can be
 * replaced by the content of another SaxBuffer if it is present in the map
 * passed to the {@link #toSAX(ContentHandler, Map)} method.
 *
 * <p>Once all events have been pushed into this buffer, you need to call
 * {@link #processParams()}.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class ParamSaxBuffer extends SaxBuffer {

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
     * Parses text in character events and extracts <code>{name}</code> parameters for later
     * substitution.
     */
    public void processParams() throws SAXException {
        // what we do here is running over all sax bits and copying them over
        // into a new list, and meanwhile processing all Character bits

        int initialSize;
        if (saxbits.size() <= 3)
            initialSize = 6;
        else
            initialSize = (int)((double)saxbits.size() * 1.35d);

        List newSaxBits = new ArrayList(initialSize);

        int i = 0;
        while (i < saxbits.size()) {
            Object bit = saxbits.get(i);
            if (bit instanceof Characters) {
                char[] chars = ((Characters)bit).ch;

                // check if there are more character events immediately following this one,
                // if so merge all data into one big array
                int consecutiveCharacterEventCount = 0;
                int totalLength = chars.length;
                for (int k = i + 1; k < saxbits.size(); k++) {
                    Object otherbit = saxbits.get(k);
                    if (otherbit instanceof Characters) {
                        consecutiveCharacterEventCount++;
                        totalLength += ((Characters)otherbit).ch.length;
                    } else {
                        break;
                    }
                }

                if (consecutiveCharacterEventCount > 0) {
                    char[] newchars = new char[totalLength];
                    System.arraycopy(chars, 0, newchars, 0, chars.length);
                    int newCharPos = chars.length;
                    for (int k = 0; k < consecutiveCharacterEventCount; k++) {
                        Object otherbit = saxbits.get(i + 1 + k);
                        char[] otherchars = ((Characters)otherbit).ch;
                        System.arraycopy(otherchars, 0, newchars, newCharPos, otherchars.length);
                        newCharPos += otherchars.length;
                    }
                    chars = newchars;
                    i += consecutiveCharacterEventCount + 1;
                } else {
                    i++;
                }

                // now process the actual {...}
                int start = 0;
                final int end = chars.length;
                for (int r = 0; r < end; r++) {
                    if (chars[r] == '{') {
                        // Send any collected characters so far
                        if (r > start) {
                            newSaxBits.add(new Characters(chars, start, r - start));
                        }

                        // Find closing brace, and construct parameter name
                        StringBuffer name = new StringBuffer();
                        int j = r + 1;
                        for (; j < end; j++) {
                            if (chars[j] == '}') {
                                break;
                            }
                            name.append(chars[j]);
                        }
                        if (j == end) {
                            throw new SAXException("Unclosed '}'");
                        }
                        newSaxBits.add(new Parameter(name.toString()));

                        // Continue processing
                        r = j;
                        start = j + 1;
                        continue;
                    }
                }

                // Send any tailing characters
                if (start < end) {
                    newSaxBits.add(new Characters(chars, start, end - start));
                }
            } else {
                newSaxBits.add(bit);
                i++;
            }
        }
        this.saxbits = newSaxBits;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        addBit(new Characters(ch, start, length));
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

        public void dump(Writer writer) throws IOException {
            writer.write("[ParamSaxBuffer.Parameter] name=" + name);
        }
    }
}
