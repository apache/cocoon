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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * Any <code>{name}</code> expression inside of the character events can be
 * replaced by the content of another SaxBuffer if it is present in the map
 * passed to the {@link #toSAX(ContentHandler, Map)} method.
 * 
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ParamSaxBuffer.java,v 1.4 2004/03/17 16:20:08 vgritsenko Exp $
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
     * Parses text and extracts <code>{name}</code> parameters for later
     * substitution.
     */
    public void characters(char ch[], int start, int length) throws SAXException {
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
                    throw new SAXException("Unclosed '}'");
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

        public void send(ContentHandler contentHandler) throws SAXException {
        }

        public void send(ContentHandler contentHandler, Map parameters) throws SAXException {
            SaxBuffer value = (SaxBuffer)parameters.get(name);
            if (value != null) {
                value.toSAX(contentHandler);
            }
        }
    }
}
