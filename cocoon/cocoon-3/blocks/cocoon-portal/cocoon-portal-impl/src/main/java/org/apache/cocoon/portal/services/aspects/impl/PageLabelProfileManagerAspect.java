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

import java.util.Collection;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.NamedItem;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext;

/**
 * $Id$
 */
public class PageLabelProfileManagerAspect
    implements ProfileManagerAspect, ThreadSafe {

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
            this.populate((CompositeLayout)rootLayout, "");
        }
        context.invokeNext(rootLayout);
    }

    protected void populate(CompositeLayout layout, String name) {
        for (int j = 0; j < layout.getSize(); j++) {
            final Item tab = layout.getItem(j);
            final StringBuffer label = new StringBuffer(name);
            if (label.length() > 0) {
                label.append(".");
            }
            label.append((tab instanceof NamedItem) ? ((NamedItem) tab).getName()
                                                    : Integer.toString(j));
            // TODO
            //layout.setTemporaryAttribute("pageLabel", label.toString());
            final Layout child = tab.getLayout();
            if (child != null && child instanceof CompositeLayout) {
                this.populate((CompositeLayout) child, label.toString());
            }
        }
    }
}
