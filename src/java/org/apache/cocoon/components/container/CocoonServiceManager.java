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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.core.container.CocoonServiceSelector;

/**
 * Default service manager for Cocoon's components.
 *
 * @version CVS $Revision: 1.6 $Id: CocoonServiceManager.java 55165 2004-10-20 16:51:50Z cziegeler $
 */
public class CocoonServiceManager
extends org.apache.cocoon.core.container.CocoonServiceManager {
    
    /** The {@link SitemapConfigurationHolder}s */
    private Map sitemapConfigurationHolders = new HashMap(15);

    /** Temporary list of parent-aware components.  Will be null for most of
     * our lifecycle. */
    private ArrayList parentAwareComponents = new ArrayList();

    /** Create the ServiceManager with a Classloader and parent ServiceManager */
    public CocoonServiceManager( final ServiceManager parent, 
                                 final ClassLoader loader ) {
        super( parent, loader);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.CocoonServiceManager#initialize(java.lang.String, java.lang.Object)
     */
    protected void initialize(String role, Object component) throws ServiceException {
        super.initialize(role, component);
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
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        super.initialize();

        if (this.parentAwareComponents == null) {
            throw new ServiceException(null, "CocoonServiceManager already initialized");
        }

        // Set parents for parentAware components
        Iterator iter = this.parentAwareComponents.iterator();
        while (iter.hasNext()) {
            String role = (String)iter.next();
            if ( this.parentManager != null && this.parentManager.hasService( role ) ) {
                // lookup new component
                Object component = null;
                try {
                    component = this.lookup( role );
                    ((CocoonServiceSelector)component).setParentLocator( this.parentManager, role );
                } catch (ServiceException ignore) {
                    // we don't set the parent then
                } finally {
                    this.release( component );
                }
            }
        }
        this.parentAwareComponents = null;  // null to save memory, and catch logic bugs.
    }

    /**
     * Add a new component to the manager.
     *
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final Object role,
                              final Class component,
                              final Configuration configuration )
    throws ServiceException {
        super.addComponent( role, component, configuration );
        // Note that at this point, we're not initialized and cannot do
        // lookups, so defer parental introductions to initialize().
        if ( CocoonServiceSelector.class.isAssignableFrom( component ) ) {
            this.parentAwareComponents.add(role);
        }
    }

}
