/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * @cocoon.sitemap.component.documentation
 * The <code>FileGeneratorFactory</code> is a class that reads XML from a source
 * and generates SAX Events. The <code>FileGeneratorFactory</code> implements the
 * <code>CacheableProcessingComponent</code> interface.
 *
 * @cocoon.sitemap.component.name   file
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.logger sitemap.generator.file
 * @cocoon.sitemap.component.documentation.caching
 *     Uses the last modification date of the xml document for validation
 *
 * @version $Id$
 */
public class FileGeneratorFactory extends AbstractLogEnabled
                                  implements GeneratorFactory, Serviceable {

    /** The service manager */
    protected ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Single threaded instance implementing generator functionality.
     */
    private class Instance extends AbstractXMLProducer
                           implements GeneratorFactory.Instance,
                                      CacheableProcessingComponent, Disposable {

        /** The source resolver */
        private SourceResolver resolver;

        /** The input source */
        private Source source;

        /** Return GeneratorFactory */
        public GeneratorFactory getFactory() {
            return FileGeneratorFactory.this;
        }

        public Instance(Logger logger) {
            enableLogging(logger);
        }

        /** Setup: resolve the source */
        public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
            this.resolver = resolver;
            try {
                this.source = this.resolver.resolveURI(src);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Source '" + src +
                                      "' resolved to <" + this.source.getURI() + ">");
                }
            } catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
            }
        }

        /** Dispose: release the source */
        public void dispose() {
            if (this.source != null) {
                this.resolver.release(this.source);
                this.source = null;
            }
        }

        /**
         * Generate the unique key.
         * This key must be unique inside the space of this component.
         *
         * @return The generated key hashes the src
         */
        public Serializable getKey() {
            return this.source.getURI();
        }

        /**
         * Generate the validity object.
         *
         * @return The generated validity object or <code>null</code> if the
         *         component is currently not cacheable.
         */
        public SourceValidity getValidity() {
            return this.source.getValidity();
        }

        /**
         * Generate XML data.
         */
        public void generate()
        throws IOException, SAXException, ProcessingException {
            try {
                SourceUtil.parse(manager, this.source, super.xmlConsumer);
            } catch (SAXException e) {
                SourceUtil.handleSAXException(this.source.getURI(), e);
            }
        }
    }

    public GeneratorFactory.Instance getInstance() {
        return new Instance(getLogger());
    }
}
