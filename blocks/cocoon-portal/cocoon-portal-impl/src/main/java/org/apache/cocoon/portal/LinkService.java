/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal;

import java.util.List;

import org.apache.cocoon.portal.event.Event;

/**
 * This is a central service of the portal. It should be used to create links
 * inside the portal pages.
 *
 * @version $Id$
 */
public interface LinkService {

    String ROLE = LinkService.class.getName();

    String DEFAULT_REQUEST_EVENT_PARAMETER_NAME = "cocoon-portal-event";

    static class ParameterDescription {
        public final String parameters;
        public ParameterDescription(String parameters) {
            this.parameters = parameters;
        }
    }

    /**
     * Get the uri for this coplet containing the additional event
     * @param event The event to add (null is also allowed for convenience)
     * @return A URI
     */
    String getLinkURI(Event event);

    /**
     * Get the uri for this coplet containing the additional event and using a secure
     * protocol if requested.
     *
     * @param event The event to add (null is also allowed for convenience)
     * @param secure true if a secure protocol is required, false otherwise.
     * @return A URI
     */
    String getLinkURI(Event event, Boolean secure);

    /**
     * Get the uri for this coplet containing the additional events.
     * @param events The events to add: These can either be {@link Event}s or {@link ParameterDescription}s.
     * @return A URI
     */
    String getLinkURI(List events);

    /**
     * Get a uri for this coplet containing the additional events. Use a secure
     * protocol if requested.
     * @param events The events to add: These can either be {@link Event}s or {@link ParameterDescription}s.
     * @param secure true if a secure protocol is required, false otherwise.
     * @return A URI
     */
    String getLinkURI(List events, Boolean secure);

    /**
     * Add this event to the list of events contained in the uri
     * @param event Event to add
     */
    void addEventToLink(Event event);

    /**
     * Add this parameter to every link.
     * If the link already contains a parameter with this name,
     * then the link will have both parameters with the same
     * name, but different values.
     * @param name  The request parameter name
     * @param value The value for the parameter
     * @see #addUniqueParameterToLink(String, String)
     */
    void addParameterToLink(String name, String value);

    /**
     * Add this parameter to every link.
     * If the link already contains a parameter with this name,
     * then this old parameter will be removed and replaced by
     * the new one.
     * @param name  The request parameter name
     * @param value The value for the parameter
     * @see #addUniqueParameterToLink(String, String)
     */
    void addUniqueParameterToLink(String name, String value);

    /**
     * Get a link that simply refreshs the portal
     * @return A URI
     */
    String getRefreshLinkURI();

    /**
     * Get a link that simply refreshs the portal
     * @param secure true if a secure protocol is required, false otherwise.
     * @return A URI
     */
    String getRefreshLinkURI(Boolean secure);

    /**
     * Determine whether the current url is using a secure protocol
     * @return true if the current url is using a secure protocol
     */
    boolean isSecure();

    /**
     * @param url The url to encode.
     * @return The enocoded URL.
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String) 
     */
    String encodeURL(String url);

    /**
     * A list of parameters that should be filtered when
     * coplets are called. The list might consist of either
     * a parameter name (string) or a prefix (string). A prefix
     * ends with a '*'.
     * @return A list of strings. This method always returns a list.
     */
    List getInternalParameterNames();

    /**
     * Test if the parameter is an internal one.
     */
    boolean isInternalParameterName(String name);
}