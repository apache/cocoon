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
package org.apache.cocoon.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.testcase.FortressTestCase;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Testcase for serializer components. It uses multiple input documents
 * and compares the output with asserted documents.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @version CVS $Id: AbstractSerializerTestCase.java,v 1.3 2004/03/08 14:04:20 cziegeler Exp $
 */
public abstract class AbstractSerializerTestCase extends FortressTestCase {
    private HashMap objectmodel = new HashMap();

    /**
     * Create a new serializer test case.
     *
     * @param name Name of test case.
     */
    public AbstractSerializerTestCase(String name) {
        super(name);
    }

    /**
     * Returns the object model.
     *
     * @return Object model.
     */
    public final Map getObjectModel() {
        return objectmodel;
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

        ServiceSelector selector = null;
        Serializer serializer = null;
        SourceResolver resolver = null;
        Source inputsource = null;

        ByteArrayOutputStream document = null;

        try {
            selector = (ServiceSelector) lookup(Serializer.ROLE+
                "Selector");
            assertNotNull("Test lookup of serializer selector", selector);

            resolver = (SourceResolver) lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if serializer name is not null", type);
            serializer = (Serializer) selector.select(type);
            assertNotNull("Test lookup of serializer", serializer);

            document = new ByteArrayOutputStream();
            serializer.setOutputStream(document);

            assertNotNull("Test if input document is not null", input);
            DOMStreamer streamer = new DOMStreamer(serializer);

            streamer.stream(input);
        } catch (ServiceException ce) {
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
                release(selector);
            }

            if (inputsource!=null) {
                resolver.release(inputsource);
            }

            if (resolver!=null) {
                release(resolver);
            }
        }

        return document.toByteArray();
    }

    /**
     * Load a XML document.
     *
     * @param source Source location.
     *
     * @return XML document.
     */
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

            assertNotNull("Test if assertion document is not null", source);
            assertionsource = resolver.resolveURI(source);
            assertNotNull("Test lookup of assertion source", assertionsource);

            DOMBuilder builder = new DOMBuilder();

            assertNotNull("Test if inputstream of the assertion source is not null",
                          assertionsource.getInputStream());

            parser.parse(new InputSource(assertionsource.getInputStream()),
                         new WhitespaceFilter(builder), builder);

            assertiondocument = builder.getDocument();
            assertNotNull("Test if assertion document exists",
                          assertiondocument);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: "+ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: "+e);
        } finally {
            if (resolver!=null) {
                resolver.release(assertionsource);
            }
            release(resolver);
            release(parser);
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

        byte[] assertiondocument = null;

        try {
            resolver = (SourceResolver) lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) lookup(SAXParser.ROLE);
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

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: "+ce.toString());
        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: "+e);
        } finally {
            if (resolver!=null) {
                resolver.release(assertionsource);
            }
            release(resolver);
            release(parser);
        }

        return assertiondocument;
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
