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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.util.NetUtils;

/**
 *
 * @version $Id$
 */
public class DefaultLinkService 
    extends AbstractComponent
    implements LinkService,
               Parameterizable {

    /** Default port used for http. */
    protected int defaultPort = 80;

    /** Default port used for https. */
    protected int defaultSecurePort = 443;

    /** List of internal parameters. */
    protected List internalParameters = new ArrayList();

    /** List of matchers for internal parameters. */
    protected List internalParametersMatchers = new ArrayList();

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.defaultPort = params.getParameterAsInteger("defaultPort", this.defaultPort);
        this.defaultSecurePort = params.getParameterAsInteger("defaultSecurePort", this.defaultSecurePort);
        final String internalParams = params.getParameter("internalParameters", "cocoon-*");
        final StringTokenizer st = new StringTokenizer(internalParams, ",");
        while ( st.hasMoreTokens() ) {
            final String parameter = st.nextToken();
            this.internalParameters.add(parameter);
            if ( parameter.endsWith("*") ) {
                this.internalParametersMatchers.add(new PrefixParameterMatcher(parameter));
            } else {
                this.internalParametersMatchers.add(new ConstantParameterMatcher(parameter));
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
        final String value = this.portalService.getEventConverter().encode(event);
        try {
            buffer.append(DEFAULT_REQUEST_EVENT_PARAMETER_NAME).append('=').append(NetUtils.encode(value, "utf-8"));
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
            info.comparableEvents.add(new Object[]{event, DEFAULT_REQUEST_EVENT_PARAMETER_NAME, value});
        } else {
            this.addParameterToLink(DEFAULT_REQUEST_EVENT_PARAMETER_NAME, value);
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
                buffer.append((String)objects[1]).append('=').append(NetUtils.encode((String)objects[2], "utf-8"));
            } catch (UnsupportedEncodingException uee) {
                // ignore this as utf-8 is always supported
            }
            hasParams = true;
        }
        return buffer.toString();
    }
    
    /**
     * @see org.apache.cocoon.portal.services.LinkService#getInternalParameterNames()
     */
    public List getInternalParameterNames() {
        return this.internalParameters;
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

    public static interface ParameterMatcher {
        boolean match(String name);
    }
    public static final class ConstantParameterMatcher implements ParameterMatcher {
        protected final String name;
        public ConstantParameterMatcher(String value) {
            this.name = value;
        }
        public boolean match(String matchingName) {
            return this.name.equals(matchingName);
        }
    }
    public static final class PrefixParameterMatcher implements ParameterMatcher {
        protected final String prefix;
        public PrefixParameterMatcher(String prefix) {
            this.prefix = prefix.substring(0, prefix.length()-1);
        }
        public boolean match(String name) {
            return name.startsWith(this.prefix);
        }
    }
}
