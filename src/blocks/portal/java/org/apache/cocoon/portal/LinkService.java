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
package org.apache.cocoon.portal;

import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.portal.event.Event;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: LinkService.java,v 1.6 2004/06/07 09:53:34 cziegeler Exp $
 */
public interface LinkService extends Component {

    String ROLE = LinkService.class.getName();
    
    String DEFAULT_REQUEST_EVENT_PARAMETER_NAME = "cocoon-portal-event";
    
    /**
     * Get the uri for this coplet containing the additional event
     * @param event The event to add (null is also allowed for convenience)
     * @return A URI
     */
    String getLinkURI(Event event);

    /**
     * Get the uri for this coplet containing the additional events
     * @param events The events to add
     * @return A URI
     */
    String getLinkURI(List events);
    
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
}
