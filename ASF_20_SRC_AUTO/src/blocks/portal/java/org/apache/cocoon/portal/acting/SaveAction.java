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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.PortalService;

/**
 * This action saves the profile
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SaveAction.java,v 1.2 2004/03/05 13:02:09 bdelacretaz Exp $
*/
public final class SaveAction
extends ServiceableAction
implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN act resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }

        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.setPortalName(par.getParameter("portal-name"));
            service.getComponentManager().getProfileManager().saveUserProfiles();
        } catch (ParameterException pe) {
            throw new ProcessingException("Parameter portal-name is required.");
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map={}");
        }

        return EMPTY_MAP;
    }

}
