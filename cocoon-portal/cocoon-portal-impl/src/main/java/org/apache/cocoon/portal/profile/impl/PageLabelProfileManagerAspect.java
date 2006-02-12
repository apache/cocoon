/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.profile.ProfileManagerAspect;
import org.apache.cocoon.portal.profile.ProfileManagerAspectContext;
import org.apache.cocoon.portal.scratchpad.Profile;

/**
 * $Id$
 */
public class PageLabelProfileManagerAspect
    implements ProfileManagerAspect, ThreadSafe {

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManagerAspect#prepare(org.apache.cocoon.portal.profile.ProfileManagerAspectContext, org.apache.cocoon.portal.scratchpad.Profile)
     */
    public void prepare(ProfileManagerAspectContext context, Profile profile) {
        final Layout rootLayout = profile.getRootLayout();
        if ( rootLayout instanceof CompositeLayout ) {
            this.populate((CompositeLayout)rootLayout, "");
        }
        context.invokeNext(profile);
    }

    private void populate(CompositeLayout layout, String name) {
        for (int j = 0; j < layout.getSize(); j++) {
            final Item tab = layout.getItem(j);
            final StringBuffer label = new StringBuffer(name);
            if (label.length() > 0) {
                label.append(".");
            }
            label.append((tab instanceof NamedItem) ? ((NamedItem) tab).getName()
                                                    : Integer.toString(j));
            layout.setTemporaryAttribute("pageLabel", label.toString());
            final Layout child = tab.getLayout();
            if (child != null && child instanceof CompositeLayout) {
                this.populate((CompositeLayout) child, label.toString());
            }
        }
    }
}
