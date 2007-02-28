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
package org.apache.cocoon.portal.event.layout;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @version $Id$
 */
public class LayoutInstanceChangeAttributeEvent
    extends AbstractLayoutInstanceEvent
    implements ComparableEvent, ConvertableEvent {

    protected String attributeName;
    protected String value;
    protected boolean temporary;

    public LayoutInstanceChangeAttributeEvent(PortalService service, String eventData) {
        super(null);
        final int pos = eventData.indexOf(':');
        if ( pos == -1 ) {
            throw new IllegalArgumentException("Corrupt event data: " + eventData);
        }
        final int pos2 = eventData.indexOf(':', pos+1);
        if ( pos2 == -1 ) {
            throw new IllegalArgumentException("Corrupt event data: " + eventData);
        }
        if ( eventData.charAt(pos+1) != 'T' && eventData.charAt(pos+1) != 'P') {
            throw new IllegalArgumentException("Corrupt event data: " + eventData);
        }
        this.temporary = (eventData.charAt(pos+1) == 'T');
        final String layoutId = eventData.substring(0, pos);
        this.attributeName = eventData.substring(pos+2, pos2);
        this.value= eventData.substring(pos2+1);
        this.target = service.getProfileManager().getLayoutInstance(service.getProfileManager().getLayout(layoutId));
    }

    /**
     * @param target
     */
    public LayoutInstanceChangeAttributeEvent(LayoutInstance target,
                                              String         attribute,
                                              String         newValue,
                                              boolean        temporary) {
        super(target);
        this.attributeName = attribute;
        this.value = newValue;
        this.temporary = temporary;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    /**
     * @see org.apache.cocoon.portal.event.ComparableEvent#equalsEvent(org.apache.cocoon.portal.event.ComparableEvent)
     */
    public boolean equalsEvent(ComparableEvent event) {
        if ( event instanceof LayoutInstanceChangeAttributeEvent ) {
            LayoutInstanceChangeAttributeEvent e = (LayoutInstanceChangeAttributeEvent)event;
            return this.temporary == e.isTemporary()
                   && ObjectUtils.equals(this.getTarget(), e.getTarget())
                   && ObjectUtils.equals(this.getAttributeName(), e.getAttributeName());
        }
        return false;
    }

    /**
     * @see org.apache.cocoon.portal.event.ConvertableEvent#asString()
     */
    public String asString() {
        final LayoutInstance l = this.getTarget();
        if ( l.getId() == null ) {
            return null;
        }
        return l.getId() + ':' + (this.temporary ? 'T' : 'P') + this.attributeName + ':' + this.value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "LayoutInstanceChangeAttributeEvent (" + this.hashCode() + ") : " + this.asString();
    }
}
