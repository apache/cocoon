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
 * @version CVS $Id: DefaultEventConverter.java,v 1.3 2003/12/08 14:24:14 cziegeler Exp $
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
