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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.testcase.FortressTestCase;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Testcase for generator components. It tests the generator
 * by comparing the output with asserted documents.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractGeneratorTestCase.java,v 1.9 2004/03/08 14:04:19 cziegeler Exp $
 */
public abstract class AbstractGeneratorTestCase extends FortressTestCase
{
    private HashMap objectmodel = new HashMap();

    /**
     * Create a new generator test case.
     *
     * @param name Name of test case.
     */
    public AbstractGeneratorTestCase(String name) {
        super(name);
    }

    public final Map getObjectModel() {
        return objectmodel;
    }

    /**
     * Generate the generator output.
     *
     * @param type Hint of the generator. 
     * @param source Source for the generator.
     * @param parameters Generator parameters.
     */
    public final Document generate(String type, String source, Parameters parameters) {

        Generator generator = null;
        SourceResolver resolver = null;
        SAXParser parser = null;

        Document document = null;
        try {

            resolver = (SourceResolver) lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if generator name is not null", type);

            generator = (Generator) lookup(Generator.ROLE + "/" + type);
            assertNotNull("Test lookup of generator", generator);

            generator.setup(new SourceResolverAdapter(resolver),
                            objectmodel, source, parameters);

            DOMBuilder builder = new DOMBuilder();
            generator.setConsumer(new WhitespaceFilter(builder));

            generator.generate();

            document = builder.getDocument();

            assertNotNull("Test for generator document", document);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (generator != null) {
                release(generator);
            }
            release(resolver);
            release(parser);
        }

        return document;
    }

    public final Document load(String source) {

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source assertionsource = null;

        Document assertiondocument = null;
        try {
            resolver = (SourceResolver) lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if assertion document is not null",
                          source);
            assertionsource = resolver.resolveURI(source);
            assertNotNull("Test lookup of assertion source",
                          assertionsource);

            DOMBuilder builder = new DOMBuilder();
            assertNotNull("Test if inputstream of the assertion source is not null",
                          assertionsource.getInputStream());

            parser.parse(new InputSource(assertionsource.getInputStream()),
                         new WhitespaceFilter(builder),
                         builder);

            assertiondocument = builder.getDocument();
            assertNotNull("Test if assertion document exists", assertiondocument);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (resolver != null) {
                resolver.release(assertionsource);
            }
            release(resolver);
            release(parser);
        }

        return assertiondocument;
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
     * @param msg The assertion message
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
     * @param msg The assertion message
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
