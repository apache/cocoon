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

/**
 * An External cache event that consists of just a name.  Examples 
 * (not necessarily useful) could include "Easter" or "Shutdown"
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version $Id: NamedEvent.java,v 1.5 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class NamedEvent extends Event {
    
    private String m_name;
    private int m_hashcode;
    
    /**
     * Constructor takes a simple String as event name.
     * 
     * @param name name
     */
    public NamedEvent(String name) {
        m_name = name;
        m_hashcode = name.hashCode();
    }
    
    /**
     * Every NamedEvent where the name string is equal must 
     * return true.
     */
	public boolean equals(Event e) {
        if (e instanceof NamedEvent) {
            return m_name.equals(((NamedEvent)e).m_name);
        }
		return false;
	}
    
    public int hashCode() {
        return m_hashcode;
    }
    
    public String toString() {
        return "NamedEvent[" + m_name + "]";
    }
}
