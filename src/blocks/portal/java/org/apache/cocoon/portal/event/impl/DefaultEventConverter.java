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
package org.apache.cocoon.portal.event.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultEventConverter.java,v 1.4 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class DefaultEventConverter
    extends AbstractLogEnabled
    implements EventConverter, Serviceable, ThreadSafe {

    protected static final String DECODE_LIST = DefaultEventConverter.class.getName() + "D";
    protected static final String ENCODE_LIST = DefaultEventConverter.class.getName() + "E";
    
    protected ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#encode(org.apache.cocoon.portal.event.Event)
     */
    public String encode(Event event) {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            List list = (List)service.getAttribute(ENCODE_LIST);
            if ( null == list ) {
                list = new ArrayList();
                service.setAttribute(ENCODE_LIST, list);
            }
            int index = list.indexOf(event);
            if ( index == -1 ) {
                list.add(event);
                index = list.size() - 1;
            }
            return String.valueOf(index);
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup component.", ce);            
        } finally {
            this.manager.release(service);
        }
        
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#decode(java.lang.String)
     */
    public Event decode(String value) {
        if (value != null) {
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                List list = (List)service.getAttribute(DECODE_LIST);
                if ( null != list ) {
                    int index = new Integer(value).intValue();
                    if (index < list.size()) {
                        return (Event)list.get(index);
                    }
                }
            } catch (ServiceException ce) {
                throw new CascadingRuntimeException("Unable to lookup component.", ce);            
            } finally {
                this.manager.release(service);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#start()
     */
    public void start() {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            List list = (List)service.getAttribute(ENCODE_LIST);
            if ( null != list ) {
                service.setAttribute(DECODE_LIST, list);
                service.removeAttribute(ENCODE_LIST);
            }
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup component.", ce);            
        } finally {
            this.manager.release(service);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventConverter#finish()
     */
    public void finish() {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.removeAttribute(DECODE_LIST);
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup component.", ce);            
        } finally {
            this.manager.release(service);
        }
    }
}
