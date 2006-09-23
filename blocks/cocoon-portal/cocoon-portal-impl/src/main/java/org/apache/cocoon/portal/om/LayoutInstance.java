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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A layout instance stores user specific information about a layout object.
 * It can contain temporary and persistent attributes. If information about
 * a layout should be persistet, the layout needs to have an id. However,
 * this is not checked in this data object. Use the {@link LayoutFeatures}
 * instead.
 *
 * @version $Id$
 */
public class LayoutInstance implements Serializable, Cloneable {

    /** The corresponding layout object. */
    protected transient final Layout layout;

    /** Persisted attributes. */
    protected Map attributes = Collections.EMPTY_MAP;

    /** Temporary attributes are not persisted. */
    protected transient Map temporaryAttributes = Collections.EMPTY_MAP;

    /**
     * Constructor to create a new layout instance object.
     */
    public LayoutInstance(Layout layout) {
        this.layout = layout;
    }

    public LayoutInstance copy() {
        try {
            return (LayoutInstance)this.clone();
        } catch (CloneNotSupportedException cnse) {
            // ignore
            return null;
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        final LayoutInstance clone = new LayoutInstance(this.layout);

        if ( this.attributes.size() > 0 ) {
            clone.attributes = new HashMap(this.attributes);
        }
        if ( this.temporaryAttributes.size() > 0 ) {
            clone.temporaryAttributes = new HashMap(this.temporaryAttributes);
        }

        return clone;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "LayoutInstance (" + this.hashCode() +
               "), id=" + this.layout.getId();
    }

    /**
     * The unique identifier of this instance.
     * @return The unique identifer.
     */
    public String getId() {
        return this.layout.getId();
    }

    public Layout getLayout() {
        return this.layout;
    }

    /**
     * Return the value of an attribute.
     * @param key The name of the attribute.
     * @return The value of the attribute or null
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * Set the value of the attribute.
     * @param key The attribute name.
     * @param value The new value.
     */
    public void setAttribute(String key, Object value) {
        if ( this.attributes.size() == 0 ) {
            this.attributes = new HashMap();
        }
        this.attributes.put(key, value);
    }

    /**
     * Remove an attribute.
     * @param key The attribute name.
     * @return If there was a value associated with the attribute, the old value is returned.
     */
    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }

    /**
     * Return a map with all attributes.
     * @return A map.
     */
    public Map getAttributes() {
        return this.attributes;
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
