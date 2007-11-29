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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.services.LayoutFactory;
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
 * class provides a two string constructor which calls {@link #Layout(String, LayoutType)}.
 *
 * @version $Id$
 */
public abstract class Layout extends AbstractParameters {

    /** The parent item of this layout or null if this is a layout root. */
    protected Item parent;

    /** The unique identifier of this layout object or null. */
    protected final String id;

    /** Is this layout object static? */
    protected boolean isStatic;

    /** The corresponding layout type. */
    protected final LayoutType layoutType;

    /** The optional renderer to render this layout. If this layout should use
     * a different renderer than the default renderer, this property will point
     * to the renderer.
     */
    protected Renderer customRenderer;

    /**
     * Create a new layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param type The type of the layout.
     * @see PortalUtils#testId(String)
     */
    public Layout(String id, LayoutType type) {
        // check id, null for id is allowed!
        if ( id != null ) {
            final String idErrorMsg = PortalUtils.testId(id);
            if ( idErrorMsg != null ) {
                throw new IllegalArgumentException(idErrorMsg);
            }
        }
        this.id = id;
        this.layoutType = type;
    }

    /**
     * Get the unique id of this object.
     * @return Unique id of the layout or null if this object does not provide a unique id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the custom {@link org.apache.cocoon.portal.layout.renderer.Renderer} for this layout.
     * @return The custom renderer or null.
     */
    public Renderer getCustomRenderer() {
        return this.customRenderer;
    }

    /**
     * The parent of this layout object or null.
     */
    public Item getParent() {
        return this.parent;
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
    public boolean isStatic() {
        return this.isStatic;
    }

    public void setCustomRenderer(Renderer value) {
        // TODO - we have to check if this renderer is allowed
        this.customRenderer = value;
    }

    public void setIsStatic(boolean value) {
        this.isStatic = value;
    }

    /**
     * Make a copy of this layout object and of all it's children.
     * This includes copies of items and coplet instances.
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
               "), type=" + this.layoutType + ", id=" + (this.getId() == null ? "" : this.getId());
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        Constructor c;
        try {
            c = this.getClass().getConstructor(new Class[] {String.class, String.class});
            final Layout clone = (Layout)c.newInstance(new Object[] {this.id, this.layoutType});

            // clone fields from AbstractParameters
            if ( this.parameters.size() > 0 ) {
                clone.parameters = new LinkedMap(this.parameters);
            }

            if ( this.temporaryAttributes.size() > 0 ) {
                clone.temporaryAttributes = new HashMap(this.temporaryAttributes);
            }

            // we don't clone the parent; we just set it to null
            clone.parent = null;
            clone.customRenderer = this.customRenderer;
            clone.isStatic = this.isStatic;

            return clone;
        } catch (NoSuchMethodException e) {
            throw new CloneNotSupportedException("Unable to find constructor for new layout object.");
        } catch (InstantiationException e) {
            throw new CloneNotSupportedException("Unable to create layout object.");
        } catch (InvocationTargetException e) {
            throw new CloneNotSupportedException("Unable to invoke constructor for new layout object.");
        } catch (IllegalArgumentException e) {
            throw new CloneNotSupportedException("Unable to invoke constructor for new layout object.");
        } catch (IllegalAccessException e) {
            throw new CloneNotSupportedException("Unable to invoke constructor for new layout object.");
        }
    }

    public LayoutType getLayoutType() {
        return this.layoutType;
    }

    /**
     * Get the renderer to render this layout.
     */
    public Renderer getRenderer() {
        if ( this.customRenderer != null ) {
            return this.customRenderer;
        }
        return this.layoutType.getDefaultRenderer();
    }
}
