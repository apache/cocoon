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

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.event.CopletDataEvent;

/**
 * This event changes the value of all instances of a coplet data
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ChangeCopletsJXPathEvent.java,v 1.2 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class ChangeCopletsJXPathEvent
extends AbstractActionEvent 
implements CopletDataEvent {

    protected String path;
    protected Object value;
    
    /**
     * Constructor
     * @param target The coplet data
     * @param path   The path for the instance data
     * @param value  The value to set
     */
    public ChangeCopletsJXPathEvent(CopletData target, String path, Object value) {
        super( target );
        this.path = path;
        this.value = value;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return this.value;
    }

}
