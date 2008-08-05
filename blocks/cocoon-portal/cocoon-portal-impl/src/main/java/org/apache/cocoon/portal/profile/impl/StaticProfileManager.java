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

import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.profile.PersistenceType;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.ProfileKey;
import org.apache.cocoon.portal.profile.ProfileStore;
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

    protected String profilesPath = "cocoon:/profiles";

    protected final StaticBucketMap copletInstances = new StaticBucketMap();
    protected final StaticBucketMap copletDefinitions = new StaticBucketMap();

    protected static final String LAYOUTKEY_PREFIX = StaticProfileManager.class.getName() + "/Layout/";

    /** The profiler loader/saver. */
    protected ProfileStore loader;

    public void setProfileLS(ProfileStore loader) {
        this.loader = loader;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getLayout(java.lang.String)
     */
    public Layout getLayout(String layoutID) {
        try {
            final String layoutKey = this.portalService.getUserService().getDefaultProfileName();

            String serviceKey = LAYOUTKEY_PREFIX + layoutKey;
            Object[] objects = (Object[]) this.portalService.getUserService().getAttribute(serviceKey);

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

            // load layout
            final ProfileKey map = new ProfileKey();
            map.setPortalName(this.portalService.getPortalName());
            map.setProfileCategory("layout");
            map.setProfileName(layoutKey);

            SourceValidity newValidity = this.loader.getValidity(map, ProfileStore.PROFILETYPE_LAYOUT);
            if (valid == SourceValidity.UNKNOWN) {
                if (sourceValidity.isValid(newValidity) == SourceValidity.VALID) {
                    return (Layout) ((Map) objects[0]).get(layoutID);
                }
            }

            // get Layout specified in the map
            final PersistenceType lType = new PersistenceType(ProfileStore.PROFILETYPE_LAYOUT);
            lType.setReferences("layoutType", this.copletTypesMap);
            lType.setReferences("customRenderer", this.rendererMap);

            Layout layout = (Layout) this.loader.loadProfile(map, lType);
            layout = this.processLayout(null, layout);

            final Map layouts = new HashMap();

            layouts.put(null, layout); //save root with null as key
            cacheLayouts(layouts, layout);

            // store the new values in the service
            if (newValidity != null) {
                objects = new Object[] { layouts, newValidity };
                this.portalService.getUserService().setAttribute(serviceKey, objects);
            }

            return (Layout) layouts.get(layoutID);
        } catch (Exception ce) {
            throw new ProfileException("Unable to get layout.", ce);
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
        this.loadCopletInstances();
        return (Map)this.copletDefinitions.get(portalName);
    }

    private Collection loadCopletInstances()
    throws Exception {
        String portalName = this.portalService.getPortalName();
        Collection instances = (Collection) this.copletInstances.get(portalName);
        if (instances != null) {
            return instances;
        }

        // CopletDefinition
        ProfileKey map = new ProfileKey();
        map.setPortalName(this.portalService.getPortalName());
        map.setProfileCategory("coplet");
        map.setProfileName("data");

        final PersistenceType cdType = new PersistenceType(ProfileStore.PROFILETYPE_COPLETDEFINITION);
        cdType.setReferences("copletType", this.copletTypesMap);

        Collection c = (Collection) this.loader.loadProfile(map, cdType);
        c = this.processCopletDefinitions(c);
        final Map definitions = new HashMap();
        final Iterator i = c.iterator();
        while ( i.hasNext() ) {
            final CopletDefinition current = (CopletDefinition)i.next();
            definitions.put(current.getId(), current);
        }
        this.copletDefinitions.put(portalName, definitions);

        //CopletInstanceData
        map = new ProfileKey();
        map.setPortalName(this.portalService.getPortalName());
        map.setProfileCategory("coplet");
        map.setProfileName("instancedata");

        final PersistenceType ciType = new PersistenceType(ProfileStore.PROFILETYPE_COPLETINSTANCE);
        ciType.setReferences("copletDefinition", definitions);

        instances = (Collection) this.loader.loadProfile(map, ciType);
        instances = this.processCopletInstances(null, instances);

        // store managers
        this.copletInstances.put(portalName, instances);
        return instances;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstance(java.lang.String)
     */
    public CopletInstance getCopletInstance(String copletID) {
        // TODO - we should store a map in the static profile manager
        //        instead of going through the collection each time
        try {
            final Iterator i = this.loadCopletInstances().iterator();
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
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletDefinition(java.lang.String)
     */
    public CopletDefinition getCopletDefinition(String copletDataId) {
        try {
            Iterator i = loadCopletInstances().iterator();
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
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstances(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public List getCopletInstances(CopletDefinition data) {
        List coplets = new ArrayList();
        try {
            Iterator iter = loadCopletInstances().iterator();
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
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletDefinitions()
     */
    public Collection getCopletDefinitions() {
        try {
            return this.getCopletDefinitionManager().values();
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletDefinitions.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletInstances()
     */
    public Collection getCopletInstances() {
        try {
            return this.loadCopletInstances();
        } catch (Exception e) {
            throw new ProfileException("Error in getCopletInstanceDatas.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getLayoutInstance(org.apache.cocoon.portal.om.Layout)
     */
    public LayoutInstance getLayoutInstance(Layout layout) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProfilesPath(String profilesPath) {
        this.profilesPath = profilesPath;
    }

}
