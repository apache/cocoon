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

import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;

/**
 * A link layout references another layout to be used instead. The reference
 * can be changed using events.
 *
 * @version $Id$
 */
public class LinkLayout extends Layout {

    protected String linkedLayoutKey;
    protected String linkedLayoutId;

    /**
     * Create a new link layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param name The name of the layout.
     */
    public LinkLayout(String id, String name) {
        super(id, name);
    }

    public void setLayoutId(String layoutId) {
        this.linkedLayoutId = layoutId;
    }

    public String getLayoutId() {
        return this.linkedLayoutId;
    }

    public String getLayoutKey() {
        return linkedLayoutKey;
    }

    public void setLayoutKey(String key) {
        linkedLayoutKey = key;
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        LinkLayout clone = (LinkLayout)super.clone();

        clone.linkedLayoutId = this.linkedLayoutId;
        clone.linkedLayoutKey = this.linkedLayoutKey;

        return clone;
    }
}
