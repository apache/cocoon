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
package org.apache.cocoon.components.persistence;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * The default implementation
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: RequestDataStoreImpl.java,v 1.5 2004/03/08 14:01:56 cziegeler Exp $
 * @since 2.1.1
 * 
 * @avalon.component
 * @avalon.service type="RequestDataStore"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="request-data-store"
 */
public class RequestDataStoreImpl
    extends AbstractLogEnabled
    implements RequestDataStore, Contextualizable {
        
    protected Context context;

    protected final String requestDataKey = this.getClass().getName() + "/RD";
    
    protected final String globalRequestDataKey = this.getClass().getName() + "/GRD";

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#getGlobalRequestData(java.lang.String)
     */
    public Object getGlobalRequestData(String key) {
        Object value = null;
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m != null ) {
            value = m.get( key );
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#getRequestData(java.lang.String)
     */
    public Object getRequestData(String key) {
        Object value = null;
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m != null ) {
            value = m.get( key );
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#removeGlobalRequestData(java.lang.String)
     */
    public void removeGlobalRequestData(String key) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m != null ) {
            objectModel.remove( key );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#removeRequestData(java.lang.String)
     */
    public void removeRequestData(String key) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m != null ) {
            objectModel.remove( key );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#setGlobalRequestData(java.lang.String, java.lang.Object)
     */
    public void setGlobalRequestData(String key, Object value) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m == null ) {
            m = new HashMap();
            objectModel.put(this.globalRequestDataKey, m);
        }
        m.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#setRequestData(java.lang.String, java.lang.Object)
     */
    public void setRequestData(String key, Object value) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m == null ) {
            m = new HashMap();
            objectModel.put(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode(), m);
        }
        m.put(key, value);
    }

}
