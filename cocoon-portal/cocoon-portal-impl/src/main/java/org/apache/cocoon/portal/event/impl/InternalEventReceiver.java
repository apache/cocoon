/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletDefinition;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.CopletInstanceFeatures;
import org.apache.cocoon.portal.event.CopletDefinitionEvent;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletInstanceRemovedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.event.coplet.CopletJXPathEvent;
import org.apache.cocoon.portal.event.layout.LayoutChangeParameterEvent;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.LayoutFeatures.RenderInfo;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.commons.jxpath.JXPathContext;

/**
 * This receiver processes all events which change an object.
 * All coplet instance data events, jx path events and layout events are processed
 * and the corresponding objects are changed.
 *
 * The receiver is automatically added by the {@link DefaultEventManager}.
 *
 * @version $Id$
 */
public final class InternalEventReceiver 
    implements Receiver {

    public InternalEventReceiver() {
        // nothing to do 
    }

    /**
     * @see Receiver
     */
    public void inform(CopletInstanceEvent event, PortalService service) {
        CopletInstanceFeatures.addChangedCopletInstanceData(service, event.getTarget());
    }

    /**
     * @see Receiver
     */
    public void inform(JXPathEvent event, PortalService service) {
        final Object target = event.getObject();
        if ( target != null && event.getPath() != null && event.getValue() != null) {
            if ( target instanceof CopletInstance && event.getPath().equals("size") ) {
                int newSize = new Integer(event.getValue().toString()).intValue();
                CopletInstanceSizingEvent e = new CopletInstanceSizingEvent((CopletInstance)target, newSize);
                this.inform(e, service);
            } else {
                final JXPathContext jxpathContext = JXPathContext.newContext(target);
                jxpathContext.setValue(event.getPath(), event.getValue());
            }
        }
    }

    /**
     * @see Receiver
     */
    public void inform(CopletInstanceSizingEvent event, PortalService service) {
        final CopletInstance cid = event.getTarget();
        Layout rootLayout = service.getProfileManager().getPortalLayout(null, null);
        if ( cid != null ) {
            final int oldSize = cid.getSize();
            cid.setSize(event.getSize());
            if ( event.getSize() == CopletInstance.SIZE_FULLSCREEN ) {
                CopletLayout layout = LayoutFeatures.searchLayout(service, cid.getId(), rootLayout);
                LayoutFeatures.setFullScreenInfo(rootLayout, layout);
            } else if ( event.getSize() == CopletInstance.SIZE_MAXIMIZED ) {
                CopletLayout layout = LayoutFeatures.searchLayout(service, cid.getId(), rootLayout);
                Item container = LayoutFeatures.searchItemForMaximizedCoplet(layout);
                if ( container != null ) {
                    final RenderInfo info = new RenderInfo(layout, container);
                    LayoutFeatures.setRenderInfo(container.getParent(), info);
                } else {
                	// TODO - Check if this is correct
                    LayoutFeatures.setFullScreenInfo(rootLayout, layout);                	
                }
            }
            if ( oldSize == CopletInstance.SIZE_FULLSCREEN ) {
                LayoutFeatures.setFullScreenInfo(rootLayout, null);
            } else if ( oldSize == CopletInstance.SIZE_MAXIMIZED ) {
                CopletLayout layout = LayoutFeatures.searchLayout(service, cid.getId(), rootLayout);
                Item container = LayoutFeatures.searchItemForMaximizedCoplet(layout);
                if ( container != null ) {
                	LayoutFeatures.setRenderInfo(container.getParent(), null);
                } else {
                	// TODO - Check if this is correct
                	LayoutFeatures.setFullScreenInfo(rootLayout, null);
                }
            }
        } else {
            if ( event.getSize() == CopletInstance.SIZE_FULLSCREEN ) {
                LayoutFeatures.setFullScreenInfo(rootLayout, null);
            }
        }
    }

    /**
     * @see Receiver
     */
    public void inform(CopletInstanceRemovedEvent e, PortalService service) {
        CopletInstance cid = e.getTarget();
        // full screen?
        if ( cid.getSize() == CopletInstance.SIZE_FULLSCREEN ) {
            Layout rootLayout = service.getProfileManager().getPortalLayout(null, null);
            LayoutFeatures.setFullScreenInfo(rootLayout, null);
        } else if ( cid.getSize() == CopletInstance.SIZE_MAXIMIZED ) {
            Layout rootLayout = service.getProfileManager().getPortalLayout(null, null);
            CopletLayout layout = LayoutFeatures.searchLayout(service, cid.getId(), rootLayout);
            Item container = LayoutFeatures.searchItemForMaximizedCoplet(layout);
            if ( container != null ) {
                LayoutFeatures.setRenderInfo(container.getParent(), null);
            }
        }
    }

    /**
     * @see Receiver
     */
    public void inform(CopletDefinitionEvent e, PortalService service) {
        CopletDefinition data = e.getTarget();
        List instances = null;

        ProfileManager profileManager = service.getProfileManager();
        instances = profileManager.getCopletInstances(data);

        if ( instances != null && e instanceof ChangeCopletsJXPathEvent ) {
            EventManager eventManager = service.getEventManager();
            final String path = ((ChangeCopletsJXPathEvent)e).getPath();
            final Object value = ((ChangeCopletsJXPathEvent)e).getValue();
  
            Iterator i = instances.iterator();
            while ( i.hasNext() ) {
                CopletInstance current = (CopletInstance) i.next();
                Event event = new CopletJXPathEvent(current, path, value);
                eventManager.send(event);
            }
        }
    }

    /**
     * @see Receiver
     */
    public void inform(LayoutChangeParameterEvent e, PortalService service) {
        final Layout l = e.getTarget();
        if ( e.isTemporary() ) {
            l.setTemporaryAttribute(e.getParameterName(), e.getValue());
        } else {
            l.setParameter(e.getParameterName(), e.getValue());
        }
    }
}
