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
package org.apache.cocoon.portal.coplet;

import java.util.HashMap;
import java.util.Map;

/**
 * A coplet base data defines a coplet type, like a pipeline based coplet,
 * a JSR 168 portlet or a WSRP portlet. For each type exists a coplet base
 * data with some configuration.
 * Based on the coplet base data, coplets are created ({@link CopletData}.
 *
 * @version $Id$
 */
public final class CopletBaseData { 

	private Map copletConfig = new HashMap();

	private final String id;

	private String copletAdapterName;

    /**
     * Create a new coplet base data object. 
     * @param id The unique id of the object.
     */
    public CopletBaseData(String id) {
        // TODO - Check for valid id's
        this.id = id;
    }

	public String getId() {
		return id;
	}

	public String getCopletAdapterName() {
		return this.copletAdapterName;
	}

	public Object getCopletConfig(String key) {
		return this.copletConfig.get(key);
	}

	public void setCopletConfig(String key, Object value) {
		this.copletConfig.put(key, value);
	}

	public Map getCopletConfig() {
		return this.copletConfig;
	}

	public void setCopletConfig(Map config) {
		this.copletConfig = config;
	}

	public void setCopletAdapterName(String name) {
		this.copletAdapterName = name;
	}

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "CopletBaseData (" + this.hashCode() +
               "), id=" + this.getId() + ", coplet-adapter=" + this.getCopletAdapterName();
    }
}
