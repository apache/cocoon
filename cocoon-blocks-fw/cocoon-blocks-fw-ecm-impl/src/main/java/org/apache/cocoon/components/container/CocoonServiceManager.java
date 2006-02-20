/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.container;

//import java.util.HashMap;
//import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ComponentInfo;
//import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.components.treeprocessor.sitemap.ProcessorComponentInfo;
import org.apache.cocoon.core.container.CoreServiceManager;

/**
 * Default service manager for Cocoon's components.
 *
 * @version $Id$
 */
public class CocoonServiceManager extends CoreServiceManager {
    
    /** The {@link SitemapConfigurationHolder}s */
    //private Map sitemapConfigurationHolders = new HashMap(15);
    
    private ProcessorComponentInfo info;

    /** Create the ServiceManager with a parent ServiceManager */
    public CocoonServiceManager( final ServiceManager parent) {
        this(parent, null);
    }
    
    /** Create the ServiceManager with a parent ServiceManager and a ClassLoader */
    public CocoonServiceManager( final ServiceManager parent, final ClassLoader classloader) {
        super(parent, classloader);
        ProcessorComponentInfo parentInfo = null;
        if (parent != null) {
            try {
                parentInfo = (ProcessorComponentInfo) parent.lookup(ProcessorComponentInfo.ROLE);
            } catch (ServiceException e) {
                // no parent
            }
        }
        this.info = new ProcessorComponentInfo(parentInfo);
    }
    
    public void addComponent(String role, String clazz, Configuration config, ComponentInfo i) throws ConfigurationException {
        this.info.prepareConfig(role, clazz, config);

        super.addComponent(role, clazz, config, i);
        // Let's ProcessorComponentInfo do its stuff.
        // Note: if more behaviours of this kind are needed, we may setup an
        // event listener mechanism on the core service manager
        this.info.componentAdded(role, clazz, config);
    }

    public void addRoleAlias(String existingRole, String newRole) throws ServiceException {
        super.addRoleAlias(existingRole, newRole);
        this.info.roleAliased(existingRole, newRole);
    }

    public void initialize() throws Exception {
        this.info.lock();
        this.addInstance(ProcessorComponentInfo.ROLE, this.info);
        super.initialize();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.CocoonServiceManager#initialize(java.lang.String, java.lang.Object)
     */
    protected void initialize(String role, Object component) throws ServiceException {
        super.initialize(role, component);
        /*
        if ( null != component && component instanceof SitemapConfigurable) {

            // FIXME: how can we prevent that this is called over and over again?
            SitemapConfigurationHolder holder;

            holder = (SitemapConfigurationHolder)this.sitemapConfigurationHolders.get( role );
            if ( null == holder ) {
                // create new holder
                holder = new DefaultSitemapConfigurationHolder( role, this.roleManager );
                this.sitemapConfigurationHolders.put( role, holder );
            }

            try {
                ((SitemapConfigurable)component).configure(holder);
            } catch (ConfigurationException ce) {
                throw new ServiceException(role, "Exception during setup of SitemapConfigurable.", ce);
            }
        }
        */
    }
}
