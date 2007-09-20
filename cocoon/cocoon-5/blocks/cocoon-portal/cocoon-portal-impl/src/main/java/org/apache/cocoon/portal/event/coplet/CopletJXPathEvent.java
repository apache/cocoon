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
package org.apache.cocoon.portal.event.coplet;

import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.portal.om.CopletInstance;

/**
 * This event changes the value of a coplet instance data.
 *
 * @version $Id$
 */
public class CopletJXPathEvent
    implements CopletInstanceEvent, JXPathEvent {

    protected final CopletInstance target;
    protected final String path;
    protected final Object value;

    public CopletJXPathEvent(CopletInstance target, String path, Object value) {
        this.target = target;
        this.path = path;
        this.value = value;
    }

    /**
     * @see org.apache.cocoon.portal.event.CopletInstanceEvent#getTarget()
     */
    public CopletInstance getTarget() {
        return this.target;
    }

    /**
     * @see org.apache.cocoon.portal.event.impl.JXPathEvent#getObject()
     */
    public Object getObject() {
        return this.getTarget();
    }

    /**
     * @see org.apache.cocoon.portal.event.impl.JXPathEvent#getPath()
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @see org.apache.cocoon.portal.event.impl.JXPathEvent#getValue()
     */
    public Object getValue() {
        return this.value;
    }
}
