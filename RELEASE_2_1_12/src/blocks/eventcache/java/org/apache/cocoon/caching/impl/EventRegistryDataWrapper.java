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
package org.apache.cocoon.caching.impl;

import java.io.Serializable;
import java.util.Map;

/**
 * A light object for persisting the state of an EventRegistry implementation 
 * based on two MultiHashMaps.
 * 
 * @version $Id$
 */
public class EventRegistryDataWrapper implements Serializable {
    
    private static final long serialVersionUID = -419774862702021018L;
    private Map m_keyMap;
    private Map m_eventMap;

    public EventRegistryDataWrapper() {
        this.m_keyMap = null;
        this.m_eventMap = null;
    }

    public void setupMaps(Map keyMap, Map eventMap) {
        this.m_keyMap = keyMap;
        this.m_eventMap = eventMap;
    }

    public Map get_eventMap() {
        return m_eventMap;
    }

    public Map get_keyMap() {
        return m_keyMap;
    }

}