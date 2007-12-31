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

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletActionProvider;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class PortletActionProviderImpl implements PortletActionProvider {

    /** The target */
    protected PortletWindow portletWindow;

    public PortletActionProviderImpl(PortletWindow portletWindow) {
        this.portletWindow = portletWindow;
    }

    /**
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletMode(PortletWindow, PortletMode)
     */
    public void changePortletMode(PortletMode mode) {
        if ( mode != null ) {
            final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            cid.setTemporaryAttribute("portlet-mode", mode.toString());
        }
    }

    /**
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletWindowState(PortletWindow, WindowState)
     */
    public void changePortletWindowState(WindowState state) {
        if ( state != null ) {
            final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            cid.setTemporaryAttribute("window-state", state.toString());
        }
    }

    public void changeRenderParameters(Map parameters) {
        final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        if ( parameters == null ) {
            cid.removeTemporaryAttribute("render-parameters");
        } else {
            cid.setTemporaryAttribute("render-parameters", new HashMap(parameters));
        }
    }
}
