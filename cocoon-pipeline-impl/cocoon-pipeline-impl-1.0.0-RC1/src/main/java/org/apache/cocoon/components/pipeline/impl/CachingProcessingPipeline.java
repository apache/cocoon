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
package org.apache.cocoon.components.pipeline.impl;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

/**
 * The CachingProcessingPipeline
 *
 * @since 2.1
 * @version $Id$
 */
public class CachingProcessingPipeline extends AbstractCachingProcessingPipeline {

    /**
    * Cache longest cacheable key
    */
    protected CachedResponse cacheResults(Environment environment, OutputStream os)  throws Exception {
        if (this.toCacheKey != null) {
            // See if there is an expires object for this resource.
            Long expiresObj = (Long) environment.getObjectModel().get(ObjectModelHelper.EXPIRES_OBJECT);

            CachedResponse response;
            if (this.cacheCompleteResponse) {
                response = new CachedResponse(this.toCacheSourceValidities,
                                              ((CachingOutputStream) os).getContent(),
                                              expiresObj);
                response.setContentType(environment.getContentType());
            } else {
                response = new CachedResponse(this.toCacheSourceValidities,
                                              (byte[]) this.xmlSerializer.getSAXFragment(),
                                              expiresObj);
            }

            this.cache.store(this.toCacheKey, response);
            return response;
        }
        return null;
    }

    /**
     * Create a new cache key
     */
    protected ComponentCacheKey newComponentCacheKey(int type, String role,Serializable key) {
        return new ComponentCacheKey(type, role, key);
    }

    /**
     * Connect the pipeline.
     */
    protected void connectCachingPipeline(Environment   environment)
    throws ProcessingException {
        XMLByteStreamCompiler localXMLSerializer = null;
        if (!this.cacheCompleteResponse) {
            this.xmlSerializer = new XMLByteStreamCompiler();
            localXMLSerializer = this.xmlSerializer;
        }

        if (this.cachedResponse == null) {
            XMLProducer prev = super.generator;
            XMLConsumer next;

            int cacheableTransformerCount = this.firstNotCacheableTransformerIndex;

            Iterator itt = this.transformers.iterator();
            while (itt.hasNext()) {
                next = (XMLConsumer) itt.next();
                if (localXMLSerializer != null) {
                    if (cacheableTransformerCount == 0) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    } else {
                        cacheableTransformerCount--;
                    }
                }
                connect(environment, prev, next);
                prev = (XMLProducer) next;
            }

            next = super.lastConsumer;
            if (localXMLSerializer != null) {
                next = new XMLTeePipe(next, localXMLSerializer);
                localXMLSerializer = null;
            }
            connect(environment, prev, next);
        } else {
            this.xmlDeserializer = new XMLByteStreamInterpreter();

            // connect the pipeline:
            XMLProducer prev = xmlDeserializer;
            XMLConsumer next;
            int cacheableTransformerCount = 0;
            Iterator itt = this.transformers.iterator();
            while (itt.hasNext()) {
                next = (XMLConsumer) itt.next();
                if (cacheableTransformerCount >= this.firstProcessedTransformerIndex) {
                    if (localXMLSerializer != null
                            && cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    connect(environment, prev, next);
                    prev = (XMLProducer) next;
                }
                cacheableTransformerCount++;
            }

            next = super.lastConsumer;
            if (localXMLSerializer != null) {
                next = new XMLTeePipe(next, localXMLSerializer);
                localXMLSerializer = null;
            }
            connect(environment, prev, next);
        }
    }
}
