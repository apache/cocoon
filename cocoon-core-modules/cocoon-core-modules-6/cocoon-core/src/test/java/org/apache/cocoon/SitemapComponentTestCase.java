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
package org.apache.cocoon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockEnvironment;
import org.apache.cocoon.environment.mock.MockRedirector;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.transformation.Transformer;
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
 * @version $Id$
 */
public abstract class SitemapComponentTestCase extends CocoonTestCase {

    public final static Parameters EMPTY_PARAMS = Parameters.EMPTY_PARAMETERS;

    private MockRedirector redirector = new MockRedirector();

    public final MockRedirector getRedirector() { 
        return redirector;
    }

    protected void addContext(DefaultContext context) {
        context.put(ContextHelper.CONTEXT_REQUEST_OBJECT, this.getRequest());
        context.put(ContextHelper.CONTEXT_RESPONSE_OBJECT, this.getResponse());
        context.put(ContextHelper.CONTEXT_OBJECT_MODEL, this.getObjectModel());
        context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, this.getContext());
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
        this.redirector.reset();
    }

    /**
     * @see org.apache.cocoon.CocoonTestCase#addComponents(org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo)
     */
    protected void addComponents(ConfigurationInfo info) 
    throws Exception {
        super.addComponents(info);
        final String[] o = this.getSitemapComponentInfo();
        if ( o != null ) {
            final String typeClassName = o[0];
            final String componentClassName = o[1];
            final String key = o[2];

            // Add component
            ComponentInfo component = new ComponentInfo();
            component.setComponentClassName(componentClassName);
            component.setConfiguration(new DefaultConfiguration("-"));
            component.setRole(typeClassName + "/" + key);
            info.addComponent(component);

            // add selector
            component = new ComponentInfo();
            component.setModel(ComponentInfo.MODEL_SINGLETON);
            component.setComponentClassName("org.apache.cocoon.core.container.DefaultServiceSelector");
            component.setRole(typeClassName + "Selector");
            component.setConfiguration(new DefaultConfiguration("-"));
            component.setDefaultValue(key);
            info.addComponent(component);
        }
    }

    /**
     * This triple can be used to add a sitemap component to the service manager
     * @return A triple of strings: class name of the type, class name of the component, key
     */
    protected String[] getSitemapComponentInfo() {
        return null;
    }

    /**
     * Match with a pattern.
     *
     * @param type Hint of the matcher. 
     * @param pattern Pattern for the matcher.
     * @param parameters Matcher parameters.
     */
    public final Map match(String type, String pattern, Parameters parameters) throws PatternException {

        ServiceSelector selector = null;
        Matcher matcher = null;
        SourceResolver resolver = null;

        Map result = null;
        try {
            selector = (ServiceSelector) this.lookup(Matcher.ROLE +
                "Selector");
            assertNotNull("Test lookup of matcher selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if matcher name is not null", type);
            matcher = (Matcher) selector.select(type);
            assertNotNull("Test lookup of matcher", matcher);

            result = matcher.match(pattern, this.getObjectModel(), parameters);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve matcher", ce);
            fail("Could not retrieve matcher: " + ce.toString());
        } finally {
            if (matcher != null) {
                selector.release(matcher);
            }
            this.release(selector);
            this.release(resolver);
        }
        return result;
    }

    /**
     * Select with a pattern.
     *
     * @param type Hint of the matcher. 
     * @param expression Expression for the selector.
     * @param parameters Matcher parameters.
     */
    public final boolean select(String type, String expression, Parameters parameters) {

        ServiceSelector selector = null;
        org.apache.cocoon.selection.Selector sel = null;
        SourceResolver resolver = null;

        boolean result = false;
        try {
            selector = (ServiceSelector) this.lookup(org.apache.cocoon.selection.Selector.ROLE +
                "Selector");
            assertNotNull("Test lookup of selector selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if selector name is not null", type);
            sel = (org.apache.cocoon.selection.Selector) selector.select(type);
            assertNotNull("Test lookup of selector", sel);
            

            result = sel.select(expression, this.getObjectModel(), parameters);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve selector", ce);
            fail("Could not retrieve selector: " + ce.toString());
        } finally {
            if (sel != null) {
                selector.release(sel);
            }
            this.release(selector);
            this.release(resolver);
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
    public final Map act(String type, String source, Parameters parameters) throws Exception {
        
        redirector.reset();

        ServiceSelector selector = null;
        Action action = null;
        SourceResolver resolver = null;

        Map result = null;
        try {
            selector = (ServiceSelector) this.lookup(Action.ROLE +
                "Selector");
            assertNotNull("Test lookup of action selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if action name is not null", type);
            action = (Action) selector.select(type);
            assertNotNull("Test lookup of action", action);

            result = action.act(redirector, new SourceResolverAdapter(resolver, this.getManager()),
                    this.getObjectModel(), source, parameters);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve action", ce);
            fail("Could not retrieve action: " + ce.toString());
        } finally {
            if (action != null) {
                selector.release(action);
            }
            this.release(selector);
            this.release(resolver);
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
    public final Document generate(String type, String source, Parameters parameters) 
        throws IOException, SAXException, ProcessingException {

        ServiceSelector selector = null;
        Generator generator = null;
        SourceResolver resolver = null;
        SAXParser parser = null;

        Document document = null;
        try {
            selector = (ServiceSelector) this.lookup(Generator.ROLE +
                "Selector");
            assertNotNull("Test lookup of generator selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if generator name is not null", type);

            generator = (Generator) selector.select(type);
            assertNotNull("Test lookup of generator", generator);

            generator.setup(new SourceResolverAdapter(resolver, getManager()),
                    this.getObjectModel(), source, parameters);

            DOMBuilder builder = new DOMBuilder();
            generator.setConsumer(new WhitespaceFilter(builder));

            generator.generate();

            document = builder.getDocument();

            assertNotNull("Test for generator document", document);

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve generator", ce);
            fail("Could not retrieve generator: " + ce.toString());
        } finally {
            if (generator != null) {
                selector.release(generator);
            }
            this.release(selector);
            this.release(resolver);
            this.release(parser);
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
    public final Document transform(String type, String source, Parameters parameters, Document input) 
    throws Exception {
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new MockEnvironment();
        env.setObjectModel(this.getObjectModel());
        Processor processor = new MockProcessor(this.getBeanFactory());
        
        EnvironmentHelper.enterProcessor(processor, env);

        try {
            ServiceSelector selector = null;
            Transformer transformer = null;
            SourceResolver resolver = null;
            SAXParser parser = null;
            Source inputsource = null;
    
            assertNotNull("Test for component manager", this.getManager());
    
            Document document = null;
            try {
                selector = (ServiceSelector) this.lookup(Transformer.ROLE+
                    "Selector");
                assertNotNull("Test lookup of transformer selector", selector);
    
                resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
                assertNotNull("Test lookup of source resolver", resolver);
    
                parser = (SAXParser) this.lookup(SAXParser.ROLE);
                assertNotNull("Test lookup of parser", parser);
    
    
                assertNotNull("Test if transformer name is not null", type);
                transformer = (Transformer) selector.select(type);
                assertNotNull("Test lookup of transformer", transformer);
    
                transformer.setup(new SourceResolverAdapter(resolver, getManager()),
                        this.getObjectModel(), source, parameters);
    
                DOMBuilder builder = new DOMBuilder();
                transformer.setConsumer(new WhitespaceFilter(builder));
    
                assertNotNull("Test if input document is not null", input);
                DOMStreamer streamer = new DOMStreamer(transformer);
                streamer.stream(input);
    
                document = builder.getDocument();
                assertNotNull("Test for transformer document", document);
    
            } catch (ServiceException ce) {
                getLogger().error("Could not retrieve transformer", ce);
                ce.printStackTrace();
                fail("Could not retrieve transformer:"+ce.toString());
            } finally {
                if (transformer!=null) {
                    selector.release(transformer);
                }
    
                if (selector!=null) {
                    this.release(selector);
                }
    
                if (inputsource!=null) {
                    resolver.release(inputsource);
                }
    
                if (resolver!=null) {
                    this.release(resolver);
                }
    
                if (parser!=null) {
                    this.release(parser);
                }
            }
    
            return document;
        } finally {
            EnvironmentHelper.leaveProcessor();           
        }
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
                                  Document input) throws SAXException, IOException{

        ServiceSelector selector = null;
        Serializer serializer = null;
        SourceResolver resolver = null;
        Source inputsource = null;

        assertNotNull("Test for component manager", this.getManager());

        ByteArrayOutputStream document = null;

        try {
            selector = (ServiceSelector) this.lookup(Serializer.ROLE+
                "Selector");
            assertNotNull("Test lookup of serializer selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
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
            fail("Could not retrieve serializer:"+ce.toString());
        } finally {
            if (serializer!=null) {
                selector.release(serializer);
            }

            if (selector!=null) {
                this.release(selector);
            }

            if (inputsource!=null) {
                resolver.release(inputsource);
            }

            if (resolver!=null) {
                this.release(resolver);
            }
        }

        return document.toByteArray();
    }
    
    /**
     * Read a document (binary) by using a reader.
     *
     * @param type Hint of the reader.
     * @param parameters Reader parameters.
     * @param source URI pointing to the document.
     *
     * @return Red data.
     */
    public final byte[] read(String type, Parameters parameters, String source) throws SAXException, IOException, ProcessingException {
        ServiceSelector selector = null;
        Reader reader = null;
        SourceResolver resolver = null;
        Source inputsource = null;

        assertNotNull("Test for component manager", this.getManager());

        ByteArrayOutputStream document = null;

        try {
            selector = (ServiceSelector) this.lookup(Reader.ROLE+
                "Selector");
            assertNotNull("Test lookup of serializer selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if reader name is not null", type);
            reader = (Reader) selector.select(type);
            assertNotNull("Test lookup of reader", reader);
            
            reader.setup(new SourceResolverAdapter(resolver, getManager()),
                    this.getObjectModel(), source, parameters);

            document = new ByteArrayOutputStream();
            reader.setOutputStream(document);

            reader.generate();
        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve serializer", ce);
            fail("Could not retrieve serializer:"+ce.toString());
        } finally {
            if (reader!=null) {
                selector.release(reader);
            }

            if (selector!=null) {
                this.release(selector);
            }

            if (inputsource!=null) {
                resolver.release(inputsource);
            }

            if (resolver!=null) {
                this.release(resolver);
            }
        }

        return document.toByteArray();
    }
    
    public String callFunction(String type, String source, String function, Map params) throws Exception {
        
        redirector.reset();
        
        ServiceSelector selector = null;
        Interpreter interpreter = null;
        SourceResolver resolver = null;

        try {
            selector = (ServiceSelector) this.lookup(Interpreter.ROLE + "Selector");
            assertNotNull("Test lookup of interpreter selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if interpreter name is not null", type);
            interpreter = (Interpreter) selector.select(type);
            assertNotNull("Test lookup of interpreter", interpreter);
            
            ((AbstractInterpreter)interpreter).register(source);
            
            ArrayList parameters = new ArrayList();
            for (Iterator i = params.keySet().iterator(); i.hasNext();) {
                String name = (String)i.next();
                String value = (String)params.get(name);
                parameters.add(new Interpreter.Argument(name, value));
            }
            
            interpreter.callFunction(function, parameters, getRedirector());
            
        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve interpeter", ce);
            fail("Could not retrieve interpreter: " + ce.toString());
        } finally {
            if (interpreter != null) {
                selector.release(interpreter);
            }
            this.release(selector);
            this.release(resolver);
        }
        return FlowHelper.getWebContinuation(getObjectModel()).getId();
    }
    
    public String callContinuation(String type, String source, String id, Map params) throws Exception {
        
        redirector.reset();
        
        ServiceSelector selector = null;
        Interpreter interpreter = null;
        SourceResolver resolver = null;

        try {
            selector = (ServiceSelector) this.lookup(Interpreter.ROLE + "Selector");
            assertNotNull("Test lookup of interpreter selector", selector);

            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if interpreter name is not null", type);
            interpreter = (Interpreter) selector.select(type);
            assertNotNull("Test lookup of interpreter", interpreter);

            ((AbstractInterpreter)interpreter).register(source);
            
            ArrayList parameters = new ArrayList();
            for (Iterator i = params.keySet().iterator(); i.hasNext();) {
                String name = (String)i.next();
                String value = (String)params.get(name);
                parameters.add(new Interpreter.Argument(name, value));
            }
            
            interpreter.handleContinuation(id, parameters, getRedirector());

        } catch (ServiceException ce) {
            getLogger().error("Could not retrieve interpreter", ce);
            fail("Could not retrieve interpreter: " + ce.toString());
        } finally {
            if (interpreter != null) {
                selector.release(interpreter);
            }
            this.release(selector);
            this.release(resolver);
        }
        return FlowHelper.getWebContinuation(getObjectModel()).getId();
    }
    
    public Object getFlowContextObject() {
        return FlowHelper.getContextObject(getObjectModel());
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

        assertNotNull("Test for component manager", this.getManager());

        Document assertiondocument = null;
        try {
            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.lookup(SAXParser.ROLE);
            assertNotNull("Test lookup of parser", parser);

            assertNotNull("Test if assertion document is not null",
                          source);
            assertionsource = resolver.resolveURI(source);
            assertNotNull("Test lookup of assertion source",
                          assertionsource);
            assertTrue("Source '" + assertionsource.getURI() + "' must exist", assertionsource.exists());

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
            this.release(resolver);
            this.release(parser);
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

        assertNotNull("Test for component manager", this.getManager());

        byte[] assertiondocument = null;

        try {
            resolver = (SourceResolver) this.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            parser = (SAXParser) this.lookup(SAXParser.ROLE);
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
            this.release(resolver);
            this.release(parser);
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

        assertTrue(msg + ", " + diff.toString(), diff.similar());
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

        assertTrue("Test if the assertion document is equal, " + diff.toString(), diff.similar());
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

        assertTrue(msg + ", " + diff.toString(), diff.identical());
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

        assertTrue("Test if the assertion document is equal, " + diff.toString(), diff.identical());
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
