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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityList;
import org.apache.pluto.om.entity.PortletEntityListCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletEntityListImpl.java,v 1.3 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class PortletEntityListImpl 
    implements PortletEntityList, PortletEntityListCtrl {

    /** all portlet entities */
    protected Map portlets = new HashMap();
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletEntity get(ObjectID objectId) {
        return (PortletEntity)this.portlets.get(objectId);
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityList#iterator()
     */
    public Iterator iterator() {
        return this.portlets.values().iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityListCtrl#add(org.apache.pluto.om.entity.PortletApplicationEntity, java.lang.String)
     */
    public PortletEntity add(PortletApplicationEntity appEntity, String definitionId) {
        // FIXME
        PortletDefinitionRegistry registry = null;
        PortletDefinition pd = registry.getPortletDefinition(ObjectIDImpl.createFromString(definitionId));
        PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
        CopletInstanceData coplet = null;
        PortletEntity portletEntity = new PortletEntityImpl(pae, coplet, pd);
        this.portlets.put(portletEntity.getId(), portletEntity);
        
        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityListCtrl#add(org.apache.pluto.om.entity.PortletApplicationEntity, java.lang.String)
     */
    public PortletEntity add(PortletApplicationEntity appEntity, String definitionId,
                             CopletInstanceData coplet, PortletDefinitionRegistry registry) {
        PortletDefinition pd = registry.getPortletDefinition(ObjectIDImpl.createFromString(definitionId));
        PortletEntity portletEntity = new PortletEntityImpl(appEntity, coplet, pd);
        this.portlets.put(portletEntity.getId(), portletEntity);
        
        return portletEntity;
    }
    
    /**
     * Remove an entity
     */
    public void remove(PortletEntity entity) {
        if ( entity != null ) {
            this.portlets.remove(entity.getId());
        }
    }
}
