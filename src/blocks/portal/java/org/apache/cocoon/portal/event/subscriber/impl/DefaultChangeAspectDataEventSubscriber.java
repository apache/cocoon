/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.subscriber.impl;

import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultChangeAspectDataEventSubscriber.java,v 1.6 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public final class DefaultChangeAspectDataEventSubscriber 
    implements Subscriber {

    public DefaultChangeAspectDataEventSubscriber() {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return ChangeAspectDataEvent.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        final ChangeAspectDataEvent event = (ChangeAspectDataEvent)e;
        final Aspectalizable target = event.getAspectalizable();
        target.setAspectData(event.getAspectName(), event.getData());
    }

}
