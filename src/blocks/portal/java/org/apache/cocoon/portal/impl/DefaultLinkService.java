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
package org.apache.cocoon.portal.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.RequestEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.util.NetUtils;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class DefaultLinkService 
    extends AbstractLogEnabled
    implements ThreadSafe, LinkService, Serviceable, Disposable, Contextualizable {

    /**
     * Helper class containing the information about the request uri
     */
    static class Info {
        StringBuffer  linkBase = new StringBuffer();
        boolean       hasParameters = false;
        ArrayList     comparableEvents = new ArrayList(5);

        public String getBase(Boolean secure) {
            //Todo actually implement secure
            return linkBase.toString();
        }
    }
    
    /** The converter used to convert an event into a request parameter */
    protected EventConverter   converter;
    /** The service manager */
    protected ServiceManager manager;
    /** The cocoon context */
    protected Context context;

    protected Boolean eventsMarshalled = null;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.converter = (EventConverter)this.manager.lookup(EventConverter.ROLE);
    }

    /**
     * Return the current info for the request
     * @return An Info object
     */
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

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.LinkService#isSecure()
    */
    public boolean isSecure() {
        return ContextHelper.getRequest(this.context).isSecure();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#encodeURL(String url).
     */
    public String encodeURL(String url) {
        return ContextHelper.getResponse(this.context).encodeURL(url);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event)
     */
    public String getLinkURI(Event event) {
        return this.getLinkURI(event, null);
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event, boolean)
    */
    public String getLinkURI(Event event, Boolean secure) {
        if (event == null) {
            return this.getRefreshLinkURI();
        }
        final Info info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.getBase(secure));
        boolean hasParams = info.hasParameters;

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
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events) {
        return this.getLinkURI(events, null);
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.LinkService#getLinkURI(java.util.List)
    */
    public String getLinkURI(List events, Boolean secure) {
        if (events == null || events.size() == 0) {
            return this.getRefreshLinkURI();
        }
        final Info info = this.getInfo();
        boolean hasParams = info.hasParameters;
        final StringBuffer buffer = new StringBuffer(info.getBase(secure));

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

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#addEventToLink(org.apache.cocoon.portal.event.Event)
     */
    public void addEventToLink(Event event) {
        if (event == null) {
            return;
        }
        StringBuffer value = new StringBuffer("");
        String parameterName = processEvent(event, value);

        final Info info = this.getInfo();
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

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#addParameterToLink(java.lang.String, java.lang.String)
     */
    public void addParameterToLink(String name, String value) {
        final Info info = this.getInfo();
        if ( info.hasParameters ) {
            info.linkBase.append('&');
        } else {
            info.linkBase.append('?');
        }
        try {
            info.linkBase.append(name).append('=').append(NetUtils.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        info.hasParameters = true;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#addUniqueParameterToLink(java.lang.String, java.lang.String)
     */
    public void addUniqueParameterToLink(String name, String value) {
        final Info info = this.getInfo();
        if ( info.hasParameters ) {
            final int pos = info.linkBase.toString().indexOf("?");
            final String queryString = info.linkBase.substring(pos + 1);
            final RequestParameters params = new RequestParameters(queryString);
            if ( params.getParameter(name) != null ) {
                // the parameter is available, so remove it
                info.linkBase.delete(pos, info.linkBase.length() + 1);
                info.hasParameters = false;
                
                Enumeration enumeration = params.getParameterNames();
                while ( enumeration.hasMoreElements() ) {
                    final String paramName = (String)enumeration.nextElement();
                    if ( !paramName.equals(name) ) {
                        String[] values = params.getParameterValues(paramName);
                        for( int i = 0; i < values.length; i++ ) {
                            this.addParameterToLink(paramName, values[i]);
                        }
                    }
                }
            }
        }
        // the parameter is not available, so just add it
        this.addParameterToLink(name, value);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.LinkService#getRefreshLinkURI()
     */
    public String getRefreshLinkURI() {
        final Info info = this.getInfo();

        final StringBuffer buffer = new StringBuffer(info.linkBase.toString());

        // add comparable events
        Iterator iter = info.comparableEvents.iterator();
        boolean hasParams = info.hasParameters;
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

    private boolean getEventsMarshalled() {
        if (this.eventsMarshalled == null) {
            this.eventsMarshalled = new Boolean(this.converter.isMarshallEvents());
        }
        return this.eventsMarshalled.booleanValue();
    }

    private String processEvent(Event event, StringBuffer value) {
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
