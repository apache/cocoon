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
import org.apache.cocoon.ProcessingException;

/**
 * <p>Service to manage event notification. The designed has been inspired by the paper by 
 * Gupta, S., J. M. Hartkopf, and S. Ramaswamy, in Java Report, Vol. 3, No. 7, July 1998, 19-36,
 * "Event Notifier: A Pattern for Event Notification".</p>  
 * 
 * <p>EventManager brokers events between a <tt>Publisher</tt>, which produces events,
 * and a <tt>Subscriber</tt>, which handles the notification of events.
 * A <tt>Filter</tt> discards events not of interest to a subscriber.
 * All Events have a common ancestor type <tt>Event</tt> and the event types are 
 * identified by a <tt>Class</tt>.</p>
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author Mauro Talevi
 * 
 * @version CVS $Id: EventManager.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public interface EventManager extends Component {
 
    /**
     * Represents Role of the service
     */
    String ROLE = EventManager.class.getName(); 
    
     /**
      *  Returns the Publisher with which events can be published.
      */
    Publisher getPublisher();
    
     /**
      *  Returns the Register with which subscribers can 
      *  subscribe and unsubscribe interest to given Events.
      */
    Register getRegister();

    /**
     * Process the events
     */
    void processEvents()
    throws ProcessingException;
     

}



