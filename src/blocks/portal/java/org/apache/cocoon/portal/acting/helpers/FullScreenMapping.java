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
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Helper class for a full screen event
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CopletMapping.java 30941 2004-07-29 19:56:58Z vgritsenko $
*/
public class FullScreenMapping extends Mapping {
    public String copletId;
    public String layoutId;
    
    public Event getEvent(PortalService service, Object data) {
        final ProfileManager manager = service.getComponentManager().getProfileManager();
        final CopletInstanceData cid = manager.getCopletInstanceData(this.copletId);
        final Layout layout = manager.getPortalLayout(null, layoutId) ;
        
        Event e = new FullScreenCopletEvent(cid, layout);
        return e;
    }

}
