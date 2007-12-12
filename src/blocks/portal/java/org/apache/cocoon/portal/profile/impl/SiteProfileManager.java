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
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.variables.VariableResolver;
import org.apache.cocoon.components.variables.VariableResolverFactory;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.util.ClassUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * @version CVS $Id$
 */
public class SiteProfileManager
    extends AbstractProfileManager
    implements Parameterizable, Contextualizable, Initializable, Disposable {

    public static final String CATEGORY_GLOBAL = "global";

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

    /** The per site profiles. */
    protected final Map profiles = new HashMap();

    /** Site expression to detect the current site. */
    protected String siteExpression;

    /** Variable resolver factory. */
    protected VariableResolverFactory variableFactory;

    /** Variable resolver. */
    protected VariableResolver variableResolver;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.variableFactory = (VariableResolverFactory) this.manager.lookup(VariableResolverFactory.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.userInfoProviderClassName = params.getParameter("userinfo-provider");
        this.siteExpression = params.getParameter("site-expression");
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
        this.variableResolver = this.variableFactory.lookup(this.siteExpression);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        ContainerUtil.dispose(this.provider);
        this.provider = null;
        if ( this.manager != null ) {
            if ( this.variableFactory != null ) {
                this.variableFactory.release(this.variableResolver);
                this.variableResolver = null;
            }
            this.manager.release(this.variableFactory);
            this.variableFactory = null;
            this.manager = null;
        }
    }

    protected String getSiteName() {
        try {
            return this.variableResolver.resolve();
        } catch (PatternException e) {
            throw new CascadingRuntimeException("Unvalid pattern " + this.siteExpression, e);
        }
    }

    protected UserProfile getUserProfile(String layoutKey) {
        if ( layoutKey == null ) {
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
               layoutKey = service.getDefaultLayoutKey();

            } catch (ServiceException e) {
                // this should never happen
                throw new CascadingRuntimeException("Unable to lookup portal service.", e);
            } finally {
                this.manager.release(service);
            }
        }
        final String siteKey = this.getSiteName() + ':' + layoutKey;
        synchronized ( this.profiles ) {
            final UserProfileInfo info = (UserProfileInfo) this.profiles.get(siteKey);
            if ( info != null && info.profile != null ) {
                return info.profile;
            }
        }
        return null;
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
        try {
            this.loadProfile(null);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Unable to load new user profile.", e);
        }
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
                profile = this.loadProfile(layoutKey);
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
    protected UserProfile loadProfile(final String layoutKey)
    throws Exception {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            final String siteKey = this.getSiteName() + ':' + (layoutKey == null ? service.getDefaultLayoutKey() : layoutKey);

            // access on profiles is synchronized
            UserProfileInfo storedProfile;
            synchronized (this.profiles) {
                storedProfile = (UserProfileInfo) this.profiles.get(siteKey);
                if ( storedProfile == null ) {
                    storedProfile = new UserProfileInfo();
                    this.profiles.put(siteKey, storedProfile);
                }
            }
            // now synchronize on per site profile
            synchronized ( storedProfile ) {
                final UserInfo info = this.provider.getUserInfo(service.getPortalName(), layoutKey == null ? service.getDefaultLayoutKey() : layoutKey);
                ProfileLS loader = null;
                try {
                    loader = (ProfileLS)this.manager.lookup( ProfileLS.ROLE );

                    this.getSiteProfile(loader, info, service, storedProfile);
                } catch (ServiceException se) {
                    throw new CascadingRuntimeException("Unable to get profilels.", se);
                } finally {
                    this.manager.release( loader );
                }
                return storedProfile.profile;
            }
        } catch (ServiceException se) {
            // this can never happen
            throw new CascadingRuntimeException("Unable to lookup portal service.", se);
        } finally {
            this.manager.release(service);
        }
    }

    protected Map getGlobalBaseDatas(final ProfileLS     loader,
                                     final UserInfo      info,
                                     final PortalService service)
    throws Exception {
        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETBASEDATA,
                info,
                true);
        final Map parameters = new HashMap();
        parameters.put(ProfileLS.PARAMETER_PROFILETYPE,
                       ProfileLS.PROFILETYPE_COPLETBASEDATA);

        synchronized ( this ) {
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
        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETDATA,
                info,
                true);
        final Map parameters = new HashMap();
        parameters.put(ProfileLS.PARAMETER_PROFILETYPE,
                       ProfileLS.PROFILETYPE_COPLETDATA);
        parameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                       profile.getCopletBaseDatas());

        synchronized ( this ) {

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

    protected void getSiteProfile(final ProfileLS       loader,
                                  final UserInfo        info,
                                  final PortalService   service,
                                  final UserProfileInfo storedProfile)
    throws Exception {
        // prepare keys and parameters first
        final Map instanceKey = this.buildKey(CATEGORY_GLOBAL,
                                              ProfileLS.PROFILETYPE_COPLETINSTANCEDATA,
                                              info,
                                              true);
        final Map instanceParameters = new HashMap();
        instanceParameters.put(ProfileLS.PARAMETER_PROFILETYPE,
                               ProfileLS.PROFILETYPE_COPLETINSTANCEDATA);
        if ( storedProfile.profile != null ) {
            instanceParameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                                   storedProfile.profile.getCopletDatas());
        }

        final Map layoutKey = this.buildKey(CATEGORY_GLOBAL,
                                            ProfileLS.PROFILETYPE_LAYOUT,
                                            info,
                                            true);
        final Map layoutParameters = new HashMap();
        layoutParameters.put(ProfileLS.PARAMETER_PROFILETYPE,
                             ProfileLS.PROFILETYPE_LAYOUT);
        if ( storedProfile.profile != null ) {
            layoutParameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                                 storedProfile.profile.getCopletInstanceDatas());
        }


        boolean storedInstancesAreValid = false;
        boolean storedLayoutsAreValid = false;

        // we'll check for coplet instance data first
        if ( storedProfile.profile != null ) {
            if ( storedProfile.instanceValidity != null
                    && storedProfile.instanceValidity.isValid() == SourceValidity.VALID) {
               storedInstancesAreValid = true;
            } else {
                final SourceValidity newValidity = loader.getValidity(instanceKey, instanceParameters);
                if ( storedProfile.instanceValidity != null
                     && newValidity != null
                     && storedProfile.instanceValidity.isValid(newValidity) == SourceValidity.VALID) {
                    storedInstancesAreValid = true;
                }
            }
        }

        // now we check the layout
        if ( storedInstancesAreValid ) {

            if ( storedProfile.layoutValidity != null
                    && storedProfile.layoutValidity.isValid() == SourceValidity.VALID) {
                storedLayoutsAreValid = true;
            } else {
               final SourceValidity newValidity = loader.getValidity(layoutKey, layoutParameters);
               if ( storedProfile.layoutValidity != null
                    && newValidity != null
                    && storedProfile.layoutValidity.isValid(newValidity) == SourceValidity.VALID) {
                   storedLayoutsAreValid = true;
               }
            }
        }
        if ( !storedInstancesAreValid || !storedLayoutsAreValid ) {
            // we reload the whole profile
            // update parameters
            final UserProfile profile = new UserProfile();

            // first "load" the global data
            profile.setCopletBaseDatas( this.getGlobalBaseDatas(loader, info, service) );
            profile.setCopletDatas( this.getGlobalDatas(loader, info, service, profile) );

            SourceValidity instanceValidity = null;
            SourceValidity layoutValidity = null;
            // load instances
            try {
                // update parameters
                instanceParameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                                       profile.getCopletDatas());

                instanceValidity = loader.getValidity(instanceKey, instanceParameters);
                final CopletInstanceDataManager cidm = (CopletInstanceDataManager)loader.loadProfile(instanceKey, instanceParameters);
                profile.setCopletInstanceDatas(cidm.getCopletInstanceData());
                this.prepareObject(profile.getCopletInstanceDatas(), service);

            } catch (Exception e) {
                if (!this.isSourceNotFoundException(e)) {
                    throw e;
                }
                throw new ProcessingException("No profile for coplet instance datas found.");
            }

            // load layout

            try {
                // update parameters
                layoutParameters.put(ProfileLS.PARAMETER_OBJECTMAP,
                                     profile.getCopletInstanceDatas());
                layoutValidity = loader.getValidity(layoutKey, layoutParameters);
                final Layout l = (Layout)loader.loadProfile(layoutKey, layoutParameters);
                this.prepareObject(l, service);
                profile.setRootLayout(l);

            } catch (Exception e) {
                if (!this.isSourceNotFoundException(e)) {
                    throw e;
                }
                throw new ProcessingException("No profile for layout found.");
            }
            // everything fine: update stored profile - this will automatically have an effect on all logged in users!
            storedProfile.instanceValidity = instanceValidity;
            storedProfile.layoutValidity = layoutValidity;
            storedProfile.profile = profile;
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

        final Map key = new LinkedMap(6);
        key.put("baseuri", uri);
        key.put("separator", "?");
        key.put("portal", info.getPortalName());
        key.put("layout", info.getLayoutKey());
        key.put("type", category);
        key.put("site", this.getSiteName());

        return key;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#storeProfile(org.apache.cocoon.portal.layout.Layout, java.lang.String)
     */
    public void storeProfile(Layout rootLayout, String layoutKey) {
        // nothing to do
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

    protected static final class UserProfileInfo {
        public UserProfile profile;
        public SourceValidity instanceValidity;
        public SourceValidity layoutValidity;
    }
}
