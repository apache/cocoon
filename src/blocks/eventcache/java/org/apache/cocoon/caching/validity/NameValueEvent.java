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
 * An external uncache event that consists of a name/value pair.  
 * An example might be "table_name", "primary_key"
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version $Id: NameValueEvent.java,v 1.4 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class NameValueEvent extends Event {

    private String m_name;
    private String m_value;
    private int m_hashcode;
    
    /**
     * Constructor requires two Strings - the name/value 
     * pair which defines this Event.
     * 
     * @param name
     * @param value
     */
	public NameValueEvent(String name, String value) {
        m_name = name;
        m_value = value;
        m_hashcode = (name + value).hashCode();
	}
    
    /**
     * Must return true when both name and value are 
     * equivalent Strings.
     */
	public boolean equals(Event e) {
		if (e instanceof NameValueEvent) {
            NameValueEvent nve = (NameValueEvent)e;
            return ( m_name.equals(nve.m_name) && 
                m_value.equals(nve.m_value) );
		}
		return false;
	}
    
    public int hashCode() {
        return m_hashcode;
    }
    
    public String toString() {
        return "NameValueEvent[" + m_name + "," + m_value + "]";
    }
}
