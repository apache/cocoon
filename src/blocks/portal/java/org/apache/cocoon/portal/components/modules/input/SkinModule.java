/*
 * Copyright 2005 The Apache Software Foundation.
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
import org.apache.cocoon.portal.layout.SkinDescription;

/**
 * FIXME We can use the module chaining!
 * 
 * @version CVS $Id: SkinModule.java,v 1.4 2005/01/07 10:21:46 cziegeler Exp $
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

            // get the current skin
            // TODO
            String skinName = (String)this.globalModule.getAttribute("skin", modeConf, objectModel);
            if ( skinName == null ) {
                skinName = "basic";
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
