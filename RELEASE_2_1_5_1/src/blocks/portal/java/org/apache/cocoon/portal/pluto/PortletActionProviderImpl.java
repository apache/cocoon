/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

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
 * @version CVS $Id: PortletActionProviderImpl.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class PortletActionProviderImpl implements PortletActionProvider {

    /** The target */
    protected PortletWindow portletWindow;

    public PortletActionProviderImpl(PortletWindow portletWindow) {
        this.portletWindow = portletWindow;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletMode(PortletWindow, PortletMode)
     */
    public void changePortletMode(PortletMode mode) {
        if ( mode != null ) {
            final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            PortletMode pm = (PortletMode) cid.getAttribute("portlet-mode");
            if ( (pm == null && !mode.equals(PortletMode.VIEW)) 
                || (pm != null && !pm.equals(mode)) ) {
                if ( pm != null ) {
                    cid.setAttribute("previous-portlet-mode", pm);
                }
                cid.setAttribute("portlet-mode", mode);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletActionProvider#changePortletWindowState(PortletWindow, WindowState)
     */
    public void changePortletWindowState(WindowState state) {
        if ( state != null ) {
            final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
            WindowState ws = (WindowState) cid.getAttribute("window-state");
            if ( (ws == null && !state.equals(PortletMode.VIEW)) 
                || (ws != null && !ws.equals(state)) ) {
                if ( ws != null ) {
                    cid.setAttribute("previous-window-state", ws);
                }
                cid.setAttribute("window-state", state);
            }
        }
    }

}
