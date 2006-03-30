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
package org.apache.cocoon.portal.layout;

import java.util.Map;

import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.util.PortalUtils;
import org.apache.commons.collections.map.LinkedMap;

/**
 * A layout describes a graphical element on the portal page. This can
 * be an element containing others ({@link CompositeLayout}) or a final
 * element, like a window for a coplet.
 *
 * Parameters and temporary attributes:
 * A parameter is a key-value pair consisting of strings for
 * both key and value. While the parameters are persisted,
 * the temporary attributes have only the life-time of a
 * session, but can contain any object.
 * As both, parameters and temporary attributes, are stored
 * in the session, it is advisable to use serializable objects only.
 *
 * If you are implementing your own layout object make sure that your
 * class provides a two string constructor which calls {@link #Layout(String, String)}.
 *
 * @version $Id$
 */
public abstract class Layout extends AbstractParameters {

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
     * @see PortalUtils#testId(String)
     */
    public Layout(String id, String name) {
        final String idErrorMsg = PortalUtils.testId(id);
        if ( idErrorMsg != null ) {
            throw new IllegalArgumentException(idErrorMsg);
        }
        this.id = id;
        this.name = name;
    }

    /**
     * The name given from the factory.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the unique id of this object.
     * @return Unique id of the layout or null if this object does not provide a unique id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the layout description.
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
     * Get the temporary attributes map.
     * This method never returns null.
     * @return A map with key value pairs.
     */
    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
    }

    /**
     * Return the temporary attribute value for the given key.
     * @param key The name of the attribute.
     * @return The value of the attribute or null.
     */
    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    /**
     * Set the temporary attribute to a value.
     * @param key The name of the attribute.
     * @param value The value.
     */
    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put(key, value);
    }

    /**
     * Remove the attribute.
     * @param key The name of the attribute.
     * @return The old value for the attribute or null.
     */
    public Object removeTemporaryAttribute(String key) {
        return this.temporaryAttributes.remove(key);
    }

    /**
     * Get the name of the {@link org.apache.cocoon.portal.layout.renderer.Renderer} to draw this layout.
     * If this layout has an own renderer {@link #getLayoutRendererName()}
     * return this, otherwise the default renderer is returned.
     * @return String The role name
     */
    public String getRendererName() {
        if ( this.rendererName == null ) {
            return this.description.getDefaultRendererName();
        }
        return this.rendererName;        
    }

    /** 
     * Get the name of a custom {@link org.apache.cocoon.portal.layout.renderer.Renderer} for this layout.
     * @return String The role name
     */
    public String getLayoutRendererName() {
        return this.rendererName;
    }

    /**
     * The parent of this layout object or null.
     */
    public Item getParent() {
        return this.getParent();
    }

    /**
     * Set the parent item for this layout.
     */
    public void setParent(Item item) {
        this.parent = item;
    }

    /**
     * Is this layout static?
     * If a layout is static, it is not removed when another layout
     * is max paged.
     */
    public Boolean isStatic() {
        return this.isStatic;
    }

    public void setLayoutRendererName(String value) {
        this.rendererName = value;
    }

    public void setIsStatic(Boolean value) {
        this.isStatic = value;
    }

    /**
     * Make a copy of this layout object and of all it's children.
     * This includes copies of items and copletinstancedatas.
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

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        Layout clone = (Layout)super.clone();

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
}
