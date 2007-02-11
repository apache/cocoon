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
package org.apache.cocoon.portal.pluto;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.adapter.PortletAdapter;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletActionProvider;

/**
 *
 *
 * @version $Id$
 */
public class PortletActionProviderImpl implements PortletActionProvider {

    /** The target. */
    protected final PortletWindow portletWindow;

    /** The portal service. */
    protected final PortalService portalService;

    /**
     * Constructor.
     */
    public PortletActionProviderImpl(PortletWindow portletWindow, PortalService service) {
        this.portletWindow = portletWindow;
        this.portalService = service;
    }

    /**
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletMode(javax.portlet.PortletMode)
     */
    public void changePortletMode(PortletMode mode) {
        if ( mode != null ) {
            final CopletInstance cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            cid.setTemporaryAttribute(PortletAdapter.PORTLET_MODE_ATTRIBUTE_NAME, mode.toString());
        }
    }

    /**
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletWindowState(javax.portlet.WindowState)
     */
    public void changePortletWindowState(WindowState state) {
        if ( state != null ) {
            final CopletInstance cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            cid.setTemporaryAttribute(PortletAdapter.WINDOW_STATE_ATTRIBUTE_NAME, state.toString());
            int size = CopletInstance.SIZE_NORMAL;
            if ( state.equals(WindowState.MAXIMIZED) ) {
                size = CopletInstance.SIZE_MAXIMIZED;
            } else if ( state.equals(WindowState.MINIMIZED) ) {
                size = CopletInstance.SIZE_MINIMIZED;
            }
            if ( size != cid.getSize() ) {
                final Event e = new CopletInstanceSizingEvent(cid, size);
                this.portalService.getEventManager().send(e);
            }
        }
    }

    public void changeRenderParameters(Map parameters) {
        final CopletInstance cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        if ( parameters == null ) {
            cid.removeTemporaryAttribute("render-parameters");
        } else {
            cid.setTemporaryAttribute("render-parameters", new HashMap(parameters));
        }
    }
}
