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

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletApplicationEntityListCtrl;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletApplicationDefinitionList;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletApplicationEntityListImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class PortletApplicationEntityListImpl
implements PortletApplicationEntityList, PortletApplicationEntityListCtrl {

    /** the portlet application entities */
    protected Map portletApplicationEntities = new HashMap();
    
    /** The registry */
    protected PortletDefinitionRegistry registry;
    
    /**
     * Constructor
     */
    public PortletApplicationEntityListImpl(PortletDefinitionRegistry pdr) {
        this.registry = pdr;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletApplicationEntityList#iterator()
     */
    public Iterator iterator() {
        return this.portletApplicationEntities.values().iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletApplicationEntityListCtrl#add(java.lang.String)
     */
    public PortletApplicationEntity add(String definitionId) {
        PortletApplicationDefinitionList padl = this.registry.getPortletApplicationDefinitionList();
        PortletApplicationDefinition pad = padl.get(org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(definitionId));
        PortletApplicationEntityImpl pae = new PortletApplicationEntityImpl(definitionId, pad);
        this.portletApplicationEntities.put(pae.getId(), pae);
        return pae;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletApplicationEntityList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletApplicationEntity get(ObjectID objectId) {
        final PortletApplicationEntity pae = (PortletApplicationEntity) this.portletApplicationEntities.get(objectId);
        return pae;
    }

}
