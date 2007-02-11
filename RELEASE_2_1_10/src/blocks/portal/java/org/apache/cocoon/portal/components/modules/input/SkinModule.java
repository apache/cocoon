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
package org.apache.cocoon.portal.components.modules.input;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.SkinDescription;

/**
 * This input module provides information about the current selected skin
 * 
 * @version CVS $Id$
 */
public class SkinModule 
extends AbstractModule
implements Disposable {
    
    protected InputModule globalModule;
    protected ServiceSelector moduleSelector;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this.moduleSelector != null ) {
                this.moduleSelector.release(this.globalModule);
                this.manager.release(this.moduleSelector);
                this.moduleSelector = null;
                this.globalModule = null;
            }
        }
    }
    
    /* (non-Javadoc)
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Object getAttribute(String name, Configuration modeConf, Map objectModel) 
    throws ConfigurationException {
        // lazy init
        if ( this.moduleSelector == null ) {
            synchronized ( this ) {
                try {
                    if ( this.moduleSelector == null ) {
                        this.moduleSelector = (ServiceSelector)this.manager.lookup(InputModule.ROLE+"Selector");
                        this.globalModule = (InputModule)this.moduleSelector.select("global");
                    }
                } catch (ServiceException e) {
                    throw new ConfigurationException("Unable to lookup input module.", e);
                }
            }
        }
            
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            String skinName = null;
            // get the current skin
            // the skin is stored as a parameter on the root layout
            // if not, the global module is used
            // fallback is: common
            final Layout rootLayout = portalService.getComponentManager().getProfileManager().getPortalLayout(null, null);
            if ( rootLayout != null ) {
                skinName = (String)rootLayout.getParameters().get("skin");
            }
            // use the global module
            if ( skinName == null ) {
                skinName = (String)this.globalModule.getAttribute("skin", modeConf, objectModel);
                if ( skinName == null ) {
                    skinName = "common";
                }
            }
            
            // find the correct skin
            SkinDescription desc = null;
            final Iterator i = portalService.getSkinDescriptions().iterator();
            while ( i.hasNext() && desc == null ) {
                final SkinDescription current = (SkinDescription)i.next();
                if ( current.getName().equals(skinName) ) {
                    desc = current;
                }
            }
            if ( desc != null ) {
                if ( "skin".equals(name) ) {
                    return skinName;
                } else if ( "skin.basepath".equals(name) ) {
                    return desc.getBasePath();
                } else if ( "skin.thumbnailpath".equals(name) ) {
                    return desc.getThumbnailPath();
                } else if ( name.startsWith("skin.thumbnailuri.") ) {
                    String selectedSkinName = name.substring(name.lastIndexOf(".")+ 1, name.length());
                    for(Iterator it = portalService.getSkinDescriptions().iterator(); it.hasNext();) {
                        SkinDescription selected = (SkinDescription) it.next();
                        if(selected.getName().equals(selectedSkinName)) {
                            return selected.getBasePath() + "/"  + selected.getThumbnailPath(); 
                        }
                    }
                }
            }
            return null;
        } catch (ServiceException e) {
            throw new ConfigurationException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(portalService);
        }
	}

}
