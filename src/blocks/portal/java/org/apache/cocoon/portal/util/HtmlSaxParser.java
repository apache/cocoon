/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This parser uses the nekohtml parser to parse html and generate sax streams.
 *
 * @version $Id:$
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
}
