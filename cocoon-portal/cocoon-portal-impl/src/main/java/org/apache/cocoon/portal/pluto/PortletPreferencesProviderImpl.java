/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.pluto.om.common.PreferenceSetImpl;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.pluto.om.common.PreferenceSet;

/**
 * FIXME - We will use the ProfileLS for this with our own type: 
 * $Id$
 */
public class PortletPreferencesProviderImpl
    extends AbstractComponent
    implements PortletPreferencesProvider {

    protected static final String PROFILETYPE_PREFERENCES = "portletpreferences";

    protected ProfileLS loader;

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.loader);
            this.loader = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.loader = (ProfileLS)this.manager.lookup(ProfileLS.ROLE);
    }

    /**
     * @see org.apache.cocoon.portal.pluto.PortletPreferencesProvider#getPreferenceSet(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public PreferenceSet getPreferenceSet(CopletInstanceData cid) {
        //final Map parameters = new HashMap();
        //parameters.put(ProfileLS.PARAMETER_PROFILETYPE, PROFILETYPE_PREFERENCES);
        //this.loader.loadProfile(key, parameters);
        return new PreferenceSetImpl();
    }

    /**
     * @see org.apache.cocoon.portal.pluto.PortletPreferencesProvider#storePreferenceSet(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void storePreferenceSet(CopletInstanceData cid) {
        //final Map parameters = new HashMap();
        //parameters.put(ProfileLS.PARAMETER_PROFILETYPE, PROFILETYPE_PREFERENCES);
        //this.loader.loadProfile(key, parameters);
    }
}
