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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.coplet.CopletDefinition;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.CopletType;
import org.apache.cocoon.portal.layout.*;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.StaticBucketMap;
import org.apache.excalibur.source.SourceValidity;

/**
 * FIXME - This profile manager implementation does not use all of the new features of 2.2 yet,
 *         like profile manager aspects etc.
 * FIXME - Events for adding/removing coplet instances/layouts not implemented yet
 * @version $Id$
 */
public class StaticProfileManager 
    extends AbstractProfileManager { 

    protected String profilesPath;

    protected final StaticBucketMap copletInstanceDataManagers = new StaticBucketMap();
    protected final StaticBucketMap copletDataManagers = new StaticBucketMap();
    protected final StaticBucketMap copletBaseDataManagers = new StaticBucketMap();

    protected static final String LAYOUTKEY_PREFIX = StaticProfileManager.class.getName() + "/Layout/";

    protected final PortalUser portalUser = new StaticPortalUser();

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(String, String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID) {
        ProfileLS adapter = null;
        try {
            if (layoutKey == null) {
                layoutKey = this.portalService.getDefaultLayoutKey();
            }

            String serviceKey = LAYOUTKEY_PREFIX + layoutKey;
            Object[] objects = (Object[]) this.portalService.getAttribute(serviceKey);

            // check if the layout is already cached and still valid
            int valid = SourceValidity.INVALID;
            SourceValidity sourceValidity = null;
            if (objects != null) {
                sourceValidity = (SourceValidity) objects[1];
                valid = sourceValidity.isValid();
                Layout layout = null;
                if (valid == SourceValidity.VALID)
                    layout = (Layout) ((Map) objects[0]).get(layoutID);
                if (layout != null)
                    return layout;
            }

            Collection c = getCopletInstanceDataManager();
            final Map objectMap = new HashMap();
            final Iterator i = c.iterator();
            while ( i.hasNext() ) {
                CopletInstance current = (CopletInstance)i.next();
                objectMap.put(current.getId(), current);
            }
            Map parameters = new HashMap();
            parameters.put("profiletype", "layout");
            parameters.put("objectmap", objectMap);

            Map map = new LinkedMap();
            map.put("base", this.profilesPath);
            map.put("portalname", this.portalService.getPortalName());
            map.put("profile", "layout");
            map.put("groupKey", layoutKey);

            adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);
            SourceValidity newValidity = adapter.getValidity(map, parameters);
            if (valid == SourceValidity.UNKNOWN) {
                if (sourceValidity.isValid(newValidity) == SourceValidity.VALID) {
                    return (Layout) ((Map) objects[0]).get(layoutID);
                }
            }

            // get Layout specified in the map
            Layout layout = (Layout) adapter.loadProfile(map, parameters);
            Map layouts = new HashMap();

            layouts.put(null, layout); //save root with null as key
            cacheLayouts(layouts, layout);

            // store the new values in the service
            if (newValidity != null) {
                objects = new Object[] { layouts, newValidity };
                this.portalService.setAttribute(serviceKey, objects);
            }

            return (Layout) layouts.get(layoutID);
        } catch (Exception ce) {
            throw new ProfileException("Unable to get layout.", ce);
        } finally {
            this.manager.release(adapter);
        }
    }

    /**
     * @param layoutMap
     * @param layout
     */
    private void cacheLayouts(Map layoutMap, Layout layout) {
        if (layout != null) {
            if (layout.getId() != null) {
                String layoutId = layout.getId();
                layoutMap.put(layoutId, layout);
            }
            if (layout instanceof CompositeLayout) {
                // step through all it's child layouts and cache them too
                CompositeLayout cl = (CompositeLayout) layout;
                Iterator i = cl.getItems().iterator();
                while (i.hasNext()) {
                    Item current = (Item) i.next();
                    this.cacheLayouts(layoutMap, current.getLayout());
                }
            }
        }
    }

    private Map getCopletDefinitionManager() 
    throws Exception {
        final String portalName = this.portalService.getPortalName();
        // ensure that profile is loaded
        this.getCopletInstanceDataManager();
        return (Map)this.copletDataManagers.get(portalName);
    }

    private Collection getCopletInstanceDataManager() 
    throws Exception {
        String portalName = this.portalService.getPortalName();
        Collection copletInstanceDataManager =
            (Collection) this.copletInstanceDataManagers.get(portalName);
        if (copletInstanceDataManager != null) {
            return copletInstanceDataManager;
        }

        ProfileLS adapter = null;
        try {
            adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);

            Map parameters = new HashMap();
            parameters.put("profiletype", "copletbasedata");
            parameters.put("objectmap", null);

            Map map = new LinkedMap();
            map.put("base", this.profilesPath);
            map.put("portalname", this.portalService.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "basedata");
            Collection cBase = (Collection) adapter.loadProfile(map, parameters);
            final Map copletBaseDataManager = new HashMap();
            Iterator i = cBase.iterator();
            while ( i.hasNext() ) {
                final CopletDefinition current = (CopletDefinition)i.next();
                copletBaseDataManager.put(current.getId(), current);
            }
            this.copletBaseDataManagers.put(portalName, copletBaseDataManager);

            //CopletDefinition
            parameters.clear();
            parameters.put("profiletype", "copletdata");
            parameters.put("objectmap", copletBaseDataManager);

            map.clear();
            map.put("base", this.profilesPath);
            map.put("portalname", this.portalService.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "data");
            Collection c = (Collection) adapter.loadProfile(map, parameters);
            final Map copletDataManager = new HashMap();
            i = c.iterator();
            while ( i.hasNext() ) {
                final CopletDefinition current = (CopletDefinition)i.next();
                copletDataManager.put(current.getId(), current);
            }
            //CopletInstanceData
            parameters.clear();
            parameters.put("profiletype", "copletinstancedata");
            parameters.put("objectmap", copletDataManager);

            map.clear();
            map.put("base", this.profilesPath);
            map.put("portalname", this.portalService.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "instancedata");
            copletInstanceDataManager = (Collection) adapter.loadProfile(map, parameters);

            // store managers
            this.copletInstanceDataManagers.put(portalName, copletInstanceDataManager);
            this.copletDataManagers.put(portalName, copletDataManager);
            return copletInstanceDataManager;
        } finally {
            this.manager.release(adapter);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstance(java.lang.String)
     */
    public CopletInstance getCopletInstance(String copletID) {
        // TODO - we should store a map in the static profile manager
        //        instead of going through the collection each time
        try {
            final Iterator i = this.getCopletInstanceDataManager().iterator();
            while ( i.hasNext() ) {
                final CopletInstance current = (CopletInstance) i.next();
                if ( current.getId().equals(copletID) ) {
                    return current;
                }
            }
            return null;
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletInstanceData", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        try {
            Iterator i = getCopletInstanceDataManager().iterator();
            boolean found = false;
            CopletInstance current = null;
            while ( !found && i.hasNext() ) {
                current = (CopletInstance)i.next();
                found = current.getCopletDefinition().getId().equals(copletDataId);
            }
            if ( found && current != null ) {
                return current.getCopletDefinition();
            }
            return null;
        } catch (PortalRuntimeException pre) {
            throw pre;
        } catch (Exception e) {
            throw new PortalRuntimeException("Unable to lookup portal service.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances(org.apache.cocoon.portal.coplet.CopletDefinition)
     */
    public List getCopletInstances(CopletDefinition data) {
        List coplets = new ArrayList();
        try {
            Iterator iter = getCopletInstanceDataManager().iterator();
            while (iter.hasNext()){
                CopletInstance current = (CopletInstance) iter.next();
                if (current.getCopletDefinition().equals(data)) {
                    coplets.add(current);
                }
            }
            return coplets;
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletInstanceData", e);
        }
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) 
    throws ConfigurationException {
        super.configure(config);
        Configuration child = config.getChild("profiles-path");
        this.profilesPath = child.getValue("cocoon:/profiles");
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletDefinitions()
     */
    public Collection getCopletDefinitions() {
        try {
            return this.getCopletDefinitionManager().values();
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletDefinitions.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstances()
     */
    public Collection getCopletInstances() {
        try {
            return this.getCopletInstanceDataManager();
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletInstanceDatas.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getUser()
     */
    public PortalUser getUser() {
        return this.portalUser;
    }

    protected static final class StaticPortalUser extends AbstractPortalUser {

        public StaticPortalUser() {
            this.setUserName("static");
            this.setAnonymous(true);
        }

        /**
         * @see org.apache.cocoon.portal.profile.PortalUser#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(String role) {
            return false;
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletType(java.lang.String)
     */
    public CopletType getCopletType(String id) {
        return (CopletType)((Map)this.copletBaseDataManagers.get(this.portalService.getPortalName())).get(id);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletTypes()
     */
    public Collection getCopletTypes() {
        return ((Map)this.copletBaseDataManagers.get(this.portalService.getPortalName())).values();
    }

}
