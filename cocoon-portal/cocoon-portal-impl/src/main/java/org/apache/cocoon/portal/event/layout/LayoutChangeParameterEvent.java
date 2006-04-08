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
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @version $Id$
 */
public class LayoutChangeParameterEvent
    extends AbstractLayoutEvent
    implements ComparableEvent, ConvertableEvent {

    protected String parameterName;
    protected String value;
    protected boolean temporary;

    public LayoutChangeParameterEvent(PortalService service, String eventData) {
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
        final String layoutId = eventData.substring(0, pos);
        this.temporary = (eventData.charAt(pos+1) == 'T');
        this.parameterName = eventData.substring(pos+2, pos2);
        this.value= eventData.substring(pos2+1);
        this.target = service.getProfileManager().getPortalLayout(null, layoutId);
    }

    /**
     * @param target
     */
    public LayoutChangeParameterEvent(Layout target,
                                      String parameterName,
                                      String value) {
        this(target, parameterName, value, false);
    }

    public LayoutChangeParameterEvent(Layout target,
                                      String parameterName,
                                      String value,
                                      boolean temporary) {
        super(target);
        this.parameterName = parameterName;
        this.value = value;
        this.temporary = temporary;
    }

    public String getParameterName() {
        return this.parameterName;
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
        if ( event instanceof LayoutChangeParameterEvent ) {
            LayoutChangeParameterEvent e = (LayoutChangeParameterEvent)event;
            return this.temporary == e.temporary
                   && ObjectUtils.equals(this.getTarget(), e.getTarget())
                   && ObjectUtils.equals(this.getParameterName(), e.getParameterName());
        }
        return false;
    }

    /**
     * @see org.apache.cocoon.portal.event.ConvertableEvent#asString()
     */
    public String asString() {
        final Layout l = this.getTarget();
        if ( l.getId() == null ) {
            return null;
        }
        return l.getId() + ':' + (this.temporary ? 'T' : 'P') + this.parameterName + ':' + this.value;
    }

}
