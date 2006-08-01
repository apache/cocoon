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
package org.apache.cocoon.portal.event.layout;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.impl.CopletLayout;

/**
 * This event can be used to remove a layout object from the profile.
 *
 * @version $Id$
 */
public class RemoveLayoutEvent
    extends AbstractLayoutEvent
    implements ConvertableEvent {

    /**
     * Constructor.
     * @param target
     */
    public RemoveLayoutEvent(Layout target) {
        super(target);
    }

    public RemoveLayoutEvent(PortalService service, String eventData) {
        super(null);
        if ( eventData.charAt(0) == 'L' ) {
            this.target = service.getProfileManager().getPortalLayout(null, eventData.substring(1));
        } else if ( eventData.charAt(0) == 'C' ) {
            final Layout rootLayout = service.getProfileManager().getPortalLayout(null, null);
            this.target = LayoutFeatures.searchLayout(service, eventData.substring(1), rootLayout);            
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.ConvertableEvent#asString()
     */
    public String asString() {
        final Layout l = this.getTarget();
        if ( l.getId() == null ) {
            // if this is a coplet layout we can use the coplet instance id
            if ( l instanceof CopletLayout ) {
                final CopletLayout cl = (CopletLayout)l;
                if ( cl.getCopletInstanceId() != null ) {
                    return 'C' + cl.getCopletInstanceId();
                }
            }
            return null;
        }
        return 'L' + l.getId();
    }
}
