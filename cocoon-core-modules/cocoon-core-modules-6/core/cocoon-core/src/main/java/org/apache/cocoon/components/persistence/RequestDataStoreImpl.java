/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * The default implementation
 *
 * @version $Id$
 * @since 2.1.1
 * @deprecated Use the scoped attributes on the Request object instead.
 *             This component will be removed with Cocoon 2.3.
 */
public class RequestDataStoreImpl extends AbstractLogEnabled
                                  implements ThreadSafe, RequestDataStore, Contextualizable {
        
    protected final String requestDataKey = getClass().getName() + "/RD";
    protected final String globalRequestDataKey = getClass().getName() + "/GRD";

    protected Context context;

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
