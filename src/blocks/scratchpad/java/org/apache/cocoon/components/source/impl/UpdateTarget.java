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
import org.apache.excalibur.source.SourceValidity;
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
 * @version CVS $Id: UpdateTarget.java,v 1.5 2004/03/24 15:19:20 unico Exp $
 */
public class UpdateTarget extends AbstractLogEnabled
implements Serviceable, ConfigurableCronJob {
    
    // service dependencies
    private ServiceManager manager;
    private SourceResolver resolver;
    
    // configuration
    private String uri;
    private String cacheRole;
    private int expires;
    private boolean failSafe;
    
    // the key under which to store the CachedResponse in the Cache
    private SimpleCacheKey cacheKey;
    
        
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
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Refreshing " + this.uri);
            }
            
            Source source = null;
            Cache cache = null;
            try {
                
                cache = (Cache) this.manager.lookup(this.cacheRole);
                source = this.resolver.resolveURI(this.uri);
                
                // check if the source is really expired and invalid
                CachedSourceResponse response = (CachedSourceResponse) cache.get(this.cacheKey);
                if (response != null) {
                    final SourceValidity sourceValidity = response.getValidityObjects()[1];
                    if (CachingSource.isValid(sourceValidity, source)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Cached response is still valid " +                                "for source " + this.uri + ".");
                        }
                        response.getValidityObjects()[0] = new ExpiresValidity(this.expires * 1000);
                        return;
                    }
                }
                
                if (source.exists()) {
                    
                    // what is in the cached response?
                    byte[] binary = null;
                    byte[] xml = null;
                    if (response != null) {
                        binary = response.getBinaryResponse();
                        xml = response.getXMLResponse();
                    }
                    
                    // create a new cached response
                    final ExpiresValidity cacheValidity = new ExpiresValidity(this.expires * 1000);
                    final SourceValidity sourceValidity = source.getValidity();
                    response = new CachedSourceResponse(new SourceValidity[] {cacheValidity, sourceValidity});
                    
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
                    // FIXME: There is a potential problem when the parent
                    // source has not yet been updated thus listing this
                    // source still as one of its children. We'll have to remove 
                    // the parent's cached response here too.
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Source " + this.uri + " no longer exists." +                            " Throwing out cached response.");
                    }
                    cache.remove(this.cacheKey);
                }
            } catch (Exception e) {
                if (!failSafe) {
                    // the content expires, so remove it
                    cache.remove(cacheKey);
                    getLogger().warn("Exception during updating of source " + this.uri, e);
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
