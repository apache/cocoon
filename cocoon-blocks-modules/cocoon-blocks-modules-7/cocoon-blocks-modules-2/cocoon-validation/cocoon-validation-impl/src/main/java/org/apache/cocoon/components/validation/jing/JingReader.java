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
package org.apache.cocoon.components.validation.jing;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.thaiopensource.xml.sax.DraconianErrorHandler;

/**
 * <p>A trivial {@link XMLReader} implementation populating and clearing the
 * {@link Stack} of {@link InputSource}s for URI resolution kept inside
 * a {@link JingResolver}.</p>
 */
final class JingReader implements XMLReader {
    
    /** <p>The underlying {@link XMLReader} to use.</p> */
    private final XMLReader reader;
    /** <p>The {@link JingResolver} associated with this instance.</p> */
    private final JingResolver context;

    /**
     * <p>Create a new {@link JingReader} instance associated with the specified
     * {@link JingResolver}.</p>
     */
    protected JingReader(JingResolver context)
    throws SAXException {
        /*
         * We have to look up the XMLReader using JAXP or SAX, as the SAXParser
         * supplied by Avalon/Excalibur does not seem to work with JING.
         */
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            this.reader = factory.newSAXParser().getXMLReader();
            this.setEntityResolver(context);
            this.setErrorHandler(new DraconianErrorHandler());
            this.context = context;
        } catch (ParserConfigurationException exception) {
            throw new SAXException("Can't create XML reader instance", exception);
        }
    }

    public boolean getFeature(String feature)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.reader.getFeature(feature);
    }

    public void setFeature(String feature, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        this.reader.setFeature(feature, value);
    }

    public Object getProperty(String property)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.reader.getProperty(property);
    }

    public void setProperty(String property, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        this.reader.setProperty(property, value);
    }

    public void setEntityResolver(org.xml.sax.EntityResolver resolver) {
        this.reader.setEntityResolver(resolver);
    }

    public org.xml.sax.EntityResolver getEntityResolver() {
        return this.reader.getEntityResolver();
    }

    public void setDTDHandler(DTDHandler handler) {
        this.reader.setDTDHandler(handler);
    }

    public DTDHandler getDTDHandler() {
        return this.reader.getDTDHandler();
    }

    public void setContentHandler(ContentHandler handler) {
        this.reader.setContentHandler(handler);
    }

    public ContentHandler getContentHandler() {
        return this.reader.getContentHandler();
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.reader.setErrorHandler(handler);
    }

    public ErrorHandler getErrorHandler() {
        return this.reader.getErrorHandler();
    }

    public void parse(InputSource source)
    throws IOException, SAXException {
        this.context.pushInputSource(source);
        this.reader.parse(source);
        this.context.popInputSource();
    }

    public void parse(String source)
    throws IOException, SAXException {
        this.parse(this.getEntityResolver().resolveEntity(null, source));
    }
}