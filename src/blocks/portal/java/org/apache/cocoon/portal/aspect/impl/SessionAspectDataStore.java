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
package org.apache.cocoon.portal.aspect.impl;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.Aspectalizable;

/**
 * An aspect data store is a component that manages aspect data objects.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: SessionAspectDataStore.java,v 1.6 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class SessionAspectDataStore 
    extends AbstractLogEnabled
    implements Component, ThreadSafe, AspectDataStore, Contextualizable {
    
    protected Context context;
    
    protected String getKey(Aspectalizable owner, String aspectName) {
        StringBuffer buffer = new StringBuffer(this.getClass().getName());
        buffer.append('/');
        buffer.append(owner.getClass().getName());
        buffer.append('/');
        buffer.append(owner.hashCode());
        buffer.append('/');
        buffer.append(aspectName);
        return buffer.toString();
    }
    
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        final Session session = ContextHelper.getRequest(this.context).getSession();
        return session.getAttribute( this.getKey( owner, aspectName ) );
    }
    
    public void setAspectData(Aspectalizable owner, String aspectName, Object data) {
        final Session session = ContextHelper.getRequest(this.context).getSession();
        if ( data == null ) {
            session.removeAttribute( this.getKey( owner, aspectName) );
        } else {
            session.setAttribute( this.getKey( owner, aspectName), data );
        }
    }

    public boolean isPersistent() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;

    }

}
