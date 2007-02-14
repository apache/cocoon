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
package org.apache.cocoon.portal.services.impl.links;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This is the default implementation of the {@link LinkService}.
 * In order to work properly this component has to be configured with the correct
 * ports.
 *
 * @version $Id$
 */
public class NewLinkService 
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

    /**
     * Construct a new link service.
     */
    public NewLinkService() {
        this.setInternalParameters(LinkService.DEFAULT_INTERNAL_PARAMETERS);
    }

    /**
     * Set the default port for http.
     */
    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    /**
     * Set the default port for https.
     */
    public void setDefaultSecurePort(int defaultSecurePort) {
        this.defaultSecurePort = defaultSecurePort;
    }

    /**
     * Set the request parameter name used for adding event information to the url.
     */
    public void setRequestParameterName(String requestParameterName) {
        this.requestParameterName = requestParameterName;
    }

    /**
     * Set the list of internal parameters.
     */
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
        LinkInfo info = (LinkInfo)request.getAttribute(NewLinkService.class.getName());
        if ( info == null ) {
            info = (LinkInfo)request.getAttribute(NewLinkService.class.getName());
            if ( info == null ) {
                info = new LinkInfo(request, this.defaultPort, this.defaultSecurePort);
                request.setAttribute(NewLinkService.class.getName(), info);
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

    protected boolean addParameter(StringBuffer buffer, String name, String value, boolean hasParams) {
        if ( hasParams ) {
            buffer.append('&');
        } else {
            buffer.append('?');
        }
        try {
            buffer.append(name).append('=').append(URLEncoder.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            // ignore this as utf-8 is always supported
        }
        return true;
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
        return this.getLinkURI(Collections.singletonList(event), secure);
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
        final StringBuffer buffer = new StringBuffer(info.getBase(secure));
        boolean hasParams = buffer.indexOf("?") != -1;

        // add comparable events
        if ( info.comparableEvents != null ) {
            final Iterator iter = info.comparableEvents.iterator();
            while (iter.hasNext()) {
                final ComparableEvent current = (ComparableEvent)iter.next();
    
                final Iterator eventIterator = events.iterator();
                boolean found = false;
                while (!found && eventIterator.hasNext()) {
                    final Object inEvent = eventIterator.next();
                    if (inEvent instanceof ComparableEvent
                        && current.equalsEvent((ComparableEvent) inEvent)) {
                        found = true;
                    }
                }
                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }

        // add events
        if ( info.events != null ) {
            final Iterator iter = info.events.iterator();
            while (iter.hasNext()) {
                final Event current = (Event)iter.next();
                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }

        // add parameters
        if ( info.parameters != null ) {
            final Iterator iter = info.parameters.entrySet().iterator();
            while ( iter.hasNext() ) {
                final Map.Entry current = (Map.Entry)iter.next();
                final String parameterName = current.getKey().toString();
                final String [] values = (String[])current.getValue();
                for(int i=0; i<values.length; i++) {
                    hasParams = this.addParameter(buffer, parameterName, values[i], hasParams);
                }
            }
        }

        // now add supplied events and parameters
        final Iterator iter = events.iterator();
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
            } else {
                throw new PortalRuntimeException("Unknown object passed to create a link. Only events " +
                            "and parameter descriptions are allowed. Unknown object: " + current);
            }
        }
        return buffer.toString();
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addEventToLink(org.apache.cocoon.portal.event.Event)
     */
    public void addEventToLink(Event event) {
        final LinkInfo info = this.getInfo();
        info.addEvent(event);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addParameterToLink(java.lang.String, java.lang.String)
     */
    public void addParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.addParameter(name, value);
    }

    /**
     * @see org.apache.cocoon.portal.services.LinkService#addUniqueParameterToLink(java.lang.String, java.lang.String)
     */
    public void addUniqueParameterToLink(String name, String value) {
        final LinkInfo info = this.getInfo();
        info.deleteParameter(name);
        info.addParameter(name, value);
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
        boolean hasParams = buffer.indexOf("?") != -1;
        
        // add comparable events
        if ( info.comparableEvents != null ) {
            final Iterator iter = info.comparableEvents.iterator();
            while (iter.hasNext()) {
                final Event current = (Event)iter.next();
                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }

        // add events
        if ( info.events != null ) {
            final Iterator iter = info.events.iterator();
            while (iter.hasNext()) {
                final Event current = (Event)iter.next();
                hasParams = this.addEvent(buffer, current, hasParams);
            }
        }

        // add parameters
        if ( info.parameters != null ) {
            final Iterator iter = info.parameters.entrySet().iterator();
            while ( iter.hasNext() ) {
                final Map.Entry current = (Map.Entry)iter.next();
                final String parameterName = current.getKey().toString();
                final String [] values = (String[])current.getValue();
                for(int i=0; i<values.length; i++) {
                    hasParams = this.addParameter(buffer, parameterName, values[i], hasParams);
                }
            }
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
