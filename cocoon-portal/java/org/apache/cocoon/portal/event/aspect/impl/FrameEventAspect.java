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
package org.apache.cocoon.portal.event.aspect.impl;

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.layout.LayoutChangeParameterEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.FrameLayout;

/**
 *
 * @version $Id$
 */
public class FrameEventAspect extends AbstractContentEventAspect {

    protected String getRequestParameterName() {
        // TODO - make this configurable
        return "frame";
    }

    protected int getRequiredValueCount() {
        return 3;
    }

    /**
     * @see org.apache.cocoon.portal.event.aspect.impl.AbstractContentEventAspect#publish(EventManager, org.apache.cocoon.portal.layout.Layout, java.lang.String[])
     */
    protected void publish(EventManager publisher,
                           Layout layout,
                           String[] values) {
        if (layout instanceof FrameLayout) {
            final Event e = new LayoutChangeParameterEvent(layout, "frame", values[2], true);
            publisher.send(e);
        } else {
            this.getLogger().warn(
                "the configured layout: "
                    + layout.getName()
                    + " is not a FrameLayout.");
        }
    }
}
