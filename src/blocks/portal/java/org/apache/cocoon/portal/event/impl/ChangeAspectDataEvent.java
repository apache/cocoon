/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: ChangeAspectDataEvent.java,v 1.5 2003/07/03 09:26:02 cziegeler Exp $
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
