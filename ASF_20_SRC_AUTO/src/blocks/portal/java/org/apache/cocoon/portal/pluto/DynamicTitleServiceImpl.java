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

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.title.DynamicTitleService;

/**
 * Our own dynamic title service
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DynamicTitleServiceImpl.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class DynamicTitleServiceImpl 
implements DynamicTitleService {    

    /* (non-Javadoc)
     * @see org.apache.pluto.services.title.DynamicTitleService#setDynamicTitle(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public void setDynamicTitle(PortletWindow window,
                                HttpServletRequest request,
                                String dynamicTitle) {
        ((PortletEntityImpl)window.getPortletEntity()).getCopletInstanceData().setAttribute("dynamic-title", dynamicTitle);
    }
                                
}
