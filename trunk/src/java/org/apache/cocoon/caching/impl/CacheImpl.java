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
package org.apache.cocoon.caching.impl;

import java.io.IOException;
import java.io.Serializable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.excalibur.store.Store;

/**
 * This is the Cocoon cache. This component is responsible for storing
 * and retrieving cached responses. It can be used to monitor the cache
 * or the investigate which responses are cached etc.
 * This component will grow!
 * 
 * 
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CacheImpl.java,v 1.10 2003/12/26 18:43:39 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=Cache
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=cache
 * 
 */
public class CacheImpl
extends AbstractLogEnabled
implements Cache, Serviceable, Disposable, Parameterizable {

    /** The store containing the cached responses */
    protected Store store;

    /** The component manager */
    protected ServiceManager manager;


    /**
     * Serviceable stage.
     * 
     * @avalon.dependency type="Store"
     */
    public void service (ServiceManager manager) {
        this.manager = manager;
    }

    /**
     * Reads the value of the <code>store</code> parameter that specifies the
     * lookup hint of the Store service to use and looks it up on the service
     * manager. If the parameter is not present the default transient store 
     * is attempted.
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        String storeName = parameters.getParameter("store", Store.TRANSIENT_STORE);
        try {
            this.store = (Store)this.manager.lookup(storeName);
        } catch (ServiceException ce) {
            throw new ParameterException("Unable to lookup store: " + storeName, ce);
        }
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        this.manager.release(this.store);
        this.store = null;
        this.manager = null;
    }

    /**
     * Store a cached response
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     * @param response    the cached response
     */
    public void store(Serializable     key,
                      CachedResponse   response)
    throws ProcessingException {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Caching new response for " + key);
        }
        try {
            this.store.store(key, response);
        } catch (IOException ioe) {
            throw new ProcessingException("Unable to cache response.", ioe);
        }
    }

    /**
     * Get a cached response.
     * If it is not available <code>null</code> is returned.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    public CachedResponse get(Serializable key) {
        final CachedResponse r = (CachedResponse)this.store.get(key);
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Cached response for " + key + " : " + 
                                   (r == null ? "not found" : "found"));
        }
        return r;
    }

    /**
     * Remove a cached response.
     * If it is not available no operation is performed.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    public void remove(Serializable key) {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Removing cached response for " + key); 
        }
        this.store.remove(key);
    }

    /**
     * clear cache of all cached responses 
     */
    public void clear() {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Clearing cache"); 
        }
        // FIXME this clears the whole store!
        this.store.clear();
    }

	/**
	 * See if a response is cached under this key
	 */
	public boolean containsKey(Serializable key) {
		return this.store.containsKey(key);
	}

}
