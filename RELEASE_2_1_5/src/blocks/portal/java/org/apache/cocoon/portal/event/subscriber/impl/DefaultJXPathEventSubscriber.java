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

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.commons.jxpath.JXPathContext;

/**
 * This subscriber processes JXPath events
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultJXPathEventSubscriber.java,v 1.3 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public final class DefaultJXPathEventSubscriber 
    implements Subscriber {

    public DefaultJXPathEventSubscriber() {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return JXPathEvent.class;
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
        final JXPathEvent event = (JXPathEvent)e;
        final Object target = event.getTarget();
        if ( target != null ) {
            final JXPathContext jxpathContext = JXPathContext.newContext(target);
            jxpathContext.setValue(event.getPath(), event.getValue());
        }
    }

}
