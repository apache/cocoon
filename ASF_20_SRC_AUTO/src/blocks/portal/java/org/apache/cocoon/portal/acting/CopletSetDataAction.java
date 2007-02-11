/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.acting;

import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.commons.jxpath.JXPathContext;

/**
 * Using this action, you can set values in a coplet
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CopletSetDataAction.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public class CopletSetDataAction 
extends ServiceableAction {

	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) 
    throws Exception {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            // determine coplet id
            String copletId = null;            
            Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if (context != null) {
                copletId = (String)context.get(Constants.COPLET_ID_KEY);
            } else {
                copletId = (String)objectModel.get(Constants.COPLET_ID_KEY);
            
                // set portal name
                portalService.setPortalName((String)objectModel.get(Constants.PORTAL_NAME_KEY));
            }
        
            if (copletId == null) {
                throw new ConfigurationException("copletId must be passed in the object model either directly (e.g. by using ObjectModelAction) or within the parent context.");
            }
        
            JXPathContext jxpathContext = JXPathContext.newContext(portalService.getComponentManager().getProfileManager().getCopletInstanceData(copletId));
            // now traverse parameters:
            // parameter name is path
            // parameter value is value
            // if the value is null or empty, the value is not set!
            final String[] names = parameters.getNames();
            if ( names != null ) {
                for(int i=0; i<names.length; i++) {
                    final String path = names[i];
                    final String value = parameters.getParameter(path, null );
                    if ( value != null && value.trim().length() > 0 ) {
                        jxpathContext.setValue(path, value);
                    }
                }
            }
            
            return EMPTY_MAP;
        
        } catch (ServiceException e) {
            throw new ConfigurationException("ComponentException ", e);
        } finally {
            this.manager.release(portalService);
        }
	}
}
