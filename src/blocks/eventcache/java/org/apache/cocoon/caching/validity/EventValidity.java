/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.caching.validity;

import org.apache.excalibur.source.SourceValidity;

/**
 * The SourceValidity object for cache invalidation based on 
 * external events.
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version $Id: EventValidity.java,v 1.4 2004/03/05 13:01:56 bdelacretaz Exp $ 
 */
public class EventValidity implements SourceValidity {
    
    private Event m_event;
    
    /**
     * Constructor requires any subclass of Event.
     * @param ev
     */
    public EventValidity(Event ev) {
        m_event = ev;
    }
    
    /**
     * Returns the specific Event this validity is based on.
     * 
     * @return Event
     */
    public Event getEvent() {
        return m_event;
    }

	/** 
     * Basic implementation is always valid until event signals 
     * otherwise.  May never need other behavior.
	 */
	public int isValid() {
		return VALID;
	}

    /** 
     * Older style of isValid
     */
	public int isValid(SourceValidity sv) {
		if (sv instanceof EventValidity) {
            return VALID;
		} 
        return INVALID;
	}
    
    
    public boolean equals(Object o) {
        if (o instanceof EventValidity) {
            return m_event.equals(((EventValidity)o).getEvent());
        } 
        return false;
    }
    
	public int hashCode() {
		return m_event.hashCode();
	}

    public String toString() {
        return "EventValidity[" + m_event + "]";
    }
}
