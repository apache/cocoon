/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om;

import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletWindowImpl.java,v 1.5 2004/03/16 15:56:43 cziegeler Exp $
 */
public class PortletWindowImpl implements PortletWindow, PortletWindowCtrl {
        
    
    private ObjectID objectId;
    private String id;
    private PortletEntity portletEntity;
    private CopletLayout layout;
    
    public PortletWindowImpl(String id) {
        this.id = id;
    }

    // PortletWindow implementation.

     /**
     * Returns the identifier of this portlet instance window as object id
     *
     * @return the object identifier
     **/
    public ObjectID getId()
    {
        if (objectId==null)
        {
            objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(id);
        }
        return objectId;
    }
    
    /**
     * Returns the portlet entity
     *
     * @return the portlet entity
     **/
    public PortletEntity getPortletEntity()
    {
        return portletEntity;
    }

    // PortletWindowCtrl implementation.
    /**
     * binds an identifier to this portlet window
     *
     * @param id the new identifier
     */
    public void setId(String id)
    {
        this.id = id;
        objectId = null;
    }
    
    /**
     * binds a portlet instance to this portlet window
     * 
     * @param portletEntity a portlet entity object
     **/
    public void setPortletEntity(PortletEntity portletEntity) {
        this.portletEntity = portletEntity;
    }

    public CopletLayout getLayout() {
        return this.layout;
    }
    
    public void setLayout(CopletLayout layout) {
        this.layout = layout;
    }
}