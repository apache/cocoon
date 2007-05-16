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
package org.apache.cocoon.portal.pluto;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.om.common.PreferenceSetImpl;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.portal.util.AbstractComponent;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.pluto.om.common.PreferenceSet;

/**
 * FIXME - Configure this component and test it.
 * This component manages the portlet preferences for a user.
 * The default implementation will read/write one xml document
 * for all instances of a portlet (TODO).
 * In future version we will provide different implementations
 * based on the coplet configuration: the preferences can then
 * either be per instance or per user!
 *
 * $Id$
 */
public class PortletPreferencesProviderImpl
    extends AbstractComponent
    implements Parameterizable, PortletPreferencesProvider {

    protected static final String PROFILETYPE_PREFERENCES = "portletpreferences";

    /** The component for loading/saving the profiles. */
    protected ProfileLS loader;

    /** The configuration for this component. */
    protected Parameters configuration;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.configuration = params;
    }

    /**
     * @see org.apache.cocoon.portal.util.AbstractComponent#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.loader);
            this.loader = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.util.AbstractComponent#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        super.service(serviceManager);
        this.loader = (ProfileLS)this.manager.lookup(ProfileLS.ROLE);
    }

    protected Map buildKey(boolean load, String copletId)
    throws ParameterException {
        final StringBuffer config = new StringBuffer(PROFILETYPE_PREFERENCES);
        config.append('-');
        config.append("user");
        config.append('-');
        if ( load ) {
            config.append("load");
        } else {
            config.append("save");            
        }
        final String uri = this.configuration.getParameter(config.toString());

        final Map key = new LinkedMap();
        key.put("baseuri", uri);
        key.put("separator", "?");
        key.put("portal", this.portalService.getPortalName());
        key.put("type", "user");
        key.put("instance", "copletId");
        key.put("user", this.portalService.getUserService().getUser().getUserName());

        return key;
    }

    /**
     * @see org.apache.cocoon.portal.pluto.PortletPreferencesProvider#getPreferenceSet(org.apache.cocoon.portal.om.CopletInstance)
     */
    public PreferenceSet getPreferenceSet(CopletInstance cid) {
        try {
            return (PreferenceSet)this.loader.loadProfile(this.buildKey(true, cid.getId()), PROFILETYPE_PREFERENCES, null);
        } catch (Exception ignore) {
            // we ignore all exceptions for now (TODO)
        }
        return new PreferenceSetImpl();
    }

    /**
     * @see org.apache.cocoon.portal.pluto.PortletPreferencesProvider#storePreferenceSet(org.apache.cocoon.portal.om.CopletInstance, org.apache.pluto.om.common.PreferenceSet)
     */
    public void storePreferenceSet(CopletInstance cid, PreferenceSet prefs) {
        try {
             this.loader.saveProfile(this.buildKey(false, cid.getId()), PROFILETYPE_PREFERENCES, prefs);
        } catch (Exception ignore) {
             // we ignore all exceptions for now (TODO)
        }
    }
}
