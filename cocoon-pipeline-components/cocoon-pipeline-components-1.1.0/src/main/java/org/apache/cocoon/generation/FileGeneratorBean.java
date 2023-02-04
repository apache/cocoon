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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.DisposableSitemapComponent;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events. The <code>FileGenerator</code> is cacheable.
 *
 * @cocoon.sitemap.component.documentation
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events. The <code>FileGenerator</code> is cacheable.
 * @cocoon.sitemap.component.name   file
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching
 * Uses the last modification date of the xml document for validation
 *
 * @version $Id$
 */
public class FileGeneratorBean extends AbstractLogEnabled
                               implements Generator, CacheableProcessingComponent, DisposableSitemapComponent {

    /** The input source */
    protected Source inputSource;

    /** The source resolver. */
    protected SourceResolver resolver;

    /** The consumer. */
    protected XMLConsumer consumer;

    /** The SAX Parser. */
    protected SAXParser parser;


    public void setParser(SAXParser parser) {
        this.parser = parser;
    }

    /**
     * @see org.apache.cocoon.sitemap.DisposableSitemapComponent#dispose()
     */
    public void dispose() {
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        this.resolver = null;
        this.consumer = null;
    }

    /**
     * Setup the file generator.
     * Try to get the last modification date of the source for caching.
     *
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;

        try {
            this.inputSource = this.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Source " + src +
                              " resolved to " + this.inputSource.getURI());
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return this.inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }

    /**
     * @see org.apache.cocoon.xml.XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            SourceUtil.parse(this.parser, this.inputSource, this.consumer);
        } catch (SAXException e) {
            SourceUtil.handleSAXException(this.inputSource.getURI(), e);
        }
    }
}
