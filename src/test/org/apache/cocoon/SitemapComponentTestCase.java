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

package org.apache.cocoon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockRedirector;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Testcase for actions, generators, transformers and serializer components. 
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @version CVS $Id: SitemapComponentTestCase.java,v 1.3 2004/02/02 11:25:57 stephan Exp $
 */
public abstract class SitemapComponentTestCase extends ExcaliburTestCase
{
    public final static Parameters EMPTY_PARAMS = Parameters.EMPTY_PARAMETERS;

    private MockRequest request = new MockRequest();
    private MockResponse response = new MockResponse();
    private MockContext context = new MockContext();
    private MockRedirector redirector = new MockRedirector();
    private HashMap objectmodel = new HashMap();

    /**
     * Create a new composite test case.
     *
     * @param name Name of test case.
     */
    public SitemapComponentTestCase(String name) {
        super(name);
    }

    public final MockRequest getRequest() {
        return request;
    }

    public final MockResponse getResponse() {
        return response;
    }

    public final MockContext getContext() {
        return context;
    }

    public final MockRedirector getRedirector() { 
        return redirector;
    }

    public final Map getObjectModel() {
        return objectmodel;
    }

    public void setUp() {
        objectmodel.clear();

        request.reset();
        objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, request);

        response.reset();
        objectmodel.put(ObjectModelHelper.RESPONSE_OBJECT, response);

        context.reset();
        objectmodel.put(ObjectModelHelper.CONTEXT_OBJECT, context);

        redirector.reset();
    }

    /**
     * Match with a pattern.
     *
     * @param type Hint of the matcher. 
     * @param pattern Pattern for the matcher.
     * @param parameters Matcher parameters.
     */
    public final Map match(String type, String pattern, Parameters parameters) {

        ComponentSelector selector = null;
        Matcher matcher = null;
        SourceResolver resolver = null;

        Map result = null;
        try {
            selector = (ComponentSelector) this.manager.lookup(Matcher.ROLE +
                "Selector");
            assertNotNull("Test lookup of matcher selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if matcher name is not null", type);
            matcher = (Matcher) selector.select(type);
            assertNotNull("Test lookup of matcher", matcher);

            result = matcher.match(pattern, objectmodel, parameters);

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (matcher != null) {
                selector.release(matcher);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
        }
        return result;
    }
    
    /**
     * Perform the action component.
     *
     * @param type Hint of the action. 
     * @param source Source for the action.
     * @param parameters Action parameters.
     */
    public final Map act(String type, String source, Parameters parameters) {

        ComponentSelector selector = null;
        Action action = null;
        SourceResolver resolver = null;

        Map result = null;
        try {
            selector = (ComponentSelector) this.manager.lookup(Action.ROLE +
                "Selector");
            assertNotNull("Test lookup of action selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if action name is not null", type);
            action = (Action) selector.select(type);
            assertNotNull("Test lookup of action", action);

            result = action.act(redirector, new SourceResolverAdapter(resolver, this.manager),
                                objectmodel, source, parameters);

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: " + e);
        } finally {
            if (action != null) {
                selector.release(action);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
        }
        return result;
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

    /**     
     * Trannsform a document by a transformer
     *      
     * @param type Hint of the transformer. 
     * @param source Source for the transformer.
     * @param parameters Generator parameters.
     * @param input Input document.
     */ 
    public final Document transform(String type, String source, Parameters parameters, Document input) {

        ComponentSelector selector = null;
        Transformer transformer = null;
        SourceResolver resolver = null;
        SAXParser parser = null;
        Source inputsource = null;

        assertNotNull("Test for component manager", this.manager);

        Document document = null;
        try {
            selector = (ComponentSelector) this.manager.lookup(Transformer.ROLE+
                "Selector");
            assertNotNull("Test lookup of transformer selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);


            assertNotNull("Test if transformer name is not null", type);
            transformer = (Transformer) selector.select(type);
            assertNotNull("Test lookup of transformer", transformer);

            transformer.setup(new SourceResolverAdapter(resolver, this.manager),
                                  objectmodel, source, parameters);

            DOMBuilder builder = new DOMBuilder();
            transformer.setConsumer(new WhitespaceFilter(builder));

            assertNotNull("Test if input document is not null", input);
            DOMStreamer streamer = new DOMStreamer(transformer);
            streamer.stream(input);

            document = builder.getDocument();
            assertNotNull("Test for transformer document", document);

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

            if (inputsource!=null)
                resolver.release(inputsource);

            if (resolver!=null)
                this.manager.release(resolver);

            if (parser!=null)
                this.manager.release((Component) parser);
        }

        return document; 
    }

    /**
     * Serialize a document by a serializer
     *
     * @param type Hint of the serializer.
     * @param parameters Serializer parameters.
     * @param input Input document.
     *
     * @return Serialized data.
     */
    public final byte[] serialize(String type, Parameters parameters,
                                  Document input) {

        ComponentSelector selector = null;
        Serializer serializer = null;
        SourceResolver resolver = null;
        Source inputsource = null;

        assertNotNull("Test for component manager", this.manager);

        ByteArrayOutputStream document = null;

        try {
            selector = (ComponentSelector) this.manager.lookup(Serializer.ROLE+
                "Selector");
            assertNotNull("Test lookup of serializer selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if serializer name is not null", type);
            serializer = (Serializer) selector.select(type);
            assertNotNull("Test lookup of serializer", serializer);

            document = new ByteArrayOutputStream();
            serializer.setOutputStream(document);

            assertNotNull("Test if input document is not null", input);
            DOMStreamer streamer = new DOMStreamer(serializer);

            streamer.stream(input);
        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve serializer", ce);
            ce.printStackTrace();
            fail("Could not retrieve serializer:"+ce.toString());
        } catch (SAXException saxe) {
            getLogger().error("Could not execute test", saxe);
            fail("Could not execute test:"+saxe.toString());
        } catch (IOException ioe) {
            getLogger().error("Could not execute test", ioe);
            fail("Could not execute test:"+ioe.toString());
        } finally {
            if (serializer!=null) {
                selector.release(serializer);
            }

            if (selector!=null) {
                this.manager.release(selector);
            }

            if (inputsource!=null) {
                resolver.release(inputsource);
            }

            if (resolver!=null) {
                this.manager.release(resolver);
            }
        }

        return document.toByteArray();
    }

    public final void print(Document document) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try
        {
          javax.xml.transform.Transformer serializer = factory.newTransformer();
          serializer.transform(new DOMSource(document), new StreamResult(System.out));
          System.out.println();
        } 
        catch (TransformerException te)
        {
          te.printStackTrace();
        }
    }

    public final Document load(String source) {

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source assertionsource = null;

        assertNotNull("Test for component manager", this.manager);

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
            assertTrue("Test if source exist", assertionsource.exists());

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
     * Load a binary document.
     *
     * @param source Source location.
     *
     * @return Binary data.
     */
    public final byte[] loadByteArray(String source) {

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source assertionsource = null;

        assertNotNull("Test for component manager", this.manager);

        byte[] assertiondocument = null;

        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if assertion document is not null", source);
            assertionsource = resolver.resolveURI(source);
            assertNotNull("Test lookup of assertion source", assertionsource);
            assertTrue("Test if source exist", assertionsource.exists());

            assertNotNull("Test if inputstream of the assertion source is not null",
                          assertionsource.getInputStream());

            InputStream input = assertionsource.getInputStream();
            long size = assertionsource.getContentLength();

            assertiondocument = new byte[(int) size];
            int i = 0;
            int c;

            while ((c = input.read())!=-1) {
                assertiondocument[i] = (byte) c;
                i++;
            }

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: "+ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: "+e);
        } finally {
            if (resolver!=null) {
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

    /**
     * Assert that the result of a byte comparison is identical.
     *
     * @param expected The expected byte array
     * @param actual The actual byte array
     */
    public final void assertIdentical(byte[] expected, byte[] actual) {
        assertEquals("Byte arrays of differing sizes, ", expected.length,
                     actual.length);

        if (expected.length>0) {
            for (int i = 0; i<expected.length; i++) {
                assertEquals("Byte array differs at index "+i, expected[i],
                             actual[i]);
            }
        }

    }
}
