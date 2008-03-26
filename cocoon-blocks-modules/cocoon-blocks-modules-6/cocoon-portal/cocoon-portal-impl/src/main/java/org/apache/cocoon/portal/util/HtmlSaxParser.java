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
package org.apache.cocoon.portal.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This parser uses the nekohtml parser to parse html and generate sax streams.
 *
 * @version $Id$
 */
public class HtmlSaxParser extends AbstractSAXParser {

    public HtmlSaxParser(Properties properties) {
        super(getConfig(properties));
    }

    protected static HTMLConfiguration getConfig(Properties properties) {
        HTMLConfiguration config = new HTMLConfiguration();
        config.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        if (properties != null) {
            for (Iterator i = properties.keySet().iterator();i.hasNext();) {
                String name = (String) i.next();
                config.setProperty(name, properties.getProperty(name));
            }
        }
        return config;
    }

    /**
     * Parse html stored in the string.
     */
    public static void parseString(String content, ContentHandler ch)
    throws SAXException {
        final HtmlSaxParser parser = new HtmlSaxParser(null);
        parser.setContentHandler(ch);
        if ( ch instanceof LexicalHandler ) {
            parser.setLexicalHandler((LexicalHandler)ch);
        }
        final InputSource is = new InputSource(new StringReader(content));
        try {
            parser.parse(is);
        } catch (IOException ioe) {
            throw new SAXException("Error during parsing of html markup.", ioe);
        }
    }

    /**
     * Return a content filter which streams every element except body and html.
     * @param ch
     * @return A content handler.
     */
    public static ContentHandler getContentFilter(ContentHandler ch) {
        if ( ch instanceof LexicalHandler ) {
            return new ExtendedContentFilter(ch);
        }
        return new ContentFilter(ch);
    }

    protected static class ContentFilter implements ContentHandler {

        protected final ContentHandler ch;

        public ContentFilter(ContentHandler ch) {
            this.ch = ch;
        }

        /**
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String loc, String raw) throws SAXException {
            if ( !loc.equals("html") && !loc.equals("body") ) {
                this.ch.endElement(uri, loc, raw);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
            if ( !loc.equals("html") && !loc.equals("body") ) {
                this.ch.startElement(uri, loc, raw, a);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            this.ch.characters(ch, start, length);
        }

        /**
         * @see org.xml.sax.ContentHandler#endDocument()
         */
        public void endDocument() throws SAXException {
            ch.endDocument();
        }

        /**
         * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
         */
        public void endPrefixMapping(String prefix) throws SAXException {
            ch.endPrefixMapping(prefix);
        }

        /**
         * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
         */
        public void ignorableWhitespace(char[] ch, int start, int length)
                throws SAXException {
            this.ch.ignorableWhitespace(ch, start, length);
        }

        /**
         * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
         */
        public void processingInstruction(String target, String data)
                throws SAXException {
            ch.processingInstruction(target, data);
        }

        /**
         * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        public void setDocumentLocator(Locator locator) {
            ch.setDocumentLocator(locator);
        }

        /**
         * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
         */
        public void skippedEntity(String name) throws SAXException {
            ch.skippedEntity(name);
        }

        /**
         * @see org.xml.sax.ContentHandler#startDocument()
         */
        public void startDocument() throws SAXException {
            ch.startDocument();
        }

        /**
         * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
         */
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            ch.startPrefixMapping(prefix, uri);
        }
    }

    protected static class ExtendedContentFilter extends ContentFilter implements LexicalHandler {

        protected final LexicalHandler lh;

        public ExtendedContentFilter(final ContentHandler ch) {
            super(ch);
            this.lh = (LexicalHandler)ch;
        }

        public void comment(char[] arg0, int arg1, int arg2)
                throws SAXException {
            lh.comment(arg0, arg1, arg2);
        }

        public void endCDATA() throws SAXException {
            lh.endCDATA();
        }

        public void endDTD() throws SAXException {
            lh.endDTD();
        }

        public void endEntity(String arg0) throws SAXException {
            lh.endEntity(arg0);
        }

        public void startCDATA() throws SAXException {
            lh.startCDATA();
        }

        public void startDTD(String arg0, String arg1, String arg2)
                throws SAXException {
            lh.startDTD(arg0, arg1, arg2);
        }

        public void startEntity(String arg0) throws SAXException {
            lh.startEntity(arg0);
        }
    }
}
