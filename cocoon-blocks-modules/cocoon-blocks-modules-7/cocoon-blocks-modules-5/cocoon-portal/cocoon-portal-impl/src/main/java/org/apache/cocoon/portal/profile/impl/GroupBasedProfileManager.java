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
import java.util.Properties;

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
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.ProfileLS;
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
 * @version $Id$
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

    /** The profiler loader/saver. */
    protected ProfileLS loader;

    /** The configuration for loading/saving the profile. */
    protected Properties configuration;

    public void setProfileLS(ProfileLS loader) {
        this.loader = loader;
    }

    protected ProfileHolder getUserProfile() {
        return (ProfileHolder)this.portalService.getUserService().getAttribute(KEY_PREFIX + "profile");
    }

    protected void removeUserProfiles() {
        this.portalService.getUserService().removeAttribute(KEY_PREFIX + "profile");
    }

    protected void storeUserProfile(ProfileHolder profile) {
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
        final ProfileHolder profile = this.getUserProfile();
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
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletInstance(copletID);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletDefinition(copletDataId);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public List getCopletInstances(CopletDefinition data) {
        final ProfileHolder profile = this.getUserProfile();
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
        final ProfileHolder profile = this.getUserProfile();
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
        final ProfileHolder profile = this.getUserProfile();
        profile.remove(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutAddedEvent event) {
        final ProfileHolder profile = this.getUserProfile();
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout added event.
     * @see Receiver
     */
    public void inform(LayoutInstanceAddedEvent event) {
        final ProfileHolder profile = this.getUserProfile();
        profile.add(event.getTarget());
    }

    /**
     * Receives a layout removed event.
     * @see Receiver
     */
    public void inform(LayoutRemovedEvent event) {
        final ProfileHolder profile = this.getUserProfile();
        profile.remove(event.getTarget());
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getLayout(java.lang.String)
     */
    public Layout getLayout(String layoutId) {
        ProfileHolder profile = this.getUserProfile();
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
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.getCopletDefinitions();
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances()
     */
    public Collection getCopletInstances() {
        final ProfileHolder profile = this.getUserProfile();
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
    protected ProfileHolder loadProfile() 
    throws ProfileException {
        final String defaultProfileName = this.portalService.getUserService().getDefaultProfileName();
        final PortalUser user = this.portalService.getUserService().getUser();
        ProfileHolder profile = new ProfileHolder();

        try {
            // first "load" the global data
            profile.setCopletTypes( this.getGlobalCopletTypes() );
            // FIXME - We should be able to merge definitions from various locations
            //         This could also be handled by aspects?
            profile.setCopletDefinitions( this.getGlobalCopletDefinitions(user, profile) );
    
            // now load the user/group specific data
            this.loadProfile(profile, user, defaultProfileName);
            // FIXME - Traverse the layout tree for link layouts
        } catch (ProfileException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfileException("Unable to load profile '" + defaultProfileName + "' for user " + user + ".", e);
        }
        this.storeUserProfile(profile);
        return profile;
    }

    protected void loadProfile(ProfileHolder profile, PortalUser user, String profileName)
    throws ProfileException {
        try {
            if ( !this.getCopletInstances(profile, user, CATEGORY_USER, profileName) ) {
                if ( user.getGroups().size() == 0 || !this.getCopletInstances(profile, user, CATEGORY_GROUP, profileName)) {
                    if ( !this.getCopletInstances(profile, user, CATEGORY_GLOBAL, profileName) ) {
                        throw new ProcessingException("No profile for copletinstancedatas found.");
                    }
                }
            }
    
            if ( !this.getLayout(profile, user, CATEGORY_USER, profileName) ) {
                if ( user.getGroups().size() == 0 || !this.getLayout(profile, user, CATEGORY_GROUP, profileName)) {
                    if ( !this.getLayout(profile, user, CATEGORY_GLOBAL, profileName) ) {
                        throw new ProcessingException("No profile for layout found.");
                    }
                }
            }        
        } catch (ProfileException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfileException("Unable to load profile '" + profileName + "' for user " + user + ".", e);
        }
    }

    /**
     * Return the current set of global coplet types.
     * @return The global set of coplet types.
     * @throws Exception
     */
    protected Map getGlobalCopletTypes()
    throws Exception {
        // if we already have loaded the profile and don't check
        // for changes, just return the profile
        if ( this.copletTypes.objects != null && !this.checkForChanges ) {
            return this.copletTypes.objects;
        }

        // build key for loading the profile
        final Map key = this.buildKey(CATEGORY_GLOBAL,
                ProfileLS.PROFILETYPE_COPLETTYPE,
                null,
                true,
                null);

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
            Collection collection = (Collection)loader.loadProfile(key, ProfileLS.PROFILETYPE_COPLETTYPE, null);
            collection = this.processCopletTypes(collection);
            final Map objects = new HashMap();
            final Iterator i = collection.iterator();
            while ( i.hasNext() ) {
                final CopletType current = (CopletType)i.next();
                objects.put(current.getId(), current);
            }
            this.copletTypes.objects = objects;
            if ( newValidity == null ) {
                newValidity = loader.getValidity(key, ProfileLS.PROFILETYPE_COPLETTYPE);
            }
            this.copletTypes.validity = newValidity;
            // now invalidate coplet definitions
            this.copletDefinitions.objects = null;
            this.copletDefinitions.validity = null;

            return this.copletTypes.objects;
        }
    }

    protected Map getGlobalCopletDefinitions(final PortalUser  info,
                                             final ProfileHolder profile)
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
                null);
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
            Collection collection = (Collection)loader.loadProfile(key, ProfileLS.PROFILETYPE_COPLETDEFINITION, profile.getCopletTypesMap());
            collection = this.processCopletDefinitions(collection);
            final Iterator i = collection.iterator();
            final Map objects = new HashMap();
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

    protected boolean getCopletInstances(final ProfileHolder profile,
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
            cidm = this.processCopletInstances(profile, cidm);
            profile.setCopletInstances(cidm);

            return true;
        } catch (Exception e) {
            if (!isSourceNotFoundException(e)) {
                throw e;
            }
            return false;
        }
    }

    protected boolean getLayout(final ProfileHolder profile,
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
            l = this.processLayout(profile, l);
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
    throws LayoutException {
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
        final String uri = this.configuration.getProperty(config.toString());
        if ( uri == null ) {
            throw new LayoutException("Configuration for key '" + config.toString() + "' is missing.");
        }
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
            final ProfileHolder profile = this.getUserProfile();
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

    protected void saveLayoutInstances() {
        try {
            final ProfileHolder profile = this.getUserProfile();
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
                this.getGlobalCopletTypes();
            } catch (ProfileException e) {
                throw e;
            } catch (Exception e) {
                throw new ProfileException("Unable to load global coplet types.", e);
            }            
        }
        return this.copletTypes.objects.values();
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getLayoutInstance(org.apache.cocoon.portal.om.Layout)
     */
    public LayoutInstance getLayoutInstance(Layout layout) {
        LayoutInstance result = null;
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            result = profile.searchLayoutInstance(layout);
        }
        return result;
    }

    public void setCheckForChanges(boolean checkForChanges) {
        this.checkForChanges = checkForChanges;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }
}
