/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
 include  the following  acknowledgment:  "This product includes  software
 developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 Alternately, this  acknowledgment may  appear in the software itself,  if
 and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
 used to  endorse or promote  products derived from  this software without
 prior written permission. For written permission, please contact
 apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
 "Apache" appear  in their name,  without prior written permission  of the
 Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * @version CVS $Id: PortletActionProviderImpl.java,v 1.1 2004/01/22 14:01:21 cziegeler Exp $
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
