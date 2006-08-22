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

import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.excalibur.xml.sax.XMLConsumer;
import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
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

    public static XMLConsumer getContentFilter(ContentHandler ch) {
        return new ContentFilter(ch);
    }

    protected static final class ContentFilter extends ContentHandlerWrapper {

        public ContentFilter(ContentHandler ch) {
            this.setContentHandler(ch);
            if ( ch instanceof LexicalHandler ) {
                this.setLexicalHandler((LexicalHandler)ch);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String loc, String raw) throws SAXException {
            if ( !loc.equals("html") && !loc.equals("body") ) {
                super.endElement(uri, loc, raw);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
            if ( !loc.equals("html") && !loc.equals("body") ) {
                super.startElement(uri, loc, raw, a);
            }
        }
    }

}
