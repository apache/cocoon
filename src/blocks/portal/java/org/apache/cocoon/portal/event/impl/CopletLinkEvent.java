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
import org.apache.cocoon.portal.event.CopletInstanceEvent;

/**
 * This class realizes a link event created by the EventLinkTransformer.
 *  
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletLinkEvent.java,v 1.3 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class CopletLinkEvent
extends AbstractActionEvent
implements CopletInstanceEvent {
    
    /**
     * The link to be handled by this event.
     */
    protected String link;
    
    /**
     * Creates a new LinkEvent.
     */
    public CopletLinkEvent(CopletInstanceData target, String link) {
        super(target);
        this.link = link;
    }
    
    /**
     * Gets this event's link.
     */
    public String getLink() {
        return this.link;
    }
}
