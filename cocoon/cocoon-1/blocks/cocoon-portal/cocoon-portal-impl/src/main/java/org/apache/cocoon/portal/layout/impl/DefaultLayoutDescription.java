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
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.layout.LayoutDescription;


/**
 * A configured layout.
 *
 * @version $Id$
 */
public class DefaultLayoutDescription
    implements LayoutDescription  {

    protected String className;

    protected String name;

    protected boolean createId = true;

    protected String defaultRendererName;

    protected List rendererNames = new ArrayList(2);

    protected String itemClassName;

    protected boolean defaultIsStatic = false;

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
     * @see org.apache.cocoon.portal.layout.LayoutDescription#getRendererNames()
     */
    public Iterator getRendererNames() {
        return this.rendererNames.iterator();
    }

    public void addRendererName(String rendererName) {
        this.rendererNames.add( rendererName );
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutDescription#getItemClassName()
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
     * @see org.apache.cocoon.portal.layout.LayoutDescription#getClassName()
     */
    public String getClassName() {
        return className;
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutDescription#getName()
     */
    public String getName() {
        return name;
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
    public void setName(String string) {
        name = string;
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutDescription#createId()
     */
    public boolean createId() {
        return this.createId;
    }

    public void setCreateId(boolean value) {
        this.createId = value;
    }

    /**
     * @see org.apache.cocoon.portal.layout.LayoutDescription#defaultIsStatic()
     */
    public boolean defaultIsStatic() {
        return this.defaultIsStatic;
    }

    public void setDefaultIsStatic(boolean value) {
        this.defaultIsStatic = value;
    }
}
