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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

/**
 * This object encapsulates a "caching session". A caching session has the
 * duration of one single request.
 * This object is used by the {@link IncludeCacheManager} and holds all required
 * configuration for performing the caching of this request.
 * 
 * The session can be configured during construction with the following parameters:
 * - purge (boolean/false) : Turn on/off purging the cache
 * - preemptive (boolean/false) : Turn on/off preemptive caching
 * - parallel (boolean/false) : Turn on/off parallel processing
 * - expires (long/0) : The lifetime of the cached content
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: IncludeCacheManagerSession.java,v 1.6 2004/03/05 13:03:00 bdelacretaz Exp $
 *  @since   2.1
 */
public final class IncludeCacheManagerSession {

    /** The expires information */
    private long expires;
    
    /** Should we purge the cache */
    private boolean purge;
    
    /** Should we load preemptive */
    private boolean preemptive;
    
    /** Should we process everything in parallel */
    private boolean parallel;

    /** The used {@link IncludeCacheStorageProxy} */
    private IncludeCacheStorageProxy storage;
    
    /** The list of all threads */
    private Map threadList;
    
    /** Cache the expires validity object */
    private SourceValidity validity;
    
    /** Cache the source objects */
    private Map sourceList = new HashMap(10);
    
    /**
     * Constructor
     * @param configuration The parameters configuring this session
     * @param proxy         The proxy used to cache the data
     */
    IncludeCacheManagerSession(Parameters configuration, 
                        IncludeCacheStorageProxy proxy) {
        this.expires = configuration.getParameterAsLong("expires", 0);
        this.purge = configuration.getParameterAsBoolean("purge", false);    
        this.preemptive = configuration.getParameterAsBoolean("preemptive", false);
        this.parallel = configuration.getParameterAsBoolean("parallel", false);
        this.storage = proxy;    
    }
    
    /**
     * Get the used storage proxy
     */
    IncludeCacheStorageProxy getCacheStorageProxy() {
        return this.storage;
    }

    /**
     * Get the expiration information
     */
    public long getExpires() {
        return this.expires;
    }

    public SourceValidity getExpiresValidity() {
        if ( this.expires > 0 && this.validity == null) {
            this.validity = new ExpiresValidity( this.expires * 1000 ); // milliseconds
        }
        return this.validity;
    }
    
    /**
     * Is the cache purged?
     */
    public boolean isPurging() {
        return this.purge;
    }

    /**
     * Do we use preemptive caching?
     */
    public boolean isPreemptive() {
        return this.preemptive;
    }

    /**
     * Do we process the includes in parallel?
     */
    public boolean isParallel() {
        return this.parallel;
    }

    /**
     * Add another object to the thread list
     * @param uri    The absolute URI
     * @param object The thread
     */
    void add(String uri, Object object) {
        if ( null == this.threadList ) {
            this.threadList = new HashMap(10);
        }
        this.threadList.put(uri, object);
    }
    
    /**
     * Get the thread object.
     * @param uri     The URI
     * @return Object The thread.
     */
    Object get(String uri) {
        if ( null != this.threadList ) {
            return this.threadList.get( uri );
        }
        return null;
    }
    
    /**
     * Turn off/on preemptive caching
     */
    void setPreemptive(boolean value) {
        this.preemptive = value;
    }
    
    /**
     * Lookup a source object and cache it
     * @param uri     Absolute URI
     * @return Source The source obejct
     */
    public Source resolveURI(String uri, SourceResolver resolver) 
    throws IOException {
        Source source = (Source)this.sourceList.get(uri);
        if ( null == source ) {
            source = resolver.resolveURI( uri );
            this.sourceList.put( source.getURI(), source );
        }
        return source;
    }
    
    /**
     * Cleanup
     * @param resolver The source resolver to release cached sources
     */
    void cleanup(SourceResolver resolver) {
        Iterator iter = this.sourceList.values().iterator();
        while ( iter.hasNext() ) {
            final Source source = (Source) iter.next();
            resolver.release( source );   
        }
    }
    
    /**
     * Print a representation of this object
     */
    public String toString() {
        return "CacheManagerSession(" + this.hashCode() + ") -" +
                " expires: " + this.expires +
                " parallel: " + this.parallel + 
                " preemptive: " + this.preemptive +
                " purge: " + this.purge;
    }
}
