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
package org.apache.cocoon.components.source.impl;

import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.cron.ConfigurableCronJob;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

/**
 * A target updating a cache entry.
 *
 * This target requires several parameters:
 * <ul>
 * <li>
 *   <code>uri (String)</code>: 
 *   The uri to cache, every valid protocol can be used, except the Cocoon protocol!
 * </li>
 * <li>
 *  <code>cache-role (String)</code>: 
 *  The role of the cache component to store the content
 * </li>
 * <li>
 *  <code>cache-expires (long)</code>: 
 *  The time in seconds the cached content is valid
 * </li>
 * <li>
 *  <code>fail-safe (boolean)</code>
 *  Whether to invalidate the cached response when updating it failed.
 * </li>
 * <li>
 *  <code>cache-key (SimpleCacheKey)</code>: 
 *  The key used to cache the content
 * </li>
 * </ul>
 *  
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: UpdateTarget.java,v 1.4 2004/03/23 16:28:54 unico Exp $
 */
public class UpdateTarget extends AbstractLogEnabled
implements Serviceable, ConfigurableCronJob {
    
    // service dependencies
    protected ServiceManager manager;
    protected SourceResolver resolver;
    
    // configuration
    protected String uri;
    protected String cacheRole;
    protected int expires;
    protected boolean failSafe;
    
    // the key under which to store the CachedResponse in the Cache
    protected SimpleCacheKey cacheKey;
    
        
    // ---------------------------------------------------- Lifecycle
    
    public UpdateTarget() {
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.scheduler.ConfigurableTarget#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters pars, Map objects) {
        this.uri = pars.getParameter("uri", null);
        this.cacheRole = pars.getParameter("cache-role", Cache.ROLE);
        this.expires = pars.getParameterAsInteger("cache-expires", 60);
        this.failSafe = pars.getParameterAsBoolean("fail-safe", true);
        this.cacheKey = (SimpleCacheKey) objects.get("cache-key");
    }
    
    
    // ---------------------------------------------------- CronJob implementation
    
    /* (non-Javadoc)
     * @see org.apache.avalon.cornerstone.services.scheduler.Target#targetTriggered(java.lang.String)
     */
    public void execute(String name) {
        if (this.uri != null) {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("Refreshing " + this.uri);
            }
            
            Source source = null;
            Cache cache = null;
            try {
                
                cache = (Cache) this.manager.lookup(this.cacheRole);
                source = this.resolver.resolveURI(this.uri);
                
                CachedSourceResponse response = (CachedSourceResponse) cache.get(this.cacheKey);
                
                if (source.exists()) {
                    
                    // what is in the cached response?
                    byte[] binary = null;
                    byte[] xml = null;
                    if (response != null) {
                        binary = response.getBinaryResponse();
                        xml = response.getXMLResponse();
                    }
                    
                    // create a new cached response
                    response = new CachedSourceResponse(new ExpiresValidity(this.expires * 1000));
                    
                    // only create objects that have previously been used
                    if (binary != null) {
                        binary = CachingSource.readBinaryResponse(source);
                        response.setBinaryResponse(binary);
                    }
                    if (xml != null) {
                        xml = CachingSource.readXMLResponse(source, binary, this.manager);
                        response.setXMLResponse(xml);
                    }
                    // meta info is always set
                    response.setExtra(CachingSource.readMeta(source));
                    
                    cache.store(this.cacheKey, response);
                }
                else if (response != null) {
                    cache.remove(this.cacheKey);
                }
            } catch (Exception e) {
                if (!failSafe) {
                    // the content expires, so remove it
                    cache.remove(cacheKey);
                    getLogger().warn("Exception during updating " + this.uri, e);
                }
                else {
                    getLogger().warn("Updating of source " + this.uri + " failed. " +
                        "Cached response (if any) will be stale.", e);
                }
            } finally {
                this.resolver.release(source);
                this.manager.release(cache);
            }
        }
    }

}
