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

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.components.cron.ConfigurableCronJob;
import org.apache.excalibur.source.SourceResolver;

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
 *  <code>cache-name (String)</code>: 
 *  The key used to cache the content
 * </li>
 * </ul>
 *  
 * @since 2.1.1
 * @version $Id$
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
    private String cacheName;
    
        
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
        this.expires = pars.getParameterAsInteger("cache-expires", 0);
        this.cacheName = pars.getParameter("cache-name", null);
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
            Cache cache = null;
            CachingSource source = null;
            try {
                cache = (Cache) manager.lookup(cacheRole);
                source = CachingSourceFactory.newCachingSource(
                                               this.resolver.resolveURI(this.uri),
                                               "cached",
                                               "cached:" + uri,
                                               expires,
                                               cacheName,
                                               true,
                                               cache,
                                               getLogger(),
                                               this.manager);
                                               
                source.refresh();
            }
            catch (IOException e) {
                getLogger().error("Error refreshing source", e);
            }
            catch (ServiceException e) {
                getLogger().error("Error refreshing source", e);
            }
            finally {
                if (cache != null) {
                    manager.release(cache);
                }
                if (source != null) {
                    this.resolver.release(source);
                }
            }
        }
    }

}
