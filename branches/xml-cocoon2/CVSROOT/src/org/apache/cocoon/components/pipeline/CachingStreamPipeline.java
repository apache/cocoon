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

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

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
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-04-18 16:56:49 $
 */
public final class CachingStreamPipeline extends AbstractStreamPipeline {

    /** The role name of the serializer */
    private String serializerRole;

    /** The role name of the serializer */
    private String readerRole;

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

    /** Set the Reader.
     */
    public void setReader (String role, String source, Parameters param, String mimeType)
    throws Exception {
        super.setReader(role, source, param, mimeType);
        this.readerRole = role;
    }

    /** Process the pipeline using a reader.
     * @throws ProcessingException if
     */
    protected boolean processReader(Environment environment)
    throws ProcessingException {

        try 
        {
            this.reader.setup((EntityResolver) environment,environment.getObjectModel(),readerSource,readerParam);
            String mimeType = this.reader.getMimeType();

            mimeType = this.reader.getMimeType();
            if ( mimeType != null ) {
                environment.setContentType(mimeType);
            } else if ( readerMimeType != null ) {
                environment.setContentType(this.readerMimeType);
            } else {
                environment.setContentType(this.sitemapReaderMimeType);
            }
        } catch (SAXException e){
            getLogger().debug("SAXException in ProcessReader", e);

            throw new ProcessingException(
                "Failed to execute pipeline.",
                e
            );
        } catch (IOException e){
            getLogger().debug("IOException in ProcessReader", e);

            throw new ProcessingException(
                "Failed to execute pipeline.",
                e
            );
        }

        try {
            boolean usedCache = false;
            OutputStream outputStream;
            PipelineCacheKey pcKey = null;
            Map validityObjects = new HashMap();

            outputStream = environment.getOutputStream();

            // test if serializer and event pipeline are cacheable
            long readerKey = 0;
            CacheValidity readerValidity = null;
            if (this.reader instanceof Cacheable
                && (readerKey = ((Cacheable)this.reader).generateKey()) != 0
                && (readerValidity = ((Cacheable)this.reader).generateValidity()) != null ){

                // response is cacheable, build the key
                ComponentCacheKey ccKey;
                pcKey = new PipelineCacheKey();
                ccKey = new ComponentCacheKey(ComponentCacheKey.ComponentType_Reader,
                                                this.readerRole,
                                                readerKey);
                validityObjects.put(ccKey, readerValidity);
                pcKey.addKey(ccKey);

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
                        byte[] response = cachedObject.getResponse();
                        outputStream.write(response);
                        if (response.length != 0) {
                            environment.setContentLength(response.length);
                        }
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

                this.reader.setOutputStream(outputStream);
                int length = this.reader.generate();
                if (length != 0) {
                    environment.setContentLength(length);
                }

                // store the response
                if (pcKey != null) {
                    this.streamCache.store(pcKey,
                        new CachedStreamObject(validityObjects,
                              ((CachingOutputStream)outputStream).getContent()));
                }
            }

        } catch ( Exception e ) {
            getLogger().debug("IOException in ProcessReader", e);

            throw new ProcessingException(
                "Failed to execute pipeline.",
                e
            );
        }

        return true;
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
                long serializerKey = 0;
                PipelineCacheKey eventPipelineKey = null;
                CacheValidity serializerValidity = null;
                Map eventPipelineValidity = null;
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
                getLogger().debug("Exception in process", e);
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
        //this.serializerRole = null;
    }
}

