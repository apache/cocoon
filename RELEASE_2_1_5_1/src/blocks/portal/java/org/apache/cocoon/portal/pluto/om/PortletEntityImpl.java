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

import java.io.IOException;
import java.util.Locale;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindowList;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletEntityImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class PortletEntityImpl implements PortletEntity, PortletEntityCtrl {

    protected PortletWindowList portletWindows;
    protected ObjectID objectId;
    protected PortletDefinition definition;
    protected CopletInstanceData coplet;
    protected PortletApplicationEntity applicationEntity;

    /**
     * Constructor
     */
    PortletEntityImpl(PortletApplicationEntity pae,
                      CopletInstanceData cid, 
                       PortletDefinition pd) {
        this.objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString("CID" + cid.hashCode());
        this.portletWindows = new PortletWindowListImpl();
        this.coplet = cid;
        this.definition = pd;
        this.applicationEntity = pae;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return this.definition.getDescription(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getId()
     */
    public ObjectID getId() {
        return this.objectId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletApplicationEntity()
     */
    public PortletApplicationEntity getPortletApplicationEntity() {
        return this.applicationEntity;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletDefinition()
     */
    public PortletDefinition getPortletDefinition() {
        return this.definition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletWindowList()
     */
    public PortletWindowList getPortletWindowList() {
        return this.portletWindows;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPreferenceSet()
     */
    public PreferenceSet getPreferenceSet() {
        return this.definition.getPreferenceSet();
    }

    /**
     * Return the coplet instance data
     */
    public CopletInstanceData getCopletInstanceData() {
        return this.coplet;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#reset()
     */
    public void reset() throws IOException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setId(java.lang.String)
     */
    public void setId(String id) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setPortletDefinition(org.apache.pluto.om.portlet.PortletDefinition)
     */
    public void setPortletDefinition(PortletDefinition portletDefinition) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#store()
     */
    public void store() throws IOException {
        // TODO Auto-generated method stub
    }

}
