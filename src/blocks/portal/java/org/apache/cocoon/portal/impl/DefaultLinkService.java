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
package org.apache.cocoon.portal.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.RequestEvent;
import org.apache.cocoon.util.NetUtils;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version $Id$
 */
public class DefaultLinkService 
    extends AbstractLogEnabled
    implements LinkService,
               ThreadSafe,
               Serviceable,
               Disposable,
               Contextualizable,
               Parameterizable {
    
    /** The converter used to convert an event into a request parameter */
    protected EventConverter   converter;
    /** The service manager */
    protected ServiceManager manager;
    /** The cocoon context */
    protected Context context;

    protected Boolean eventsMarshalled;

    /** Default port used for http. */
    protected int defaultPort = 80;
    /** Default port used for https. */
    protected int defaultSecurePort = 443;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.converter = (EventConverter)this.manager.lookup(EventConverter.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.defaultPort = params.getParameterAsInteger("defaultPort", this.defaultPort);
        this.defaultSecurePort = params.getParameterAsInteger("defaultSecurePort", this.defaultSecurePort);
    }

    /**
     * Return the current info for the request.
     * @return A LinkInfo object.
     */
    protected LinkInfo getInfo() {
        final Request request = ContextHelper.getRequest( this.context );
        LinkInfo info = (LinkInfo)request.getAttribute(DefaultLinkService.class.getName());
        if ( info == null ) {
            synchronized ( this ) {
                info = (LinkInfo)request.getAttribute(DefaultLinkService.class.getName());
                if ( info == null ) {
                    info = new LinkInfo(request, this.defaultPort, this.defaultSecurePort);
                    request.setAttribute(DefaultLinkService.class.getName(), info);
                }
            }
        }
        return info;
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#isSecure()
     */
    public boolean isSecure() {
        return ContextHelper.getRequest(this.context).isSecure();
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#encodeURL(String url)
     */
    public String encodeURL(String url) {
        return ContextHelper.getResponse(this.context).encodeURL(url);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event)
     */
    public String getLinkURI(Event event) {
        return this.getLinkURI(event, null);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event, Boolean)
     */
    public String getLinkURI(Event event, Boolean secure) {
        if (event == null) {
            return this.getRefreshLinkURI(secure);
        }
        final LinkInfo info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(initBuffer(info, event, secure));
        boolean hasParams = info.hasParameters();

        // add comparable events
        final boolean comparableEvent = event instanceof ComparableEvent;
        Iterator iter = info.comparableEvents.iterator();
        while (iter.hasNext()) {
            Object[] objects = (Object[]) iter.next();
            ComparableEvent current = (ComparableEvent) objects[0];
            if (!comparableEvent || !current.equalsEvent((ComparableEvent) event)) {
                if (hasParams) {
                    buffer.append('&');
                } else {
                    buffer.append('?');
                }
                try {
                    buffer.append((String) objects[1]).append('=').append(NetUtils.encode((String) objects[2], "utf-8"));
                } catch (UnsupportedEncodingException uee) {
                    // ignore this as utf-8 is always supported
                }
                hasParams = true;
            }
        }

        // now add event
        hasParams = this.addEvent(buffer, event, hasParams);

        return buffer.toString();
    }

    protected String initBuffer(LinkInfo info, Event event, Boolean secure)
    {
        return info.getBase(secure);
    }

    /**
     * Add one event to the buffer
     * @return Returns true, if the link contains a parameter
     */
    protected boolean addEvent(StringBuffer buffer, Event event, boolean hasParams) {
        if ( hasParams ) {
            buffer.append('&');
        } else {
            buffer.append('?');
        }
        StringBuffer value = new StringBuffer("");
        String parameterName = processEvent(event, value);
        try {
            buffer.append(parameterName).append('=').append(NetUtils.encode(value.toString(), "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        return true;
    }
    
    /**
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events) {
        return this.getLinkURI(events, null);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events, Boolean secure) {
        if (events == null || events.size() == 0) {
            return this.getRefreshLinkURI(secure);
        }
        final LinkInfo info = this.getInfo();
        boolean hasParams = info.hasParameters();
        final StringBuffer buffer = new StringBuffer(initBuffer(info, events, secure));

        // add comparable events
        Iterator iter = info.comparableEvents.iterator();
        while (iter.hasNext()) {
            Object[] objects = (Object[]) iter.next();
            ComparableEvent current = (ComparableEvent) objects[0];

            Iterator eventIterator = events.iterator();
            boolean found = false;
            while (!found && eventIterator.hasNext()) {
                final Object inEvent = eventIterator.next();
                if (inEvent instanceof ComparableEvent
                    && current.equalsEvent((ComparableEvent) inEvent)) {
                    found = true;
                }
            }
            if (!found) {
                if (hasParams) {
                    buffer.append('&');
                } else {
                    buffer.append('?');
                }
                try {
                    buffer.append((String) objects[1]).append('=').append(NetUtils.encode((String) objects[2], "utf-8"));
                } catch (UnsupportedEncodingException uee) {
                    // ignore this as utf-8 is always supported
                }
                hasParams = true;
            }
        }

        // now add events
        iter = events.iterator();
        while (iter.hasNext()) {
            final Object current = iter.next();
            if (current instanceof Event) {
                hasParams = this.addEvent(buffer, (Event) current, hasParams);
            } else if ( current instanceof ParameterDescription ) {
                if (hasParams) {
                    buffer.append('&');
                } else {
                    buffer.append('?');
                    hasParams = true;
                }
                buffer.append(((ParameterDescription) current).parameters);
            }
        }
        return buffer.toString();
    }

    protected String initBuffer(LinkInfo info, List events, Boolean secure) {
        return info.getBase(secure);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#addEventToLink(org.apache.cocoon.portal.event.Event)
     */
    public void addEventToLink(Event event) {
        if (event == null) {
            return;
        }
        StringBuffer value = new StringBuffer("");
        String parameterName = processEvent(event, value);

        final LinkInfo info = this.getInfo();
        if (event instanceof ComparableEvent) {
            // search if we already have an event for this!
            final Iterator iter = info.comparableEvents.iterator();
            boolean found = false;
            while (!found && iter.hasNext()) {
                Object[] objects = (Object[])iter.next();
                if (((ComparableEvent) objects[0]).equalsEvent((ComparableEvent) event)) {
                    found = true;
                    info.comparableEvents.remove(objects);
                }
            }
            info.comparableEvents.add(new Object[]{event, parameterName, value.toString()});
        } else {
            this.addParameterToLink(parameterName, value.toString());
        }
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#addParameterToLink(java.lang.String, java.lang.String)
     */
    public void addParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.addParameterToBase(name, value);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#addUniqueParameterToLink(java.lang.String, java.lang.String)
     */
    public void addUniqueParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.deleteParameterFromBase(name);
        this.addParameterToLink(name, value);
    }
    
    /**
     * @see org.apache.cocoon.portal.LinkService#getRefreshLinkURI()
     */
    public String getRefreshLinkURI() {
        return this.getRefreshLinkURI(null);
    }

    /**
     * @see org.apache.cocoon.portal.LinkService#getRefreshLinkURI(java.lang.Boolean)
     */
    public String getRefreshLinkURI(Boolean secure) {
        final LinkInfo info = this.getInfo();

        final StringBuffer buffer = new StringBuffer(info.getBase(secure));

        // add comparable events
        Iterator iter = info.comparableEvents.iterator();
        boolean hasParams = info.hasParameters();
        while (iter.hasNext()) {
            Object[] objects = (Object[])iter.next();
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
            }
            try {
                buffer.append((String)objects[1]).append('=').append(NetUtils.encode((String)objects[2], "utf-8"));
            } catch (UnsupportedEncodingException uee) {
                // ignore this as utf-8 is always supported
            }
            hasParams = true;
        }
        return buffer.toString();
    }
    
    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release( this.converter );
            this.converter = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    private boolean getEventsMarshalled() {
        if (this.eventsMarshalled == null) {
            this.eventsMarshalled = new Boolean(this.converter.isMarshallEvents());
        }
        return this.eventsMarshalled.booleanValue();
    }

    protected String processEvent(Event event, StringBuffer value) {
        String parameterName = DEFAULT_REQUEST_EVENT_PARAMETER_NAME;
        if (event instanceof ConvertableEvent && getEventsMarshalled()) {
            final String eventParName = ((ConvertableEvent) event).getRequestParameterName();
            String eventStr = ((ConvertableEvent) event).asString();
            if (eventStr == null) {
                // Could not convert the event
                value.append(this.converter.encode(event));
            } else {
                parameterName = DEFAULT_CONVERTABLE_EVENT_PARAMETER_NAME;
                try {
                    String eventValue = NetUtils.encode(eventStr, "utf-8");
                    value.append(eventParName).append('(').append(eventValue).append(')');
                } catch (UnsupportedEncodingException uee) {
                    // ignore this as utf-8 is always supported
                }
            }
        } else {
            if (event instanceof RequestEvent) {
                final String eventParName = ((RequestEvent) event).getRequestParameterName();
                if (eventParName != null) {
                    parameterName = eventParName;
                }
            }
            value.append(this.converter.encode(event));
        }
        return parameterName;
    }
}
