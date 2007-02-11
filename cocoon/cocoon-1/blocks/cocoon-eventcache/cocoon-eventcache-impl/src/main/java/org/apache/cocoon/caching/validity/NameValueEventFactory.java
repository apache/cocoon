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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * Factory for NameValueEvents
 */
public class NameValueEventFactory implements EventFactory {
    
    /**
     * Parameter key 'event-name'.
     */
    public static final String EVENT_NAME_PARAM = "event-name";
    
    /**
     * Parameter key 'event-value'.
     */
    public static final String EVENT_VALUE_PARAM = "event-value";
    
    public Event createEvent(Parameters params) throws ParameterException {
        final String name = params.getParameter(EVENT_NAME_PARAM);
        final String value = params.getParameter(EVENT_VALUE_PARAM);
        return new NameValueEvent(name,value);
    }
}
