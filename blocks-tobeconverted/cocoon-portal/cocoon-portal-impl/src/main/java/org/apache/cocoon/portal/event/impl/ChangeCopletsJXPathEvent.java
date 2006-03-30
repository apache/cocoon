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
package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.event.CopletDataEvent;

/**
 * This event changes the value of all instances of a coplet data.
 *
 * @version $Id$
 */
public class ChangeCopletsJXPathEvent
    implements CopletDataEvent {

    protected String path;
    protected Object value;
    protected CopletData target;

    /**
     * Constructor
     * @param target The coplet data
     * @param path   The path for the instance data
     * @param value  The value to set
     */
    public ChangeCopletsJXPathEvent(CopletData target, String path, Object value) {
        this.path = path;
        this.value = value;
        this.target = target;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @see org.apache.cocoon.portal.event.CopletDataEvent#getTarget()
     */
    public CopletData getTarget() {
        return this.target;
    }
}
