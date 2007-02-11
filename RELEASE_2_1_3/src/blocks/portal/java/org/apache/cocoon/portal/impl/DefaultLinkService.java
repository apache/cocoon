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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.RequestEvent;
import org.apache.excalibur.source.SourceUtil;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultLinkService.java,v 1.7 2003/10/20 13:36:56 cziegeler Exp $
 */
public class DefaultLinkService 
    extends AbstractLogEnabled
    implements ThreadSafe, LinkService, Serviceable, Disposable, Contextualizable {


    class Info {
        StringBuffer  linkBase = new StringBuffer();
        boolean      hasParameters = false;
        ArrayList     comparableEvents = new ArrayList(5);
    }
    
    
    protected EventConverter   converter;
    protected ServiceManager manager;
    protected Context context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.converter = (EventConverter)this.manager.lookup(EventConverter.ROLE);
    }

    protected Info getInfo() {
        final Request request = ContextHelper.getRequest( this.context );
        Info info = (Info)request.getAttribute(DefaultLinkService.class.getName());
        if ( info == null ) {
            synchronized ( this ) {
                info = (Info)request.getAttribute(DefaultLinkService.class.getName());
                if ( info == null ) {
                    info = new Info();
                    request.setAttribute(DefaultLinkService.class.getName(), info);
                    String baseURI = request.getSitemapURI();
                    final int pos = baseURI.lastIndexOf('/');
                    if ( pos != -1 ) {
                        baseURI = baseURI.substring(pos+1);
                    }
                    info.linkBase.append(baseURI);
                }
            }
        }
        return info;
    }
    
    public String getLinkURI(Event event) {
        final Info info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.linkBase.toString());
        boolean hasParams = info.hasParameters;

        // add comparable events
        Iterator iter = info.comparableEvents.iterator();
        while (iter.hasNext()) {
            ComparableEvent current = (ComparableEvent)iter.next();
            if (!(event instanceof ComparableEvent)
                 || ( !current.equalsEvent((ComparableEvent)event))) {

                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }
        
        // now add event
        hasParams = this.addEvent(buffer, event, hasParams);

        return buffer.toString();
    }

    protected boolean addEvent(StringBuffer buffer, Event event, boolean hasParams) {
        if ( hasParams ) {
            buffer.append('&');
        } else {
            buffer.append('?');
        }
        String parameterName = DEFAULT_REQUEST_EVENT_PARAMETER_NAME;
        if (event instanceof RequestEvent ) {
            final String eventParName = ((RequestEvent)event).getRequestParameterName();
            if ( eventParName != null ) {
                parameterName = eventParName;
            }
        }
        final String value = this.converter.encode( event );
        buffer.append(parameterName).append('=').append(SourceUtil.encode(value));
        return true;
    }
    
    public String getLinkURI(List events) {
        final Info info = this.getInfo();
        boolean hasParams = info.hasParameters;
        final StringBuffer buffer = new StringBuffer(info.toString());
        
        // add comparable events
        Iterator iter = info.comparableEvents.iterator();
        while (iter.hasNext()) {
            ComparableEvent current = (ComparableEvent)iter.next();
            
            Iterator eventIterator = events.iterator();
            boolean found = false;
            while (!found && eventIterator.hasNext()) {
                Event inEvent = (Event)eventIterator.next();
                if ( inEvent instanceof ComparableEvent
                     && current.equalsEvent((ComparableEvent)inEvent)) {
                     found = true;
                }
            }
            if (!found) {
                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }

        // now add events
        iter = events.iterator();
        while ( iter.hasNext()) {
            final Event current = (Event)iter.next();
            hasParams = this.addEvent(buffer, current, hasParams);
        }
        return buffer.toString();
    }

    public void addEventToLink(Event event) {
        final Info info = this.getInfo();
        if ( event instanceof ComparableEvent) {
            info.comparableEvents.add( event );
        } else {
            final String value = converter.encode(event);
            String parameterName = LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME;
            if (event instanceof RequestEvent) {
                final String eventParName = ((RequestEvent)event).getRequestParameterName();
                if ( eventParName != null ) {
                    parameterName = eventParName;
                }
            }
            this.addParameterToLink(parameterName, value);
        }
    }

    public void addParameterToLink(String name, String value) {
        final Info info = this.getInfo();
        if ( info.hasParameters ) {
            info.linkBase.append('&');
        } else {
            info.linkBase.append('?');
        }
        info.linkBase.append(name).append('=').append(SourceUtil.encode(value));
        info.hasParameters = true;
    }

    public String getRefreshLinkURI() {
        final Info info = this.getInfo();
        return info.linkBase.toString();
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release( this.converter );
            this.converter = null;
            this.manager = null;
        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}
