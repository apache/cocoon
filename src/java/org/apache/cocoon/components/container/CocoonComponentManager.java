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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.ParentAware;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;

/**
 * Cocoon Component Manager.
 * This manager extends the {@link ExcaliburComponentManager}
 * by a special lifecycle handling for a {@link SitemapConfigurable} component.
 * WARNING: This is a "private" Cocoon core class - do NOT use this class
 * directly - and do not assume that a {@link ComponentManager} you get
 * via the compose() method is an instance of CocoonComponentManager.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonComponentManager.java,v 1.2 2004/05/26 01:31:06 joerg Exp $
 */
public final class CocoonComponentManager
extends ExcaliburComponentManager {
 
    /** The {@link SitemapConfigurationHolder}s */
    private Map sitemapConfigurationHolders = new HashMap(15);
    
    /** The parent component manager for implementing parent aware components */
    private ComponentManager parentManager;
    
    /** Temporary list of parent-aware components.  Will be null for most of
     * our lifecycle. */
    private ArrayList parentAwareComponents = new ArrayList();

    /** Create the ComponentManager */
    public CocoonComponentManager() {
        super( null, Thread.currentThread().getContextClassLoader() );
    }

    /** Create the ComponentManager with a Classloader */
    public CocoonComponentManager(final ClassLoader loader) {
        super( null, loader );
    }

    /** Create the ComponentManager with a Classloader and parent ComponentManager */
    public CocoonComponentManager(final ComponentManager manager, final ClassLoader loader) {
        super( manager, loader );
        this.parentManager = manager;
    }

    /** Create the ComponentManager with a parent ComponentManager */
    public CocoonComponentManager(final ComponentManager manager) {
        super( manager);
        this.parentManager = manager;
    }

    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Component lookup( final String role )
    throws ComponentException {
        if( null == role ) {
            final String message =
                "ComponentLocator Attempted to retrieve component with null role.";

            throw new ComponentException( role, message );
        }

        final Component component = super.lookup( role );
        if ( null != component && component instanceof SitemapConfigurable) {

            // FIXME: how can we prevent that this is called over and over again?
            SitemapConfigurationHolder holder;
            
            holder = (SitemapConfigurationHolder)this.sitemapConfigurationHolders.get( role );
            if ( null == holder ) {
                // create new holder
                holder = new DefaultSitemapConfigurationHolder( role );
                this.sitemapConfigurationHolders.put( role, holder );
            }

            try {
                ((SitemapConfigurable)component).configure(holder);
            } catch (ConfigurationException ce) {
                throw new ComponentException(role, "Exception during setup of SitemapConfigurable.", ce);
            }
        }
        return component;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.ExcaliburComponentManager#addComponent(java.lang.String, java.lang.Class, org.apache.avalon.framework.configuration.Configuration)
     */
    public void addComponent(String role, Class clazz, Configuration conf)
        throws ComponentException {
        super.addComponent(role, clazz, conf);
        // Note that at this point, we're not initialized and cannot do
        // lookups, so defer parental introductions to initialize().
        if ( ParentAware.class.isAssignableFrom( clazz ) ) {
            parentAwareComponents.add(role);
        }
    }

    public void initialize()
        throws Exception
    {
        super.initialize();
        if (parentAwareComponents == null) {
            throw new ComponentException(null, "CocoonComponentManager already initialized");
        }
        // Set parents for parentAware components
        Iterator iter = parentAwareComponents.iterator();
        while (iter.hasNext()) {
            String role = (String)iter.next();
            getLogger().debug(".. "+role);
            if ( parentManager != null && parentManager.hasComponent( role ) ) {
                // lookup new component
                Component component = null;
                try {
                    component = this.lookup( role );
                    ((ParentAware)component).setParentLocator( new ComponentLocatorImpl(this.parentManager, role ));
                } catch (ComponentException ignore) {
                    // we don't set the parent then
                } finally {
                    this.release( component );
                }
            }
        }
        parentAwareComponents = null;  // null to save memory, and catch logic bugs.
    }
}

