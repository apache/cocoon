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
package org.apache.cocoon.components.container;

import org.apache.avalon.fortress.ContainerManagerConstants;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.lifecycle.Creator;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * SitemapConfigurableCreator is the Lifecycle extension for [@link SitemapConfigurable}
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @author <a href="cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $ Revision: 1.1 $
 */
public class SitemapConfigurableCreator 
implements Creator {

    /** 
     * The {@link SitemapConfigurationHolder}s 
     */
    private Map m_sitemapConfigurationHolders = new HashMap( 15 );

    /* (non-Javadoc)
     * @see org.apache.avalon.lifecycle.Creator#create(java.lang.Object, org.apache.avalon.framework.context.Context)
     */
    public void create(Object object, Context context) 
    throws Exception {
        if ( object instanceof SitemapConfigurable ) {
            ServiceManager manager = (ServiceManager) context.get(ContainerManagerConstants.SERVICE_MANAGER);
            MetaInfoManager metaInfoManager = (MetaInfoManager)manager.lookup(MetaInfoManager.ROLE);
            try {
                String role = metaInfoManager.getMetaInfoForClassname(object.getClass().getName()).getConfigurationName();
                SitemapConfigurationHolder holder;
                
                holder = (SitemapConfigurationHolder) m_sitemapConfigurationHolders.get( role );
                if ( null == holder ) {
                    // create new holder
                    holder = new DefaultSitemapConfigurationHolder( role );
                    m_sitemapConfigurationHolders.put( role, holder );
                }
                
                ( (SitemapConfigurable) object ).configure( holder );
            } finally {
                manager.release(metaInfoManager);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.lifecycle.Creator#destroy(java.lang.Object, org.apache.avalon.framework.context.Context)
     */
    public void destroy(Object object, Context context) {
    }

}
