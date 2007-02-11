/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.pluto.om;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.*;
import org.apache.pluto.om.portlet.PortletDefinition;

import java.util.*;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletEntityListImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
