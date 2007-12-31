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
package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.RequestEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.PortalService;

/**
 * EventSource: copletID
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 *
 * @version CVS $Id$
 */
public class FullScreenCopletEvent
    extends CopletStatusEvent
    implements RequestEvent, ConvertableEvent {

    public static final String REQUEST_PARAMETER_NAME = "cocoon-portal-fs";

    protected Layout layout;

    public FullScreenCopletEvent(CopletInstanceData data, Layout layout) {
        this.coplet = data;
        this.layout = layout;
    }

    FullScreenCopletEvent(PortalService service, String eventData) {
        int index = eventData.indexOf('_');
        String copletId;
        if (index > 0) {
            copletId = eventData.substring(0, index);
            this.coplet =
                service.getComponentManager().getProfileManager().getCopletInstanceData(copletId);
            if (eventData.length() > index + 1) {
                String layoutId = eventData.substring(index + 1);
                this.layout =
                    service.getComponentManager().getProfileManager().getPortalLayout(null, layoutId);
            }
        } else {
            this.layout = null;
            this.coplet =
                service.getComponentManager().getProfileManager().getCopletInstanceData(eventData);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.RequestEvent#getRequestParameterName()
     */
    public String getRequestParameterName() {
        return REQUEST_PARAMETER_NAME;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public String asString() {
        if (this.layout == null) {
            return this.coplet.getId();
        }
        String layoutId = this.layout.getId();
        if (layoutId == null) {
            // Without a layout id this can't be marshalled.
            return null;
        }
        return this.coplet.getId() + "_" + layoutId;
    }
}