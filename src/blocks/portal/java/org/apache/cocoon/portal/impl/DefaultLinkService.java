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
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.RequestEvent;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultLinkService.java,v 1.1 2003/05/07 06:22:26 cziegeler Exp $
 */
public class DefaultLinkService 
    extends AbstractLogEnabled
    implements ThreadSafe, LinkService, Composable, Disposable {

// FIXME - make public and use it in RequestParameter..
    protected static final String REQUEST_EVENT_PARAMETER_NAME = "cocoon-portal-event";

    class Info {
        StringBuffer  linkBase = new StringBuffer();
        boolean      hasParameters = false;
        ArrayList     comparableEvents = new ArrayList(5);
    }
    
    
    protected EventConverter   converter;
    protected ComponentManager manager;
    // FIXME - comparable events are not completly implemented yet
    
    protected Info getInfo() {
        final Map objectModel = CocoonComponentManager.getCurrentEnvironment().getObjectModel();
        Info info = (Info)objectModel.get(DefaultLinkService.class.getName());
        if ( info == null ) {
            info = new Info();
            objectModel.put(DefaultLinkService.class.getName(), info);
        }
        return info;
    }
    
    public String getLinkURI(Event event) {
        final Info info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.linkBase.toString());
        boolean hasParams = info.hasParameters;
        Iterator iter = info.comparableEvents.iterator();
        while (iter.hasNext()) {
            ComparableEvent ce = (ComparableEvent)iter.next();
            if (event instanceof ComparableEvent) {
                if ( !ce.equalsEvent((ComparableEvent)event)) {
                    if ( hasParams ) {
                        buffer.append('&');
                    } else {
                        buffer.append('?');
                    }
                    hasParams = true;
                    String parameterName = REQUEST_EVENT_PARAMETER_NAME;
                    if (ce instanceof RequestEvent) {
                        parameterName = ((RequestEvent)ce).getRequestParameterName();
                    }
                    // FIXME Encode value
                    final String value = this.converter.encode( ce );
                    buffer.append(parameterName).append('=').append(value);
                }
            }
        }
        
        final String value = this.converter.encode( event );
        if ( hasParams ) {
            buffer.append('&');
        } else {
            buffer.append('?');
        }
        String parameterName = REQUEST_EVENT_PARAMETER_NAME;
        if (event instanceof RequestEvent) {
            parameterName = ((RequestEvent)event).getRequestParameterName();
        }
        // FIXME Encode value
        buffer.append(parameterName).append('=').append(value);

        return buffer.toString();
    }

    public String getLinkURI(List events) {
        final Info info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.toString());
        
        Iterator iter = events.iterator();
        boolean hasPars = info.hasParameters;
        while ( iter.hasNext()) {
            final Event current = (Event)iter.next();
            final String value = this.converter.encode( current );
            if ( hasPars ) {
                buffer.append('&');
            } else {
                buffer.append('?');
            }
            hasPars = true;
            String parameterName = REQUEST_EVENT_PARAMETER_NAME;
            if (current instanceof RequestEvent) {
                parameterName = ((RequestEvent)current).getRequestParameterName();
            }
            // FIXME Encode value
            buffer.append(parameterName).append('=').append(value);
        }
        return buffer.toString();
    }

    public void addEventToLink(Event event) {
        final Info info = this.getInfo();
        if ( event instanceof ComparableEvent) {
            info.comparableEvents.add( event );
        } else {
            final String value = converter.encode(event);
            // FIXME - remove hardcoded parameter name
            String parameterName = "frame-event";
            if (event instanceof RequestEvent) {
                parameterName = ((RequestEvent)event).getRequestParameterName();
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
        // FIXME Encode value
        info.linkBase.append(name).append('=').append(value);
        info.hasParameters = true;
    }

    public String getRefreshLinkURI() {
        final Info info = this.getInfo();
        return info.linkBase.toString();
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager)
    throws ComponentException {
        this.manager = manager;
        this.converter = (EventConverter)this.manager.lookup(EventConverter.ROLE);
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

}
