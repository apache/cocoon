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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.StaticBucketMap;
import org.apache.excalibur.source.SourceValidity;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * 
 * @version CVS $Id: StaticProfileManager.java,v 1.11 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class StaticProfileManager extends AbstractProfileManager implements Configurable
{
    protected String profilesPath;

    protected StaticBucketMap copletInstanceDataManagers = new StaticBucketMap();

    protected static final String LAYOUTKEY_PREFIX = StaticProfileManager.class.getName() + "/Layout/";

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(String, String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID) {
        PortalService service = null;
        ProfileLS adapter = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            if (layoutKey == null) {
                Layout l = getEntryLayout();
                if (null != l) {
                    return l;
                }
                layoutKey = getDefaultLayoutKey();
            }

            String serviceKey = LAYOUTKEY_PREFIX + layoutKey;
            Object[] objects = (Object[]) service.getAttribute(serviceKey);

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

            CopletInstanceDataManager copletInstanceDataManager = getCopletInstanceDataManager(service);

            Map parameters = new HashMap();
            parameters.put("profiletype", "layout");
            parameters.put("objectmap", copletInstanceDataManager.getCopletInstanceData());

            Map map = new LinkedMap();
            map.put("base", this.profilesPath);
            map.put("portalname", service.getPortalName());
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

            LayoutFactory factory = service.getComponentManager().getLayoutFactory();
            factory.prepareLayout(layout);

            // store the new values in the service
            if (newValidity != null) {
                objects = new Object[] { layouts, newValidity };
                service.setAttribute(serviceKey, objects);
            }

            return (Layout) layouts.get(layoutID);
        } catch (Exception ce) {
            throw new CascadingRuntimeException("Unable to get layout.", ce);
        } finally {
            this.manager.release(service);
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

    private CopletInstanceDataManager getCopletInstanceDataManager(PortalService service) 
    throws Exception {
        String portalName = service.getPortalName();
        CopletInstanceDataManager copletInstanceDataManager =
            (CopletInstanceDataManager) this.copletInstanceDataManagers.get(portalName);
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
            map.put("portalname", service.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "basedata");
            CopletBaseDataManager copletBaseDataManager = (CopletBaseDataManager) adapter.loadProfile(map, parameters);

            //CopletData
            parameters.clear();
            parameters.put("profiletype", "copletdata");
            parameters.put("objectmap", copletBaseDataManager.getCopletBaseData());

            map.clear();
            map.put("base", this.profilesPath);
            map.put("portalname", service.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "data");
            CopletDataManager copletDataManager = (CopletDataManager) adapter.loadProfile(map, parameters);

            //CopletInstanceData
            parameters.clear();
            parameters.put("profiletype", "copletinstancedata");
            parameters.put("objectmap", copletDataManager.getCopletData());

            map.clear();
            map.put("base", this.profilesPath);
            map.put("portalname", service.getPortalName());
            map.put("profile", "coplet");
            map.put("name", "instancedata");
            copletInstanceDataManager = (CopletInstanceDataManager) adapter.loadProfile(map, parameters);

            CopletFactory copletFactory = service.getComponentManager().getCopletFactory();
            Iterator iterator = copletDataManager.getCopletData().values().iterator();
            while (iterator.hasNext()) {
                CopletData cd = (CopletData) iterator.next();
                copletFactory.prepare(cd);
            }
            iterator = copletInstanceDataManager.getCopletInstanceData().values().iterator();
            while (iterator.hasNext()){
                CopletInstanceData cid = (CopletInstanceData) iterator.next();
                copletFactory.prepare(cid);
            }

            this.copletInstanceDataManagers.put(portalName, copletInstanceDataManager);
            return copletInstanceDataManager;
        } finally {
            this.manager.release(service);
            this.manager.release(adapter);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(java.lang.String)
     */
    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            return getCopletInstanceDataManager(service).getCopletInstanceData(copletID);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error in getCopletInstanceData", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletData(java.lang.String)
     */
    public CopletData getCopletData(String copletDataId) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            Iterator i = getCopletInstanceDataManager(service).getCopletInstanceData().values().iterator();
            boolean found = false;
            CopletInstanceData current = null;
            while ( !found && i.hasNext() ) {
                current = (CopletInstanceData)i.next();
                found = current.getCopletData().getId().equals(copletDataId);
            }
            if ( found ) {
                return current.getCopletData();
            }
            return null;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#getCopletInstanceData(org.apache.cocoon.portal.coplet.CopletData)
     */
    public List getCopletInstanceData(CopletData data) {
        List coplets = new ArrayList();
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            Iterator iter = getCopletInstanceDataManager(service).getCopletInstanceData().values().iterator();
            while (iter.hasNext()){
                CopletInstanceData current = (CopletInstanceData) iter.next();
                if (current.getCopletData().equals(data)) {
                    coplets.add(current);
                }
            }
            return coplets;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error in getCopletInstanceData", e);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void register(CopletInstanceData coplet) {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void unregister(CopletInstanceData coplet) {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
     */
    public void unregister(Layout layout) {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        super.configure(configuration);
        Configuration child = configuration.getChild("profiles-path");
        this.profilesPath = child.getValue("cocoon:/profiles");
        //this.profilesPath = "context://samples/simple-portal/profiles";
    }
}
