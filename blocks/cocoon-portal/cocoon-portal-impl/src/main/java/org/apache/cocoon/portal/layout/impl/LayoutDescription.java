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
package org.apache.cocoon.portal.layout.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cocoon.portal.layout.LayoutFactory;


/**
 * A configured layout.
 * This description is used to instantiate new layout objects by the {@link LayoutFactory}.
 * A layout has an associated class and an associated type. The type is used to differentiate
 * layout objects having the same class but providing different features. For example
 * a composite layout can either be a row or a column - both use the same implementation class
 * but have different types.
 *
 * @version $Id$
 */
public class LayoutDescription {

    protected String className;

    protected String type;

    protected boolean createId = true;

    protected String defaultRendererName;

    protected List rendererNames = new ArrayList(2);

    protected String itemClassName;

    protected boolean defaultIsStatic = false;

    /**
     * This is the name of the renderer used by default to render this layout object.
     * @return the default renderer name
     */
    public String getDefaultRendererName() {
        return defaultRendererName;
    }

    /**
     * @param string
     */
    public void setDefaultRendererName(String string) {
        defaultRendererName = string;
    }

    /**
     * Each layout can have several associated renderers.
     * @return the names of all allowed renderers.
     */
    public Collection getRendererNames() {
        return this.rendererNames;
    }

    public void addRendererName(String rendererName) {
        this.rendererNames.add( rendererName );
    }

    /**
     * Each composite layout object can contain items. This is the class name
     * of the item implementation.
     * @return The class name of the item.
     */
    public String getItemClassName() {
        return this.itemClassName;
    }

    /**
     * @param itemClassName The itemClassName to set.
     */
    public void setItemClassName(String itemClassName) {
        this.itemClassName = itemClassName;
    }

    /**
     * The name of the implementation class for this layout object.
     * @return The class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * The associated type for this layout object.
     * @return The configured type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param string
     */
    public void setClassName(String string) {
        className = string;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        type = string;
    }

    /**
     * Should the layout factory create a unique id for objects of this type?
     */
    public boolean createId() {
        return this.createId;
    }

    public void setCreateId(boolean value) {
        this.createId = value;
    }

    /**
     * Default setting for static.
     */
    public boolean defaultIsStatic() {
        return this.defaultIsStatic;
    }

    public void setDefaultIsStatic(boolean value) {
        this.defaultIsStatic = value;
    }
}
