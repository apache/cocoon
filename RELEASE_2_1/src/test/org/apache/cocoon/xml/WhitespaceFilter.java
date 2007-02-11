/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: WhitespaceFilter.java,v 1.4 2003/05/26 08:44:30 stephan Exp $
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
