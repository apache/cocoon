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
package org.apache.cocoon.webapps.authentication.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;

/**
 *  This is the authentication action
 *  This action contains the complete configuration for the authentication
 *  Manager. During configuration the AuthenticationManager class gets this
 *  configuration to configure the instances properly.
 *  The main task of this action is to check if the user is authenticated
 *  using a handler. If not a redirect takes place.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AuthAction.java,v 1.5 2004/03/05 13:01:40 bdelacretaz Exp $
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
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN act resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }
        String handlerName = null;
        String applicationName = null;
        AuthenticationManager authManager = null;
        Map map = null;

        try {
            handlerName = par.getParameter("handler", null);
            applicationName = par.getParameter("application", null);

            authManager = (AuthenticationManager) this.manager.lookup( AuthenticationManager.ROLE );

            // do authentication
            if ( !authManager.checkAuthentication(redirector, handlerName, applicationName) ) {
                // All events are ignored
                // the sitemap.xsl ensures that only the redirect is processed
            } else {
                RequestState state = authManager.getState();
                map = state.getHandler().getContext().getContextInfo();
            }
        } finally {
            this.manager.release( authManager );
        }
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map="+map);
        }
        return map;
    }

}
