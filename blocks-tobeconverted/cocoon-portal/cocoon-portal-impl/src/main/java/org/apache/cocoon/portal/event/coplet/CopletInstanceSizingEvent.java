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
package org.apache.cocoon.portal.event.coplet;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.CopletInstanceEvent;

/**
 * This event is fired for resizing a coplet.
 *
 * @version $Id$
 */
public class CopletInstanceSizingEvent
    extends AbstractCopletInstanceEvent
    implements ComparableEvent, ConvertableEvent {

    protected int size;

    public CopletInstanceSizingEvent(PortalService service, String eventData) {
        super(null);
        final int pos = eventData.indexOf(':');
        if ( pos == -1 ) {
            throw new IllegalArgumentException("Corrupt event data: " + eventData);
        }
        final String cid = eventData.substring(0, pos);
        this.size = new Integer(eventData.substring(pos+1)).intValue();
        this.target = service.getProfileManager().getCopletInstanceData(cid);
    }

    public CopletInstanceSizingEvent(CopletInstanceData target, int size) {
        super(target);
        if ( size < CopletInstanceData.SIZE_MINIMIZED ||
             size > CopletInstanceData.SIZE_FULLSCREEN ) {
            throw new IllegalArgumentException("Unknown size for coplet: " + size);
        }
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    /**
     * @see org.apache.cocoon.portal.event.ComparableEvent#equalsEvent(org.apache.cocoon.portal.event.ComparableEvent)
     */
    public boolean equalsEvent(ComparableEvent event) {
        if ( event instanceof CopletInstanceEvent
             && CopletInstanceDataFeatures.isSizingEvent((CopletInstanceEvent)event) ) {
            if ( this.getTarget().equals( ((CopletInstanceEvent)event).getTarget()) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.apache.cocoon.portal.event.ConvertableEvent#asString()
     */
    public String asString() {
        return this.getTarget().getId() + ':' + this.getSize();
    }
}
