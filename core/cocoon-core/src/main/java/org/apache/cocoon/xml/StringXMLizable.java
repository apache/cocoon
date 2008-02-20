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
 */
public class StringXMLizable implements XMLizable {
    private static class Context {
        SAXParser parser;
        Context() throws SAXException {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parser = null;
            try {
                parser = parserFactory.newSAXParser();
            } catch (ParserConfigurationException e) {
                throw new SAXException("Error creating SAX parser.", e);
            }
        }
    }

    private static final ThreadLocal context = new ThreadLocal();
    private String data;

    public StringXMLizable(final String data) {
        this.data = data;
    }

    private Context getContext() throws SAXException {
        if (context.get() == null) {
            context.set(new Context());
        }
        return (Context) context.get();
    }

    public void toSAX(ContentHandler contentHandler) throws SAXException {
        final SAXParser parser = getContext().parser;
        parser.getXMLReader().setContentHandler(contentHandler);
        InputSource is = new InputSource(new StringReader(data));
        try {
            parser.getXMLReader().parse(is);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
}
