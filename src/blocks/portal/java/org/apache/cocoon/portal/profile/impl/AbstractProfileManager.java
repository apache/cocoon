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

import java.util.List;

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
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Base class for all profile managers
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractProfileManager.java,v 1.7 2004/04/28 13:58:16 cziegeler Exp $
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
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(org.apache.cocoon.portal.coplet.CopletData)
     */
    public List getCopletInstanceData(CopletData data) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(java.lang.String)
     */
    public CopletInstanceData getCopletInstanceData(String copletID) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(java.lang.String, java.lang.String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void register(CopletInstanceData coplet) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void unregister(CopletInstanceData coplet) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
     */
    public void unregister(Layout layout) {
        // TODO Auto-generated method stub

    }

    /**
     * Change the default layout key for most functions
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
    
    /**
     * Get the default layout key
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
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#logout()
     */
    public void logout() {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#setEntryLayout(org.apache.cocoon.portal.layout.Layout)
     */
    public void setEntryLayout(Layout object) {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            if ( object == null ) {
                service.removeTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
            } else {
                service.setTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey, object);
            }
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup service manager.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getEntryLayout()
     */
    public Layout getEntryLayout() {
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            return (Layout)service.getTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
        } catch (ServiceException e) {
            throw new CascadingRuntimeException("Unable to lookup service manager.", e);
        } finally {
            this.manager.release(service);
        }
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration child = configuration.getChild("default-layout-key");
        // get configured default LayoutKey
        this.defaultLayoutKey = child.getValue("portal");
    }
}
