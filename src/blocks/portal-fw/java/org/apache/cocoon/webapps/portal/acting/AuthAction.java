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
package org.apache.cocoon.webapps.portal.acting;

import java.util.Map;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.portal.components.PortalManager;

/**
 *  This is the authentication action for the portal
 *  This action protecteds a pipeline by using a coplet ID.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AuthAction.java,v 1.4 2004/03/05 13:02:18 bdelacretaz Exp $
*/
public final class AuthAction
extends ServiceableAction
implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("BEGIN act resolver="+resolver+
                              ", objectModel="+objectModel+
                              ", source="+source+
                              ", par="+par);
        }
        final String copletID = par.getParameter("coplet", null);

        PortalManager portal = (PortalManager)this.manager.lookup( PortalManager.ROLE );
        try {
            portal.configurationTest();
            if (null != copletID) {
                portal.checkAuthentication(redirector, copletID);
            }
        } finally {
            this.manager.release( portal );
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END act map={}");
        }
        return EMPTY_MAP;
    }

}
