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

package org.apache.cocoon.slop.parsing;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.slop.interfaces.SlopParser;
import org.apache.cocoon.slop.interfaces.SlopConstants;

/** Simplistic SLOP parser, recognizes the following constructs:
 *
 *      Field: a line starting with letters and : is considered a field
 *
 *      Empty lines are detected.
 *      Other lines are output as line elements
 *
 *  This is sufficient for basic parsing of RFC 822 headers,
 *  but a configurable rfc822 mode would be good to differentiate
 *  between the header and body of the email message and parse them
 *  with different rules.
 *
 * @author <a href="mailto:bdelacretaz@apache.org">Bertrand Delacretaz</a>
 * @version CVS $Id: SimpleSlopParser.java,v 1.1 2003/08/06 12:59:13 bdelacretaz Exp $
 */

public class SimpleSlopParser implements SlopParser,SlopConstants {
    private ContentHandler contentHandler;

    /** chars that can be part of a field name (other than letters) */
    private final static String FIELD_CHARS = "-_";

    /** result of parsing a line */
    static class ParsedLine {
        final String name;
        final String contents;

        ParsedLine(String elementName, String elementContents) {
            name = elementName;
            contents = elementContents;
        }
    }

    /** must be called before any call to processLine() */
    public void startDocument(ContentHandler destination)
        throws SAXException, ProcessingException {
        contentHandler = destination;
        contentHandler.startDocument();
        contentHandler.startPrefixMapping("", SLOP_NAMESPACE_URI);
        final AttributesImpl atts = new AttributesImpl();
        contentHandler.startElement(SLOP_NAMESPACE_URI, SLOP_ROOT_ELEMENT, SLOP_ROOT_ELEMENT, atts);
    }

    /** must be called once all calls to processLine() are done */
    public void endDocument()
        throws SAXException, ProcessingException {
        contentHandler.endElement(SLOP_NAMESPACE_URI, SLOP_ROOT_ELEMENT, SLOP_ROOT_ELEMENT);
        contentHandler.endPrefixMapping("");
        contentHandler.endDocument();
        contentHandler = null;
    }

    /** call this to process input lines, does the actual parsing */
    public void processLine(String line)
        throws SAXException, ProcessingException {
        if(contentHandler == null) {
            throw new ProcessingException("SimpleSlopParser content handler is null (startDocument not called?)");
        }

        // find out which element name to use, based on the contents of the line
        final ParsedLine p = parseLine(line);

        // generate the element and its contents
        final AttributesImpl atts = new AttributesImpl();
        contentHandler.startElement(SLOP_NAMESPACE_URI, p.name, p.name, atts);
        contentHandler.characters(p.contents.toCharArray(),0,p.contents.length());
        contentHandler.endElement(SLOP_NAMESPACE_URI, p.name, p.name);
    }

    /** parse a line, extract element name and contents */
    protected ParsedLine parseLine(String line) {
        ParsedLine result = null;

        // empty lines
        if(line == null || line.trim().length()==0) {
            result = new ParsedLine(SLOP_EMPTY_LINE_ELEMENT,"");
        }

        // simple extraction of field names, lines starting with alpha chars followed
        // by a colon are parsed as follows:
        //
        //  input:
        //      field-name: this line is a field
        //  output:
        //      <field-name>this line is a field</field-name>
        if(result == null) {
            final int colonPos = line.indexOf(':');
            if(colonPos > 0) {
                boolean fieldFound = true;
                for(int i=0; i < colonPos; i++) {
                    final char c = line.charAt(i);
                    final boolean isFieldChar = Character.isLetter(c) || FIELD_CHARS.indexOf(c) >= 0;
                    if(!isFieldChar) {
                        fieldFound = false;
                        break;
                    }
                }

                if(fieldFound) {
                    String contents = "";
                    if(line.length() > colonPos + 1) {
                        contents = line.substring(colonPos+1).trim();
                    }
                    result = new ParsedLine(line.substring(0,colonPos),contents);
                }
            }
        }

        // default: output a line element
        if(result == null) {
            result = new ParsedLine(SLOP_LINE_ELEMENT,line.trim());
        }

        return result;
    }
}
