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
package org.apache.butterfly.components.pipeline.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.butterfly.components.pipeline.InvalidPipelineException;
import org.apache.butterfly.generation.FileGenerator;
import org.apache.butterfly.generation.Generator;
import org.apache.butterfly.reading.Reader;
import org.apache.butterfly.serialization.Serializer;
import org.apache.butterfly.source.SourceResolver;
import org.apache.butterfly.source.impl.FileSourceFactory;
import org.apache.butterfly.transformation.Transformer;
import org.apache.butterfly.transformation.TraxTransformer;
import org.apache.butterfly.xml.Parser;
import org.apache.butterfly.xml.WhitespaceFilter;
import org.apache.butterfly.xml.XMLConsumer;
import org.apache.butterfly.xml.dom.DOMBuilder;
import org.apache.butterfly.xml.xslt.TraxTransformerFactory;
import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * test case for NonCachingProcessingPipeline.
 * 
 * @version CVS $Id$
 */
public class NonCachingProcessingPipelineTestCase extends MockObjectTestCase {
    
    private Generator stubGenerator;
    private Reader stubReader;
    private Transformer stubTransformer;
    private Serializer stubSerializer;
    private FileGenerator fileGenerator;
    private TraxTransformerFactory transformerFactory;

    public NonCachingProcessingPipelineTestCase(String name) {
        super(name);
    }
    
    public void setUp() {
        // Set up stubs
        stubGenerator = new Generator() {
            public void generate() {}
            public void setConsumer(XMLConsumer consumer) {}
        };
        
        stubReader = new Reader() {
            public void generate() {}
            public long getLastModified() {
                return 0;
            }
            public void setOutputStream(OutputStream out) {}
            public String getMimeType() {
                return null;
            }
            public boolean shouldSetContentLength() {
                return false;
            }
            public void setObjectModel(Map objectModel) {
            }
        }; 
        
        stubTransformer = new Transformer() {
            public void setDocumentLocator(Locator arg0) {}
            public void startDocument() throws SAXException {}
            public void endDocument() throws SAXException {}
            public void startPrefixMapping(String arg0, String arg1) throws SAXException {}
            public void endPrefixMapping(String arg0) throws SAXException {}
            public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {}
            public void endElement(String arg0, String arg1, String arg2) throws SAXException {}
            public void characters(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void processingInstruction(String arg0, String arg1) throws SAXException {}
            public void skippedEntity(String arg0) throws SAXException {}
            public void startDTD(String arg0, String arg1, String arg2) throws SAXException {}
            public void endDTD() throws SAXException {}
            public void startEntity(String arg0) throws SAXException {}
            public void endEntity(String arg0) throws SAXException {}
            public void startCDATA() throws SAXException {}
            public void endCDATA() throws SAXException {}
            public void comment(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void setConsumer(XMLConsumer consumer) {}
        };
        
        stubSerializer = new Serializer() {
            public void setDocumentLocator(Locator arg0) {}
            public void startDocument() throws SAXException {}
            public void endDocument() throws SAXException {}
            public void startPrefixMapping(String arg0, String arg1) throws SAXException {}
            public void endPrefixMapping(String arg0) throws SAXException {}
            public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {}
            public void endElement(String arg0, String arg1, String arg2) throws SAXException {}
            public void characters(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void processingInstruction(String arg0, String arg1) throws SAXException {}
            public void skippedEntity(String arg0) throws SAXException {}
            public void startDTD(String arg0, String arg1, String arg2) throws SAXException {}
            public void endDTD() throws SAXException {}
            public void startEntity(String arg0) throws SAXException {}
            public void endEntity(String arg0) throws SAXException {}
            public void startCDATA() throws SAXException {}
            public void endCDATA() throws SAXException {}
            public void comment(char[] arg0, int arg1, int arg2) throws SAXException {}
            public void setConsumer(XMLConsumer consumer) {}
            public void setOutputStream(OutputStream out) {}
            public String getMimeType() {
                return null;
            }
            public boolean shouldSetContentLength() {
                return false;
            }
            public void setObjectModel(Map objectModel) {}
        };
        
        // Set up real compponents
        SourceResolver sourceResolver = new SourceResolver();
        Map sourceFactories = new HashMap();
        sourceFactories.put("*", new FileSourceFactory());
        sourceResolver.setFactories(sourceFactories);
        Parser parser = new Parser();
        parser.setSaxDriver("org.apache.xerces.parsers.SAXParser");
        parser.initialize();
        fileGenerator = new FileGenerator();
        fileGenerator.setSourceResolver(sourceResolver);
        fileGenerator.setParser(parser);
        transformerFactory = new TraxTransformerFactory();
        transformerFactory.setSourceResolver(sourceResolver);
    }

    /**
     * Test if setting reader after having set the generator causes
     * error.
     */
    public void testSetReaderAfterGenerator() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setGenerator(stubGenerator);
        try {
            pipeline.setReader(stubReader);
            fail("Setting reader after generator did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if setting generator after having set the reader causes
     * error.
     */
    public void testSetGeneratorAfterReader() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        try {
            pipeline.setGenerator(stubGenerator);
            fail("Setting generator after reader did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }

    /**
     * Test if setting generator twice causes error.
     */
    public void testSetGeneratorTwice() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setGenerator(stubGenerator);
        try {
            pipeline.setGenerator(stubGenerator);
            fail("Setting generator twice did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }

    /**
     * Test if setting reader twice causes error.
     */
    public void testSetReaderTwice() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        try {
            pipeline.setReader(stubReader);
            fail("Setting reader twice did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if adding a transformer without having set the generator causes
     * error.
     */
    public void testAddTransformerWithoutGenerator() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        try {
            pipeline.addTransformer(stubTransformer);
            fail("Adding transformer without a generator did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if adding a transformer after having set the reader causes
     * error.
     */
    public void testAddTransformerAfterReader() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        try {
            pipeline.addTransformer(stubTransformer);
            fail("Adding transformer after reader did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if setting serializer after having set the reader causes
     * error.
     */
    public void testSetSerializerAfterReader() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        try {
            pipeline.setSerializer(stubSerializer);
            fail("Setting generator after reader did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if setting serializer without having set the generator causes
     * error.
     */
    public void testSetSerializerWithoutGenerator() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        try {
            pipeline.setSerializer(stubSerializer);
            fail("Setting generator after reader did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }

    /**
     * Test if setting serializer twice causes error.
     */
    public void testSetSerializerTwice() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setGenerator(stubGenerator);
        pipeline.setSerializer(stubSerializer);
        try {
            pipeline.setSerializer(stubSerializer);
            fail("Setting serializer twice did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }

    /**
     * Test if processing an internal pipeline with a reader causes error.
     */
    public void testProcessInternalWithReader() {
        NonCachingProcessingPipeline pipeline = 
            new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        try {
            pipeline.process(null, null);
            fail("Processing an internal pipeline with a reader did not cause an exception.");
        } catch(InvalidPipelineException e) {}
    }
    
    /**
     * Test if the @link{NonCachingProcessingPipeline#connect(Environment)} method
     * correctly connects pipeline components. 
     */
    public void testConnect() {
        NonCachingProcessingPipeline pipeline = 
            new NonCachingProcessingPipeline();
        // Setup mocks
        Mock mockGenerator = new Mock(Generator.class);
        Mock mockTransformer1 = new Mock(Transformer.class);
        Mock mockTransformer2 = new Mock(Transformer.class);
        Mock mockSerializer = new Mock(Serializer.class);
        mockGenerator.expects(once()).method("setConsumer").with(same(mockTransformer1.proxy()));
        mockTransformer1.expects(once()).method("setConsumer").with(same(mockTransformer2.proxy()));
        mockTransformer2.expects(once()).method("setConsumer").with(same(mockSerializer.proxy()));

        pipeline.setGenerator((Generator) mockGenerator.proxy());
        pipeline.addTransformer((Transformer) mockTransformer1.proxy());
        pipeline.addTransformer((Transformer) mockTransformer2.proxy());
        pipeline.setSerializer((Serializer) mockSerializer.proxy());
        pipeline.connectPipeline(null);
        
        mockGenerator.verify();
    }
    
    /**
     * Test if sane pipeline passes check.
     */
    public void testCheckSanePipeline() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setGenerator(stubGenerator);
        pipeline.addTransformer(stubTransformer);
        pipeline.setSerializer(stubSerializer);
        assertTrue("Pipeline should be sane.", pipeline.checkPipeline());
    }
    
    /**
     * Test if pipeline with reader passes check.
     */
    public void testCheckReaderPipeline() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setReader(stubReader);
        assertTrue("Pipeline should be sane.", pipeline.checkPipeline());
    }
    
    /**
     * Verify that empty pipeline does not pass check.
     */
    public void testCheckEmptyPipeline() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        assertFalse("Pipeline should not be sane.", pipeline.checkPipeline());
    }
    
    /**
     * Verify that pipeline without serializer does not pass check.
     */
    public void testCheckPipelineWithoutSerializer() {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        pipeline.setGenerator(stubGenerator);
        pipeline.addTransformer(stubTransformer);
        assertFalse("Pipeline should not be sane.", pipeline.checkPipeline());
    }
    
    /**
     * Test event pipeline processing with only a generator.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void testProcessSimpleEventPipeline() throws IOException, SAXException, ParserConfigurationException {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        XMLUnit.setIgnoreWhitespace(true);
        fileGenerator.setInputSource("testdata/test1.xml");
        pipeline.setGenerator(fileGenerator);
        pipeline.setSerializer(stubSerializer);
        DOMBuilder builder = new DOMBuilder();
        pipeline.process(null, new WhitespaceFilter(builder));
        assertTrue("Output from pipeline does not match input file.",
                XMLUnit.compareXML(
                        XMLUnit.buildControlDocument(new InputSource("testdata/test1.xml")),
                        builder.getDocument()).similar());
    }
    
    /**
     * Test event pipeline processing with a transformer.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void testProcessEventPipeline() throws IOException, SAXException, ParserConfigurationException {
        NonCachingProcessingPipeline pipeline = 
                new NonCachingProcessingPipeline();
        XMLUnit.setIgnoreWhitespace(true);
        fileGenerator.setInputSource("testdata/traxtest-input.xml");
        pipeline.setGenerator(fileGenerator);
        TraxTransformer transformer = transformerFactory.getTransformer("testdata/traxtest-style.xsl");
        pipeline.addTransformer(transformer);
        pipeline.setSerializer(stubSerializer);
        DOMBuilder builder = new DOMBuilder();
        pipeline.process(null, new WhitespaceFilter(builder));
        assertTrue("Output from pipeline does not match control file.",
                XMLUnit.compareXML(
                        XMLUnit.buildControlDocument(new InputSource("testdata/traxtest-result.xml")),
                        builder.getDocument()).similar());
    }
}
