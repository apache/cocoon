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

import org.apache.cocoon.xml.AbstractXMLPipe;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A SAX filter to remove whitespace character, which disturb the
 * XML matching process.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: WhitespaceFilter.java,v 1.5 2004/03/08 14:04:20 cziegeler Exp $
 */
public class WhitespaceFilter extends AbstractXMLPipe {
    private StringBuffer buffer = null;

    /**
     * Create a new WhitespaceFilter.
     *
     * @param handler Content handler.
     */
    public WhitespaceFilter(ContentHandler handler) {
        setContentHandler(handler);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char c[], int start, int len) throws SAXException {
        if (contentHandler==null) {
            return;
        }

        if (buffer==null) {
            buffer = new StringBuffer();
        }

        buffer.append(c, start, len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char c[], int start,
                                    int len) throws SAXException {
        // ignore
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName,
                             Attributes atts) throws SAXException {

        pushText();      
        contentHandler.startElement(namespaceURI, localName, qName, atts);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
        throws SAXException {

        pushText();
        contentHandler.endElement(uri, loc, raw);        
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        pushText();
        contentHandler.processingInstruction(target, data);
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
        throws SAXException {
  
        pushText();
        super.comment(ch, start, len);
    }


    public void pushText() throws SAXException {

        if (buffer!=null) {
            String text = buffer.toString();

            StringBuffer normalized = new StringBuffer();

            for(int i=0; i<text.length(); i++) {
                if (Character.isWhitespace(text.charAt(i))) {
                    normalized.append(' ');
                    while (((i+1)<text.length()) && (Character.isWhitespace(text.charAt(i+1))))
                        i++;
                } else {
                    normalized.append(text.charAt(i));
                }
            }

            text = normalized.toString().trim();

            if (text.length()>0) {
                contentHandler.characters(text.toCharArray(), 0,
                                          text.length());
            }

            buffer = null;
        }
    }
}
