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
package org.apache.cocoon.portal.event;

import org.apache.avalon.framework.component.Component;

/**
 * Convert events from and into strings
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: EventConverter.java,v 1.3 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public interface EventConverter extends Component {

    String ROLE = EventConverter.class.getName();
    
    /**
     * Encode an event.
     * This is used to "activate" events using a link
     * @param event The event to encode
     * @return A unique string representation for this event
     */
    String encode(Event event);
    
    /**
     * Decode an event
     * This is used to "activate" events using a link
     * @param value The string representation created using {@link #encode(Event)}
     * @return The event or null 
     */
    Event decode(String value);

    /**
     * This notifies the converter that a new event processing phase starts
     */
    void start();
    
    /**
     * This notifies the converter that an event processing phase ends
     */
    void finish();
}
