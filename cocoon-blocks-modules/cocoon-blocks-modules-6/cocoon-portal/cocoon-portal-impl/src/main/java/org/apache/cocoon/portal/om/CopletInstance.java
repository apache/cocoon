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
 * A coplet instance data describes an instance of a coplet.
 *
 * Attributes and temporary attributes
 * An attribute is a key-value pair consisting of a string for
 * the key and any object for the value. While the attributes are persisted,
 * the temporary attributes have only the life-time of a
 * session. As both, attributes and temporary attributes, are stored
 * in the session, it is advisable to use serializable objects only.
 *
 * Sizing:
 *   A coplet can have different sizes:
 *   SIZE_NORMAL - this is the normal size, it means that the coplet shares it's
 *                 space with other coplets.
 *   SIZE_MINIMIZED - the coplet is minimized. Usually in this state only the
 *                    title of the coplet is rendered.
 *   SIZE_FULLSCREEN - the coplet is the only coplet on the screen.
 *   SIZE_MAXIMIZED - the coplet gets the most available space, but still shares
 *                  its space with other coplets, e.g. a navigation etc.
 *
 * @version $Id$
 */
public final class CopletInstance {

    public final static int SIZE_MINIMIZED  = 0;
    public final static int SIZE_NORMAL     = 1;
    public final static int SIZE_MAXIMIZED   = 2;
    public final static int SIZE_FULLSCREEN = 3;

    /** The corresponding {@link CopletDefinition}. */
	protected CopletDefinition copletDefinition;

    /** The title of the coplet instance (if user specific). */
    protected String title;

    /** The size of the coplet. */
    protected int size = SIZE_NORMAL;

    /** The unique identifier.
     * @see PortalUtils#testId(String)
     */
    protected final String id;

    /** Persisted attributes. */
    protected Map attributes = Collections.EMPTY_MAP;

    /** Temporary attributes are not persisted. */
    protected transient Map temporaryAttributes = Collections.EMPTY_MAP;

    /**
	 * Constructor to create a new coplet instance data object.
     * Never create a coplet instance data object directly. Use the
     * {@link org.apache.cocoon.portal.services.CopletFactory} instead.
     * @param id The unique id of the object.
     * @see PortalUtils#testId(String)
	 */
	public CopletInstance(String id, CopletDefinition def) {
        final String idErrorMsg = PortalUtils.testId(id);
        if ( idErrorMsg != null ) {
            throw new IllegalArgumentException(idErrorMsg);
        }
        this.copletDefinition = def;
        this.id = id;
	}

    public CopletInstance(String id) {
        this(id, null);
    }

	/**
	 * @return CopletDefinition
	 */
	public CopletDefinition getCopletDefinition() {
		return this.copletDefinition;
	}

	public void setCopletDefinition(final CopletDefinition cd) {
	    this.copletDefinition = cd;
	}

    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        return this.getCopletDefinition().getTitle();
    }

    public String getInstanceTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the actual size of this coplet.
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the size of this coplet.
     */
    public void setSize(int size) {
        if ( size < SIZE_MINIMIZED || size > SIZE_FULLSCREEN ) {
            throw new IllegalArgumentException("Unknown size for coplet: " + size);
        }
        this.size = size;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "CopletInstance (" + this.hashCode() +
               "), id=" + this.getId() + ", coplet-definition=" + (this.getCopletDefinition() == null ? "null" : this.getCopletDefinition().getId());
    }

    /**
     * The unique identifier of this instance.
     * @return The unique identifer.
     */
    public String getId() {
        return this.id;
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