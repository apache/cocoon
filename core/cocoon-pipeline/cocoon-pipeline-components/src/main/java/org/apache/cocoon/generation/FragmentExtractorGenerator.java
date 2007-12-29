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

import java.io.Serializable;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.store.Store;

import org.xml.sax.SAXException;

/**
 * @cocoon.sitemap.component.documentation
 * The generation half of FragmentExtractor (see also <code>FragmentExtractorTransformer)</code>.
 *
 * <p>FragmentExtractor is a transformer-generator pair which is designed to allow
 * sitemap managers to extract certain nodes from a SAX stream and move them
 * into a separate pipeline. The main use for this is to extract inline SVG
 * images and serve them up through a separate pipeline, usually serializing
 * them to PNG or JPEG format first.
 *
 * <p>This is by no means complete yet, but it should prove useful, particularly
 * for offline generation.
 *
 * @cocoon.sitemap.component.documentation
 * The generation half of FragmentExtractor (see also <code>FragmentExtractorTransformer)</code>.
 * @cocoon.sitemap.component.documentation.caching Yes
 *
 * @version $Id$
 */
public class FragmentExtractorGenerator extends ServiceableGenerator
                                        implements CacheableProcessingComponent {

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return this.source;
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    public void generate() throws SAXException, ProcessingException {
        // Obtain the fragmentID  (which is simply the filename portion of the source)
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Retrieving fragment " + source + ".");
        }

        Store store = null;
        Object fragment = null;
        try {
            store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
            fragment = store.get(source);
            if (fragment == null) {
                throw new ResourceNotFoundException("Could not find fragment " + source + " in store");
            }

            XMLByteStreamInterpreter deserializer = new XMLByteStreamInterpreter();
            deserializer.setConsumer(this.xmlConsumer);
            deserializer.deserialize(fragment);

        } catch (ServiceException ce) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not lookup for component.", ce);
            }
            throw new SAXException("Could not lookup for component.", ce);
        } finally {
            this.manager.release(store);
        }
    }
}
