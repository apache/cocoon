/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.layout.LayoutRemoveEvent;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutException;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.util.ClassUtils;

/**
 *
 * <h2>Configuration</h2>
 * <table><tbody>
 *  <tr><th>layouts</th>
 *      <td>List of layouts.</td>
 *      <td>req</td>
 *      <td>Configuration</td>
 *      <td><code>null</code></td>
 * </tr>
 * <tr>
 *   <th>layouts/layout</th>
 *   <td>Multiple configured layouts.
 *   </td>
 *   <td>req</td>
 *   <td>Configuration</td>
 *   <td><code>null</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/attribute::name</th>
 *   <td>Unique layout name.</td>
 *   <td>req</td>
 *   <td>String</td>
 *   <td><code>null</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/attribute::create-id</th>
 *   <td></td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/renderers/attribute::default</th>
 *   <td></td>
 *   <td>req</td>
 *   <td>String</td>
 *   <td><code>null</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/renderers/renderer</th>
 *   <td></td>
 *   <td>req</td>
 *   <td>Configuration</td>
 *   <td><code>null</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/renderers/renderer/attribute::name</th>
 *   <td></td>
 *   <td>req</td>
 *   <td>String</td>
 *   <td><code>null</code></td>
 *  </tr>
 * <tr>
 *   <th>layouts/layout/aspects/aspect</th>
 *   <td></td>
 *   <td>req</td>
 *   <td>String</td>
 *   <td><code>null</code></td>
 *  </tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public class DefaultLayoutFactory
	extends AbstractComponent
    implements LayoutFactory, Configurable, Receiver {

    protected Map layouts = new HashMap();

    protected Configuration[] layoutsConf;

    protected static long idCounter = System.currentTimeMillis();

    /** 
     * Configure a layout
     */
    protected void configureLayout(Configuration layoutConf) 
    throws ConfigurationException {
        DefaultLayoutDescription desc = new DefaultLayoutDescription();
        final String name = layoutConf.getAttribute("name");
   
        // unique test
        if ( this.layouts.get(name) != null) {
            throw new ConfigurationException("Layout name must be unique. Double definition for " + name);
        }
        desc.setName(name);
        desc.setClassName(layoutConf.getAttribute("class"));        
        desc.setCreateId(layoutConf.getAttributeAsBoolean("create-id", false));
        desc.setItemClassName(layoutConf.getAttribute("item-class", null));
        desc.setDefaultIsStatic(layoutConf.getAttributeAsBoolean("default-is-static", false));

        // the renderers
        final String defaultRenderer = layoutConf.getChild("renderers").getAttribute("default");
        desc.setDefaultRendererName(defaultRenderer); 
         
        final Configuration[] rendererConfs = layoutConf.getChild("renderers").getChildren("renderer");
        if ( rendererConfs != null ) {
            boolean found = false;
            for(int m=0; m < rendererConfs.length; m++) {
                final String rName = rendererConfs[m].getAttribute("name");
                desc.addRendererName(rName);
                if ( defaultRenderer.equals(rName) ) {
                    found = true;
                }
            }
            if ( !found ) {
                throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + name + "'");
            }
        } else {
            throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + name + "'");
        }

        this.layouts.put(desc.getName(), desc);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        this.layoutsConf = configuration.getChild("layouts").getChildren("layout");
    }

    protected void init() {
        // FIXME when we switch to another container we can remove
        //        the lazy evaluation
        if ( this.layoutsConf != null ) {
            synchronized (this) {
                if ( this.layoutsConf != null ) {
                    for(int i=0; i < layoutsConf.length; i++ ) {
                        try {
                            this.configureLayout( layoutsConf[i] );
                        } catch (ConfigurationException ce) {
                            throw new PortalRuntimeException("Unable to configure layout.", ce);
                        }
                    }
                    this.layoutsConf = null;
                }
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#prepareLayout(org.apache.cocoon.portal.layout.Layout)
     */
    public void prepareLayout(Layout layout)
    throws LayoutException {
        if ( layout != null ) {

            this.init();

            final String layoutName = layout.getName();
            if ( layoutName == null ) {
                throw new LayoutException("Layout '"+layout.getId()+"' has no associated name.");
            }
            DefaultLayoutDescription layoutDescription = (DefaultLayoutDescription)this.layouts.get( layoutName );

            if ( layoutDescription == null ) {
                throw new LayoutException("LayoutDescription with name '" + layoutName + "' not found.");
            }

            layout.setDescription( layoutDescription );

            // recursive
            if ( layout instanceof CompositeLayout ) {
                CompositeLayout composite = (CompositeLayout)layout;

                Iterator items = composite.getItems().iterator();
                while ( items.hasNext() ) {
                    this.prepareLayout( ((Item)items.next()).getLayout() );
                }
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#newInstance(java.lang.String)
     */
    public Layout newInstance(String layoutName) 
    throws LayoutException {
        return this.newInstance(layoutName, null);
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#newInstance(java.lang.String, java.lang.String)
     */
    public Layout newInstance(String layoutName, String id) 
    throws LayoutException {
        this.init();

        DefaultLayoutDescription layoutDescription = (DefaultLayoutDescription)this.layouts.get( layoutName );

        if ( layoutDescription == null ) {
            throw new LayoutException("LayoutDescription with name '" + layoutName + "' not found.");
        }

        if ( layoutDescription.createId() && id == null ) {
            synchronized (this) {
                id = layoutName + '-' + idCounter;
                idCounter += 1;
            }
        }
        Layout layout = null;
        try {
            Class clazz = ClassUtils.loadClass( layoutDescription.getClassName() );
            Constructor constructor = clazz.getConstructor(new Class[] {String.class, String.class});
            layout = (Layout)constructor.newInstance(new Object[] {id, layoutName});
        } catch (Exception e) {
            throw new LayoutException("Unable to create new layout instance for: " + layoutDescription , e );
        }

        layout.setDescription( layoutDescription );

        this.portalService.getProfileManager().register(layout);

        return layout;
    }

    /**
     * @see Receiver
     */
    public void inform(LayoutRemoveEvent event, PortalService service) {
        this.remove( event.getTarget() );
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#remove(org.apache.cocoon.portal.layout.Layout)
     */
    public void remove(Layout layout) {
        if ( layout != null ) {
            this.init();
            if ( layout instanceof CompositeLayout ) {
                final CompositeLayout cl = (CompositeLayout)layout;
                while ( cl.getItems().size() > 0 ) {
                    final Item i = cl.getItem(0);
                    this.remove( i.getLayout() );
                }
            }

            if ( layout instanceof CopletLayout ) {
                CopletFactory factory = this.portalService.getCopletFactory();
                factory.remove( ((CopletLayout)layout).getCopletInstanceData());
            }

            Item parent = layout.getParent();
            if ( parent != null && parent.getParent() != null) {
                parent.getParent().removeItem( parent );
            }

            ProfileManager profileManager = this.portalService.getProfileManager();
            profileManager.unregister(layout);
        }
    }
}
