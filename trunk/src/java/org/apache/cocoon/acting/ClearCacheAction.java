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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Simple action which ensures the cache is cleared of all
 * cached results
 *
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @version CVS $Id: ClearCacheAction.java,v 1.8 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Action
 * @x-avalon.lifestyle type=singleton
 */
public class ClearCacheAction extends ServiceableAction implements Action {


    /**
     * @avalon.dependency type="Cache"
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
    }
    
    public Map act(Redirector redirector,
                    SourceResolver resolver,
                    Map objectModel,
                    String src,
                    Parameters par
    ) throws Exception {
        final String cacheRole = par.getParameter("cache-role", Cache.ROLE);
        Cache cache = null;

        try {
            cache = (Cache)this.manager.lookup(cacheRole);
            cache.clear();
            return EMPTY_MAP;
        } catch (Exception ex) {
	        if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Exception while trying to clear Cache with role " + cacheRole, ex);
            }
            return null;
        } finally {
            this.manager.release( cache );
        }
    }

}
