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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.IdentifierCacheKey;
import org.apache.cocoon.components.pipeline.impl.ExpiresCachingProcessingPipeline;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 * This is a helper action for the expires pipeline implementation.
 * It can:
 * - calculate the cache-key and the cache-expires information for the pipeline
 *   Three parameters: cache-key and cache-expires (both are optional)
 *                     action with value set (required)
 * 
 * - delete one single cache entry
 *   Three parameters: cache-role and cache-key (cache-key is required)
 *                   action with value remove (required)
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ExpiresPipelineAction.java,v 1.4 2004/04/15 08:05:56 cziegeler Exp $
 * @since 2.1.1
 */
public class ExpiresPipelineAction extends ServiceableAction implements ThreadSafe {

    public Map act(Redirector redirector,
                    SourceResolver resolver,
                    Map objectModel,
                    String src,
                    Parameters par
    ) throws Exception {
        final String action = par.getParameter("action");
        if ( "remove".equals(action)) {
            final String cacheRole = par.getParameter("cache-role", Cache.ROLE);
            final String cacheKey = par.getParameter("cache-key");
        
            if ( cacheKey != null ) {
                Cache cache = null;

                IdentifierCacheKey key = new IdentifierCacheKey(cacheKey, true);
                try {
                    cache = (Cache)this.manager.lookup(cacheRole);
                    cache.remove(key);
                
                    key = new IdentifierCacheKey(cacheKey, false);
                    cache.remove(key);
                } catch (Exception ex) {
                    if (this.getLogger().isDebugEnabled()) {
                        getLogger().debug("Exception while trying to remove entry "+cacheKey+" from Cache with role " + cacheRole, ex);
                    }
                } finally {
                    this.manager.release( cache );
                }
            }
        } else if ( "set".equals(action) ) {
            final String cacheKey = par.getParameter("cache-key", null);
            if ( cacheKey != null ) {
                objectModel.put( ExpiresCachingProcessingPipeline.CACHE_KEY_KEY, cacheKey );
            }
            final String expires = par.getParameter("cache-expires", null);
            if ( expires != null ) {
                objectModel.put( ExpiresCachingProcessingPipeline.CACHE_EXPIRES_KEY, expires );
            }
        } else {
            throw new ProcessingException("The action can either be 'set' or 'remove' and not " + action);
        }
        return EMPTY_MAP;
    }
}
