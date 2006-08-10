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

import java.io.Serializable;
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
public final class CopletInstance implements Serializable {

    public final static int SIZE_MINIMIZED  = 0;
    public final static int SIZE_NORMAL     = 1;
    public final static int SIZE_MAXIMIZED   = 2;
    public final static int SIZE_FULLSCREEN = 3;

    /** The unique identifier.
     * @see PortalUtils#testId(String)
     */
    protected final String id;

    /** The corresponding {@link CopletDefinition}. */
	protected CopletDefinition copletDefinition;

    /** Persisted attributes. */
    protected Map attributes = Collections.EMPTY_MAP;

    /** Temporary attributes are not persisted. */
    transient protected Map temporaryAttributes = Collections.EMPTY_MAP;

    /** The title of the coplet instance (if user specific). */
    protected String title;

    /** The size of the coplet. */
    protected int size = SIZE_NORMAL;

    /**
	 * Constructor to create a new coplet instance data object.
     * Never create a coplet instance data object directly. Use the
     * {@link CopletFactory} instead.
     * @param id The unique id of the object.
     * @see PortalUtils#testId(String)
	 */
	public CopletInstance(String id) {
        final String idErrorMsg = PortalUtils.testId(id);
        if ( idErrorMsg != null ) {
            throw new IllegalArgumentException(idErrorMsg);
        }
        this.id = id;
	}

    /**
     * The unique identifier of this instance.
     * @return The unique identifer.
     */
    public String getId() {
        return this.id;
    }

	/**
	 * @return CopletDefinition
	 */
	public CopletDefinition getCopletDefinition() {
		return this.copletDefinition;
	}

	/**
	 * Sets the coplet definition..
	 * @param copletDef The copletDef to set
	 */
	public void setCopletDefinition(CopletDefinition copletDef) {
		this.copletDefinition = copletDef;
	}

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        if ( this.attributes.size() == 0 ) {
            this.attributes = new HashMap();
        }
        this.attributes.put(key, value);
    }

    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    public void setTemporaryAttribute(String key, Object value) {
        if ( this.temporaryAttributes.size() == 0 ) {
            this.temporaryAttributes = new HashMap();
        }
        this.temporaryAttributes.put(key, value);
    }

    public Object removeTemporaryAttribute(String key) {
        return this.temporaryAttributes.remove(key);
    }

    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
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
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        CopletInstance clone = new CopletInstance(this.id);

        clone.copletDefinition = this.copletDefinition;
        if ( this.attributes.size() > 0 ) {
            clone.attributes = new HashMap(this.attributes);
        }
        if ( this.temporaryAttributes.size() > 0 ) {
            clone.temporaryAttributes = new HashMap(this.temporaryAttributes);
        }

        return clone;
    }

    public CopletInstance copy() {
        try {
            return (CopletInstance)this.clone();
        } catch (CloneNotSupportedException cnse) {
            // ignore
            return null;
        }
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
}