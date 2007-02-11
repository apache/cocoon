/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * @version CVS $Id: SimpleSlopParser.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
 */

public class SimpleSlopParser implements SlopParser,SlopConstants {
    private ContentHandler contentHandler;

    /** chars that can be part of a field name (other than letters) */
    private final static String DEFAULT_TAGNAME_CHARS = "-_";
    private String tagnameChars = DEFAULT_TAGNAME_CHARS;

    /** valid characters in an XML element name (in addition to letters and digits) */
    final static String VALID_TAGNAME_CHARS = "_-";
    final static String TAGNAME_REPLACEMENT_CHAR = "_";

    /** optionally preserve whitespace in input */
    private boolean preserveSpace = false;

    /** count lines */
    private int lineCounter;

    /** result of parsing a line */
    static class ParsedLine {
        final String name;
        final String contents;

        ParsedLine(String elementName, String elementContents) {
            name = filterElementName(elementName);
            contents = elementContents;
        }
    }

    /** make sure element names are valid XML */
    static String filterElementName(String str) {
        final StringBuffer sb = new StringBuffer();
        for(int i=0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if(Character.isLetter(c)) {
                sb.append(c);
            } else if(Character.isDigit(c) && i > 0) {
                sb.append(c);
            } else if(VALID_TAGNAME_CHARS.indexOf(c) >= 0) {
                sb.append(c);
            } else {
                sb.append(TAGNAME_REPLACEMENT_CHAR);
            }
        }
        return sb.toString();
    }

    /** set the list of valid chars for tag names (in addition to letters) */
    public void setValidTagnameChars(String str) {
        tagnameChars = (str == null ? DEFAULT_TAGNAME_CHARS : str.trim());
    }

    /** optionally preserve whitespace in input */
    public void setPreserveWhitespace(boolean b) {
        preserveSpace = b;
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

    /** add simple name-value attribute to attr */
    private void setAttribute(AttributesImpl attr,String name,String value) {
        final String ATTR_TYPE = "NMTOKEN";
        attr.addAttribute("",name,name,ATTR_TYPE,value);
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
        lineCounter++;
        final AttributesImpl atts = new AttributesImpl();
        setAttribute(atts,SLOP_ATTR_LINENUMBER,String.valueOf(lineCounter));
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
                    final boolean isFieldChar = Character.isLetter(c) || tagnameChars.indexOf(c) >= 0;
                    if(!isFieldChar) {
                        fieldFound = false;
                        break;
                    }
                }

                if(fieldFound) {
                    String contents = "";
                    if(line.length() > colonPos + 1) {
                        final String str = line.substring(colonPos+1);
                        contents = (preserveSpace ? str : str.trim());
                    }
                    result = new ParsedLine(line.substring(0,colonPos),contents);
                }
            }
        }

        // default: output a line element
        if(result == null) {
            final String str = (preserveSpace ? line : line.trim());
            result = new ParsedLine(SLOP_LINE_ELEMENT,str);
        }

        return result;
    }
}
