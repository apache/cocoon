/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: AbstractProfileManager.java,v 1.5 2003/12/17 15:03:27 cziegeler Exp $
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
            service.setTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey, object);
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
