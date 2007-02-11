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
package org.apache.cocoon.components.pipeline.impl;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * The CachingProcessingPipeline
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachingProcessingPipeline.java,v 1.6 2004/05/06 19:34:13 upayavira Exp $
 */
public class CachingProcessingPipeline
    extends AbstractCachingProcessingPipeline {

    /**
    * Cache longest cacheable key
    */
    protected void cacheResults(Environment environment, OutputStream os)  throws Exception {
        if (this.toCacheKey != null) {
            // See if there is an expires object for this resource.                
            Long expiresObj = (Long) environment.getObjectModel().get(ObjectModelHelper.EXPIRES_OBJECT);
            if ( this.cacheCompleteResponse ) {
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          ((CachingOutputStream)os).getContent(),
                                          expiresObj);
                this.cache.store(this.toCacheKey,
                                 response);
            } else {
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          (byte[])this.xmlSerializer.getSAXFragment(),
                                          expiresObj);
                this.cache.store(this.toCacheKey,
                                 response);
            }
        }
    }

    /**
     * create a new cache key
     */
    protected ComponentCacheKey newComponentCacheKey(int type, String role,Serializable key) {
        return new ComponentCacheKey(type, role, key);
    }


    /**
     * Connect the pipeline.
     */
    protected void connectCachingPipeline(Environment   environment)
    throws ProcessingException {
            try {
                XMLSerializer localXMLSerializer = null;
                if (!this.cacheCompleteResponse) {
                    this.xmlSerializer = (XMLSerializer)this.manager.lookup( XMLSerializer.ROLE );
                    localXMLSerializer = this.xmlSerializer;
                }
                if ( this.cachedResponse == null ) {
                    XMLProducer prev = super.generator;
                    XMLConsumer next;

                    int cacheableTransformerCount = this.firstNotCacheableTransformerIndex;

                    Iterator itt = this.transformers.iterator();
                    while ( itt.hasNext() ) {
                        next = (XMLConsumer) itt.next();
                        if (localXMLSerializer != null) {
                            if (cacheableTransformerCount == 0) {
                                next = new XMLTeePipe(next, localXMLSerializer);
                                localXMLSerializer = null;
                            } else {
                                cacheableTransformerCount--;
                            }
                        }
                        this.connect(environment, prev, next);
                        prev = (XMLProducer) next;
                    }
                    next = super.lastConsumer;
                    if (localXMLSerializer != null) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    this.connect(environment, prev, next);
                } else {
                    this.xmlDeserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                    // connect the pipeline:
                    XMLProducer prev = xmlDeserializer;
                    XMLConsumer next;
                    int cacheableTransformerCount = 0;
                    Iterator itt = this.transformers.iterator();
                    while ( itt.hasNext() ) {
                        next = (XMLConsumer) itt.next();
                        if (cacheableTransformerCount >= this.firstProcessedTransformerIndex) {
                            if (localXMLSerializer != null
                                    && cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                                next = new XMLTeePipe(next, localXMLSerializer);
                                localXMLSerializer = null;
                            }
                            this.connect(environment, prev, next);
                            prev = (XMLProducer)next;
                        }
                        cacheableTransformerCount++;
                    }
                    next = super.lastConsumer;
                    if (localXMLSerializer != null) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    this.connect(environment, prev, next);
                }

            } catch ( ComponentException e ) {
                throw new ProcessingException("Could not connect pipeline.", e);
            }
    }

}
