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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.parameters.Parameters;
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
 * @version CVS $Id: AbstractGeneratorTestCase.java,v 1.7 2003/08/04 03:06:30 joerg Exp $
 */
public abstract class AbstractGeneratorTestCase extends ExcaliburTestCase
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

        ComponentSelector selector = null;
        Generator generator = null;
        SourceResolver resolver = null;
        SAXParser parser = null;

        Document document = null;
        try {
            selector = (ComponentSelector) this.manager.lookup(Generator.ROLE +
                "Selector");
            assertNotNull("Test lookup of generator selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if generator name is not null", type);

            generator = (Generator) selector.select(type);
            assertNotNull("Test lookup of generator", generator);

            generator.setup(new SourceResolverAdapter(resolver, this.manager),
                            objectmodel, source, parameters);

            DOMBuilder builder = new DOMBuilder();
            generator.setConsumer(new WhitespaceFilter(builder));

            generator.generate();

            document = builder.getDocument();

            assertNotNull("Test for generator document", document);

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (generator != null) {
                selector.release(generator);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
            this.manager.release((Component) parser);
        }

        return document;
    }

    public final Document load(String source) {

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source assertionsource = null;

        Document assertiondocument = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
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

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (resolver != null) {
                resolver.release(assertionsource);
            }
            this.manager.release(resolver);
            this.manager.release((Component) parser);
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
