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
package org.apache.cocoon.portal.pluto.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityList;
import org.apache.pluto.om.entity.PortletEntityListCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 *
 * @version $Id$
 */
public class PortletEntityListImpl 
    implements PortletEntityList, PortletEntityListCtrl {

    /** all portlet entities. */
    protected Map portlets = new HashMap();

    /**
     * @see org.apache.pluto.om.entity.PortletEntityList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletEntity get(ObjectID objectId) {
        if ( objectId == null ) {
            return null;
        }
        return (PortletEntity)this.portlets.get(objectId);
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityList#iterator()
     */
    public Iterator iterator() {
        return this.portlets.values().iterator();
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityListCtrl#add(org.apache.pluto.om.entity.PortletApplicationEntity, java.lang.String)
     */
    public PortletEntity add(PortletApplicationEntity appEntity, String definitionId) {
        PortletDefinitionRegistry registry = (PortletDefinitionRegistry)PortletContainerServices.get(PortletDefinitionRegistry.class);
        PortletDefinition pd = registry.getPortletDefinition(ObjectIDImpl.createFromString(definitionId));
        PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
        // FIXME
        CopletInstance coplet = null;
        PortletEntity portletEntity = new PortletEntityImpl(pae, coplet, pd, registry.getPortalService());
        this.portlets.put(portletEntity.getId(), portletEntity);

        return portletEntity;
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityListCtrl#add(org.apache.pluto.om.entity.PortletApplicationEntity, java.lang.String)
     */
    public PortletEntity add(PortletApplicationEntity appEntity,
                             String definitionId,
                             CopletInstance coplet,
                             PortletDefinitionRegistry registry) {
        PortletDefinition pd = registry.getPortletDefinition(ObjectIDImpl.createFromString(definitionId));
        PortletEntity portletEntity = new PortletEntityImpl(appEntity, coplet, pd, registry.getPortalService());
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
