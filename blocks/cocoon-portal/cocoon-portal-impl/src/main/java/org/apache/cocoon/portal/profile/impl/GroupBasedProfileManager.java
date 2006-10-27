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
import org.apache.cocoon.portal.om.PortalUser;
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

    protected static final String KEY_PREFIX = GroupBasedProfileManager.class.getName() + ':';

    protected static final class ProfileInfo {
        public Map            objects;
        public SourceValidity validity;
    }

    final protected ProfileInfo copletTypes = new ProfileInfo();
    final protected ProfileInfo copletDefinitions = new ProfileInfo();

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

    protected ProfileImpl getUserProfile() {
        return (ProfileImpl)this.portalService.getUserService().getAttribute(KEY_PREFIX + "profile");
    }

    protected void removeUserProfiles() {
        this.portalService.getUserService().removeAttribute(KEY_PREFIX + "profile");
    }

    protected void storeUserProfile(Profile profile) {
        this.portalService.getUserService().setAttribute(KEY_PREFIX + "profile", profile);
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#login(org.apache.cocoon.portal.om.PortalUser)
     */
    protected void login(PortalUser user) {
        super.login(user);
        // TODO - we should move most of the stuff from getLayout to here
        // for now we use a hack :)
        this.getLayout(null);
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#logout(org.apache.cocoon.portal.om.PortalUser)
     */
    protected void logout(PortalUser user) {
        final Profile profile = this.getUserProfile();
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
        final Profile profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletInstance(copletID);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        final ProfileImpl profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletDefinition(copletDataId);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public List getCopletInstances(CopletDefinition data) {
        final Profile profile = this.getUserProfile();
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
    public void inform(CopletInstanceAddedEvent event) {
        final ProfileImpl profile = this.getUserProfile();
        profile.add(event.getTarget());
    }

    /**
     * Receives a coplet data added event.
     * @see Receiver
     */
    public void inform(CopletDefinitionAddedEvent event) {
        this.deployedCopletDefinitions.put(event.getTarget().getId(), event.getTarget());
        if ( this.copletDefinitions.objects != null ) {
            this.copletDefinitions.objects.put(event.getTarget().getId(), event.getTarget());
        }
    }

    /**
     * Receives a coplet instance data added event.
     * @see Receiver
     */
    public void inform(CopletInstanceRemovedEvent event) {
        final ProfileImpl profile = this.getUserProfile();
        profile.remove(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutAddedEvent event) {
        final ProfileImpl profile = this.getUserProfile();
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutInstanceAddedEvent event) {
        final ProfileImpl profile = this.getUserProfile();
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout removed event.
     * @see Receiver
     */
    public void inform(LayoutRemovedEvent event) {
        final ProfileImpl profile = this.getUserProfile();
        profile.remove(event.getTarget());
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getLayout(java.lang.String)
     */
    public Layout getLayout(String layoutId) {
        Profile profile = this.getUserProfile();
        if ( profile == null ) {
            profile = this.loadProfile();
        }
        if ( profile == null ) {
            throw new ProfileException("Unable to get standard profile with layout key '" + this.portalService.getUserService().getDefaultProfileName() + "' for user " + this.portalService.getUserService().getUser());
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
        final ProfileImpl profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.getCopletDefinitions();
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances()
     */
    public Collection getCopletInstances() {
        final Profile profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.getCopletInstances();
        }
        return null;
    }

    /**
     * Load the profile.
     * This loads the profile for the current user. First the default profile is read. This might
     * contain links to other profiles which are then loaded recursively.
     */
    protected Profile loadProfile() 
    throws ProfileException {
        final String layoutKey = this.portalService.getUserService().getDefaultProfileName();
        final PortalUser user = this.portalService.getUserService().getUser();
        ProfileImpl profile = new ProfileImpl();

        try {
            // first "load" the global data
            profile.setCopletTypes( this.getGlobalCopletTypes(layoutKey) );
            profile.setCopletDefinitions( this.getGlobalDatas(user, profile, layoutKey) );
    
            // now load the user/group specific data
            if ( !this.getCopletInstanceDatas(profile, user, CATEGORY_USER, layoutKey) ) {
                if ( user.getGroups().size() == 0 || !this.getCopletInstanceDatas(profile, user, CATEGORY_GROUP, layoutKey)) {
                    if ( !this.getCopletInstanceDatas(profile, user, CATEGORY_GLOBAL, layoutKey) ) {
                        throw new ProcessingException("No profile for copletinstancedatas found.");
                    }
                }
            }

            if ( !this.getLayout(profile, user, CATEGORY_USER, layoutKey) ) {
                if ( user.getGroups().size() == 0 || !this.getLayout(profile, user, CATEGORY_GROUP, layoutKey)) {
                    if ( !this.getLayout(profile, user, CATEGORY_GLOBAL, layoutKey) ) {
                        throw new ProcessingException("No profile for layout found.");
                    }
                }
            }
        } catch (ProfileException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfileException("Unable to load profile '" + layoutKey + "' for user " + user + ".", e);
        }
        final Profile processedProfile = this.processProfile(profile);
        this.storeUserProfile(processedProfile);
        return processedProfile;
    }

    protected Map getGlobalCopletTypes(final String profileName)
    throws Exception {
        // if we already have loaded the profile and don't check
        // for changes, just return the profile
        if ( this.copletTypes.objects != null && !this.checkForChanges ) {
            return this.copletTypes.objects;
        }

        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETTYPE,
                null,
                true,
                profileName);

        SourceValidity newValidity = null;
        // if we have a profile, check for reloading
        if ( this.copletTypes.validity != null ) {
            // if it's still valid just return the profile
            final int validity = this.copletTypes.validity.isValid();
            if ( validity == SourceValidity.VALID) {
                return this.copletTypes.objects;
            } else if ( validity == SourceValidity.UNKNOWN ) {
                newValidity = loader.getValidity(key, ProfileLS.PROFILETYPE_COPLETTYPE);
                if ( newValidity != null
                     && this.copletTypes.validity.isValid(newValidity) == SourceValidity.VALID) {
                    return this.copletTypes.objects;
                }
            }
        }

        // we have to load/reload
        synchronized ( this ) {
            final Map objects = new HashMap();
            final Iterator i = ((Collection)loader.loadProfile(key, ProfileLS.PROFILETYPE_COPLETTYPE, null)).iterator();
            while ( i.hasNext() ) {
                final CopletType current = (CopletType)i.next();
                objects.put(current.getId(), current);
            }
            this.copletTypes.objects = objects;
            if ( newValidity == null ) {
                newValidity = loader.getValidity(key, ProfileLS.PROFILETYPE_COPLETTYPE);
            }
            this.copletTypes.validity = newValidity;
            this.copletDefinitions.objects = null;
            this.copletDefinitions.validity = null;
            this.prepareObject(null, this.copletTypes.objects);
            return this.copletTypes.objects;
        }
    }

    protected Map getGlobalDatas(final PortalUser  info,
                                 final ProfileImpl profile,
                                 final String      layoutKey)
    throws Exception {
        // if we already have loaded the profile and don't check
        // for changes, just return the profile
        if ( this.copletDefinitions.objects != null && !this.checkForChanges ) {
            return this.copletDefinitions.objects;
        }

        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETDEFINITION,
                info,
                true,
                layoutKey);
        SourceValidity newValidity = null;
        // if we have a profile, check for reloading
        if ( this.copletDefinitions.validity != null ) {
            // if it's still valid just return the profile
            final int validity = this.copletDefinitions.validity.isValid();
            if ( validity == SourceValidity.VALID) {
                return this.copletDefinitions.objects;
            } else if ( validity == SourceValidity.UNKNOWN ) {
                newValidity = loader.getValidity(key, ProfileLS.PROFILETYPE_COPLETDEFINITION);
                if ( newValidity != null
                     && this.copletDefinitions.validity.isValid(newValidity) == SourceValidity.VALID) {
                    return this.copletDefinitions.objects;
                }
            }
        }

        synchronized ( this ) {
            final Map objects = new HashMap();
            final Iterator i = ((Collection)loader.loadProfile(key, ProfileLS.PROFILETYPE_COPLETDEFINITION, profile.getCopletTypesMap())).iterator();
            while ( i.hasNext() ) {
                final CopletDefinition current = (CopletDefinition)i.next();
                // only add coplet data if coplet base data has been found
                if ( current.getCopletType() != null ) {
                    objects.put(current.getId(), current);
                } else {
                    this.getLogger().error("CopletType not found for CopletDefinition: " + current);
                }
            }
            this.copletDefinitions.objects = objects;
            // now add deployed coplets
            this.copletDefinitions.objects.putAll(this.deployedCopletDefinitions);
            if ( newValidity == null ) {
                newValidity = loader.getValidity(key, ProfileLS.PROFILETYPE_COPLETDEFINITION);
            }
            this.copletDefinitions.validity = newValidity;
            this.prepareObject(profile, this.copletDefinitions.objects);
            return this.copletDefinitions.objects;
        }
    }

    private boolean isSourceNotFoundException(Throwable e) {
        Throwable t = e;
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
        try {
            Collection cidm = (Collection)loader.loadProfile(key, ProfileLS.PROFILETYPE_COPLETINSTANCE, profile.getCopletDefinitionsMap());
            profile.setCopletInstances(cidm);
            this.prepareObject(profile, profile.getCopletInstancesMap());

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
        try {
            Layout l = (Layout)loader.loadProfile(key, ProfileLS.PROFILETYPE_LAYOUT, profile.getCopletInstancesMap());
            this.prepareObject(profile, l);
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
                           String   profileName)
    throws ParameterException {
        if ( profileName == null ) {
            profileName = this.portalService.getUserService().getDefaultProfileName();
        }
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
        key.put("layout", profileName);
        key.put("type", category);
        if ( CATEGORY_GROUP.equals(category) ) {
            // TODO Groups is a collection!
            key.put("group", "none");
            //key.put("group", info.getGroups());
        } else if ( CATEGORY_USER.equals(category) ) {
            key.put("user", info.getUserName());
        }

        return key;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
        this.saveCopletInstances();
        this.saveLayoutInstances();
    }

    protected void saveCopletInstances() {
        try {
            final ProfileImpl profile = this.getUserProfile();
            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_COPLETINSTANCE,
                                          this.portalService.getUserService().getUser(),
                                          false,
                                          null);
            this.loader.saveProfile(key, ProfileLS.PROFILETYPE_COPLETINSTANCE, profile.getCopletInstances());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.AbstractProfileManager#saveUserLayout(java.lang.String)
     */
    public void saveLayoutInstances() {
        try {
            final Profile profile = this.getUserProfile();
            final Map key = this.buildKey(CATEGORY_USER,
                                          ProfileLS.PROFILETYPE_LAYOUTINSTANCE, 
                                          this.portalService.getUserService().getUser(),
                                          false,
                                          null);
            this.loader.saveProfile(key, ProfileLS.PROFILETYPE_LAYOUTINSTANCE, profile.getRootLayout());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletType(java.lang.String)
     */
    public CopletType getCopletType(String id) {
        if ( this.copletTypes.objects == null ) {
            this.getCopletTypes();
        }
        return (CopletType)this.copletTypes.objects.get(id);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletTypes()
     */
    public Collection getCopletTypes() {
        if ( this.copletTypes.objects == null ) {
            try {
                // first "load" the global data
                this.getGlobalCopletTypes(this.portalService.getUserService().getDefaultProfileName());
            } catch (Exception e) {
                throw new ProfileException("Unable to load global base datas.", e);
            }            
        }
        return this.copletTypes.objects.values();
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getLayoutInstance(org.apache.cocoon.portal.om.Layout)
     */
    public LayoutInstance getLayoutInstance(Layout layout) {
        LayoutInstance result = null;
        final Profile profile = this.getUserProfile();
        if ( profile != null ) {
            result = profile.searchLayoutInstance(layout);
        }
        return result;
    }
}
