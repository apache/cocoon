/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Base class for all profile managers
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public abstract class AbstractProfileManager 
    extends AbstractLogEnabled 
    implements Serviceable, Configurable, ProfileManager, ThreadSafe {

    protected String defaultLayoutKey;

    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void register(CopletInstanceData coplet) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles(String)
     */
    public void saveUserProfiles(String layoutKey) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void unregister(CopletInstanceData coplet) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
     */
    public void unregister(Layout layout) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#setDefaultLayoutKey(java.lang.String)
     */
    public void setDefaultLayoutKey(String layoutKey) {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            if ( layoutKey == null ) {
                service.removeAttribute("default-layout-key");
            } else {
                service.setAttribute("default-layout-key", layoutKey);
            }
        } catch (ServiceException ce) {
            // this should never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getDefaultLayoutKey()
     */
    public String getDefaultLayoutKey() {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            String defaultLayoutKey = (String)service.getAttribute("default-layout-key");
            if ( defaultLayoutKey == null ) {
                return this.defaultLayoutKey;
            }
            return defaultLayoutKey;
        } catch (ServiceException ce) {
            // this should never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
        
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#login()
     */
    public void login() {
        // overwrite in subclass
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#logout()
     */
    public void logout() {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration child = configuration.getChild("default-layout-key");
        // get configured default LayoutKey
        this.defaultLayoutKey = child.getValue("portal");
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#copyProfile(java.lang.String)
     */
    public Layout copyProfile(String layoutKey) {
        throw new RuntimeException("Copy profile is not implemented.");
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#storeProfile(org.apache.cocoon.portal.layout.Layout, java.lang.String)
     */
    public void storeProfile(Layout rootLayout, String layoutKey) {
        throw new RuntimeException("Store profile is not implemented.");
    }
}
