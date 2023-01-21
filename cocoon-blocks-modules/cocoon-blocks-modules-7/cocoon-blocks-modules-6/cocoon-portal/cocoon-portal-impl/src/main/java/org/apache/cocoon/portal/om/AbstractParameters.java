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

import org.apache.commons.collections.map.LinkedMap;

/**
 *
 * @version $Id$
 */
public abstract class AbstractParameters {

    protected Map parameters = Collections.EMPTY_MAP;

    /** Temporary attributes are not persisted. */
    protected transient Map temporaryAttributes = Collections.EMPTY_MAP;

    public Map getParameters() {
        return this.parameters;
    }

    /**
     * Return the parameter value for the given key.
     * @param key The name of the parameter.
     * @return The value of the parameter or null.
     */
    public String getParameter(String key) {
        return (String)this.parameters.get(key);
    }

    /**
     * Set the parameter to a value.
     * @param key The name of the parameter.
     * @param value The value.
     */
    public void setParameter(String key, String value) {
        if ( this.parameters.size() == 0 ) {
            this.parameters = new LinkedMap();
        }
        this.parameters.put(key, value);
    }

    /**
     * Remove the parameter.
     * @param key The name of the parameter.
     * @return The old value for the parameter or null.
     */
    public String removeParameter(String key) {
        return (String)this.parameters.remove(key);
    }

    /**
     * Return the value of an attribute.
     * @param key The name of the attribute.
     * @return The value of the attribute or null
     */
    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    /**
     * Set the value of the attribute.
     * @param key The attribute name.
     * @param value The new value.
     */
    public void setTemporaryAttribute(String key, Object value) {
        if ( this.temporaryAttributes.size() == 0 ) {
            this.temporaryAttributes = new HashMap();
        }
        this.temporaryAttributes.put(key, value);
    }

    /**
     * Remove a temporary attribute.
     * @param key The attribute name.
     * @return If there was a value associated with the attribute, the old value is returned.
     */
    public Object removeTemporaryAttribute(String key) {
        return this.temporaryAttributes.remove(key);
    }

    /**
     * Return a map with all temporary attributes.
     * @return A map.
     */
    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
    }
}
