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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Testcase for transformer components. It uses multiple input documents
 * and compares the output with asserted documents.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractTransformerTestCase.java,v 1.3 2003/04/09 11:12:20 stephan Exp $
 */
public abstract class AbstractTransformerTestCase extends ExcaliburTestCase
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
     * Create a new transformer test case.
     *
     * @param name Name of test case.
     */
    public AbstractTransformerTestCase(String name) {
        super(name);
    }

    /**
     * Add a new test step to the test case.
     *
     * @param transformer Hint of the transformer.
     * @param objectmodel Object model.
     * @param source Source for the transformer.
     * @param parameters Transformer parameters.
     * @param input Input XML document, which go throught
     *              the transformer.
     * @param assertion Assertion XML document.
     * @param assertiontype (EQUAL|NOTEQUAL|IDENTICAL|NOTIDENTICAL)
     */
    public final void addTestStep(String transformer, Map objectmodel,
                                  String source, Parameters parameters,
                                  String input, String assertion,
                                  int assertiontype) {
        TestStep test = new TestStep();

        test.transformer = transformer;
        test.objectmodel = objectmodel;
        test.source = source;
        test.parameters = parameters;
        test.input = input;
        test.assertion = assertion;
        test.assertiontype = assertiontype;

        teststeps.addElement(test);
    }

    /**
     * Test the transformers and his output
     */
    public final void testTransformer() {

        ComponentSelector selector = null;
        Transformer transformer = null;
        SourceResolver resolver = null;
        SAXParser parser = null;
        Source inputsource = null;
        Source assertionsource = null;

        try {
            selector = (ComponentSelector) this.manager.lookup(Transformer.ROLE+
                "Selector");
            assertNotNull("Test lookup of transformer selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            TestStep test;
            int count = 0;

            getLogger().debug("Count of test steps: "+teststeps.size());

            for (Enumeration e = teststeps.elements(); e.hasMoreElements(); ) {
                test = (TestStep) e.nextElement();
                count++;
                getLogger().info(count+".Test step");

                assertNotNull("Test if transformer name is not null",
                              test.transformer);
                transformer = (Transformer) selector.select(test.transformer);
                assertNotNull("Test lookup of transformer", transformer);

                DOMBuilder builder = new DOMBuilder();

                if ((test.assertiontype==EQUAL) ||
                    (test.assertiontype==NOTEQUAL)) {
                    transformer.setConsumer(new WhitespaceFilter(builder));
                } else {
                    transformer.setConsumer(builder);
                }

                transformer.setup(new SourceResolverAdapter(resolver, this.manager),
                                  test.objectmodel, test.source,
                                  test.parameters);

                assertNotNull("Test if input document is not null",
                              test.input);
                inputsource = resolver.resolveURI(test.input);
                assertNotNull("Test lookup of input source", inputsource);

                assertNotNull("Test if inputstream of the input source is not null",
                              inputsource.getInputStream());
                parser.parse(new InputSource(inputsource.getInputStream()),
                             (ContentHandler) transformer,
                             (LexicalHandler) transformer);

                Document document = builder.getDocument();

                assertNotNull("Test for transformer document", document);

                assertNotNull("Test if assertion document is not null",
                              test.assertion);
                assertionsource = resolver.resolveURI(test.assertion);
                assertNotNull("Test lookup of assertion source",
                              assertionsource);

                assertNotNull("Test if inputstream of the assertion source is not null",
                              assertionsource.getInputStream());
                builder = new DOMBuilder();
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

                selector.release(transformer);
                transformer = null;

                resolver.release(inputsource);
                inputsource = null;

                resolver.release(assertionsource);
                assertionsource = null;
            }

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve transformer", ce);
            ce.printStackTrace();
            fail("Could not retrieve transformer:"+ce.toString());
        } catch (SAXException saxe) {
            getLogger().error("Could not execute test", saxe);
            fail("Could not execute test:"+saxe.toString());
        } catch (IOException ioe) {
            getLogger().error("Could not execute test", ioe);
            fail("Could not execute test:"+ioe.toString());
        } catch (ProcessingException pe) {
            getLogger().error("Could not execute test", pe);
            pe.printStackTrace();
            fail("Could not execute test:"+pe.toString());
        } finally {
            if (transformer!=null)
                selector.release(transformer);

            if (selector!=null)
                this.manager.release(selector);

            if (resolver!=null)
                this.manager.release(resolver);

            if (inputsource!=null)
                resolver.release(inputsource);

            if (inputsource!=null)
                resolver.release(assertionsource);

            if (resolver!=null)
                this.manager.release(resolver);

            if (parser!=null)
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

        /*TransformerFactory factory = (TransformerFactory) TransformerFactory.newInstance();
        try
        {
          javax.xml.transform.Transformer serializer = factory.newTransformer();
          System.out.println("Control document:");
          serializer.transform(new DOMSource(control), new StreamResult(System.out));
          System.out.println();

          serializer = factory.newTransformer();
          System.out.println("Test document:");
          serializer.transform(new DOMSource(test), new StreamResult(System.out));
          System.out.println();
        } 
        catch (TransformerException te)
        {
          te.printStackTrace();
        }*/

        return new Diff(control, test);
    }

    /**
     * Assert that the result of an XML comparison is or is not similar.
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is similar
     */
    public final void assertXMLEqual(Diff diff, boolean assertion) {
        XMLUnit.setIgnoreWhitespace(true);
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
        XMLUnit.setIgnoreWhitespace(true);
        assertEquals(msg+", "+diff.toString(), assertion, diff.similar());
    }

    /**
     * Assert that the result of an XML comparison is or is not identical
     * @param diff the result of an XML comparison
     * @param assertion true if asserting that result is identical
     */
    public final void assertXMLIdentical(Diff diff, boolean assertion) {
        XMLUnit.setIgnoreWhitespace(false);
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
        XMLUnit.setIgnoreWhitespace(false);
        assertEquals(msg+", "+diff.toString(), assertion, diff.identical());
    }

    /**
     * Inner class for a test step.
     */
    private class TestStep
    {

        /** Hint of the transformer. */
        public String transformer = null;

        /** Object model. */
        public Map objectmodel = null;

        /** Source for the transformer. */
        public String source = null;

        /** Transformer parameters. */
        public Parameters parameters = null;

        /** Input XML document, which go throught
         *  the transformer. */
        public String input = null;

        /** Assertion XML document. */
        public String assertion = null;

        /** (EQUAL|NOTEQUAL|IDENTICAL|NOTIDENTICAL) */
        public int assertiontype = EQUAL;
    }
}
