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
public final class CopletInstanceData implements Serializable {

    public final static int SIZE_MINIMIZED  = 0;
    public final static int SIZE_NORMAL     = 1;
    public final static int SIZE_MAXIMIZED   = 2;
    public final static int SIZE_FULLSCREEN = 3;

    protected final String id;

    /** The corresponding {@link CopletData}. */
	protected CopletData copletData;

    /** Persisted attributes. */
    protected Map attributes = new HashMap();

    /** Temporary attributes are not persisted. */
    transient protected Map temporaryAttributes = new HashMap();

    /** The title of the coplet. */
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
	public CopletInstanceData(String id) {
        // FIXME - Due to a bug in castor, we have to allow null ids for now
        if ( id == null ) {
            this.id = null;
            return;
        }
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
	 * @return CopletData
	 */
	public CopletData getCopletData() {
		return copletData;
	}

	/**
	 * Sets the copletData.
	 * @param copletData The copletData to set
	 */
	public void setCopletData(CopletData copletData) {
		this.copletData = copletData;
	}

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
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
        return this.getCopletData().getTitle();
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
        CopletInstanceData clone = new CopletInstanceData(this.id);

        clone.copletData = this.copletData;
        clone.attributes = new HashMap(this.attributes);
        clone.temporaryAttributes = new HashMap(this.temporaryAttributes);

        return clone;
    }

    public CopletInstanceData copy() {
        try {
            return (CopletInstanceData)this.clone();
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
        return "CopletInstanceData (" + this.hashCode() +
               "), id=" + this.getId() + ", coplet-data=" + (this.getCopletData() == null ? "null" : this.getCopletData().getId());
    }
}