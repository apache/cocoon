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
package org.apache.cocoon.transformation.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * The preemptive loader is a singleton that runs in the background
 * and loads content into the cache.
 * 
 * @since   2.1
 * @version $Id$
 */
public final class PreemptiveLoader extends AbstractLogEnabled {

    private static final PreemptiveLoader INSTANCE = new PreemptiveLoader();
    
    /** The list of proxies currently used for caching */
    private Map   cacheStorageProxyMap = new HashMap(20);

    /** The list of URIs to load */
    private List  loadList = new ArrayList(50);

    /** Is this thread still alive? */
    boolean alive;
    
    /**
     * Return singleton.
     *
     * @return PreemptiveLoader
     */
    static PreemptiveLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Add a new task
     * @param proxy   The cache to store the content
     * @param uri     The absolute URI to load
     * @param expires The expires information used for the cache
     */
    public void add(IncludeCacheStorageProxy proxy, String uri, long expires) {
        boolean addItem = true;
        List uriList = (List)this.cacheStorageProxyMap.get(proxy);
        if ( null == uriList ) {
             uriList = new ArrayList(50);
             this.cacheStorageProxyMap.put(proxy, uriList);
        } else {
            synchronized (uriList) {
                // nothing to do: uri is alredy in list
               if (uriList.contains(uri)) {
                   addItem = false;
               } 
            }
        }
        if ( addItem ) {
            uriList.add(uri);
            this.loadList.add(new Object[] {proxy, uri, new Long(expires), uriList});
        }

        synchronized (this.cacheStorageProxyMap) {
            this.cacheStorageProxyMap.notify();
        }
    }
    
    /**
     * Start the preemptive loading
     * @param resolver  A source resolver
     */
    public void process(SourceResolver resolver) {
        process(resolver, getLogger());
    }

    /**
     * Start the preemptive loading
     * @param resolver  A source resolver
     * @param logger    A logger
     */
    public void process(SourceResolver resolver,
                        Log logger) {
        this.alive = true;
        if (logger.isDebugEnabled()) {
            logger.debug("PreemptiveLoader: Starting preemptive loading");
        }

        while (this.alive) {
            while (this.loadList.size() > 0) {
                Object[] object = (Object[])this.loadList.get(0);
                final String uri = (String)object[1];
                this.loadList.remove(0);
                synchronized (object[3]) {
                    ((List)object[3]).remove(uri);
                }
                
                Source source = null;
                XMLByteStreamCompiler serializer;

                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("PreemptiveLoader: Loading " + uri);
                    }

                    source = resolver.resolveURI(uri);
                    serializer = new XMLByteStreamCompiler();
                
                    SourceUtil.toSAX(source, serializer);
                
                    SourceValidity[] validities = new SourceValidity[1];
                    validities[0] = new ExpiresValidity(((Long)object[2]).longValue() * 1000); // milliseconds!
                    CachedResponse response = new CachedResponse(validities,
                                                                 (byte[])serializer.getSAXFragment());
                    ((IncludeCacheStorageProxy)object[0]).put(uri, response);
                     
                } catch (Exception ignore) {
                    // all exceptions are ignored!
                } finally {
                    resolver.release( source );
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("PreemptiveLoader: Finished loading " + uri);
                }
            }
            synchronized (this.cacheStorageProxyMap) {
                try {
                    this.cacheStorageProxyMap.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("PreemptiveLoader: Finished preemptive loading");
        }
    }
    
    /**
     * Stop the loading. 
     * The loader stops when all tasks from the queue are processed.
     */
    synchronized public void stop() {
        this.alive = false;
        synchronized (this.cacheStorageProxyMap) {
            this.cacheStorageProxyMap.notify();
        }
    }
}
