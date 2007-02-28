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
package org.apache.cocoon.portal.services.aspects.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.layout.ChangeTabEvent;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.om.NamedItem;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * $Id$
 */
public class PageLabelProfileManagerAspect
    extends AbstractBean
    implements ProfileManagerAspect {

    protected boolean stickyTabs = false;

    /**
     * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspect#prepareCopletDefinitions(org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext, java.util.Collection)
     */
    public void prepareCopletDefinitions(ProfileManagerAspectContext context, Collection copletDefinitions) {
        context.invokeNext(copletDefinitions);
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspect#prepareCopletInstances(org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext, java.util.Collection)
     */
    public void prepareCopletInstances(ProfileManagerAspectContext context, Collection copletInstances) {
        context.invokeNext(copletInstances);
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspect#prepareCopletTypes(org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext, java.util.Collection)
     */
    public void prepareCopletTypes(ProfileManagerAspectContext context, Collection copletTypes) {
        context.invokeNext(copletTypes);
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspect#prepareLayout(org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext, org.apache.cocoon.portal.om.Layout)
     */
    public void prepareLayout(ProfileManagerAspectContext context, Layout rootLayout) {
        if ( rootLayout instanceof CompositeLayout ) {
            final Map pageLabelMap = new HashMap();
            this.populate(context.getPortalService(), (CompositeLayout)rootLayout, pageLabelMap, "", null);
            context.getPortalService().getUserService().setAttribute("pageLabelMap", pageLabelMap);
        }
        context.invokeNext(rootLayout);
    }

    private List populate(PortalService service, CompositeLayout layout, Map map, String name, List parentEvents) {
        List lhList = null;
        for (int j = 0; j < layout.getSize(); j++) {
            final Item tab = layout.getItem(j);
            final PageLabelEventInfo event = new PageLabelEventInfo(layout, tab);
            final StringBuffer label = new StringBuffer(name);
            if (label.length() > 0) {
                label.append(".");
            }
            label.append((tab instanceof NamedItem) ? ((NamedItem) tab).getName()
                                                    : Integer.toString(j));
            try {
                tab.setTemporaryAttribute("pageLabel", URLEncoder.encode(label.toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                // this can never happen
            }

            final List events = (parentEvents != null ? new ArrayList(parentEvents) : new ArrayList());
            events.add(event);

            Layout child = tab.getLayout();
            List allEvents = null;
            if (child != null && child instanceof CompositeLayout) {
                allEvents = populate(service, (CompositeLayout) child, map, label.toString(), events);
            }
            if ( !this.stickyTabs ) {
                // With non-sticky tabs the non-leaf nodes always display
                // the left-most child tabs
                if (lhList == null) {
                    if (allEvents != null) {
                        lhList = allEvents;
                    } else {
                        lhList = events;
                    }
                }
                if (allEvents != null) {
                    map.put(label.toString(), allEvents);
                } else {
                    map.put(label.toString(), events);
                }
            } else {
                map.put(label.toString(), events);
            }
        }
        return lhList;
    }

    public static final class PageLabelEventInfo {

        protected final CompositeLayout layout;
        protected final Item            item;

        public PageLabelEventInfo(final CompositeLayout l, final Item i) {
            this.layout = l;
            this.item = i;
        }

        public ChangeTabEvent createEvent(PortalService service) {
            return new ChangeTabEvent(LayoutFeatures.getLayoutInstance(service, this.layout, true), this.item, false);
        }
    }
}
