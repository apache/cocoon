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
import org.apache.cocoon.webapps.authentication.user.UserHandler;

/**
 *  This action tests if the user is logged in for a given handler.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: LoggedInAction.java,v 1.6 2004/03/05 13:01:40 bdelacretaz Exp $
*/
public final class LoggedInAction
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

        Map map = null;
        String handlerName = par.getParameter("handler", null);
        AuthenticationManager authManager = null;

        final boolean testNotLoggedIn = par.getParameterAsBoolean("negate-result", false); 
        
        try {
            authManager = (AuthenticationManager) this.manager.lookup(AuthenticationManager.ROLE);
            UserHandler handler = authManager.isAuthenticated(handlerName);
            if ( testNotLoggedIn ) {
                if ( handler == null ) {
                    map = EMPTY_MAP;
                }
            } else {
                if ( handler != null ) {
                    map = EMPTY_MAP;
                }
            }
        } finally {
            this.manager.release( authManager);
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map="+map);
        }

        return map;
    }

}
