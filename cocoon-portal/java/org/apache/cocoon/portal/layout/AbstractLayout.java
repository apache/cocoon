/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout;

import java.util.Map;

import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.commons.collections.map.LinkedMap;

/**
 * This class can be used as a base class for all layout implementations that
 * represent a leave in the layout tree.
 * If you want to implement a container layout, use the
 * {@link org.apache.cocoon.portal.layout.impl.CompositeLayoutImpl} class instead.
 *
 * @version $Id$
 */
public abstract class AbstractLayout
    extends AbstractParameters
    implements Layout {

    /** The renderer to render this layout if this layout object wants to use a different
     * render than the default renderer.
     */
    protected String rendererName;

    /** The parent item of this layout or null if this is a layout root. */
    protected Item parent;

    /** The name of the layout. */
    protected String name;

    /** The unique identifier of this layout object or null. */
    protected String id;

    /** The corresponding layout descripton. */
    transient protected LayoutDescription description;

    /** The temporary attributes. */
    transient protected Map temporaryAttributes = new LinkedMap();

    /** Is this layout object static? */
    protected Boolean isStatic;

    /**
     * Create a new layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param name The name of the layout.
     */
    public AbstractLayout(String id, String name) {
        // TODO - Check for valid id's
        this.id = id;
        this.name = name;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#setDescription(org.apache.cocoon.portal.layout.LayoutDescription)
     */
    public void setDescription(LayoutDescription description) {
        if ( this.description != null ) {
            throw new PortalRuntimeException("The layout has already a layout description.");
        }
        this.description = description;
        if ( this.isStatic == null ) {
            this.isStatic = Boolean.valueOf(this.description.defaultIsStatic());
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getRendererName()
     */
    public String getRendererName() {
        if ( this.rendererName == null ) {
            return this.description.getDefaultRendererName();
        }
        return this.rendererName;
    }

    public void setLayoutRendererName(String value) {
		this.rendererName = value;
	}

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getParent()
     */
    public Item getParent() {
        return this.parent;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#setParent(org.apache.cocoon.portal.layout.Item)
     */
    public void setParent(Item item) {
        this.parent = item;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getLayoutRendererName()
     */
    public String getLayoutRendererName() {
        return this.rendererName;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getTemporaryAttribute(java.lang.String)
     */
    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getTemporaryAttributes()
     */
    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#setTemporaryAttribute(java.lang.String, java.lang.Object)
     */
    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put(key, value);
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#removeTemporaryAttribute(java.lang.String)
     */
    public Object removeTemporaryAttribute(String key) {
        return this.temporaryAttributes.remove(key);
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#isStatic()
     */
    public Boolean isStatic() {
        return this.isStatic;
    }

    public void setIsStatic(Boolean value) {
        this.isStatic = value;
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        AbstractLayout clone = (AbstractLayout)super.clone();

        // we don't clone the parent; we just set it to null
        clone.name = this.name;
        clone.id = this.id;
        clone.description = this.description;
        clone.rendererName = this.rendererName;
        clone.isStatic = this.isStatic;
        clone.temporaryAttributes = new LinkedMap(this.temporaryAttributes);
        clone.parent = null;

        return clone;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#copy()
     */
    public Layout copy() {
        try {
            return (Layout)this.clone();
        } catch (CloneNotSupportedException cnse) {
            // ignore
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Layout (" + this.getClass() + '.' + this.hashCode() +
               "), name=" + this.name + ", id=" + (this.getId() == null ? "" : this.getId());
    }
}
