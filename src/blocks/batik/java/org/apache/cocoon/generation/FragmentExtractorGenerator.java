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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLDeserializer;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.store.Store;

import org.xml.sax.SAXException;

/**
 * The generation half of FragmentExtractor.
 *
 * FragmentExtractor is a transformer-generator pair which is designed to allow
 * sitemap managers to extract certain nodes from a SAX stream and move them
 * into a separate pipeline. The main use for this is to extract inline SVG
 * images and serve them up through a separate pipeline, usually serializing
 * them to PNG or JPEG format first.
 *
 * This is by no means complete yet, but it should prove useful, particularly
 * for offline generation.
 *
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Id: FragmentExtractorGenerator.java,v 1.5 2004/03/05 13:01:46 bdelacretaz Exp $
 */
public class FragmentExtractorGenerator extends ServiceableGenerator
                                        implements CacheableProcessingComponent {

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
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
        XMLDeserializer deserializer = null;
        Object fragment = null;
        try {
            store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
            fragment = store.get(source);
            if (fragment == null) {
                throw new ResourceNotFoundException("Could not find fragment " + source + " in store");
            }

            deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
            deserializer.setConsumer(this.xmlConsumer);
            deserializer.deserialize(fragment);

        } catch (ServiceException ce) {
            getLogger().error("Could not lookup for component.", ce);
            throw new SAXException("Could not lookup for component.", ce);
        } finally {
            this.manager.release(store);
            this.manager.release(deserializer);
        }
    }
}
