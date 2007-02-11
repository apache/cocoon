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

import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;

/**
 * A coplet layout holds a coplet.
 *
 * @version $Id$
 */
public final class CopletLayout extends Layout {

    /** The coplet instance data. */
    protected CopletInstance copletInstanceData;

    /**
     * Create a new coplet layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param name The name of the layout.
     */
    public CopletLayout(String id, String name) {
        super(id, name);
    }

    public void setCopletInstanceData(CopletInstance cid) {
        this.copletInstanceData = cid;
    }

    public CopletInstance getCopletInstanceData() {
        return this.copletInstanceData;
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        CopletLayout clone = (CopletLayout)super.clone();

        clone.copletInstanceData = null;

        return clone;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#copy()
     */
    public Layout copy() {
        CopletLayout clone = (CopletLayout)super.copy();

        if ( this.copletInstanceData != null ) {
            clone.copletInstanceData = this.copletInstanceData.copy();
        }

        return clone;
    }
}
