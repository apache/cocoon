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
package org.apache.cocoon.portal.aspect.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.aspect.AspectDataHandler;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.aspect.AspectalizableDescription;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultAspectDataHandler.java,v 1.7 2004/03/10 12:28:29 cziegeler Exp $
 */
public class DefaultAspectDataHandler 
    implements AspectDataHandler {

    protected AspectalizableDescription description;
    
    protected ServiceSelector storeSelector;
    
    /**
     * Constructor
     */
    public DefaultAspectDataHandler(AspectalizableDescription desc,
                                    ServiceSelector storeSelector) {
        this.description = desc;
        this.storeSelector = storeSelector;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String)
     */
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        // is this aspect allowed?
        AspectDescription aspectDesc = this.description.getAspectDescription( aspectName );
        if ( aspectDesc == null ) return null;
        
        // lookup storage
        AspectDataStore store = null;
        Object data = null;
        try {
            store = (AspectDataStore)this.storeSelector.select(aspectDesc.getStoreName());
            data = store.getAspectData(owner, aspectName);

            if ( data == null && aspectDesc.isAutoCreate() ) {
                data = AspectUtil.createNewInstance(aspectDesc);
                store.setAspectData( owner, aspectName, data );
            }

        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup aspect data store " + aspectDesc.getStoreName(), ce);
        } finally {
            this.storeSelector.release( store );
        }        

        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getAspectDatas(org.apache.cocoon.portal.aspect.Aspectalizable)
     */
    public Map getAspectDatas(Aspectalizable owner)  {
        AspectDatasHashMap datas = new AspectDatasHashMap(owner, this);
        Iterator iter = this.description.getAspectDescriptions().iterator();
        while ( iter.hasNext() ) {
            AspectDescription current = (AspectDescription)iter.next();
            Object data = this.getAspectData(owner, current.getName());
            if ( data != null ) {
                datas.put( current.getName(), data );
            }
        }
        datas.initialize();
        return datas;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getPersistentAspectDatas(org.apache.cocoon.portal.aspect.Aspectalizable)
     */
    public Map getPersistentAspectDatas(Aspectalizable owner)  {
        Map datas = new HashMap();
        Iterator iter = this.description.getAspectDescriptions().iterator();
        while ( iter.hasNext() ) {
            AspectDescription current = (AspectDescription)iter.next();

            // lookup storage
            AspectDataStore store = null;
            Object data = null;
            try {
                store = (AspectDataStore)this.storeSelector.select(current.getStoreName());
                if ( store.isPersistent() ) {
                    data = store.getAspectData(owner, current.getName());

                    if ( data == null && current.isAutoCreate() ) {
                        data = AspectUtil.createNewInstance(current);
                        store.setAspectData( owner, current.getName(), data );
                    }

                    if ( data != null ) {
                        datas.put( current.getName(), data );
                    }
                }

            } catch (ServiceException ce) {
                throw new CascadingRuntimeException("Unable to lookup aspect data store " + current.getStoreName(), ce);
            } finally {
                this.storeSelector.release( store );
            }        

        }
        return datas;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#setAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String, java.lang.Object)
     */
    public void setAspectData(Aspectalizable owner,
                               String aspectName,
                               Object data) {
        // is this aspect allowed?
        AspectDescription aspectDesc = this.description.getAspectDescription( aspectName );
        if ( aspectDesc == null ) return;

        // lookup storage
        AspectDataStore store = null;
        try {
            store = (AspectDataStore)this.storeSelector.select(aspectDesc.getStoreName());
            store.setAspectData(owner, aspectName, AspectUtil.convert(aspectDesc, data));
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup aspect data store " + aspectDesc.getStoreName(), ce);
        } finally {
            this.storeSelector.release( store );
        }        
    }

    /**
     * Is this supported
     */
    public boolean isAspectSupported(String aspectName) {
        return (this.description.getAspectDescription(aspectName) != null);
    }
}

final class AspectDatasHashMap extends HashMap {
    
    protected AspectDataHandler handler;
    protected Aspectalizable owner;
    protected boolean init = false;
    
    public AspectDatasHashMap(Aspectalizable owner, AspectDataHandler handler) {
        this.handler = handler;
        this.owner = owner;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        if ( this.init ) {
            this.handler.setAspectData(this.owner, key.toString(), value);
            value = this.handler.getAspectData(this.owner, key.toString());
        }
        return super.put(key, value);
    }

    public void initialize() {
        this.init = true;
    }
}
