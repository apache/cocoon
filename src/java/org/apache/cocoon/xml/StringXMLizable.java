/*
 * Copyright 2004 The Apache Software Foundation.
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

import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * XMLizable a String
 * 
 * @since 2.1.7
 * @author Bruno Dumon
 */
public class StringXMLizable implements XMLizable {
    private String data;

    public StringXMLizable(String data) {
        this.data = data;
    }

    public void toSAX(ContentHandler contentHandler) throws SAXException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser parser = null;
        try {
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new SAXException("Error creating SAX parser.", e);
        }
        parser.getXMLReader().setContentHandler(contentHandler);
        InputSource is = new InputSource(new StringReader(data));
        try {
            parser.getXMLReader().parse(is);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
}
