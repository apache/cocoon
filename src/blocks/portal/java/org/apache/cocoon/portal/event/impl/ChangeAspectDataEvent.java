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

import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.event.ActionEvent;
import org.apache.cocoon.portal.event.ComparableEvent;
import org.apache.cocoon.portal.event.RequestEvent;

/**
 * This events set the aspect data for an {@link Aspectalizable} object
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ChangeAspectDataEvent.java,v 1.6 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class ChangeAspectDataEvent
    extends AbstractActionEvent
    implements ActionEvent, RequestEvent, ComparableEvent {

    protected String aspectName;
    
    protected Object data;
    
    protected String requestParameterName;
    
    
    public ChangeAspectDataEvent(Aspectalizable target, String aspectName, Object data) {
        super(target);
        this.aspectName = aspectName;
        this.data = data;
    }

    /**
     * @return The aspect name
     */
    public String getAspectName() {
        return this.aspectName;
    }

    /**
     * @return The value to set
     */
    public Object getData() {
        return this.data;
    }

    /**
     * @return The target to change
     */
    public Object getTarget() {
        return this.target;
    }


    public Aspectalizable getAspectalizable() {
        return (Aspectalizable)this.target;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.RequestEvent#getRequestParameterName()
     */
    public String getRequestParameterName() {
        return this.requestParameterName;
    }

    public void setRequestParameterName(String value) {
        this.requestParameterName = value;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.ComparableEvent#equalsEvent(org.apache.cocoon.portal.event.ComparableEvent)
     */
    public boolean equalsEvent(ComparableEvent event) {
        if ( event instanceof ChangeAspectDataEvent ) {
            ChangeAspectDataEvent other = (ChangeAspectDataEvent)event;
            return (this.getTarget().equals(other.getTarget())
                     && this.getAspectName().equals(other.getAspectName()));
        }

        return false;
    }

}
