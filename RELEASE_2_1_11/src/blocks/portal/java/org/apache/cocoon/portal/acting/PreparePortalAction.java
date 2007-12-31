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
package org.apache.cocoon.portal.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;

/**
 * Prepare the portal by setting the portal name.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public class PreparePortalAction
extends ServiceableAction implements ThreadSafe {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) 
    throws Exception {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            if ( portalService.getPortalName() == null ) {
                final String portalName = parameters.getParameter("portal-name",
                                              (String)objectModel.get(Constants.PORTAL_NAME_KEY));
                portalService.setPortalName(portalName);
            }
            return EMPTY_MAP;

        } catch (ServiceException e) {
            throw new ProcessingException("Unable to lookup portal service.", e);
        } finally {
            this.manager.release(portalService);
        }
	}
}
