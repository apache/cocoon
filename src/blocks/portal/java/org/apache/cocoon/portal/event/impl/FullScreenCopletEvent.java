/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.RequestEvent;
import org.apache.cocoon.portal.layout.Layout;

/**
 * EventSource: copletID
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: FullScreenCopletEvent.java,v 1.2 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class FullScreenCopletEvent 
    extends CopletStatusEvent 
    implements RequestEvent {

    public static final String REQUEST_PARAMETER_NAME = "cocoon-portal-fs";
    
    protected Layout layout;
    
    public FullScreenCopletEvent(CopletInstanceData data, Layout layout) {
        this.coplet = data;
        this.layout = layout;
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

}
