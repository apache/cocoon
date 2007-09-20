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
package org.apache.cocoon.caching.validity;

import java.io.Serializable;

/**
 * Base class encapsulating the information about an external 
 * uncache event.
 * 
 * @version $Id$
 */
public abstract class Event implements Serializable {
    
    /**
     * Used by EventValidity for equals(Object o) which 
     * is important for determining whether a received event 
     * should uncache a held Pipeline key.
     * 
     * @param e Another Event to compare.
     * @return true if
     */
    public abstract boolean equals(Event e);
    
    /**
     * This hash code is the only way the system can locate 
     * matching Events when a new event notification is received.
     */
    public abstract int hashCode();
    
    public boolean equals(Object o) {
        if (o instanceof Event) {
            return equals((Event)o);   
        }
        return false;
    }
    
}
