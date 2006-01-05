/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.MockLogger;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;
import org.apache.excalibur.xml.sax.SAXParser;
import org.custommonkey.xmlunit.Diff;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @version $Id$
 */
public class FileGeneratorTestCase extends MockObjectTestCase {
    private Map objectModel = new HashMap();
    private SAXParser parser;
    private Mock manager = new Mock(ServiceManager.class);
    
    public void setUp() throws SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser = new SAXParser() {

            public void parse(InputSource src, 
                              ContentHandler contentHandler) throws SAXException, IOException {
                xmlReader.setContentHandler(contentHandler);
                xmlReader.parse(src);
            }

            public void parse(InputSource src, 
                              ContentHandler contentHandler, 
                              LexicalHandler lexicalHandler) throws SAXException, IOException {
                parse(src, contentHandler);
            }
            
        };
    }
    
    public void testFileGenerator() throws Exception {
        String src = "resource://org/apache/cocoon/generation/FileGeneratorTestCase.source.xml";
        Parameters parameters = new Parameters();
        String result = "resource://org/apache/cocoon/generation/FileGeneratorTestCase.source.xml";
        FileGenerator generator = new FileGenerator();
        generator.enableLogging(new MockLogger(generator.getClass()));
        manager.expects(atLeastOnce()).method("lookup").with(same(SAXParser.ROLE)).
                will(returnValue(parser));
        manager.expects(once()).method("release").with(same(parser));
        Mock resolver = new Mock(SourceResolver.class);
        Source source = new ResourceSource(src);
        resolver.expects(once()).method("resolveURI").with(same(src)).
                will(returnValue(source));
        resolver.expects(once()).method("release").with(same(source));
        generator.service((ServiceManager) manager.proxy());
        generator.setup((SourceResolver) resolver.proxy(), objectModel, src, parameters);
        DOMBuilder builder = new DOMBuilder();
        generator.setConsumer(new WhitespaceFilter(builder));
        generator.generate();
        assertEqual(load(result), builder.getDocument());
    }

    protected Document load(String src) throws ProcessingException, SAXException, IOException {
        Source source = new ResourceSource(src);
        manager.expects(atLeastOnce()).method("lookup").with(same(SAXParser.ROLE)).
        will(returnValue(parser));
        manager.expects(once()).method("release").with(same(parser));
        DOMBuilder builder = new DOMBuilder();
        SourceUtil.parse((ServiceManager) manager.proxy(), source, new WhitespaceFilter(builder));
        return builder.getDocument();
    }
    
    /**
     * Compare two XML documents provided as strings
     * @param control Control document
     * @param test Document to test
     * @return Diff object describing differences in documents
     */
    public final Diff compareXML(Document control, Document test) {
        return new Diff(control, test);
    }

    /**
     * Assert that the result of an XML comparison is similar.
     *
     * @param msg The assertion message
     * @param expected The expected XML document
     * @param actual The actual XML Document
     */
    public final void assertEqual(String msg, Document expected, Document actual) {

        expected.getDocumentElement().normalize();
        actual.getDocumentElement().normalize();

        Diff diff = compareXML(expected, actual);

        assertEquals(msg + ", " + diff.toString(), true, diff.similar());
    }

    /**
     * Assert that the result of an XML comparison is similar.
     *
     * @param expected The expected XML document
     * @param actual The actual XML Document
     */  
    public final void assertEqual(Document expected, Document actual) {

        expected.getDocumentElement().normalize();
        actual.getDocumentElement().normalize();

        Diff diff = compareXML(expected, actual);

        assertEquals("Test if the assertion document is equal, " + diff.toString(), true, diff.similar());
    }

    /**
     * Assert that the result of an XML comparison is identical.
     *
     * @param msg The assertion message
     * @param expected The expected XML document
     * @param actual The actual XML Document
     */
    public final void assertIdentical(String msg, Document expected, Document actual) {

        expected.getDocumentElement().normalize();
        actual.getDocumentElement().normalize();

        Diff diff = compareXML(expected, actual);

        assertEquals(msg + ", " + diff.toString(), true, diff.identical());
    }

    /**
     * Assert that the result of an XML comparison is identical.
     *
     * @param expected The expected XML document
     * @param actual The actual XML Document
     */
    public final void assertIdentical(Document expected, Document actual) {

        expected.getDocumentElement().normalize();
        actual.getDocumentElement().normalize();

        Diff diff = compareXML(expected, actual);

        assertEquals("Test if the assertion document is equal, " + diff.toString(), true, diff.identical());
    }
    
}
