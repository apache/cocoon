/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.acting.helpers;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.portal.layout.Layout;

/**
 * Helper class for a layout event
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: LayoutMapping.java,v 1.2 2004/03/05 13:02:09 bdelacretaz Exp $
*/
public class LayoutMapping extends Mapping {
    public String layoutId;
    public String path;
    
    public Event getEvent(PortalService service, Object data) {
        Layout layout = service.getComponentManager().getProfileManager().getPortalLayout(null, this.layoutId);
        Event e = new JXPathEvent(layout, this.path, data);
        return e;
    }
}
