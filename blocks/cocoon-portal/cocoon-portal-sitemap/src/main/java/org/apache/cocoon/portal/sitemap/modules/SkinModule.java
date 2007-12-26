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
package org.apache.cocoon.portal.sitemap.modules;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.SkinDescription;

/**
 * This input module provides information about the current selected skin
 *
 * @version $Id$
 */
public class SkinModule
    extends AbstractModule {

    /** The global input module. */
    protected InputModule globalModule;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.globalModule);
            this.globalModule = null;
        }
        super.dispose();
    }

    /**
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        String key = name;
        // lazy init
        if ( this.globalModule == null ) {
            synchronized ( this ) {
                try {
                    if ( this.globalModule == null ) {
                        this.globalModule = (InputModule)this.manager.lookup(InputModule.ROLE+"/global");
                    }
                } catch (ServiceException e) {
                    throw new ConfigurationException("Unable to lookup global input module.", e);
                }
            }
        }

        String skinName = null;
        // if the attribute already contains a ':', the name is prefixed with the skin name
        int pos = key.indexOf(':');
        if ( pos != -1 ) {
            skinName = key.substring(0, pos);
            key = key.substring(pos+1);
        } else {
            // get the current skin
            // the skin is stored as a parameter on the root layout
            // if not, the global module is used
            // fallback is: common
            final Layout rootLayout = this.portalService.getProfileManager().getLayout(null);
            if ( rootLayout != null ) {
                skinName = rootLayout.getParameter("skin");
            }
            // use the global module
            if ( skinName == null ) {
                skinName = (String)this.globalModule.getAttribute("skin", modeConf, objectModel);
                if ( skinName == null ) {
                    skinName = "common";
                }
            }
        }

        // find the correct skin
        SkinDescription desc = null;
        final Iterator i = this.portalService.getSkinDescriptions().iterator();
        while ( i.hasNext() && desc == null ) {
            final SkinDescription current = (SkinDescription)i.next();
            if ( current.getName().equals(skinName) ) {
                desc = current;
            }
        }
        if ( desc != null ) {
            if ( "skin".equals(key) ) {
                return skinName;
            } else if ( "skin.basepath".equals(key) ) {
                return desc.getBasePath().getAbsoluteFile();
            } else if ( "skin.thumbnailpath".equals(key) ) {
                return desc.getThumbnailPath();
            } else if ( key.startsWith("skin.thumbnailuri.") ) {
                String selectedSkinName = key.substring(key.lastIndexOf(".")+ 1, key.length());
                for(Iterator it = portalService.getSkinDescriptions().iterator(); it.hasNext();) {
                    SkinDescription selected = (SkinDescription) it.next();
                    if(selected.getName().equals(selectedSkinName)) {
                        return selected.getBasePath() + "/"  + selected.getThumbnailPath();
                    }
                }
            }
        }
        return null;
	}
}
