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
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;

/**
 * Helper class for an coplet event
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CopletMapping.java,v 1.2 2004/03/05 13:02:09 bdelacretaz Exp $
*/
public class CopletMapping extends Mapping {
    public String copletId;
    public String path;

    public Event getEvent(PortalService service, Object data) {
        CopletInstanceData cid = service.getComponentManager().getProfileManager().getCopletInstanceData(this.copletId);
        Event e = new CopletJXPathEvent(cid, this.path, data);
        return e;
    }

}
