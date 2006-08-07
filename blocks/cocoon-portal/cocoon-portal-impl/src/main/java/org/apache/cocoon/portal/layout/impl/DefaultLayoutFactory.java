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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.layout.LayoutAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutRemovedEvent;
import org.apache.cocoon.portal.event.layout.RemoveLayoutEvent;
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
 *   <th>layouts/layout/attribute::type</th>
 *   <td>Unique layout type.</td>
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

    /** All configured layouts. */
    protected final Map layouts = new HashMap();

    protected static long idCounter = System.currentTimeMillis();

    /** 
     * Configure a layout
     */
    protected void configureLayout(Configuration layoutConf) 
    throws ConfigurationException {
        LayoutDescription desc = new LayoutDescription();
        final String type = layoutConf.getAttribute("type");
   
        // unique test
        if ( this.layouts.get(type) != null) {
            throw new ConfigurationException("Layout type must be unique. Double definition for " + type);
        }
        desc.setType(type);
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
                throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + type + "'");
            }
        } else {
            throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + type + "'");
        }
        this.layouts.put(desc.getType(), desc);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        final Configuration[] layoutsConf = configuration.getChild("layouts").getChildren("layout");
        for(int i=0; i < layoutsConf.length; i++ ) {
            try {
                this.configureLayout( layoutsConf[i] );
            } catch (ConfigurationException ce) {
                throw new PortalRuntimeException("Unable to configure layout.", ce);
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#newInstance(java.lang.String)
     */
    public Layout newInstance(String layoutType) 
    throws LayoutException {
        return this.newInstance(layoutType, null);
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#newInstance(java.lang.String, java.lang.String)
     */
    public Layout newInstance(String layoutType, String id) 
    throws LayoutException {
        LayoutDescription layoutDescription = (LayoutDescription)this.layouts.get( layoutType );

        if ( layoutDescription == null ) {
            throw new LayoutException("LayoutDescription for type '" + layoutType + "' not found.");
        }

        String layoutId = id;
        if ( layoutDescription.createId() && layoutId == null ) {
            synchronized (this) {
                layoutId = layoutType + '_' + idCounter;
                idCounter += 1;
            }
        }
        Layout layout = null;
        try {
            Class clazz = ClassUtils.loadClass( layoutDescription.getClassName() );
            Constructor constructor = clazz.getConstructor(new Class[] {String.class, String.class});
            layout = (Layout)constructor.newInstance(new Object[] {layoutId, layoutType});
        } catch (Exception e) {
            throw new LayoutException("Unable to create new layout instance for: " + layoutDescription , e );
        }

        layout.setIsStatic( layoutDescription.defaultIsStatic() );

        this.portalService.getEventManager().send(new LayoutAddedEvent(layout));

        return layout;
    }

    /**
     * @see Receiver
     */
    public void inform(RemoveLayoutEvent event, PortalService service) {
        this.remove( event.getTarget() );
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#remove(org.apache.cocoon.portal.layout.Layout)
     */
    public void remove(Layout layout) {
        if ( layout != null ) {
            if ( layout instanceof CompositeLayout ) {
                final CompositeLayout cl = (CompositeLayout)layout;
                while ( cl.getItems().size() > 0 ) {
                    final Item i = cl.getItem(0);
                    this.remove( i.getLayout() );
                }
            }
            final ProfileManager profileManager = this.portalService.getProfileManager();

            if ( layout instanceof CopletLayout ) {
                final CopletFactory copletFactory = this.portalService.getCopletFactory();
                final String copletId = ((CopletLayout)layout).getCopletInstanceId();
                if ( copletId != null ) {
                    final CopletInstance instance = profileManager.getCopletInstance(copletId);
                    copletFactory.remove( instance );
                }
            }

            Item parent = layout.getParent();
            if ( parent != null && parent.getParent() != null) {
                parent.getParent().removeItem( parent );
            }

            this.portalService.getEventManager().send(new LayoutRemovedEvent(layout));
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#getRendererName(org.apache.cocoon.portal.layout.Layout)
     */
    public String getRendererName(Layout layout) {
        if ( layout != null ) {
            if ( layout.getRendererName() != null ) {
                return layout.getRendererName();
            }
            LayoutDescription description = (LayoutDescription) this.layouts.get(layout.getType());
            return description.getDefaultRendererName();
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#getLayoutTypes()
     */
    public Collection getLayoutTypes() {
        return this.layouts.keySet();
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#getRendererNames(java.lang.String)
     */
    public Collection getRendererNames(String type) {
        LayoutDescription desc = (LayoutDescription) this.layouts.get(type);
        if ( desc == null ) {
            return Collections.EMPTY_LIST;
        }
        return desc.getRendererNames();
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutFactory#createItem(org.apache.cocoon.portal.layout.Layout)
     */
    public Item createItem(Layout layout)
    throws LayoutException {
        LayoutDescription desc = (LayoutDescription) this.layouts.get(layout.getType());
        if ( desc == null ) {
            throw new LayoutException("Description not found for layout " + layout);
        }
        if ( desc.getItemClassName() == null ) {
            return new Item();
        }
        try {
            return (Item) ClassUtils.newInstance(desc.getItemClassName());
        } catch (Exception e ) {
            throw new LayoutException("Unable to create new item for layout " + layout, e);
        }
    }
}
