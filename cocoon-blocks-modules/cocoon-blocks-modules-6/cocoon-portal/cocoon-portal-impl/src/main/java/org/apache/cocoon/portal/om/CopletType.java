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
package org.apache.cocoon.portal.om;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.portal.util.PortalUtils;

/**
 * A coplet type defines a way of adding content to the portal, like a
 * Cocoon pipeline based coplet,
 * a JSR 168 portlet or a WSRP portlet. For each type exists a coplet type
 * with corresponding configuration.
 *
 * Based on the coplet type, coplets are created ({@link CopletDefinition}.
 *
 * @version $Id$
 */
public final class CopletType {

    /** The configuration. */
	protected Map copletConfig = Collections.EMPTY_MAP;

	/** The unique identifier. */
    protected final String id;

    /** The coplet adapter. */
    protected CopletAdapter copletAdapter;

    /**
     * Create a new coplet base data object.
     * @param id The unique id of the object.
     * @see PortalUtils#testId(String)
     */
    public CopletType(String id, CopletAdapter adapter) {
        final String idErrorMsg = PortalUtils.testId(id);
        if ( idErrorMsg != null ) {
            throw new IllegalArgumentException(idErrorMsg);
        }
        this.id = id;
        this.copletAdapter = adapter;
    }

    /**
     * Create a new coplet base data object.
     * @param id The unique id of the object.
     * @see PortalUtils#testId(String)
     */
    public CopletType(String id) {
        this(id, null);
    }

    /**
     * Return the unique identifier for the coplet type.
     * @return The non-null unique identifier.
     */
	public String getId() {
		return id;
	}

	public CopletAdapter getCopletAdapter() {
		return this.copletAdapter;
	}

	public void setCopletAdapter(final CopletAdapter ca) {
	    this.copletAdapter = ca;
	}

	public Object getCopletConfig(String key) {
		return this.copletConfig.get(key);
	}

	public void setCopletConfig(String key, Object value) {
	    if ( this.copletConfig.size() == 0 ) {
	        this.copletConfig = new HashMap();
	    }
		this.copletConfig.put(key, value);
	}

    public Object removeCopletConfig(String key) {
        final Object oldValue = this.copletConfig.remove(key);
        if ( this.copletConfig.size() == 0 ) {
            this.copletConfig = Collections.EMPTY_MAP;
        }
        return oldValue;
    }

	public Map getCopletConfig() {
		return this.copletConfig;
	}

	public void setCopletConfig(final Map config) {
	    if ( config == null || config.size() == 0 ) {
	        this.copletConfig = Collections.EMPTY_MAP;
	    } else {
		    this.copletConfig = new HashMap(config);
	    }
	}

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "CopletType (" + this.hashCode() +
               "), id=" + this.getId() + ", coplet-adapter=" + this.getCopletAdapter();
    }
}
