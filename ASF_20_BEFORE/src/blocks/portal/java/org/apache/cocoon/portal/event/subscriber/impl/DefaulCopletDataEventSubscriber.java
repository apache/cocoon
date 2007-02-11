/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.portal.event.subscriber.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletDataEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.impl.ChangeCopletsJXPathEvent;
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaulCopletDataEventSubscriber.java,v 1.1 2004/02/12 09:33:29 cziegeler Exp $
 */
public final class DefaulCopletDataEventSubscriber 
implements Subscriber, Serviceable {

    /** The service manager */
    protected ServiceManager manager;
    
    /**
     * Constructor
     */
    public DefaulCopletDataEventSubscriber() {
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return CopletDataEvent.class;
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
        CopletData data = (CopletData) ((CopletDataEvent)e).getTarget();
        PortalService service = null;
        List instances = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            ProfileManager profileManager = service.getComponentManager().getProfileManager();
            instances = profileManager.getCopletInstanceData(data);
        } catch (Exception ignore) {
        } finally {
            this.manager.release(service);
        }
        if ( instances != null ) {
            Publisher publisher = null;
            EventManager eventManager = null;
            try {
                eventManager = (EventManager) this.manager.lookup(EventManager.ROLE);
                publisher = eventManager.getPublisher();
            } catch (Exception ignore) {
            } finally {
                this.manager.release(eventManager);
            }

            if ( publisher != null ) {
                if ( e instanceof ChangeCopletsJXPathEvent ) {
                    final String path = ((ChangeCopletsJXPathEvent)e).getPath();
                    final Object value = ((ChangeCopletsJXPathEvent)e).getValue();
                    
                    Iterator i = instances.iterator();
                    while ( i.hasNext() ) {
                        CopletInstanceData current = (CopletInstanceData) i.next();
                        Event event = new CopletJXPathEvent(current, path, value);
                        publisher.publish(event);
                    }
                }
            }
        }
    }

}
