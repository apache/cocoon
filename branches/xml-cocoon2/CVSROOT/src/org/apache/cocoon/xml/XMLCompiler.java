/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */
package org.apache.cocoon.xml;

import java.io.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-11 12:41:31 $
 */

public final class XMLCompiler implements ContentHandler {

    private CompiledXMLOutputStream out;

    public void setOutputStream(OutputStream stream) throws IOException {
        this.out = new CompiledXMLOutputStream(stream);
    }

    public static final int START_DOCUMENT         = 0;
    public static final int END_DOCUMENT           = 1;
    public static final int START_PREFIX_MAPPING   = 2;
    public static final int END_PREFIX_MAPPING     = 3;
    public static final int START_ELEMENT          = 4;
    public static final int END_ELEMENT            = 5;
    public static final int CHARACTERS             = 6;
    public static final int IGNORABLE_WHITESPACE   = 7;
    public static final int PROCESSING_INSTRUCTION = 8;


        public void startDocument() throws SAXException {
            try {
                out.writeEvent(START_DOCUMENT);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void endDocument() throws SAXException {
            try {
                out.writeEvent(END_DOCUMENT);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
            try {
                out.writeEvent(START_PREFIX_MAPPING);
                out.writeString(prefix);
                out.writeString(uri);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void endPrefixMapping(java.lang.String prefix) throws SAXException {
            try {
                out.writeEvent(END_PREFIX_MAPPING);
                out.writeString(prefix);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void startElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName, Attributes atts) throws SAXException {
            try {
                int length = atts.getLength();
                out.writeEvent(START_ELEMENT);
                out.writeAttributes(length);
                for (int i = 0; i < length; i++) {
                    out.writeString(atts.getURI(i));
                    out.writeString(atts.getLocalName(i));
                    out.writeString(atts.getQName(i));
                    out.writeString(atts.getType(i));
                    out.writeString(atts.getValue(i));
                }
                out.writeString(namespaceURI);
                out.writeString(localName);
                out.writeString(qName);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void endElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName) throws SAXException {
            try {
                out.writeEvent(END_ELEMENT);
                out.writeString(namespaceURI);
                out.writeString(localName);
                out.writeString(qName);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            try {
                out.writeEvent(CHARACTERS);
                out.writeChars(ch, start, length);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            try {
                out.writeEvent(IGNORABLE_WHITESPACE);
                out.writeChars(ch, start, length);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void processingInstruction(java.lang.String target, java.lang.String data) throws SAXException {
            try {
                out.writeEvent(PROCESSING_INSTRUCTION);
                out.writeString(target);
                out.writeString(data);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public void setDocumentLocator(Locator locator) {
            // ignore.
        }

        public void skippedEntity(java.lang.String name) throws SAXException {
            // ignore.
        }
    }

    class ErrorHandler implements org.xml.sax.ErrorHandler {

        /** Warning. */
        public void warning(SAXParseException ex) {
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Error. */
        public void error(SAXParseException ex) {
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Fatal error. */
        public void fatalError(SAXParseException ex) throws SAXException {
            System.err.println("[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Returns a string of the location. */
        private String getLocationString(SAXParseException ex) {
            StringBuffer str = new StringBuffer();

            String systemId = ex.getSystemId();
            if (systemId != null) {
                int index = systemId.lastIndexOf('/');
                if (index != -1)
                    systemId = systemId.substring(index + 1);
                str.append(systemId);
            }
            str.append(':');
            str.append(ex.getLineNumber());
            str.append(':');
            str.append(ex.getColumnNumber());

            return str.toString();
        }
}
