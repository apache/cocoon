/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes software
    developed  by the  Apache Software Foundation (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself, if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Testcase for generator components. It tests the generator
 * by comparing the output with asserted documents.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractGeneratorTestCase.java,v 1.1 2003/03/09 00:10:40 pier Exp $
 */
public abstract class AbstractGeneratorTestCase extends ExcaliburTestCase
{

    /** If the result document should be equal. */
    public final static int EQUAL = 0;

    /** If the result document should not be equal. */
    public final static int NOTEQUAL = 1;

    /** If the result document should be identical. */
    public final static int IDENTICAL = 2;

    /** If the result document should not be identical. */
    public final static int NOTIDENTICAL = 3;

    private Vector teststeps = new Vector();

    /**
     * Create a new generator test case.
     *
     * @param name Name of test case.
     */
    public AbstractGeneratorTestCase(String name) {
        super(name);

    }

    /**
     * Add a new test step to the test case.
     *
     * @param generator Hint of the generator. 
     * @param objectmodel Object model.
     * @param source Source for the transformer.
     * @param parameters Generator parameters.
     * @param assertion Assertion XML document. 
     * @param assertiontype (EQUAL|NOTEQUAL|IDENTICAL|NOTIDENTICAL)
     */
    public final void addTestStep(String generator, Map objectmodel,
                                  String source, Parameters parameters,
                                  String assertion, int assertiontype) {
        TestStep test = new TestStep();

        test.generator = generator;
        test.objectmodel = objectmodel;
        test.source = source;
        test.parameters = parameters;
        test.assertion = assertion;
        test.assertiontype = assertiontype;

        teststeps.addElement(test);
    }

    /**
     * Test the generators and his output
     */
    public final void testGenerator() {

        ComponentSelector selector = null;
        Generator generator = null;
        SourceResolver resolver = null;
        SAXParser parser = null;
        Source assertionsource = null;

        try {
            selector = (ComponentSelector) this.manager.lookup(Generator.ROLE+
                "Selector");
            assertNotNull("Test lookup of generator selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            TestStep test;
            int count = 0;

            for (Enumeration e = teststeps.elements(); e.hasMoreElements(); ) {
                test = (TestStep) e.nextElement();
                count++;
                getLogger().info(count+".Test step");

                assertNotNull("Test if generator name is not null",
                              test.generator);
                generator = (Generator) selector.select(test.generator);
                assertNotNull("Test lookup of generator", generator);

                DOMBuilder builder = new DOMBuilder();

                if ((test.assertiontype==EQUAL) ||
                    (test.assertiontype==NOTEQUAL)) {
                    generator.setConsumer(new WhitespaceFilter(builder));
                } else {
                    generator.setConsumer(builder);
                }

                generator.setup(new SourceResolverAdapter(resolver, this.manager),
                                test.objectmodel, test.source,
                                test.parameters);
                generator.generate();

                Document document = builder.getDocument();

                assertNotNull("Test for generator document", document);

                assertNotNull("Test if assertion document is not null",
                              test.assertion);
                assertionsource = resolver.resolveURI(test.assertion);
                assertNotNull("Test lookup of assertion source",
                              assertionsource);

                builder = new DOMBuilder();
                assertNotNull("Test if inputstream of the assertion source is not null",
                              assertionsource.getInputStream());
                if ((test.assertiontype==EQUAL) ||
                    (test.assertiontype==NOTEQUAL)) {
                    parser.parse(new InputSource(assertionsource.getInputStream()),
                                 (ContentHandler) new WhitespaceFilter(builder),
                                 (LexicalHandler) builder);
                } else {
                    parser.parse(new InputSource(assertionsource.getInputStream()),
                                 (ContentHandler) builder,
                                 (LexicalHandler) builder);
                }
                Document assertiondocument = builder.getDocument();

                assertNotNull("Test if assertion document exists", resolver);

                assertTrue("Test if assertion type is correct",
                           (test.assertiontype>=EQUAL) &&
                           (test.assertiontype<=NOTIDENTICAL));

                switch (test.assertiontype) {
                    case EQUAL :
                        document.getDocumentElement().normalize();
                        assertiondocument.getDocumentElement().normalize();
                        assertXMLEqual(compareXML(assertiondocument, document),
                                       true,
                                       "Test if the assertion document is equal");
                        break;

                    case NOTEQUAL :
                        document.getDocumentElement().normalize();
                        assertiondocument.getDocumentElement().normalize();
                        assertXMLEqual(compareXML(assertiondocument, document),
                                       false,
                                       "Test if the assertion document is not equal");
                        break;

                    case IDENTICAL :
                        assertXMLIdentical(compareXML(assertiondocument, document),
                                           true,
                                           "Test if the assertion document is identical");
                        break;

                    case NOTIDENTICAL :
                        assertXMLIdentical(compareXML(assertiondocument, document),
                                           false,
                                           "Test if the assertion document is not identical");
                        break;
                }

                selector.release(generator);
                generator = null;

                resolver.release(assertionsource);
                assertionsource = null;
            }

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator:"+ce.toString());
        } catch (SAXException saxe) {
            getLogger().error("Could not execute test", saxe);
            fail("Could not execute test:"+saxe.toString());
        } catch (IOException ioe) {
            getLogger().error("Could not execute test", ioe);
            fail("Could not execute test:"+ioe.toString());
/*        } catch (SourceException se) {
            getLogger().error("Could not retrieve sources", se);
            fail("Could not retrieve sources:"+se.toString());*/
        } catch (ProcessingException pe) {
            getLogger().error("Could not execute test", pe);
            pe.printStackTrace();
            fail("Could not execute test:"+pe.toString());
        } finally {
            if (generator!=null) {
                selector.release(generator);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
            resolver.release(assertionsource);
            this.manager.release((Component) parser);
        }
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
     * Assert that the result of an XML comparison is or is not similar.
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is similar
     */
    public final void assertXMLEqual(Diff diff, boolean assertion) {
        assertEquals(diff.toString(), assertion, diff.similar());
    }

    /**
     * Assert that the result of an XML comparison is or is not similar.
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is similar
     * @param msg additional message to display if assertion fails
     */
    public final void assertXMLEqual(Diff diff, boolean assertion,
                                     String msg) {
        assertEquals(msg+", "+diff.toString(), assertion, diff.similar());
    }

    /**
     * Assert that the result of an XML comparison is or is not identical
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is identical
     */
    public final void assertXMLIdentical(Diff diff, boolean assertion) {
        assertEquals(diff.toString(), assertion, diff.identical());
    }

    /**
     * Assert that the result of an XML comparison is or is not identical
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is identical
     * @param msg additional message to display if assertion fails
     */
    public final void assertXMLIdentical(Diff diff, boolean assertion,
                                         String msg) {
        assertEquals(msg+", "+diff.toString(), assertion, diff.identical());
    }

    /**
     * Inner class for a test step. 
     */
    private class TestStep
    {

        /** Hint of the generator. */
        public String generator = null;

        /** Object model. */
        public Map objectmodel = null;

        /** Source for the transformer. */
        public String source = null;

        /** Generator parameters. */
        public Parameters parameters = null;

        /** Assertion XML document. */
        public String assertion = null;

        /** (EQUAL|NOTEQUAL|IDENTICAL|NOTIDENTICAL) */
        public int assertiontype = EQUAL;
    }
}
