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
package org.apache.cocoon;

// TODO: cleanup imports
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.source.SourceResolverAdapter;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockEnvironment;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.WhitespaceFilter;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * Adds some utility functions
 */
public abstract class ExtendedSitemapComponentTestCase extends
        SitemapComponentTestCase {
    /**
     * Generates with a supplied generator
     */
    public void generate(AbstractGenerator generator, String source,
            Parameters parameters, XMLConsumer consumer) throws Exception {
        MockEnvironment env = new MockEnvironment();
        Processor processor = new MockProcessor();

        EnvironmentHelper.enterProcessor(processor, this.getManager(), env);

        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        generator.setup(new SourceResolverAdapter(resolver, getManager()),
                getObjectModel(), source, parameters);
        generator.setConsumer(new WhitespaceFilter(consumer));
        generator.generate();
        generator.recycle();
        release(resolver);

        EnvironmentHelper.leaveProcessor();
    }

    /**
     * Transforms with a supplied transformer
     */
    public Document transform(Transformer transformer, String source,
            Parameters parameters, Document input) throws Exception {
        MockEnvironment env = new MockEnvironment();
        Processor processor = new MockProcessor();

        EnvironmentHelper.enterProcessor(processor, this.getManager(), env);

        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        transformer.setup(new SourceResolverAdapter(resolver, getManager()),
                getObjectModel(), source, parameters);

        SAXParser parser = (SAXParser) lookup(SAXParser.ROLE);
        DOMBuilder builder = new DOMBuilder();
        transformer.setConsumer(new WhitespaceFilter(builder));
        DOMStreamer streamer = new DOMStreamer(transformer);
        streamer.stream(input);
        Document document = builder.getDocument();

        release(resolver);
        release(parser);

        EnvironmentHelper.leaveProcessor();
        return document;
    }

    public Source resolveURI(String uri) throws Exception {
        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        Source source = resolver.resolveURI(uri);
        release(resolver);
        return source;
    }

    public void parse(String uri, ContentHandler contentHandler)
            throws Exception {
        Source source = resolveURI(uri);
        SourceUtil.parse(getManager(), source, contentHandler);
    }

    public Configuration conf(String name) {
        return new DefaultConfiguration(name);
    }

    public Configuration conf(String name, Configuration child) {
        DefaultConfiguration conf = new DefaultConfiguration(name);
        conf.addChild(child);
        return conf;
    }

    public Configuration conf(String name, String value) {
        DefaultConfiguration conf = new DefaultConfiguration(name);
        conf.setValue(value);
        return conf;
    }
}