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

import java.io.IOException;
import java.util.Locale;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.PortletPreferencesProvider;
import org.apache.pluto.PortletContainerServices;
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
 * @version $Id$
 */
public class PortletEntityImpl implements PortletEntity, PortletEntityCtrl {

    protected final PortletWindowList portletWindows;
    protected final ObjectID objectId;
    protected final PortletDefinition definition;
    protected final CopletInstance coplet;
    protected final PortletApplicationEntity applicationEntity;
    protected final PortalService service;
    protected final PortletPreferencesProvider prefProvider;

    protected static final String ATTR_PREFERENCES = PortletEntityImpl.class.getName() + "/Preferences";

    /**
     * Constructor.
     */
    PortletEntityImpl(PortletApplicationEntity pae,
                      CopletInstance cid, 
                      PortletDefinition pd,
                      PortalService service) {
        this.objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString("CID" + cid.hashCode());
        this.portletWindows = new PortletWindowListImpl();
        this.coplet = cid;
        this.definition = pd;
        this.applicationEntity = pae;
        this.service = service;
        this.prefProvider = (PortletPreferencesProvider)PortletContainerServices.get(PortletPreferencesProvider.class);
    }
    
    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return this.definition.getDescription(locale);
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getId()
     */
    public ObjectID getId() {
        return this.objectId;
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletApplicationEntity()
     */
    public PortletApplicationEntity getPortletApplicationEntity() {
        return this.applicationEntity;
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletDefinition()
     */
    public PortletDefinition getPortletDefinition() {
        return this.definition;
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletWindowList()
     */
    public PortletWindowList getPortletWindowList() {
        return this.portletWindows;
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntity#getPreferenceSet()
     */
    public PreferenceSet getPreferenceSet() {
        PreferenceSet prefs = (PreferenceSet)this.coplet.getTemporaryAttribute(ATTR_PREFERENCES);
        if ( prefs == null ) {
            prefs = this.prefProvider.getPreferenceSet(this.coplet);
            this.coplet.setTemporaryAttribute(ATTR_PREFERENCES, prefs);
        }
        return prefs;
    }

    /**
     * Return the coplet instance data.
     */
    public CopletInstance getCopletInstanceData() {
        return this.coplet;
    }
    
    
    /**
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#reset()
     */
    public void reset() throws IOException {
        // This method doesn't seem to be called by anything.
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setId(java.lang.String)
     */
    public void setId(String id) {
        // This method doesn't seem to be called by anything.
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setPortletDefinition(org.apache.pluto.om.portlet.PortletDefinition)
     */
    public void setPortletDefinition(PortletDefinition portletDefinition) {
        // This method doesn't seem to be called by anything
    }

    /**
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#store()
     */
    public void store() throws IOException {
        this.prefProvider.storePreferenceSet(this.coplet, (PreferenceSet)this.coplet.getTemporaryAttribute(ATTR_PREFERENCES));
    }

}
