/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.services.impl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.layout.LayoutAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutInstanceAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutRemovedEvent;
import org.apache.cocoon.portal.event.layout.RemoveLayoutEvent;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.LayoutType;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.services.ProfileManager;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * Default implementation of the layout factory.
 *
 * @version $Id$
 */
public class DefaultLayoutFactory
	extends AbstractBean
    implements LayoutFactory, Receiver {

    /** All configured layout types. */
    protected Map layoutTypes = Collections.EMPTY_MAP;

    protected static long idCounter = System.currentTimeMillis();

    public void setLayoutTypesMap(final Map typesMap) {
        if ( typesMap != null ) {
            this.layoutTypes = typesMap;
        } else {
            this.layoutTypes = Collections.EMPTY_MAP;
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.LayoutFactory#newInstance(java.lang.String)
     */
    public Layout newInstance(String layoutTypeName)
    throws LayoutException {
        return this.newInstance(layoutTypeName, null);
    }

    /**
     * @see org.apache.cocoon.portal.services.LayoutFactory#newInstance(java.lang.String, java.lang.String)
     */
    public Layout newInstance(String layoutTypeName, String id)
    throws LayoutException {
        final LayoutType layoutType = (LayoutType)this.layoutTypes.get( layoutTypeName );

        if ( layoutType == null ) {
            throw new LayoutException("Layout type '" + layoutTypeName + "' not found.");
        }

        String layoutId = id;
        if ( layoutType.createId() && layoutId == null ) {
            synchronized (this) {
                layoutId = layoutTypeName + '_' + idCounter;
                idCounter += 1;
            }
        }
        Layout layout = null;
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass( layoutType.getLayoutClassName() );
            Constructor constructor = clazz.getConstructor(new Class[] {String.class});
            layout = (Layout)constructor.newInstance(new Object[] {layoutId});
            layout.setLayoutType(layoutType);
        } catch (Exception e) {
            throw new LayoutException("Unable to create new layout instance for: " + layoutType , e );
        }

        layout.setIsStatic( layoutType.defaultIsStatic() );

        this.portalService.getEventManager().send(new LayoutAddedEvent(layout));

        return layout;
    }

    /**
     * Inform this bean about a remove layout event.
     * @param event The remove layout event
     * @see Receiver
     */
    public void inform(RemoveLayoutEvent event) {
        this.remove( event.getTarget() );
    }

    /**
     * @see org.apache.cocoon.portal.services.LayoutFactory#remove(org.apache.cocoon.portal.om.Layout)
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
     * @see org.apache.cocoon.portal.services.LayoutFactory#getLayoutTypes()
     */
    public Collection getLayoutTypes() {
        return this.layoutTypes.values();
    }

    /**
     * @see org.apache.cocoon.portal.services.LayoutFactory#newInstance(org.apache.cocoon.portal.om.Layout)
     */
    public LayoutInstance newInstance(Layout layout) {
        final LayoutInstance instance = new LayoutInstance(layout);
        this.portalService.getEventManager().send(new LayoutInstanceAddedEvent(instance));
        return instance;
    }

}
