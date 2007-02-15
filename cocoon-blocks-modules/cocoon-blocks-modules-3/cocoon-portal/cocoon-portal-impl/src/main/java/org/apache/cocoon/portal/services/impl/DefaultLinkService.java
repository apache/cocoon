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
package org.apache.cocoon.portal.services.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.services.impl.links.ConstantParameterMatcher;
import org.apache.cocoon.portal.services.impl.links.ParameterMatcher;
import org.apache.cocoon.portal.services.impl.links.PrefixParameterMatcher;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This is the default implementation of the {@link LinkService}.
 * In order to work properly this component has to be configured with the correct
 * ports.
 *
 * @version $Id$
 */
public class DefaultLinkService 
    extends AbstractBean
    implements LinkService {

    /** Default port used for http. */
    protected int defaultPort = 80;

    /** Default port used for https. */
    protected int defaultSecurePort = 443;

    /** List of matchers for internal parameters. */
    protected List internalParametersMatchers = new ArrayList();

    /** The name of the request parameter for events. */
    protected String requestParameterName = LinkService.DEFAULT_REQUEST_EVENT_PARAMETER_NAME;

    public DefaultLinkService() {
        this.setInternalParameters(LinkService.DEFAULT_INTERNAL_PARAMETERS);
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public void setDefaultSecurePort(int defaultSecurePort) {
        this.defaultSecurePort = defaultSecurePort;
    }

    public void setRequestParameterName(String requestParameterName) {
        this.requestParameterName = requestParameterName;
    }

    public void setInternalParameters(List internalParams) {
        this.internalParametersMatchers.clear();
        if ( internalParams != null ) {
            final Iterator i = internalParams.iterator();
            while ( i.hasNext() ) {
                final String parameter = i.next().toString();
                if ( parameter.endsWith("*") ) {
                    this.internalParametersMatchers.add(new PrefixParameterMatcher(parameter));
                } else {
                    this.internalParametersMatchers.add(new ConstantParameterMatcher(parameter));
                }
            }            
        }
    }

    /**
     * Return the current info for the request.
     * @return A LinkInfo object.
     */
    protected LinkInfo getInfo() {
        final Request request = ObjectModelHelper.getRequest(this.portalService.getProcessInfoProvider().getObjectModel());
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
     * @see org.apache.cocoon.portal.services.LinkService#isSecure()
     */
    public boolean isSecure() {
        final Request request = ObjectModelHelper.getRequest(this.portalService.getProcessInfoProvider().getObjectModel());
        return request.isSecure();
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#encodeURL(String url)
     */
    public String encodeURL(String url) {
        final Response response = ObjectModelHelper.getResponse(this.portalService.getProcessInfoProvider().getObjectModel());
        return response.encodeURL(url);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event)
     */
    public String getLinkURI(Event event) {
        return this.getLinkURI(event, null);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#getLinkURI(org.apache.cocoon.portal.event.Event, Boolean)
     */
    public String getLinkURI(Event event, Boolean secure) {
        if (event == null) {
            return this.getRefreshLinkURI(secure);
        }
        final LinkInfo info = this.getInfo();
        final StringBuffer buffer = new StringBuffer(info.getBase(secure));
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
                    buffer.append((String) objects[1]).append('=').append(URLEncoder.encode((String) objects[2], "utf-8"));
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
        final String value = this.portalService.getEventConverter().encode(event);
        try {
            buffer.append(this.requestParameterName).append('=').append(URLEncoder.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        return true;
    }
    
    /**
     * @see org.apache.cocoon.portal.services.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events) {
        return this.getLinkURI(events, null);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#getLinkURI(java.util.List)
     */
    public String getLinkURI(List events, Boolean secure) {
        if (events == null || events.size() == 0) {
            return this.getRefreshLinkURI(secure);
        }
        final LinkInfo info = this.getInfo();
        boolean hasParams = info.hasParameters();
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
                    buffer.append((String) objects[1]).append('=').append(URLEncoder.encode((String) objects[2], "utf-8"));
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

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addEventToLink(org.apache.cocoon.portal.event.Event)
     */
    public void addEventToLink(Event event) {
        if (event == null) {
            return;
        }
        final String value = this.portalService.getEventConverter().encode(event);
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
            info.comparableEvents.add(new Object[]{event, this.requestParameterName, value});
        } else {
            this.addParameterToLink(this.requestParameterName, value);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addParameterToLink(java.lang.String, java.lang.String)
     */
    public void addParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.addParameterToBase(name, value);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addUniqueParameterToLink(java.lang.String, java.lang.String)
     */
    public void addUniqueParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.deleteParameterFromBase(name);
        this.addParameterToLink(name, value);
    }
    
    /**
     * @see org.apache.cocoon.portal.services.LinkService#getRefreshLinkURI()
     */
    public String getRefreshLinkURI() {
        return this.getRefreshLinkURI(null);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#getRefreshLinkURI(java.lang.Boolean)
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
                buffer.append((String)objects[1]).append('=').append(URLEncoder.encode((String)objects[2], "utf-8"));
            } catch (UnsupportedEncodingException uee) {
                // ignore this as utf-8 is always supported
            }
            hasParams = true;
        }
        return buffer.toString();
    }
    
    /**
     * @see org.apache.cocoon.portal.services.LinkService#isInternalParameterName(java.lang.String)
     */
    public boolean isInternalParameterName(String name) {
        final Iterator i = this.internalParametersMatchers.iterator();
        while ( i.hasNext() ) {
            final ParameterMatcher current = (ParameterMatcher)i.next();
            if ( current.match(name) ) {
                return true;
            }
        }
        return false;
    }
}
