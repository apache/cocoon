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

import java.util.Iterator;


/**
 * A configured layout.
 * This description is used to instantiate new layout objects by the {@link LayoutFactory}.
 * A layout has an associated class and an associated name. The name is used to differentiate
 * layout objects having the same class but providing different features. For example
 * a composite layout can either be a row or a column - both use the same implementation class
 * but have different names.
 *
 * @version $Id$
 */
public interface LayoutDescription {

    /**
     * The name of the implementation class for this layout object.
     * @return The class name.
     */
    String getClassName();

    /**
     * The associated name for this layout object.
     * @return The configured name.
     */
    String getName();

    /**
     * Should the layout factory create a unique id for objects of this type?
     */
    boolean createId();

    /**
     * Default setting for static.
     */
    boolean defaultIsStatic();

    /**
     * This is the name of the renderer used by default to render this layout object.
     * @return the default renderer name
     */
    String getDefaultRendererName();

    /**
     * Each layout can have several associated renderers.
     * @return the names of all allowed renderers.
     */
    Iterator getRendererNames();

    /**
     * Each composite layout object can contain items. This is the class name
     * of the item implementation.
     * @return The class name of the item.
     */
    String getItemClassName();
}
