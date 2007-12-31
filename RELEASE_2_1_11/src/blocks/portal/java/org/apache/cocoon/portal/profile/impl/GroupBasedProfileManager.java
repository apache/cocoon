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
package org.apache.cocoon.portal.profile.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.util.ClassUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * This profile manager uses a group based approach:
 * The coplet-base-data and the coplet-data are global, these are shared
 * between all users.
 * If the user has is own set of coplet-instance-datas/layouts these are
 * loaded.
 * If the user has not an own set, the group set is loaded - therefore
 * each user has belong to exactly one group.
 * In the case that the user does not belong to a group, a global
 * profile is loaded.
 * 
 * This profile manager does not check for changes of the profile,
 * which means for example once a global profile is loaded, it is
 * used until Cocoon is restarted. (This will be changed later on)
 * 
 * THIS IS A WORK IN PROGRESS - IT'S NOT FINISHED/WORKING YET
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AbstractUserProfileManager.java 37123 2004-08-27 12:11:53Z cziegeler $
 */
public class GroupBasedProfileManager 
    extends AbstractProfileManager
    implements Parameterizable, Contextualizable, Initializable, Disposable { 

    public static final String CATEGORY_GLOBAL = "global";
    public static final String CATEGORY_GROUP  = "group";
    public static final String CATEGORY_USER   = "user";
    
    protected static final String KEY_PREFIX = GroupBasedProfileManager.class.getName() + ':';
    
    protected static final class ProfileInfo {
        public Map            objects;
        public SourceValidity validity;
    }
    
    protected ProfileInfo copletBaseDatas;
    protected ProfileInfo copletDatas;
    
    /** The userinfo provider - the connection to the authentication mechanism */
    protected UserInfoProvider provider;
    
    /** The class name of the userinfo provider */
    protected String userInfoProviderClassName;
    
    /** The component context */
    protected Context context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.userInfoProviderClassName = params.getParameter("userinfo-provider");
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.provider = (UserInfoProvider)ClassUtils.newInstance(this.userInfoProviderClassName);
        ContainerUtil.enableLogging(this.provider, this.getLogger());
        ContainerUtil.contextualize(this.provider, this.context);
        ContainerUtil.service(this.provider, this.manager);
        ContainerUtil.initialize(this.provider);
        this.copletBaseDatas = new ProfileInfo();
        this.copletDatas = new ProfileInfo();
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        ContainerUtil.dispose(this.provider);
        this.provider = null;
        this.manager = null;
    }
    
    protected UserProfile getUserProfile(String layoutKey) {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            if ( layoutKey == null ) {
                layoutKey = service.getDefaultLayoutKey();
            }

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
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            final String layoutKey = service.getDefaultLayoutKey();

            service.removeAttribute(KEY_PREFIX + layoutKey);
        } catch (ServiceException e) {
            // this should never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    protected void storeUserProfile(String layoutKey, PortalService service, UserProfile profile) {
        if ( layoutKey == null ) {
            layoutKey = service.getDefaultLayoutKey();
        }
        service.setAttribute(KEY_PREFIX + layoutKey, profile);
    }
    
    /**
     * Prepares the object by using the specified factory.
     */
    protected void prepareObject(Object object, PortalService service)
    throws ProcessingException {
        if ( object != null ) {
            if ( object instanceof Map ) {
                object = ((Map)object).values();
            }
            if (object instanceof Layout) {
                service.getComponentManager().getLayoutFactory().prepareLayout((Layout)object);
            } else if (object instanceof Collection) {
                ServiceSelector adapterSelector = null;
                try {
                    final CopletFactory copletFactory = service.getComponentManager().getCopletFactory();
                    final Iterator iterator = ((Collection)object).iterator();
                    while (iterator.hasNext()) {
                        final Object o = iterator.next();
                        if ( o instanceof CopletData ) {
                            copletFactory.prepare((CopletData)o);
                        } else if ( o instanceof CopletInstanceData) {
                            if ( adapterSelector == null ) {
                                adapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");                            
                            }
                            CopletInstanceData cid = (CopletInstanceData)o;
                            copletFactory.prepare(cid);
                            // now invoke login on each instance
                            CopletAdapter adapter = null;
                            try {
                                adapter = (CopletAdapter) adapterSelector.select(cid.getCopletData().getCopletBaseData().getCopletAdapterName());
                                adapter.login( cid );
                            } finally {
                                adapterSelector.release( adapter );
                            }
                        }
                    }
                } catch (ServiceException se) {
                    // this should never happen
                    throw new ProcessingException("Unable to get component.", se);
                } finally {
                    this.manager.release(adapterSelector);
                }
            }
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
                layoutKey = service.getDefaultLayoutKey();
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
     * Load the profile
     */
    protected UserProfile loadProfile(final String layoutKey, final PortalService service) 
    throws Exception {
        final UserInfo info = this.provider.getUserInfo(service.getPortalName(), layoutKey);
        ProfileLS loader = null;
        try {
            loader = (ProfileLS)this.manager.lookup( ProfileLS.ROLE );
            final UserProfile profile = new UserProfile();
            this.storeUserProfile(layoutKey, service, profile);
            
            // first "load" the global data
            profile.setCopletBaseDatas( this.getGlobalBaseDatas(loader, info, service) );
            profile.setCopletDatas( this.getGlobalDatas(loader, info, service, profile) );
            
            // now load the user/group specific data
            if ( !this.getCopletInstanceDatas(loader, profile, info, service, CATEGORY_USER) ) {
                if ( info.getGroup() == null || !this.getCopletInstanceDatas(loader, profile, info, service, CATEGORY_GROUP)) {
                    if ( !this.getCopletInstanceDatas(loader, profile, info, service, CATEGORY_GLOBAL) ) {
                        throw new ProcessingException("No profile for copletinstancedatas found.");
                    }
                }
            }

            if ( !this.getLayout(loader, profile, info, service, CATEGORY_USER) ) {
                if ( info.getGroup() == null || !this.getLayout(loader, profile, info, service, CATEGORY_GROUP)) {
                    if ( !this.getLayout(loader, profile, info, service, CATEGORY_GLOBAL) ) {
                        throw new ProcessingException("No profile for layout found.");
                    }
                }
            }

            return profile;
        } catch (ServiceException se) {
            throw new CascadingRuntimeException("Unable to get component profilels.", se);
        } finally {
            this.manager.release( loader );
        }
    }
    
    protected Map getGlobalBaseDatas(final ProfileLS     loader,
                                     final UserInfo      info,
                                     final PortalService service) 
    throws Exception {
        synchronized ( this ) {
            final Map key = this.buildKey(CATEGORY_GLOBAL, 
                    ProfileLS.PROFILETYPE_COPLETBASEDATA, 
                    info, 
                    true);
            final Map parameters = new HashMap();
            parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_COPLETBASEDATA);
            
            if ( this.copletBaseDatas.validity != null
                 && this.copletBaseDatas.validity.isValid() == SourceValidity.VALID) {
                return this.copletBaseDatas.objects;
            }
            final SourceValidity newValidity = loader.getValidity(key, parameters);
            if ( this.copletBaseDatas.validity != null 
                 && newValidity != null
                 && this.copletBaseDatas.validity.isValid(newValidity) == SourceValidity.VALID) {
                return this.copletBaseDatas.objects;
            }
            this.copletBaseDatas.objects = ((CopletBaseDataManager)loader.loadProfile(key, parameters)).getCopletBaseData();
            this.copletBaseDatas.validity = newValidity;
            this.copletDatas.objects = null;
            this.copletDatas.validity = null;
            this.prepareObject(this.copletBaseDatas.objects, service);
            return this.copletBaseDatas.objects;
        }
    }
    
    protected Map getGlobalDatas(final ProfileLS     loader,
                                 final UserInfo      info,
                                 final PortalService service,
                                 final UserProfile   profile) 
    throws Exception {
        synchronized ( this ) {
            final Map key = this.buildKey(CATEGORY_GLOBAL, 
                    ProfileLS.PROFILETYPE_COPLETDATA, 
                    info, 
                    true);
            final Map parameters = new HashMap();
            parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_COPLETDATA);
            parameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                           profile.getCopletBaseDatas());
            
            if ( this.copletDatas.validity != null
                 && this.copletDatas.validity.isValid() == SourceValidity.VALID) {
                return this.copletDatas.objects;
            }
            final SourceValidity newValidity = loader.getValidity(key, parameters);
            if ( this.copletDatas.validity != null 
                 && newValidity != null
                 && this.copletDatas.validity.isValid(newValidity) == SourceValidity.VALID) {
                return this.copletDatas.objects;
            }
            this.copletDatas.objects = ((CopletDataManager)loader.loadProfile(key, parameters)).getCopletData();
            this.copletDatas.validity = newValidity;
            this.prepareObject(this.copletDatas.objects, service);
            return this.copletDatas.objects;
        }
    }

    private boolean isSourceNotFoundException(Throwable t) {
        while (t != null) {
            if (t instanceof SourceNotFoundException) {
                return true;
            }
            t = ExceptionUtils.getCause(t);
        }
        return false;
    }

    protected boolean getCopletInstanceDatas(final ProfileLS     loader,
                                             final UserProfile   profile,
                                             final UserInfo      info,
                                             final PortalService service,
                                             final String        category) 
    throws Exception {
        Map key = this.buildKey(category, 
                                ProfileLS.PROFILETYPE_COPLETINSTANCEDATA, 
                                info, 
                                true);
        Map parameters = new HashMap();
        parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                       ProfileLS.PROFILETYPE_COPLETINSTANCEDATA);        
        parameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                       profile.getCopletDatas());

        try {
            CopletInstanceDataManager cidm = (CopletInstanceDataManager)loader.loadProfile(key, parameters);
            profile.setCopletInstanceDatas(cidm.getCopletInstanceData());
            this.prepareObject(profile.getCopletInstanceDatas(), service);
            
            return true;
        } catch (Exception e) {
            if (!isSourceNotFoundException(e)) {
                throw e;
            }
            return false;
        }
    }

    protected boolean getLayout(final ProfileLS     loader,
                                final UserProfile   profile,
                                final UserInfo      info,
                                final PortalService service,
                                final String        category) 
    throws Exception {
        final Map key = this.buildKey(category, 
                                      ProfileLS.PROFILETYPE_LAYOUT,  
                                      info, 
                                      true);
        final Map parameters = new HashMap();
        parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                       ProfileLS.PROFILETYPE_LAYOUT);        
        parameters.put(ProfileLS.PARAMETER_OBJECTMAP, 
                       profile.getCopletInstanceDatas());
        try {
            Layout l = (Layout)loader.loadProfile(key, parameters);
            this.prepareObject(l, service);
            profile.setRootLayout(l);

            return true;
        } catch (Exception e) {
            if (!isSourceNotFoundException(e)) {
                throw e;
            }
            return false;
        }
    }

    protected Map buildKey(String        category,
                           String        profileType,
                           UserInfo      info,
                           boolean       load) {
        final StringBuffer config = new StringBuffer(profileType);
        config.append('-');
        config.append(category);
        config.append('-');
        if ( load ) {
            config.append("load");
        } else {
            config.append("save");            
        }
        final String uri = (String)info.getConfigurations().get(config.toString());

        final Map key = new LinkedMap();
        key.put("baseuri", uri);
        key.put("separator", "?");
        key.put("portal", info.getPortalName());
        key.put("layout", info.getLayoutKey());
        key.put("type", category);
        if ( "group".equals(category) ) {
            key.put("group", info.getGroup());
        }
        if ( "user".equals(category) ) {
            key.put("user", info.getUserName());
        }
        
        return key;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#storeProfile(org.apache.cocoon.portal.layout.Layout, java.lang.String)
     */
    public void storeProfile(Layout rootLayout, String layoutKey) {
        PortalService service = null;

        try {
            UserProfile oldProfile = this.getUserProfile(null);
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            if ( null == layoutKey ) {
                layoutKey = service.getDefaultLayoutKey();
            }
            // FIXME for now we just copy the root profile, except the root layout
            UserProfile newProfile = new UserProfile();
            newProfile.setCopletBaseDatas(oldProfile.getCopletBaseDatas());
            newProfile.setCopletDatas(oldProfile.getCopletDatas());
            newProfile.setCopletInstanceDatas(oldProfile.getCopletInstanceDatas());
            newProfile.setRootLayout(rootLayout);
            
            this.storeUserProfile(layoutKey, service, newProfile);
        } catch (Exception ce) {
            throw new CascadingRuntimeException("Exception during loading of profile.", ce);
        } finally {
            this.manager.release(service);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getUser()
     */
    public PortalUser getUser() {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            final String layoutKey = service.getDefaultLayoutKey();
            final UserInfo info = this.provider.getUserInfo(service.getPortalName(), layoutKey);
            return info;
        } catch (Exception ce) {
            throw new CascadingRuntimeException("Exception during getUser().", ce);
        } finally {
            this.manager.release(service);
        }            
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserCopletInstanceDatas(java.lang.String)
     */
    public void saveUserCopletInstanceDatas(String layoutKey) {
        ProfileLS adapter = null;
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);
            if (layoutKey == null) {
                layoutKey = service.getDefaultLayoutKey();
            }
            final UserProfile profile = this.getUserProfile(layoutKey);

            final Map parameters = new HashMap();
            parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_COPLETINSTANCEDATA);        

            final UserInfo info = this.provider.getUserInfo(service.getPortalName(), layoutKey);
            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_COPLETINSTANCEDATA, 
                                          info, 
                                          false);
            // FIXME - we should be able to save without creating a CopletInstanceDataManager
            CopletInstanceDataManager cidm = new CopletInstanceDataManager(profile.getCopletInstanceDatas());
            adapter.saveProfile(key, parameters, cidm);
        } catch (Exception e) {
            // TODO
            throw new CascadingRuntimeException("Exception during save profile", e);
        } finally {
            this.manager.release(service);
            this.manager.release(adapter);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserLayout(java.lang.String)
     */
    public void saveUserLayout(String layoutKey) {
        ProfileLS adapter = null;
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);
            if (layoutKey == null) {
                layoutKey = service.getDefaultLayoutKey();
            }
            final UserProfile profile = this.getUserProfile(layoutKey);

            final Map parameters = new HashMap();
            parameters.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_LAYOUT);        

            final UserInfo info = this.provider.getUserInfo(service.getPortalName(), layoutKey);
            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_LAYOUT, 
                                          info, 
                                          false);
            adapter.saveProfile(key, parameters, profile.getRootLayout());
        } catch (Exception e) {
            // TODO
            throw new CascadingRuntimeException("Exception during save profile", e);
        } finally {
            this.manager.release(service);
            this.manager.release(adapter);
        }
    }
}
