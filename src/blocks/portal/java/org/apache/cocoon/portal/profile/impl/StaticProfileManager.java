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
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.profile.ProfileLS;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.excalibur.source.SourceValidity;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * 
 * @version CVS $Id: StaticProfileManager.java,v 1.6 2003/07/18 14:41:45 cziegeler Exp $
 */
public class StaticProfileManager
    extends AbstractProfileManager
    implements Configurable {

    protected String profilesPath;

    protected String defaultLayoutGroup = null;

    protected static final String LAYOUTKEY_PREFIX =
        StaticProfileManager.class.getName() + "/Layout/";

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#getPortalLayout(String, String)
     */
    public Layout getPortalLayout(String layoutKey, String layoutID) {
        PortalService service = null;
        ProfileLS adapter = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            if (null == layoutID) {
                Layout l =
                    (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT");
                if (null != l) {
                    return l;
                }
            }

            String serviceKey = null;
            if (layoutKey == null) {
                layoutKey = defaultLayoutGroup; // Default group to load
            }
            serviceKey =
                LAYOUTKEY_PREFIX + service.getPortalName() + "/" + layoutKey;
            if (layoutID == null) {
                // look for the default key
                // it is set with the id of the root layoutId of a layoutGroup
                layoutID =
                    (String) service.getAttribute(serviceKey + "defaultKey");
            }

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
            adapter = (ProfileLS) this.manager.lookup(ProfileLS.ROLE);

            Map parameters = new HashMap();
            parameters.put("profiletype", "layout");
            
            Map map = new SequencedHashMap();
            map.put("base", this.profilesPath);
            map.put("portalname", service.getPortalName());
            map.put("profile", "layout");
            map.put("groupKey", layoutKey);

            SourceValidity newValidity = adapter.getValidity(map, parameters);
            if (valid == SourceValidity.UNKNOWN) {
                if (sourceValidity.isValid(newValidity)
                    == SourceValidity.VALID) {
                    // XXX what if objects is null ? Could it be ?
                    return (Layout) ((Map) objects[0]).get(layoutID);
                }
            }

            // get Layout specified in the map
            Layout layout = (Layout) adapter.loadProfile(map, parameters);
            Map layouts = null;
            if (objects != null) {
                layouts = (Map) objects[0];
            } else {
                layouts = new HashMap();
            }
            // save the root layout as default of an group if no key is given
            service.setAttribute(serviceKey + "defaultKey", layout.getId());
            cacheLayouts(layouts, layout);

            LayoutFactory factory = service.getComponentManager().getLayoutFactory();
            factory.prepareLayout(layout);

            // store the new values in the service
            if (newValidity != null) {
                objects = new Object[] { layouts, newValidity };
                service.setAttribute(serviceKey, objects);
            }

            // is the default layout wanted ?
            if ((layout.getId().equals(layoutID)) || layoutID == null) {
                return layout;
            }

            // or a layout in the group ?
            return (Layout) layouts.get(layoutID);

        } catch (Exception ce) {
            throw new CascadingRuntimeException("Unable to get layout.", ce);
        } finally {
            this.manager.release(service);
            this.manager.release((Component)adapter);
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

    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute =
                StaticProfileManager.class.getName()
                    + "/"
                    + service.getPortalName()
                    + "/CopletInstanceData";
            CopletInstanceDataManager copletInstanceDataManager =
                (CopletInstanceDataManager) service.getAttribute(attribute);

            return copletInstanceDataManager.getCopletInstanceData(copletID);
        } catch (ComponentException e) {
            throw new CascadingRuntimeException(
                "Unable to lookup portal service.",
                e);
        } finally {
            this.manager.release(service);
        }
    }

    public List getCopletInstanceData(CopletData data) {
        List coplets = new ArrayList();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);

            attribute =
                StaticProfileManager.class.getName()
                    + "/"
                    + service.getPortalName()
                    + "/CopletInstanceData";
            CopletInstanceDataManager copletInstanceDataManager =
                (CopletInstanceDataManager) service.getAttribute(attribute);

            Iterator iter =
                copletInstanceDataManager
                    .getCopletInstanceData()
                    .values()
                    .iterator();
            while (iter.hasNext()) {
                CopletInstanceData current = (CopletInstanceData) iter.next();
                if (current.getCopletData().equals(data)) {
                    coplets.add(current);
                }
            }
            return coplets;
        } catch (ComponentException e) {
            throw new CascadingRuntimeException(
                "Unable to lookup portal service.",
                e);
        } finally {
            this.manager.release(service);
        }
    }

    public void register(CopletInstanceData coplet) {
    }

    public void unregister(CopletInstanceData coplet) {
    }

    public void register(Layout layout) {
    }

    public void unregister(Layout layout) {
    }

    public void saveUserProfiles() {
    }

    public void configure(Configuration configuration)
        throws ConfigurationException {
        Configuration child = configuration.getChild("default-layout-group");
        if (child != null) {
            // get configured default LayoutGroup
            defaultLayoutGroup = child.getValue();
        }

        if (this.defaultLayoutGroup == null) {
            // if none is configured set it to "portal"
            this.defaultLayoutGroup = "portal";
        }
        child = configuration.getChild("profiles-path");
        if (child != null) {
            this.profilesPath = child.getValue();
        }
        if ( this.profilesPath == null ) {
            this.profilesPath = "context://samples/simple-portal/profiles";
        }
    }

    public Layout getPortalLayout(String key) {
        return getPortalLayout(null, key);
    }

}
