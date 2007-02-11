/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 *  @version CVS $Id: IncludeCacheManagerSession.java,v 1.3 2003/03/12 15:08:04 cziegeler Exp $
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
    private Map sourceList = new HashMap(10);;
    
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
