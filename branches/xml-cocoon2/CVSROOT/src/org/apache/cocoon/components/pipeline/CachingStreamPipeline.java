/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.configuration.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CachedStreamObject;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.caching.PipelineCacheKey;
import org.apache.cocoon.caching.StreamCache;
import org.apache.cocoon.environment.Environment;

/** A <CODE>StreamPipeline</CODE> either
 * <UL>
 *  <LI>collects a <CODE>Reader</CODE> and let it process</LI>
 *  <LI>or connects a <CODE>EventPipeline</CODE> with a
 *  <CODE>Serializer</CODE> and let them produce the requested
 * resource
 * </UL>
 *
 * This stream pipeline is able to cache the response, if
 * <ul>
 *  <li>a) the serializer is cacheable</li>
 *  <li>b) the <code>EventPipeline</code> is cacheable</li>
 *  </ul>
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-17 10:33:14 $
 */
public final class CachingStreamPipeline extends AbstractStreamPipeline {

    /** The role name of the serializer */
    private String serializerRole;

    /** The cache for the responses */
    private StreamCache streamCache;

    public void compose (ComponentManager manager)
    throws ComponentManagerException {
        super.compose(manager);
        this.streamCache = (StreamCache)this.manager.lookup(Roles.STREAM_CACHE);
    }

    public void dispose() {
        super.dispose();
        if (this.streamCache != null)
            this.manager.release((Component)this.streamCache);
    }

    /** Set the serializer.
     */
    public void setSerializer (String role, String source, Parameters param, String mimeType)
    throws Exception {
        super.setSerializer(role, source, param, mimeType);
        this.serializerRole = role;
    }

   /**
     * Process the request.
     */
    public boolean process(Environment environment)
    throws ProcessingException {
        if ( super.reader != null ) {
            return super.process(environment);
        } else {
            if ( !this.checkPipeline() ) {
                throw new ProcessingException("Attempted to process incomplete pipeline.");
            }

            try {

                boolean usedCache = false;
                OutputStream outputStream;
                PipelineCacheKey pcKey = null;
                Map validityObjects = null;

                outputStream = environment.getOutputStream();

                this.setupPipeline(environment);
                this.connectPipeline();

                // test if serializer and event pipeline are cacheable
                long serializerKey;
                PipelineCacheKey eventPipelineKey;
                CacheValidity serializerValidity;
                Map eventPipelineValidity;
                if (this.serializer instanceof Cacheable
                    && this.eventPipeline instanceof CacheableEventPipeline
                    && (serializerKey = ((Cacheable)this.serializer).generateKey()) != 0
                    && (serializerValidity = ((Cacheable)this.serializer).generateValidity()) != null
                    && (eventPipelineKey = ((CacheableEventPipeline)this.eventPipeline).generateKey(environment)) != null
                    && (eventPipelineValidity = ((CacheableEventPipeline)this.eventPipeline).generateValidity(environment)) != null) {

                    // response is cacheable, build the key
                    validityObjects = eventPipelineValidity;
                    ComponentCacheKey ccKey;
                    pcKey = new PipelineCacheKey();
                    ccKey = new ComponentCacheKey(ComponentCacheKey.ComponentType_Serializer,
                                                    this.serializerRole,
                                                    serializerKey);
                    validityObjects.put(ccKey, serializerValidity);
                    pcKey.addKey(ccKey);
                    pcKey.addKey(eventPipelineKey);

                    // now we have the key to get the cached object
                    CachedStreamObject cachedObject = (CachedStreamObject)this.streamCache.get(pcKey);

                    if (cachedObject != null) {
                        getLogger().debug("Found cached response.");

                        Iterator validityIterator = validityObjects.keySet().iterator();
                        ComponentCacheKey validityKey;
                        boolean valid = true;
                        while (validityIterator.hasNext() == true && valid == true) {
                            validityKey = (ComponentCacheKey)validityIterator.next();
                            valid = cachedObject.isValid(validityKey, (CacheValidity)validityObjects.get(validityKey));
                        }
                        if (valid == true) {

                            getLogger().debug("Using valid cached content.");
                            usedCache = true;
                            outputStream.write(cachedObject.getResponse());
                        } else {

                            getLogger().debug("Cached content is invalid.");
                            // remove invalid cached object
                            this.streamCache.remove(pcKey);
                            cachedObject = null;
                        }
                    }
                    if (cachedObject == null) {

                        getLogger().debug("Caching content for further requests.");
                        outputStream = new CachingOutputStream(outputStream);
                    }
                }


                if (usedCache == false) {
                    // set the output stream
                    this.serializer.setOutputStream(outputStream);

                    // execute the pipeline:
                    this.eventPipeline.process(environment);

                    // store the response
                    if (pcKey != null) {
                        this.streamCache.store(pcKey,
                            new CachedStreamObject(validityObjects,
                                  ((CachingOutputStream)outputStream).getContent()));
                    }
                }

            } catch ( Exception e ) {
                throw new ProcessingException(
                    "Failed to execute pipeline.",
                    e
                );
            }
            return true;
        }
    }

    /**
     * Recycle this component
     */
    public void recycle() {
        getLogger().debug("Recycling of CachingStreamPipeline");

        super.recycle();
        this.serializerRole = null;
    }
}

