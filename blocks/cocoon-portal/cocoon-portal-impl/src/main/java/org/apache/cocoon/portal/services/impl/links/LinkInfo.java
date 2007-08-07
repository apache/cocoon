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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.Event;

/**
 * Helper class containing the information about common parts for each link
 * that will be generated in the portal page.
 *
 * @version $Id$
 */
public class LinkInfo {

    /** Link base contains the base url for the http protocol. */
    protected final String       httpLinkBase;
    protected final String       secureLinkBase;
    protected List comparableEvents;
    protected List events;
    protected Map  parameters;

    /** Is the page called using https? */
    protected final boolean isSecure;

    public LinkInfo(HttpServletRequest request, int defaultPort, int defaultSecurePort) {
        this.isSecure = request.getScheme().equals("https");
        // create relative url
        String relativeURI = request.getServletPath();
        if ( request.getPathInfo() != null ) {
            relativeURI = relativeURI + request.getPathInfo();
        }
        final int pos = relativeURI.lastIndexOf('/');
        if ( pos != -1 ) {
            relativeURI = relativeURI.substring(pos+1);
        }

        // do we need a protocol shift for link base?
        if ( this.isSecure ) {
            this.httpLinkBase = this.getAbsoluteUrl(request, false, defaultPort);
            this.secureLinkBase = relativeURI;
        } else {
            this.httpLinkBase = relativeURI;
            this.secureLinkBase = this.getAbsoluteUrl(request, true, defaultSecurePort);
        }
    }

    protected String getAbsoluteUrl(HttpServletRequest request, boolean useSecure, int port) {
        final StringBuffer buffer = new StringBuffer();
        if ( useSecure ) {
            buffer.append("https://");
        } else {
            buffer.append("http://");
        }
        buffer.append(request.getServerName());
        if (  (  useSecure && port != 443)
           || ( !useSecure && port != 80 ) ) {
            buffer.append(':');
            buffer.append(port);
        }
        buffer.append(request.getContextPath());
        buffer.append('/');
        buffer.append(request.getServletPath());
        if ( request.getPathInfo() != null ) {
            buffer.append(request.getPathInfo());
        }
        return buffer.toString();
    }

    public String getBase(Boolean secure) {
        // if no information is provided, we stay with the same protocol
        if ( (secure == null && this.isSecure ) || (secure != null && secure.booleanValue() )) {
            return this.secureLinkBase;
        }
        return this.httpLinkBase;
    }

    /**
     * Delete a parameter.
     */
    public void deleteParameter(String parameterName) {
        if ( this.parameters != null ) {
            this.parameters.remove(parameterName);
        }
    }

    /**
     * Add a parameter to each link in this page.
     */
    public void addParameter(String name, String value) {
        if ( this.parameters == null ) {
            this.parameters = new HashMap();
        }
        // do we already have a value for this parameter?
        final String[] previousValues = (String[]) this.parameters.get(name);
        if ( previousValues == null ) {
            this.parameters.put(name, new String[] {value});
        } else {
            final String[] newValues = new String[previousValues.length+1];
            System.arraycopy(previousValues, 0, newValues, 0, previousValues.length);
            newValues[previousValues.length] = value;
            this.parameters.put(name, newValues);
        }
    }

    /**
     * Add an event to each link in this page.
     */
    public void addEvent(Event event) {
        if ( event != null ) {
            if (event instanceof ComparableEvent) {
                if ( this.comparableEvents != null ) {
                    // search if we already have an event for this!
                    final Iterator iter = this.comparableEvents.iterator();
                    boolean found = false;
                    while (!found && iter.hasNext()) {
                        final ComparableEvent e = (ComparableEvent)iter.next();
                        if (e.equalsEvent((ComparableEvent) event)) {
                            found = true;
                            iter.remove();
                        }
                    }
                } else {
                    this.comparableEvents = new ArrayList();
                }
                this.comparableEvents.add(event);
            } else {
                if ( this.events == null ) {
                    this.events = new ArrayList();
                }
                this.events.add(event);
            }
        }
    }
}