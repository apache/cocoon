/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.coplet;

import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;

/**
 * This event changes the value of a coplet instance data.
 *
 * @version $Id$
 */
public class CopletJXPathEvent
    extends JXPathEvent
    implements CopletInstanceEvent {

    public CopletJXPathEvent(CopletInstance target, String path, Object value) {
        super( target, path, value );
    }

    /**
     * @see org.apache.cocoon.portal.event.CopletInstanceEvent#getTarget()
     */
    public CopletInstance getTarget() {
        return (CopletInstance)this.target;
    }

}
