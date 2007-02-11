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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;

/**
 * This aspect creates an event and subscribes to it as well
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractActionAspect.java,v 1.2 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public abstract class AbstractActionAspect
    extends AbstractAspect
    implements Subscriber, Disposable, Initializable {

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try { 
                eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
                eventManager.getRegister().unsubscribe( this );
            } catch (Exception ignore) {
            } finally {
                this.manager.release( eventManager ); 
            }
            
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        EventManager eventManager = null;
        try { 
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.getRegister().subscribe( this );
        } finally {
            this.manager.release( eventManager );
        }
    }

}
