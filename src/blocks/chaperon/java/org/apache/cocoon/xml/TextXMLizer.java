/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:   "This product includes software
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * This XMLizer creates a SAX stream of a plain text document. The
 * text will be embedded in a text element.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: TextXMLizer.java,v 1.1 2003/03/09 00:02:50 pier Exp $
 */
public class TextXMLizer implements SAXParser, ThreadSafe, Component {

    /** The URI of the text element */
    public final static String URI = "http://chaperon.sourceforge.net/schema/text/1.0";

    /**
     * Parse the {@link InputSource} and send
     * SAX events to the consumer.
     *
     * @param source Input source.
     * @param contentHandler Content handler.
     * @param lexicalHandler Lexical handler.
     */
    public void parse(InputSource source, ContentHandler contentHandler,
                      LexicalHandler lexicalHandler)
                        throws SAXException, IOException {
        parse(source, contentHandler);
    }

    /**
     * Parse the {@link InputSource} and send
     * SAX events to the consumer.
     * Attention: the consumer can  implement the
     * {@link LexicalHandler} as well.
     * The parse should take care of this.
     *
     * @param source
     * @param contentHandler
     */
    public void parse(InputSource source,
                      ContentHandler contentHandler)
                        throws SAXException, IOException {

        LocatorImpl locator = new LocatorImpl();

        locator.setPublicId(source.getPublicId());
        locator.setSystemId(source.getSystemId());
        locator.setLineNumber(1);
        locator.setColumnNumber(1);

        contentHandler.setDocumentLocator(locator);
        contentHandler.startDocument();
        contentHandler.startPrefixMapping("", URI);

        AttributesImpl atts = new AttributesImpl();

        contentHandler.startElement(URI, "text", "text", atts);

        LineNumberReader reader = null;

        if (source.getCharacterStream()!=null) {
            reader = new LineNumberReader(source.getCharacterStream());
        } else {
            reader = new LineNumberReader(new InputStreamReader(source.getByteStream()));
        }

        String line, newline = null;
        String separator = System.getProperty("line.separator");

        while (true) {
            if (newline==null) {
                line = reader.readLine();
            } else {
                line = newline;
            }

            if (line==null) {
                break;
            }

            newline = reader.readLine();

            line = (newline!=null) ? line+separator : line;

            locator.setLineNumber(reader.getLineNumber());
            locator.setColumnNumber(1);
            contentHandler.characters(line.toCharArray(), 0, line.length());

            if (newline==null) {
                break;
            }
        }

        contentHandler.endElement(URI, "text", "text");

        contentHandler.endPrefixMapping("");
        contentHandler.endDocument();

    }
}
