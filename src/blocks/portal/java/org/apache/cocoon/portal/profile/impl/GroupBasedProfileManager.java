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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;

/**
 * The profile manager using the authentication framework
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractUserProfileManager.java 37123 2004-08-27 12:11:53Z cziegeler $
 */
public class GroupBasedProfileManager 
    extends AbstractProfileManager { 

    protected static final String KEY_PREFIX = GroupBasedProfileManager.class.getName() + ':';
    
    protected UserProfile getUserProfile(String layoutKey) {
        if ( layoutKey == null ) {
            layoutKey = this.getDefaultLayoutKey();
        }
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);

            return (UserProfile)service.getAttribute(KEY_PREFIX + layoutKey);
        } catch (ServiceException e) {
            // this should never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }
    
    protected void removeUserProfiles() {
        // TODO: remove all profiles - we have to rememember all used layout keys
        String layoutKey = this.getDefaultLayoutKey();
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);

            service.removeAttribute(KEY_PREFIX + layoutKey);
        } catch (ServiceException e) {
            // this should never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#login()
     */
    public void login() {
        super.login();
        // TODO - we should move most of the stuff from getPortalLayout to here
        // for now we use a hack :)
        this.getPortalLayout(null, null);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#logout()
     */
    public void logout() {
        final UserProfile profile = this.getUserProfile(null);
        if ( profile != null ) {
            ServiceSelector adapterSelector = null;
            try {
                adapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");

                Iterator iter = profile.getCopletInstanceDatas().values().iterator();
                while ( iter.hasNext() ) {
                    CopletInstanceData cid = (CopletInstanceData) iter.next();
                    CopletAdapter adapter = null;
                    try {
                        adapter = (CopletAdapter)adapterSelector.select(cid.getCopletData().getCopletBaseData().getCopletAdapterName());
                        adapter.logout( cid );
                    } finally {
                        adapterSelector.release( adapter );
                    }
                }

            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup portal service.", e);
            } finally {
                this.manager.release(adapterSelector);
            }
            this.removeUserProfiles();
        }
        super.logout();
    }
       
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(java.lang.String)
     */
    public CopletInstanceData getCopletInstanceData(String copletID) {
        final UserProfile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return (CopletInstanceData)profile.getCopletInstanceDatas().get(copletID);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletData(java.lang.String)
     */
    public CopletData getCopletData(String copletDataId) {
        final UserProfile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return (CopletData)profile.getCopletDatas().get(copletDataId);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(org.apache.cocoon.portal.coplet.CopletData)
     */
    public List getCopletInstanceData(CopletData data) {
        final UserProfile profile = this.getUserProfile(null);
        final List coplets = new ArrayList();
        if ( profile != null ) {
            final Iterator iter = profile.getCopletInstanceDatas().values().iterator();
            while ( iter.hasNext() ) {
                final CopletInstanceData current = (CopletInstanceData)iter.next();
                if ( current.getCopletData().equals(data) ) {
                    coplets.add( current );
                }
            }
        }
        return coplets;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void register(CopletInstanceData coplet) {
        final UserProfile profile = this.getUserProfile(null);
        profile.getCopletInstanceDatas().put(coplet.getId(), coplet);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void unregister(CopletInstanceData coplet) {
        final UserProfile profile = this.getUserProfile(null);
        profile.getCopletInstanceDatas().remove(coplet.getId());
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
        if ( layout != null && layout.getId() != null ) {
            final UserProfile profile = this.getUserProfile(null);    
            profile.getLayouts().put(layout.getId(), layout);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
     */
    public void unregister(Layout layout) {
        if ( layout != null && layout.getId() != null ) {
            final UserProfile profile = this.getUserProfile(null);
            profile.getLayouts().remove(layout.getId());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(java.lang.String, java.lang.String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutId) {
        PortalService service = null;

        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            if ( null == layoutKey ) {
                layoutKey = this.getDefaultLayoutKey();
            }
            // FIXME actually this is a hack for full screen
            Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
            if ( null != l) {
                return l;
            }
            
            UserProfile profile = this.getUserProfile(layoutKey);
            if ( profile == null ) {
                profile = this.loadProfile(layoutKey, service);
            }
            if ( profile == null ) {
                throw new RuntimeException("Unable to load profile: " + layoutKey);
            }
            if ( layoutId != null ) {
                return (Layout)profile.getLayouts().get(layoutId);
            }
            return profile.getRootLayout();
        } catch (Exception ce) {
            throw new CascadingRuntimeException("Exception during loading of profile.", ce);
        } finally {
            this.manager.release(service);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDatas()
     */
    public Collection getCopletDatas() {
        final UserProfile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.getCopletDatas().values();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceDatas()
     */
    public Collection getCopletInstanceDatas() {
        final UserProfile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.getCopletInstanceDatas().values();
        }
        return null;
    }

    /**
     * Return the user info about the current user.
     * This implementation uses the authentication framework - if you
     * want to use a different authentication method just overwrite this
     * method.
     */
    protected UserInfo getUserInfo() {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            final UserInfo info = new UserInfo();
            info.setUserName(authManager.getState().getHandler().getUserId());
            try {
                info.setGroup((String)authManager.getState().getHandler().getContext().getContextInfo().get("group"));
            } catch (ProcessingException pe) {
                // ignore this
            }
            return info;    
        } catch (ServiceException ce) {
            // ignore this here
            return null;
        } finally {
            this.manager.release( authManager );
        }
    }
        
    /**
     * Load the profile
     */
    protected UserProfile loadProfile(final String layoutKey, final PortalService service) {
        final UserProfile profile = new UserProfile();
        
        return profile;
    }
}
