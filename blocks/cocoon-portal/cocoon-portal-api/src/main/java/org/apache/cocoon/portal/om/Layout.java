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

import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.util.PortalUtils;

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
 * class provides a one string constructor which calls {@link #Layout(String)}.
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
    protected LayoutType layoutType;

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
     * @see PortalUtils#testId(String)
     */
    public Layout(String id) {
        // check id, null for id is allowed!
        if ( id != null ) {
            final String idErrorMsg = PortalUtils.testId(id);
            if ( idErrorMsg != null ) {
                throw new IllegalArgumentException(idErrorMsg);
            }
        }
        this.id = id;
    }

    /**
     * Get the unique id of this object.
     * @return Unique id of the layout or null if this object does not provide a unique id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the custom {@link org.apache.cocoon.portal.om.Renderer} for this layout.
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Layout (" + this.getClass() + '.' + this.hashCode() +
               "), type=" + this.layoutType + ", id=" + (this.getId() == null ? "" : this.getId());
    }

    /**
     * The type of the layout object.
     * @return A layout type.
     */
    public LayoutType getLayoutType() {
        return this.layoutType;
    }

    /**
     * Set the type of the layout object.
     */
    public void setLayoutType(final LayoutType t) {
        this.layoutType = t;
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
