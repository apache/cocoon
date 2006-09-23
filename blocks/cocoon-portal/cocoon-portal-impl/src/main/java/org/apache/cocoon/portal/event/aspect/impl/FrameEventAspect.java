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
package org.apache.cocoon.portal.event.aspect.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.layout.LayoutInstanceChangeAttributeEvent;
import org.apache.cocoon.portal.om.FrameLayout;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.om.LayoutInstance;

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
        return 2;
    }

    /**
     * @see org.apache.cocoon.portal.event.aspect.impl.AbstractContentEventAspect#publish(PortalService, org.apache.cocoon.portal.om.Layout, java.lang.String[])
     */
    protected void publish(PortalService service,
                           Layout        layout,
                           String[]      values) {
        if (layout instanceof FrameLayout) {
            LayoutInstance instance;
            instance = LayoutFeatures.getLayoutInstance(service, layout, false);
            if ( instance == null ) {
                final Event e = new LayoutInstanceChangeAttributeEvent(instance, "frame", values[2], true);
                service.getEventManager().send(e);                    
            }
        } else {
            this.getLogger().warn(
                "The configured layout: "
                    + layout.getType()
                    + " is not a FrameLayout.");
        }
    }
}
