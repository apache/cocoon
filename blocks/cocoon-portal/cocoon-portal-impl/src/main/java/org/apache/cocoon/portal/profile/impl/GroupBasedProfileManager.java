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

import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletDefinitionAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceRemovedEvent;
import org.apache.cocoon.portal.event.layout.LayoutAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutInstanceAddedEvent;
import org.apache.cocoon.portal.event.layout.LayoutRemovedEvent;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.profile.PersistenceType;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.ProfileKey;
import org.apache.cocoon.portal.profile.ProfileStore;
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

    final protected ProfileInfo copletDefinitions = new ProfileInfo();

    /** All deployed coplet datas. */
    final protected Map deployedCopletDefinitions = new HashMap();

    /** Check for changes? */
    protected boolean checkForChanges = true;

    /** The profiler loader/saver. */
    protected ProfileStore loader;

    public void setProfileStore(ProfileStore loader) {
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
                CopletAdapter adapter = cid.getCopletDefinition().getCopletType().getCopletAdapter();
                adapter.logout( cid );
            }

            this.removeUserProfiles();
        }
        super.logout(user);
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstance(java.lang.String)
     */
    public CopletInstance getCopletInstance(String copletID) {
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletInstance(copletID);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.searchCopletDefinition(copletDataId);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstances(org.apache.cocoon.portal.om.CopletDefinition)
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
     * @see org.apache.cocoon.portal.services.ProfileManager#getLayout(java.lang.String)
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
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletDefinitions()
     */
    public Collection getCopletDefinitions() {
        final ProfileHolder profile = this.getUserProfile();
        if ( profile != null ) {
            return profile.getCopletDefinitions();
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstances()
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
        profile.setLayoutTypes(this.portalService.getLayoutFactory().getLayoutTypes());

        try {
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
                        throw new ProfileException("No profile for copletinstancedatas found.");
                    }
                }
            }

            if ( !this.getLayout(profile, user, CATEGORY_USER, profileName) ) {
                if ( user.getGroups().size() == 0 || !this.getLayout(profile, user, CATEGORY_GROUP, profileName)) {
                    if ( !this.getLayout(profile, user, CATEGORY_GLOBAL, profileName) ) {
                        throw new ProfileException("No profile for layout found.");
                    }
                }
            }
        } catch (ProfileException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfileException("Unable to load profile '" + profileName + "' for user " + user + ".", e);
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

        final ProfileKey key = this.buildKey(CATEGORY_GLOBAL,
                info,
                null);
        SourceValidity newValidity = null;
        // if we have a profile, check for reloading
        if ( this.copletDefinitions.validity != null ) {
            // if it's still valid just return the profile
            final int validity = this.copletDefinitions.validity.isValid();
            if ( validity == SourceValidity.VALID) {
                return this.copletDefinitions.objects;
            } else if ( validity == SourceValidity.UNKNOWN ) {
                newValidity = loader.getValidity(key, ProfileStore.PROFILETYPE_COPLETDEFINITION);
                if ( newValidity != null
                     && this.copletDefinitions.validity.isValid(newValidity) == SourceValidity.VALID) {
                    return this.copletDefinitions.objects;
                }
            }
        }

        synchronized ( this ) {
            final PersistenceType persType = new PersistenceType(ProfileStore.PROFILETYPE_COPLETDEFINITION);
            persType.setReferences("copletType", this.copletTypesMap);

            Collection collection = (Collection)loader.loadProfile(key, persType);
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
                newValidity = loader.getValidity(key, ProfileStore.PROFILETYPE_COPLETDEFINITION);
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
        ProfileKey key = this.buildKey(category,
                                info,
                                layoutKey);
        try {
            final PersistenceType persType = new PersistenceType(ProfileStore.PROFILETYPE_COPLETINSTANCE);
            persType.setReferences("copletDefinition", profile.getCopletDefinitionsMap());

            Collection cidm = (Collection)loader.loadProfile(key, persType);
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
        final ProfileKey key = this.buildKey(category,
                                      info,
                                      layoutKey);
        try {
            final PersistenceType persType = new PersistenceType(ProfileStore.PROFILETYPE_LAYOUT);
            persType.setReferences("layoutType", profile.getLayoutTypesMap());
            persType.setReferences("customRenderer", this.rendererMap);

            Layout l = (Layout)loader.loadProfile(key, persType);
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

    protected ProfileKey buildKey(String   category,
                                  PortalUser info,
                                  String   profileName)
    throws LayoutException {
        if ( profileName == null ) {
            profileName = this.portalService.getUserService().getDefaultProfileName();
        }
        final ProfileKey key = new ProfileKey();
        key.setPortalName(this.portalService.getPortalName());
        key.setProfileName(profileName);
        key.setProfileCategory(category);
        if ( CATEGORY_GROUP.equals(category) ) {
            key.setUserGroups(info.getGroups());
        } else if ( CATEGORY_USER.equals(category) ) {
            key.setUserName(info.getUserName());
        }

        return key;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
        this.saveCopletInstances();
        this.saveLayoutInstances();
    }

    protected void saveCopletInstances() {
        try {
            final ProfileHolder profile = this.getUserProfile();
            final ProfileKey key = this.buildKey(CATEGORY_USER,
                                          this.portalService.getUserService().getUser(),
                                          null);
            final PersistenceType persType = new PersistenceType(ProfileStore.PROFILETYPE_COPLETINSTANCE);
            persType.setReferences("copletDefinition", profile.getCopletDefinitionsMap());

            this.loader.saveProfile(key, persType, profile.getCopletInstances());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    protected void saveLayoutInstances() {
        try {
            final ProfileHolder profile = this.getUserProfile();
            final ProfileKey key = this.buildKey(CATEGORY_USER,
                                          this.portalService.getUserService().getUser(),
                                          null);
            final PersistenceType persType = new PersistenceType(ProfileStore.PROFILETYPE_LAYOUTINSTANCE);
            persType.setReferences("layout", profile.keyedLayouts);

            this.loader.saveProfile(key, persType, profile.getRootLayout());
        } catch (Exception e) {
            // TODO
            throw new ProfileException("Exception during save profile", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getLayoutInstance(org.apache.cocoon.portal.om.Layout)
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
}
