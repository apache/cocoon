/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.layout;

import org.apache.cocoon.portal.event.LayoutEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.portal.layout.Layout;

/**
 * This event changes the value of a layout object.
 *
 * @version $Id$
 */
public class LayoutJXPathEvent
    extends JXPathEvent
    implements LayoutEvent {

    public LayoutJXPathEvent(Layout target, String path, Object value) {
        super( target, path, value );
    }

    /**
     * @see org.apache.cocoon.portal.event.LayoutEvent#getTarget()
     */
    public Layout getTarget() {
        return (Layout)this.target;
    }
}
