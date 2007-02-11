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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.impl.EventAwareCacheImpl;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Simple action to cause notification of a NamedEvent to an EventAwareCacheImpl.
 * The event name is taken from a sitemap parameter named "event".
 * 
 * This action returns null (fails) if the configured event is null or the 
 * empty string.  Otherwise, it succeeds and returns an empty Map.
 * 
 * This is used in the Event based cache example. 
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version CVS $Id: CacheEventAction.java,v 1.5 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class CacheEventAction extends ServiceableAction implements ThreadSafe {

    /**
     * Lookup the cache and call its processEvent method. Returns an 
     * empty map to signal success.
     */
    public Map act(Redirector redirector,
                    SourceResolver resolver,
                    Map objectModel,
                    String src,
                    Parameters par
    ) throws Exception {
        Cache cache = (Cache)this.manager.lookup(Cache.ROLE + "/EventAware");
        if (cache instanceof EventAwareCacheImpl) {
            String eventName = par.getParameter("event");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Configured for cache event named: " + eventName);
            }
            if (eventName == null || "".equals(eventName)) {
                return null;
            }
            ((EventAwareCacheImpl)cache).processEvent(
                                                new NamedEvent(eventName));
        }
        this.manager.release(cache);
        return EMPTY_MAP;
    }
}