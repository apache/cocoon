/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletDefinitionAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceRemovedEvent;
import org.apache.cocoon.portal.event.layout.LayoutAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutInstanceAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutRemovedEvent;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.portal.scratchpad.Profile;
import org.apache.cocoon.portal.scratchpad.ProfileImpl;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
 * This profile manager uses a group based approach:
 * The coplet-base-data and the coplet-data are global, these are shared
 * between all users.
 * If the user has his own set of coplet-instance-datas/layouts these are
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
 * THIS IS A WORK IN PROGRESS - IT'S NOT FINISHED YET
 * 
 * @version $Id: AbstractUserProfileManager.java 37123 2004-08-27 12:11:53Z cziegeler $
 */
public class GroupBasedProfileManager 
    extends AbstractProfileManager { 

    public static final String CATEGORY_GLOBAL = "global";
    public static final String CATEGORY_GROUP  = "group";
    public static final String CATEGORY_USER   = "user";

    protected static final Map MAP_FOR_BASE_DATA = Collections.singletonMap(ProfileLS.PARAMETER_PROFILETYPE,
                                                                            ProfileLS.PROFILETYPE_COPLETTYPE);

    protected static final String KEY_PREFIX = GroupBasedProfileManager.class.getName() + ':';

    protected static final class ProfileInfo {
        public Map            objects;
        public SourceValidity validity;
    }

    final protected ProfileInfo copletBaseDatas = new ProfileInfo();
    final protected ProfileInfo copletDatas = new ProfileInfo();

    /** All deployed coplet datas. */
    final protected Map deployedCopletDefinitions = new HashMap();

    /** Check for changes? */
    protected boolean checkForChanges = true;

    /** The parameters for the profile configuration. */
    protected Parameters parameters;

    /** The profiler loader/saver. */
    protected ProfileLS loader;

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        this.parameters = Parameters.fromConfiguration(config);
        this.checkForChanges = this.parameters.getParameterAsBoolean("check-for-changes", this.checkForChanges);
    }

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
    public void service(ServiceManager serviceManager) throws ServiceException {
        super.service(serviceManager);
        this.loader = (ProfileLS)this.manager.lookup(ProfileLS.ROLE);
    }

    protected ProfileImpl getUserProfile(String layoutKey) {
        if ( layoutKey == null ) {
            layoutKey = this.portalService.getDefaultLayoutKey();
        }

        return (ProfileImpl)this.portalService.getAttribute(KEY_PREFIX + layoutKey);
    }

    protected void removeUserProfiles() {
        // TODO: remove all profiles - we have to rememember all used layout keys
        final String layoutKey = this.portalService.getDefaultLayoutKey();

        this.portalService.removeAttribute(KEY_PREFIX + layoutKey);
    }

    protected void storeUserProfile(String layoutKey, Profile profile) {
        if ( layoutKey == null ) {
            layoutKey = this.portalService.getDefaultLayoutKey();
        }
        this.portalService.setAttribute(KEY_PREFIX + layoutKey, profile);
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#login(org.apache.cocoon.portal.profile.PortalUser)
     */
    protected void login(PortalUser user) {
        super.login(user);
        // TODO - we should move most of the stuff from getPortalLayout to here
        // for now we use a hack :)
        this.getPortalLayout(null, null);
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#logout(org.apache.cocoon.portal.profile.PortalUser)
     */
    protected void logout(PortalUser user) {
        final Profile profile = this.getUserProfile(null);
        if ( profile != null ) {

            Iterator iter = profile.getCopletInstances().iterator();
            while ( iter.hasNext() ) {
                CopletInstance cid = (CopletInstance) iter.next();
                CopletAdapter adapter = this.portalService.getCopletAdapter(cid.getCopletDefinition().getCopletType().getCopletAdapterName());
                adapter.logout( cid );
            }

            this.removeUserProfiles();
        }
        super.logout(user);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstance(java.lang.String)
     */
    public CopletInstance getCopletInstance(String copletID) {
        final Profile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.searchCopletInstance(copletID);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        final ProfileImpl profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.searchCopletDefinition(copletDataId);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public List getCopletInstances(CopletDefinition data) {
        final Profile profile = this.getUserProfile(null);
        if ( profile != null ) {
            final List coplets = new ArrayList();
            final Iterator iter = profile.getCopletInstances().iterator();
            while ( iter.hasNext() ) {
                final CopletInstance current = (CopletInstance)iter.next();
                if ( current.getCopletDefinition().equals(data) ) {
                    coplets.add( current );
                }
            }
            return coplets;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Receives a coplet instance data added event.
     * @see Receiver
     */
    public void inform(CopletInstanceAddedEvent event, PortalService service) {
        final ProfileImpl profile = this.getUserProfile(null);
        profile.add(event.getTarget());
    }

    /**
     * Receives a coplet data added event.
     * @see Receiver
     */
    public void inform(CopletDefinitionAddedEvent event, PortalService service) {
        this.deployedCopletDefinitions.put(event.getTarget().getId(), event.getTarget());
        if ( this.copletDatas.objects != null ) {
            this.copletDatas.objects.put(event.getTarget().getId(), event.getTarget());
        }
    }

    /**
     * Receives a coplet instance data added event.
     * @see Receiver
     */
    public void inform(CopletInstanceRemovedEvent event, PortalService service) {
        final ProfileImpl profile = this.getUserProfile(null);
        profile.remove(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutAddedEvent event, PortalService service) {
        final ProfileImpl profile = this.getUserProfile(null);
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutInstanceAddedEvent event, PortalService service) {
        final ProfileImpl profile = this.getUserProfile(null);
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout removed event.
     * @see Receiver
     */
    public void inform(LayoutRemovedEvent event, PortalService service) {
        final ProfileImpl profile = this.getUserProfile(null);
        profile.remove(event.getTarget());
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(java.lang.String, java.lang.String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutId) {
        if ( null == layoutKey ) {
            layoutKey = this.portalService.getDefaultLayoutKey();
        }

        Profile profile = this.getUserProfile(layoutKey);
        if ( profile == null ) {
            try {
                profile = this.loadProfile(layoutKey);
            } catch (Exception e) {
                throw new ProfileException("Unable to load profile: " + layoutKey, e);
            }
        }
        if ( profile == null ) {
            throw new ProfileException("Unable to load profile: " + layoutKey);
        }
        if ( layoutId != null ) {
            return profile.searchLayout(layoutId);
        }
        return profile.getRootLayout();
    }
    
    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinitions()
     */
    public Collection getCopletDefinitions() {
        final ProfileImpl profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.getCopletDefinitions();
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances()
     */
    public Collection getCopletInstances() {
        final Profile profile = this.getUserProfile(null);
        if ( profile != null ) {
            return profile.getCopletInstances();
        }
        return null;
    }

    /**
     * Load the profile
     */
    protected Profile loadProfile(final String layoutKey) 
    throws Exception {
        final PortalUser info = (PortalUser)this.portalService.getTemporaryAttribute(USER_ATTRIBUTE);
        ProfileImpl profile = new ProfileImpl(layoutKey);

        // first "load" the global data
        profile.setCopletTypes( this.getGlobalBaseDatas(layoutKey) );
        profile.setCopletDefinitions( this.getGlobalDatas(info, profile, layoutKey) );

        // now load the user/group specific data
        if ( !this.getCopletInstanceDatas(profile, info, CATEGORY_USER, layoutKey) ) {
            if ( info.getGroup() == null || !this.getCopletInstanceDatas(profile, info, CATEGORY_GROUP, layoutKey)) {
                if ( !this.getCopletInstanceDatas(profile, info, CATEGORY_GLOBAL, layoutKey) ) {
                    throw new ProcessingException("No profile for copletinstancedatas found.");
                }
            }
        }

        if ( !this.getLayout(profile, info, CATEGORY_USER, layoutKey) ) {
            if ( info.getGroup() == null || !this.getLayout(profile, info, CATEGORY_GROUP, layoutKey)) {
                if ( !this.getLayout(profile, info, CATEGORY_GLOBAL, layoutKey) ) {
                    throw new ProcessingException("No profile for layout found.");
                }
            }
        }

        final Profile processedProfile = this.processProfile(profile);
        this.storeUserProfile(layoutKey, processedProfile);
        return processedProfile;
    }

    protected Map getGlobalBaseDatas(final String     layoutKey)
    throws Exception {
        // if we already have loaded the profile and don't check
        // for changes, just return the profile
        if ( this.copletBaseDatas.objects != null && !this.checkForChanges ) {
            return this.copletBaseDatas.objects;
        }

        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETTYPE,
                null,
                true,
                layoutKey);

        SourceValidity newValidity = null;
        // if we have a profile, check for reloading
        if ( this.copletBaseDatas.validity != null ) {
            // if it's still valid just return the profile
            final int validity = this.copletBaseDatas.validity.isValid();
            if ( validity == SourceValidity.VALID) {
                return this.copletBaseDatas.objects;
            } else if ( validity == SourceValidity.UNKNOWN ) {
                newValidity = loader.getValidity(key, MAP_FOR_BASE_DATA);
                if ( newValidity != null
                     && this.copletBaseDatas.validity.isValid(newValidity) == SourceValidity.VALID) {
                    return this.copletBaseDatas.objects;
                }
            }
        }

        // we have to load/reload
        synchronized ( this ) {
            final Map objects = new HashMap();
            final Iterator i = ((Collection)loader.loadProfile(key, MAP_FOR_BASE_DATA)).iterator();
            while ( i.hasNext() ) {
                final CopletType current = (CopletType)i.next();
                objects.put(current.getId(), current);
            }
            this.copletBaseDatas.objects = objects;
            if ( newValidity == null ) {
                newValidity = loader.getValidity(key, MAP_FOR_BASE_DATA);
            }
            this.copletBaseDatas.validity = newValidity;
            this.copletDatas.objects = null;
            this.copletDatas.validity = null;
            this.prepareObject(this.copletBaseDatas.objects);
            return this.copletBaseDatas.objects;
        }
    }

    protected Map getGlobalDatas(final PortalUser  info,
                                 final ProfileImpl profile,
                                 final String      layoutKey)
    throws Exception {
        // if we already have loaded the profile and don't check
        // for changes, just return the profile
        if ( this.copletDatas.objects != null && !this.checkForChanges ) {
            return this.copletDatas.objects;
        }

        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETDEFINITION,
                info,
                true,
                layoutKey);
        final Map profileLSParams = new HashMap();
        profileLSParams.put(ProfileLS.PARAMETER_PROFILETYPE,
                       ProfileLS.PROFILETYPE_COPLETDEFINITION);
        profileLSParams.put(ProfileLS.PARAMETER_OBJECTMAP,
                       profile.getCopletTypesMap());

        SourceValidity newValidity = null;
        // if we have a profile, check for reloading
        if ( this.copletDatas.validity != null ) {
            // if it's still valid just return the profile
            final int validity = this.copletDatas.validity.isValid();
            if ( validity == SourceValidity.VALID) {
                return this.copletDatas.objects;
            } else if ( validity == SourceValidity.UNKNOWN ) {
                newValidity = loader.getValidity(key,profileLSParams);
                if ( newValidity != null
                     && this.copletDatas.validity.isValid(newValidity) == SourceValidity.VALID) {
                    return this.copletDatas.objects;
                }
            }
        }

        synchronized ( this ) {
            final Map objects = new HashMap();
            final Iterator i = ((Collection)loader.loadProfile(key, profileLSParams)).iterator();
            while ( i.hasNext() ) {
                final CopletDefinition current = (CopletDefinition)i.next();
                // only add coplet data if coplet base data has been found
                if ( current.getCopletType() != null ) {
                    objects.put(current.getId(), current);
                } else {
                    this.getLogger().error("CopletType not found for CopletDefinition: " + current);
                }
            }
            this.copletDatas.objects = objects;
            // now add deployed coplets
            this.copletDatas.objects.putAll(this.deployedCopletDefinitions);
            if ( newValidity == null ) {
                newValidity = loader.getValidity(key, profileLSParams);
            }
            this.copletDatas.validity = newValidity;
            this.prepareObject(this.copletDatas.objects);
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

    protected boolean getCopletInstanceDatas(final ProfileImpl profile,
                                             final PortalUser  info,
                                             final String      category,
                                             final String      layoutKey)
    throws Exception {
        Map key = this.buildKey(category,
                                ProfileLS.PROFILETYPE_COPLETINSTANCE,
                                info,
                                true,
                                layoutKey);
        Map profileLSParams = new HashMap();
        profileLSParams.put(ProfileLS.PARAMETER_PROFILETYPE,
                       ProfileLS.PROFILETYPE_COPLETINSTANCE);
        profileLSParams.put(ProfileLS.PARAMETER_OBJECTMAP,
                       profile.getCopletDefinitionsMap());

        try {
            Collection cidm = (Collection)loader.loadProfile(key, profileLSParams);
            profile.setCopletInstances(cidm);
            this.prepareObject(profile.getCopletInstancesMap());

            return true;
        } catch (Exception e) {
            if (!isSourceNotFoundException(e)) {
                throw e;
            }
            return false;
        }
    }

    protected boolean getLayout(final ProfileImpl profile,
                                final PortalUser  info,
                                final String      category,
                                final String      layoutKey)
    throws Exception {
        final Map key = this.buildKey(category,
                                      ProfileLS.PROFILETYPE_LAYOUT,
                                      info,
                                      true,
                                      layoutKey);
        final Map profileLSParams = new HashMap();
        profileLSParams.put(ProfileLS.PARAMETER_PROFILETYPE,
                       ProfileLS.PROFILETYPE_LAYOUT);
        profileLSParams.put(ProfileLS.PARAMETER_OBJECTMAP,
                       profile.getCopletInstancesMap());
        try {
            Layout l = (Layout)loader.loadProfile(key, profileLSParams);
            this.prepareObject(l);
            profile.setRootLayout(l);

            return true;
        } catch (Exception e) {
            if (!isSourceNotFoundException(e)) {
                throw e;
            }
            return false;
        }
    }

    protected Map buildKey(String   category,
                           String   profileType,
                           PortalUser info,
                           boolean  load,
                           String   layoutKey)
    throws ParameterException {
        final StringBuffer config = new StringBuffer(profileType);
        config.append('-');
        config.append(category);
        config.append('-');
        if ( load ) {
            config.append("load");
        } else {
            config.append("save");            
        }
        final String uri = this.parameters.getParameter(config.toString());

        final Map key = new LinkedMap();
        key.put("baseuri", uri);
        key.put("separator", "?");
        key.put("portal", this.portalService.getPortalName());
        key.put("layout", layoutKey);
        key.put("type", category);
        if ( CATEGORY_GROUP.equals(category) ) {
            key.put("group", info.getGroup());
        } else if ( CATEGORY_USER.equals(category) ) {
            key.put("user", info.getUserName());
        }

        return key;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getUser()
     */
    public PortalUser getUser() {
        return (PortalUser)this.portalService.getTemporaryAttribute(USER_ATTRIBUTE);
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#saveUserCopletInstanceDatas(java.lang.String)
     */
    public void saveUserCopletInstanceDatas(String layoutKey) {
        try {
            if (layoutKey == null) {
                layoutKey = this.portalService.getDefaultLayoutKey();
            }
            final ProfileImpl profile = this.getUserProfile(layoutKey);

            final Map profileLSParams = new HashMap();
            profileLSParams.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_COPLETINSTANCE);        

            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_COPLETINSTANCE,
                                          this.getUser(),
                                          false,
                                          layoutKey);
            this.loader.saveProfile(key, profileLSParams, profile.getCopletInstances());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#saveUserLayout(java.lang.String)
     */
    public void saveUserLayout(String layoutKey) {
        try {
            if (layoutKey == null) {
                layoutKey = this.portalService.getDefaultLayoutKey();
            }
            final Profile profile = this.getUserProfile(layoutKey);

            final Map profileLSParams = new HashMap();
            profileLSParams.put(ProfileLS.PARAMETER_PROFILETYPE, 
                           ProfileLS.PROFILETYPE_LAYOUT);        

            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_LAYOUT, 
                                          this.getUser(), 
                                          false,
                                          layoutKey);
            this.loader.saveProfile(key, profileLSParams, profile.getRootLayout());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletType(java.lang.String)
     */
    public CopletType getCopletType(String id) {
        if ( this.copletBaseDatas.objects == null ) {
            this.getCopletTypes();
        }
        return (CopletType)this.copletBaseDatas.objects.get(id);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletTypes()
     */
    public Collection getCopletTypes() {
        if ( this.copletBaseDatas.objects == null ) {
            try {
                // first "load" the global data
                this.getGlobalBaseDatas(this.portalService.getDefaultLayoutKey());
            } catch (Exception e) {
                throw new ProfileException("Unable to load global base datas.", e);
            }            
        }
        return this.copletBaseDatas.objects.values();
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getLayoutInstance(org.apache.cocoon.portal.om.Layout)
     */
    public LayoutInstance getLayoutInstance(Layout layout) {
        LayoutInstance result = null;
        final Profile profile = this.getUserProfile(null);
        if ( profile != null ) {
            result = profile.searchLayoutInstance(layout);
        }
        return result;
    }
}
