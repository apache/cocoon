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

/**
 * TODO - Convert this in an abstract class which all implementations
 * have to extend (we can use AbstractLayout for this).
 *
 * A layout describes a graphical element on the portal page. This can
 * be an element containing others ({@link CompositeLayout}) or a final
 * element, like a window for a coplet.
 *
 * Parameters and temporary attributes:
 * A parameter is a key-value pair consisting of strings for
 * both key and value. While the parameters are persisted,
 * the temporary attributes have only the life-time of a
 * session, but can contain any object.
 *
 * @version $Id$
 */
public interface Layout {

    /**
     * The name given from the factory.
     */
    String getName();

    /**
     * Get the unique id of this object.
     * @return Unique id of the layout or null if this object does not provide a unique id.
     */
    String getId();

    /**
     * Set the layout description.
     */
    void setDescription(LayoutDescription description);

    /**
     * Get parameters map.
     * This method never returns null.
     * @return A map with key value pairs.
     */
    Map getParameters();

    /**
     * Return the parameter value for the given key.
     * @param key The name of the parameter.
     * @return The value of the parameter or null.
     */
    String getParameter(String key);

    /**
     * Set the parameter to a value.
     * @param key The name of the parameter.
     * @param value The value.
     */
    void setParameter(String key, String value);

    /**
     * Remove the parameter.
     * @param key The name of the parameter.
     * @return The old value for the parameter or null.
     */
    String removeParameter(String key);
    
    /**
     * Get the temporary attributes map.
     * This method never returns null.
     * @return A map with key value pairs.
     */
    Map getTemporaryAttributes();

    /**
     * Return the temporary attribute value for the given key.
     * @param key The name of the attribute.
     * @return The value of the attribute or null.
     */
    Object getTemporaryAttribute(String key);

    /**
     * Set the temporary attribute to a value.
     * @param key The name of the attribute.
     * @param value The value.
     */
    void setTemporaryAttribute(String key, Object value);

    /**
     * Remove the attribute.
     * @param key The name of the attribute.
     * @return The old value for the attribute or null.
     */
    Object removeTemporaryAttribute(String key);

    /**
     * Get the name of the {@link org.apache.cocoon.portal.layout.renderer.Renderer} to draw this layout.
     * If this layout has an own renderer {@link #getLayoutRendererName()}
     * return this, otherwise the default renderer is returned.
     * @return String The role name
     */
    String getRendererName();

    /** 
     * Get the name of a custom {@link org.apache.cocoon.portal.layout.renderer.Renderer} for this layout.
     * @return String The role name
     */
    String getLayoutRendererName();

    /**
     * The parent of this layout object or null.
     */
    Item getParent();

    /**
     * Set the parent item for this layout.
     */
    void setParent(Item item);

    /**
     * Make a copy of this layout object and of all it's children.
     * This includes copies of items and copletinstancedatas.
     */
    Layout copy();

    /**
     * Is this layout static?
     * If a layout is static, it is not removed when another layout
     * is max paged.
     */
   Boolean isStatic();
}
