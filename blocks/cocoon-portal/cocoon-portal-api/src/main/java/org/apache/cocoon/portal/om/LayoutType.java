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
import java.util.List;



/**
 * A configured layout type.
 * Layout types a per portal configurations and define the possible layout objects for
 * creating the portal pages. Ususally a portal has layout types for structuring like
 * columns or rows and layout types for content like coplets.
 * A layout type requires a class name which has to be of type {@link Layout}.
 *
 * @version $Id$
 */
public class LayoutType {

    /** The class name of the layout object. */
    protected String layoutClassName;

    /** Should an ID be generated for this layout object. */
    protected boolean createId = false;

    /** The class name of the item (if a special class is used) for composite layouts. */
    protected String itemClassName;

    /** The renderers for this layout object. */
    protected List renderers = Collections.EMPTY_LIST;

    /** TODO */
    protected boolean defaultIsStatic = false;

    /** The unique id of this layout type. */
    protected final String id;

    public LayoutType(String typeId) {
        this.id = typeId;
    }

    public Renderer getDefaultRenderer() {
        return (Renderer) renderers.get(0);
    }

    /**
     * Each layout can have several associated renderers.
     * @return the names of all allowed renderers.
     */
    public List getRenderers() {
        return this.renderers;
    }

    public void setRenderers(List r) {
        this.renderers = r;
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
    public String getLayoutClassName() {
        return this.layoutClassName;
    }

    /**
     * @param string
     */
    public void setLayoutClassName(String string) {
        this.layoutClassName = string;
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

    /**
     * Return the unique id of this layout type.
     * @return A non-null identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Create a new item which can be used with all layout objects of this type.
     */
    public Item createItem()
    throws LayoutException {
        if ( this.itemClassName == null ) {
            return new Item();
        }
        try {
            return (Item) Thread.currentThread().getContextClassLoader().loadClass(this.itemClassName).newInstance();
        } catch (Exception e ) {
            throw new LayoutException("Unable to create new item for layout type " + this + " of class" + this.itemClassName, e);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "LayoutType (" + this.hashCode() +
               "), id=" + this.getId() + ", layout-class=" + this.getLayoutClassName();
    }
}
