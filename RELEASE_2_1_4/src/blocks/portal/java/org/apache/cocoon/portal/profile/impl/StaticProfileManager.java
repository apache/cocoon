/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: StaticProfileManager.java,v 1.9 2004/01/27 14:58:05 cziegeler Exp $
 */
public class StaticProfileManager extends AbstractProfileManager implements Configurable
{
    protected String profilesPath;

    protected StaticBucketMap copletInstanceDataManagers = new StaticBucketMap();

    protected static final String LAYOUTKEY_PREFIX = StaticProfileManager.class.getName() + "/Layout/";

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(String, String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID)
    {
        PortalService service = null;
        ProfileLS adapter = null;
        try
        {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            if (layoutKey == null)
            {
                Layout l = getEntryLayout();
                if (null != l)
                {
                    return l;
                }
                layoutKey = getDefaultLayoutKey();
            }

            String serviceKey = LAYOUTKEY_PREFIX + layoutKey;
            Object[] objects = (Object[]) service.getAttribute(serviceKey);

            // check if the layout is already cached and still valid
            int valid = SourceValidity.INVALID;
            SourceValidity sourceValidity = null;
            if (objects != null)
            {
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
            if (valid == SourceValidity.UNKNOWN)
            {
                if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
                {
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
            if (newValidity != null)
            {
                objects = new Object[] { layouts, newValidity };
                service.setAttribute(serviceKey, objects);
            }

            return (Layout) layouts.get(layoutID);
        }
        catch (Exception ce)
        {
            throw new CascadingRuntimeException("Unable to get layout.", ce);
        }
        finally
        {
            this.manager.release(service);
            this.manager.release(adapter);
        }
    }

    /**
     * @param layoutMap
     * @param layout
     */
    private void cacheLayouts(Map layoutMap, Layout layout)
    {
        if (layout != null)
        {
            if (layout.getId() != null)
            {
                String layoutId = layout.getId();
                layoutMap.put(layoutId, layout);
            }
            if (layout instanceof CompositeLayout)
            {
                // step through all it's child layouts and cache them too
                CompositeLayout cl = (CompositeLayout) layout;
                Iterator i = cl.getItems().iterator();
                while (i.hasNext())
                {
                    Item current = (Item) i.next();
                    this.cacheLayouts(layoutMap, current.getLayout());
                }
            }
        }

    }

    private CopletInstanceDataManager getCopletInstanceDataManager(PortalService service) throws Exception
    {
        String portalName = service.getPortalName();
        CopletInstanceDataManager copletInstanceDataManager =
            (CopletInstanceDataManager) this.copletInstanceDataManagers.get(portalName);
        if (copletInstanceDataManager != null)
            return copletInstanceDataManager;

        ProfileLS adapter = null;
        try
        {
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
            while (iterator.hasNext())
            {
                CopletData cd = (CopletData) iterator.next();
                copletFactory.prepare(cd);
            }
            iterator = copletInstanceDataManager.getCopletInstanceData().values().iterator();
            while (iterator.hasNext())
            {
                CopletInstanceData cid = (CopletInstanceData) iterator.next();
                copletFactory.prepare(cid);
            }

            this.copletInstanceDataManagers.put(portalName, copletInstanceDataManager);
            return copletInstanceDataManager;
        }
        finally
        {
            this.manager.release(service);
            this.manager.release(adapter);
        }
    }

    public CopletInstanceData getCopletInstanceData(String copletID)
    {
        PortalService service = null;
        try
        {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            return getCopletInstanceDataManager(service).getCopletInstanceData(copletID);
        }
        catch (Exception e)
        {
            throw new CascadingRuntimeException("Error in getCopletInstanceData", e);
        }
        finally
        {
            this.manager.release(service);
        }
    }

    public List getCopletInstanceData(CopletData data)
    {
        List coplets = new ArrayList();
        PortalService service = null;
        try
        {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            Iterator iter = getCopletInstanceDataManager(service).getCopletInstanceData().values().iterator();
            while (iter.hasNext())
            {
                CopletInstanceData current = (CopletInstanceData) iter.next();
                if (current.getCopletData().equals(data))
                {
                    coplets.add(current);
                }
            }
            return coplets;
        }
        catch (Exception e)
        {
            throw new CascadingRuntimeException("Error in getCopletInstanceData", e);
        }
        finally
        {
            this.manager.release(service);
        }
    }

    public void register(CopletInstanceData coplet)
    {
    }

    public void unregister(CopletInstanceData coplet)
    {
    }

    public void register(Layout layout)
    {
    }

    public void unregister(Layout layout)
    {
    }

    public void saveUserProfiles()
    {
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        Configuration child = configuration.getChild("profiles-path");
        this.profilesPath = child.getValue("cocoon:/profiles");
        //this.profilesPath = "context://samples/simple-portal/profiles";
    }
}
