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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.Aspectalizable;

/**
 * An aspect data store is a component that manages aspect data objects.
 * This store holds the data for the current request.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: TemporaryAspectDataStore.java,v 1.2 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class TemporaryAspectDataStore 
    extends AbstractLogEnabled
    implements Component, Serviceable, ThreadSafe, AspectDataStore, Contextualizable {
    
    protected Context context;
    
    protected ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    protected Map getMap(Aspectalizable owner) {
        final Request request = ContextHelper.getRequest(this.context);
        Map componentMap = (Map)request.getAttribute(this.getClass().getName());
        if ( componentMap == null) {
            componentMap = new HashMap(3);
            request.setAttribute(this.getClass().getName(), componentMap);
        }
        Map ownerMap = (Map)componentMap.get( owner );
        if ( ownerMap == null ) {
            ownerMap = new HashMap(3);
            componentMap.put( owner, ownerMap );
        }
        return ownerMap;
    }
    
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        return this.getMap(owner).get( aspectName );
    }
    
    public void setAspectData(Aspectalizable owner, String aspectName, Object data) {
        this.getMap(owner).put(aspectName, data);
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
